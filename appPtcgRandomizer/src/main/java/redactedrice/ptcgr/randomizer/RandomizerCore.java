package redactedrice.ptcgr.randomizer;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import redactedrice.ptcgr.rules.Rules;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.rom.RomData;
import redactedrice.ptcgr.rom.RomIO;
import redactedrice.ptcgr.rom.Texts;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.lua.ExecutionResult;
import redactedrice.randomizer.lua.ExecutionRequest;
import redactedrice.randomizer.utils.ManifestResourceExtractor;
import redactedrice.randomizer.utils.ErrorTracker;
import redactedrice.randomizer.utils.Logger;

import redactedrice.ptcgr.constants.CardDataConstants.CardType;
import redactedrice.ptcgr.constants.CardDataConstants.EnergyType;
import redactedrice.ptcgr.constants.CardDataConstants.EvolutionStage;

public class RandomizerCore {
    static final String SEED_LOG_EXTENSION = ".seed.txt";
    static final String LOG_FILE_EXTENSION = ".log.txt";
    static final String MODULES_DIRECTORY = "modules";
    static final String RANDOMIZER_DIRECTORY = "randomizer";

    private RomData romData;
    private Rules rules;
    private ActionBank actionBank;
    private LuaRandomizerWrapper luaRandomizer;

    public RandomizerCore() {
        setupLuaRandomizer();
        actionBank = new ActionBank(luaRandomizer);
    }

    public ActionBank getActionBank() {
        return actionBank;
    }

    public LuaRandomizerWrapper getLuaRandomizer() {
        return luaRandomizer;
    }

    private void setupLuaRandomizer() {
        // Extract bundled randomizer files if they dont exist or need updating
        File randomizerDir = new File(RANDOMIZER_DIRECTORY);
        String randomizerPath = randomizerDir.getAbsolutePath();
        try {
            ManifestResourceExtractor.extract("randomizer", randomizerPath, true);
            System.out.println("Using randomizer files from: " + randomizerPath);
        } catch (IOException e) {
            System.err.println("Failed to extract core lua randomizer files: " + e.getMessage());
            e.printStackTrace();
            // Try to continue anyway in case files already exist
        }

        // Extract bundled module files if they dont exist or need updating
        File modulesDir = new File(MODULES_DIRECTORY);
        String modulesPath = modulesDir.getAbsolutePath();
        try {
            ManifestResourceExtractor.extract("modules", modulesPath, true);
            System.out.println("Using module files from: " + modulesPath);
        } catch (IOException e) {
            System.err.println("Failed to extract core modules: " + e.getMessage());
            e.printStackTrace();
            // Try to continue anyway in case files already exist
        }

        // Prepare allowed directories and search paths
        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerPath);
        allowedDirectories.add(modulesPath);

        List<String> searchPaths = new ArrayList<>();
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            searchPaths.add(modulesDir.getAbsolutePath());
        }

        // Now that the paths are set we can make the wrapper
        luaRandomizer = new LuaRandomizerWrapper(allowedDirectories, searchPaths);

        Logger.setEnabled(true);

        int loadedCount = luaRandomizer.loadModules();
        if (loadedCount > 0) {
            System.out.println("Loaded " + loadedCount + " Lua modules");
        } else {
            System.out.println("No Lua modules found in " + modulesDir.getAbsolutePath());
        }

        logErrorTrackerMessages("Errors loading Lua modules:");
    }

    public void openRom(File romFile, Component toCenterPopupsOn) {
        try {
            romData = RomIO.readFromFile(romFile);
        } catch (IOException e) {
            // TODO later: Auto-generated catch block
            e.printStackTrace();
        }

        rules = new Rules(romData, toCenterPopupsOn);
        rules.getIo().displayWarnings();
    }

    public Rules getRules() {
        return rules;
    }

    public void randomizeAndSaveRom(File romFile, Settings settings, List<Action> actionBank)
            throws IOException {
        String romBasePath = romFile.getPath();
        romBasePath = romBasePath.substring(0, romBasePath.lastIndexOf('.'));

        if (settings.isLogSeed()) {
            FileWriter seedFile = new FileWriter(romBasePath + SEED_LOG_EXTENSION);
            try {
                String seedText = settings.getSeedString();
                String seedVal = String.valueOf(settings.getSeedValue());
                if (!seedText.equals(seedVal)) {
                    seedFile.write("Text: \"" + seedText + "\", Numeric Equivalent: " + seedVal);
                } else {
                    seedFile.write("Seed value: " + seedText);
                }
            } finally {
                seedFile.close();
            }
        }

        OutputStream detailLogStream = null;
        try {
            if (settings.isLogDetails()) {
                detailLogStream = new FileOutputStream(romBasePath + LOG_FILE_EXTENSION);
                Logger.addStreamForAllLevels(detailLogStream);
            }

            randomize(settings, actionBank);
        } finally {
            if (detailLogStream != null) {
                detailLogStream.close();
                Logger.clearAllStreams();
            }
        }

        // TODO later: Due to an error, the same data was being written more than once
        // and when this happened, the text for some cards compoundly got worse.
        // Need to look into why this is happening and if it still is
        RomIO.writePatch(romData, romFile);
    }

    public void randomize(Settings settings, List<Action> actions) {
        // get and store the base seed as the next one to use
        // TODO this needs to be revamped entirely with seed offset from lua
        int seed = settings.getSeedValue();

        // Ensure the rom data is back to the original data (for multiple randomizations
        // without reloading) and prepare it to be modified so we know that
        // it will need to be reset
        romData.prepareForModification();

        // Expose objects to be modified
        // TODO: Add original vs modified and add more
        JavaContext context = new JavaContext();
        context.register("original", romData.original);
        context.register("modified", romData.modified);

        if (rules != null) {
            context.register("rules", rules);
        }

        // Register card some enums
        // TODO: Add others. Could I do this dynamically or just specify all of them
        context.registerEnum(CardType.class);
        context.registerEnum(EnergyType.class);
        context.registerEnum(EvolutionStage.class);

        // Enable lua based change detection. Setup of what is monitored is done in the
        // setup script
        context.setConfig("changeDetectionActive", true);

        // Prepare execution requests for each module
        // TODO: Tie in to allow arguments to be specified via the GUI with the data
        // from the modules. Same for seeds to override default offset
        List<ExecutionRequest> executionRequests = new LinkedList<>();
        for (Action action : actions) {
            String name = action.getName();
            Map<String, Object> arguments = new HashMap<>();

            // Use withDefaultSeedOffset to respect module's seed offset if it has one
            ExecutionRequest request = ExecutionRequest.withDefaultSeedOffset(name, arguments, seed,
                    luaRandomizer.getModuleRegistry());
            executionRequests.add(request);
        }

        // Execute modules and check for errors
        List<ExecutionResult> results = luaRandomizer.executeModules(executionRequests, context);
        logErrorTrackerMessages("Errors executing Lua modules:");

        for (ExecutionResult result : results) {
            if (!result.isSuccess()) {
                Logger.error("Module " + result.getModuleName() + " failed: "
                        + result.getErrorMessage());
            } else {
                Logger.info("Module " + result.getModuleName() + " executed with seed "
                        + result.getSeedUsed());
            }
        }
    }

    private static void logErrorTrackerMessages(String heading) {
        if (ErrorTracker.hasErrors()) {
            Logger.error(heading);
            for (String error : ErrorTracker.getErrors()) {
                Logger.error("  " + error);
            }
        }
    }

    public static void test(CardGroup<Card> cards) {
        for (Card card : cards.iterable()) {
            System.out.println(card.toString() + "\n");
        }
    }

    public static void test(Texts allText) {
        for (short i = 1; i < 20; i++) // String text : allText)
        {
            System.out.println(allText.getAtId(i));
        }
    }

    public String getFileExtension() {
        return ".bps";
    }
}

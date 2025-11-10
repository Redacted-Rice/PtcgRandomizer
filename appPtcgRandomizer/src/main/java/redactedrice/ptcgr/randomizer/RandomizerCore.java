package redactedrice.ptcgr.randomizer;


import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

import redactedrice.gbcframework.utils.Logger;
import redactedrice.ptcgr.config.Configs;
import redactedrice.ptcgr.data.Card;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.rom.RomData;
import redactedrice.ptcgr.rom.RomIO;
import redactedrice.ptcgr.rom.Texts;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.wrapper.LuaRandomizerWrapper;
import redactedrice.randomizer.wrapper.ExecutionResult;
import redactedrice.randomizer.wrapper.RandomizerResourceExtractor;

import redactedrice.ptcgr.constants.CardDataConstants.CardType;
import redactedrice.ptcgr.constants.CardDataConstants.EnergyType;
import redactedrice.ptcgr.constants.CardDataConstants.EvolutionStage;

public class RandomizerCore {
    static final String SEED_LOG_EXTENSION = ".seed.txt";
    static final String LOG_FILE_EXTENSION = ".log.txt";
    static final String MODULES_DIRECTORY = "modules";
    static final String RANDOMIZER_DIRECTORY = "randomizer";

    private Logger logger;
    private RomData romData;
    private Configs configs;
    private ActionBank actionBank;
    private LuaRandomizerWrapper luaRandomizer;

    public RandomizerCore() {
        logger = new Logger();
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
        RandomizerResourceExtractor.setPath(randomizerPath);
        try {
            RandomizerResourceExtractor.extract(true);
            System.out.println("Using randomizer files from: " + randomizerPath);
        } catch (IOException e) {
            System.err.println("Failed to extract core lua randomizer files: " + e.getMessage());
            e.printStackTrace();
            // Try to continue anyway in case files already exist
        }

        File modulesDir = new File(MODULES_DIRECTORY);
        String modulesPath = modulesDir.getAbsolutePath();
        PtcgrResourceExtractor.setPath(modulesPath);
        try {
            // TODO: Make configurable for testing?
            PtcgrResourceExtractor.extract(true);
            System.out.println("Using module files from: " + modulesPath);
        } catch (IOException e) {
            System.err.println("Failed to extract core modules: " + e.getMessage());
            e.printStackTrace();
            // Try to continue anyway in case files already exist
        }

        // Now that the path is set we can make the wrapper
        luaRandomizer = new LuaRandomizerWrapper();

        // Log everything to one file for now. Eventually will tie this into
        // the existing logging or replace it
        luaRandomizer.setLogEnabled(true);
        try {
            luaRandomizer.addStreamForAllLogLevels(
                    new FileOutputStream(new File("randomizer.log"), false));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        luaRandomizer.setChangeDetectionEnabled(true);

        // Add modules directory relative to current working directory
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            luaRandomizer.addSearchPath(modulesDir.getAbsolutePath());
        }
        int loadedCount = luaRandomizer.loadModules();
        if (loadedCount > 0) {
            System.out.println("Loaded " + loadedCount + " Lua modules");
        } else {
            System.out.println("No Lua modules found in " + modulesDir.getAbsolutePath());
        }

        // Check for errors loading
        List<String> loadErrors = luaRandomizer.getLoadErrors();
        if (!loadErrors.isEmpty()) {
            System.err.println("Errors loading Lua modules:");
            for (String error : loadErrors) {
                System.err.println("  " + error);
            }
        }
    }

    public void openRom(File romFile, Component toCenterPopupsOn) {
        try {
            romData = RomIO.readFromFile(romFile);
        } catch (IOException e) {
            // TODO later: Auto-generated catch block
            e.printStackTrace();
        }

        // TODO later: Move to saving?
        // Skip loading config files for now
        // configs = new Configs(romData, toCenterPopupsOn);
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

        if (settings.isLogDetails()) {
            logger.open(romBasePath + LOG_FILE_EXTENSION);
        }

        randomize(settings, actionBank);

        logger.close();

        // TODO later: Due to an error, the same data was being written more than once
        // and when this happened, the text for some cards compoundly got worse.
        // Need to look into why this is happening and if it still is
        RomIO.writePatch(romData, romFile);
    }

    public void randomize(Settings settings, List<Action> actions) {
        // get and store the base seed as the next one to use
        // TODO this needs to be revamped entirely with seed offset from lua
        int nextSeed = settings.getSeedValue();

        // Ensure the rom data is back to the original data (for multiple randomizations
        // without reloading) and prepare it to be modified so we know that
        // it will need to be reset
        romData.prepareForModification();

        // Expose objects to be modified
        // TODO: Add original vs modified and add more
        JavaContext context = new JavaContext();
        context.register("original", romData.original);
        context.register("modified", romData.modified);

        // Register card some enums
        // TODO: Add others. Could I do this dynamically or just specify all of them
        context.registerEnum(CardType.class);
        context.registerEnum(EnergyType.class);
        context.registerEnum(EvolutionStage.class);

        // TODO: Set more later
        // Set monitored objects for change detection with custom identifier
        // Monitor all monster cards and use a custom identifier showing "cardName (cardId)"
        luaRandomizer.setMonitoredObjectsFromCollection(
                romData.modified.allCards.cards().monsterCards().iterable(), card -> {
                    MonsterCard monsterCard = (MonsterCard) card;
                    return monsterCard.name.toString() + " (" + monsterCard.id + ")";
                });

        // Prepare arguments and seeds per module
        // TODO: Tie in to allow arguements to be specified via the GUI with the data
        // from the modules
        Map<String, Map<String, Object>> argumentsPerModule = new HashMap<>();
        Map<String, Integer> seedsPerModule = new HashMap<>();
        List<String> moduleNames = new LinkedList<>();
        for (Action action : actions) {
            String name = action.getName();
            moduleNames.add(name);
            argumentsPerModule.put(name, new HashMap<>());
            seedsPerModule.put(name, nextSeed++);
        }

        // Execute modules and check for errors
        List<ExecutionResult> results = luaRandomizer.executeModules(moduleNames, context,
                argumentsPerModule, seedsPerModule);
        List<String> executionErrors = luaRandomizer.getExecutionErrors();
        if (!executionErrors.isEmpty()) {
            System.err.println("Errors executing Lua modules:");
            for (String error : executionErrors) {
                System.err.println("  " + error);
            }
        }

        // Log execution results
        // TODO: Integrate the two loggers and figure out how I want changes to be logged
        for (ExecutionResult result : results) {
            if (result.hasChanges()) {
                System.out.println("Module " + result.getModuleName() + " made changes:");
                Map<String, Map<String, String>> changes = result.getChanges();
                for (Map.Entry<String, Map<String, String>> entry : changes.entrySet()) {
                    System.out.println("  " + entry.getKey() + ":");
                    for (Map.Entry<String, String> change : entry.getValue().entrySet()) {
                        System.out.println("    " + change.getKey() + ": " + change.getValue());
                    }
                }
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

package redactedrice.ptcgr.randomizer;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.configs.rules.RulesConfig;
import redactedrice.ptcgr.constants.PtcgRandomizerVersion;
import redactedrice.ptcgr.constants.romenums.CardType;
import redactedrice.ptcgr.data.CardGroup;
import redactedrice.ptcgr.data.MonsterCard;
import redactedrice.ptcgr.rules.MoveAssignment;
import redactedrice.ptcgr.constants.romenums.EnergyType;
import redactedrice.ptcgr.constants.romenums.EvolutionStage;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.actions.ActionBank;
import redactedrice.ptcgr.randomizer.gui.moduleconfig.ArgumentConstraintDescription;
import redactedrice.ptcgr.resources.PtcgBundledResources;
import redactedrice.ptcgr.rom.RomData;
import redactedrice.ptcgr.rom.RomIO;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.randomizer.LuaRandomizerWrapper;
import redactedrice.randomizer.context.JavaContext;
import redactedrice.randomizer.lua.ExecutionRequest;
import redactedrice.randomizer.lua.ExecutionResult;
import redactedrice.randomizer.lua.requirements.CoreRequirements;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.utils.Logger;

public class RandomizerCore {
    static final String SEED_LOG_EXTENSION = ".seed.txt";
    static final String LOG_FILE_EXTENSION = ".log.txt";
    public static final String PATCH_FILE_EXTENSION = ".bps";
    public static final String DEFAULT_PATCH_BASE_NAME = "ptcg_randomized";

    private RomData romData;
    private RulesConfig pendingRules;
    private ActionBank actionBank;
    private LuaRandomizerWrapper luaRandomizer;
    private final PtcgBundledResources bundledResources;
    private final Component popupParent;

    public RandomizerCore(Component toCenterPopupsOn) {
        popupParent = toCenterPopupsOn;
        bundledResources = new PtcgBundledResources();
        bundledResources.installAll();
        setupLuaRandomizer();
        actionBank = new ActionBank(luaRandomizer);
        checkLoadedModuleArgumentConstraints(actionBank);
        pendingRules = loadBundledDefaultRules();
    }

    private RulesConfig loadBundledDefaultRules() {
        IssueTracker.clear();
        try {
            File rulesFile = bundledResources.getUnsupportedMovesFile();
            RulesConfig bundled =
                    RulesConfig.readFromLoadedYamlMap(YamlIO.load(rulesFile), "Unsupported moves");
            IssuePresenter.displayWarnings(popupParent, "default rules");
            return bundled;
        } catch (IOException e) {
            IssueTracker.addWarning("Failed to load bundled default rules: " + e.getMessage());
            IssuePresenter.displayWarnings(popupParent, "default rules");
            return RulesConfig.empty();
        }
    }

    public ActionBank getActionBank() {
        return actionBank;
    }

    private void setupLuaRandomizer() {
        File randomizerDir = bundledResources.getRandomizerDir();
        File modulesDir = bundledResources.getModulesDir();
        String randomizerPath = randomizerDir.getAbsolutePath();
        String modulesPath = modulesDir.getAbsolutePath();

        // Prepare allowed directories and search paths
        List<String> allowedDirectories = new ArrayList<>();
        allowedDirectories.add(randomizerPath);
        allowedDirectories.add(modulesPath);

        List<String> searchPaths = new ArrayList<>();
        if (modulesDir.exists() && modulesDir.isDirectory()) {
            searchPaths.add(modulesDir.getAbsolutePath());
        }

        CoreRequirements requirements = new CoreRequirements();
        // We just use the PTCGR version instead of all 3 (PTCGR, URJ and URC) for simplicity and
        // since they are bundled together
        requirements.addCoreRequirement(PtcgRandomizerVersion.PLATFORM_KEY,
                PtcgRandomizerVersion.VERSION, true);
        luaRandomizer =
                new LuaRandomizerWrapper(allowedDirectories, searchPaths, null, null, requirements);

        // Register built in PTCGR enums in the shared enum context instead of in the runtime
        // context so they're merged into every execution context the same way module registered
        // (onLoad) enums are, and so they're resolvable by name for the config UI's ENUM argument
        // dropdowns even before a randomization has run.
        // TODO later: Add others. Could I do this dynamically or just specify all of them?
        luaRandomizer.getSharedContext().registerEnum(CardType.class);
        luaRandomizer.getSharedContext().registerEnum(EnergyType.class);
        luaRandomizer.getSharedContext().registerEnum(EvolutionStage.class);

        Logger.setEnabled(true);

        IssueTracker.clear();
        int loadedCount = luaRandomizer.loadModules();
        if (loadedCount > 0) {
            System.out.println("Loaded " + loadedCount + " Lua modules");
        } else {
            System.out.println("No Lua modules found in " + modulesDir.getAbsolutePath());
        }

        // Already logged on add. Popup summarizes then clears the phase store
        IssuePresenter.finishPhase(popupParent, "module load");
    }

    private void checkLoadedModuleArgumentConstraints(ActionBank bank) {
        IssueTracker.clear();
        for (Action action : bank.get()) {
            ArgumentConstraintDescription.checkModuleConstraints(action.getModule());
        }
        IssuePresenter.displayWarnings(popupParent, "loaded module scripts");
    }

    public boolean isRomLoaded() {
        return romData != null;
    }

    public boolean openRom(File romFile, Component toCenterPopupsOn) {
        try {
            romData = RomIO.readFromFile(romFile);
            IssueTracker.clear();
            pendingRules.recreateRules(romData.rules, romData.getOriginalMonsterCards());
            romData.applyRulesToOriginal();
            IssuePresenter.displayWarnings(toCenterPopupsOn, "loaded rules");
            return true;
        } catch (IOException e) {
            romData = null;
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Replaces session rules from a config (or other source). If a ROM is already loaded, original
     * is rebuilt from the stored ROM bytes and the new rules are applied.
     */
    public void replacePendingRules(RulesConfig rulesConfig) {
        pendingRules = rulesConfig;
        if (romData != null) {
            romData.reloadOriginalFromRom();
            pendingRules.recreateRules(romData.rules, romData.getOriginalMonsterCards());
            romData.applyRulesToOriginal();
        }
    }

    public RulesConfig getPendingRules() {
        return pendingRules;
    }

    public List<MoveAssignment> getMoveAssignments() {
        if (romData == null) {
            return List.of();
        }
        return romData.rules.getMoveAssignments().getAllAssignments();
    }

    public CardGroup<MonsterCard> getOriginalMonsterCards() {
        return romData != null ? romData.getOriginalMonsterCards() : null;
    }

    public boolean randomizeAndSaveRom(File romFile, Settings settings, List<Action> actions)
            throws IOException {
        if (!isRomLoaded() || !hasSelectedActions(actions)) {
            return false;
        }

        String romBasePath = romFile.getPath();
        romBasePath = romBasePath.substring(0, romBasePath.lastIndexOf('.'));

        Path seedFilePath = Path.of(romBasePath + SEED_LOG_EXTENSION);
        try (var seedFile = Files.newBufferedWriter(seedFilePath, StandardCharsets.UTF_8)) {
            String seedText = settings.getSeedString();
            String seedVal = String.valueOf(settings.getSeedValue());
            if (!seedText.equals(seedVal)) {
                seedFile.write("Text: \"" + seedText + "\", Numeric Equivalent: " + seedVal);
            } else {
                seedFile.write("Seed value: " + seedText);
            }
        }

        OutputStream detailLogStream = null;
        try {
            Logger.setMinLogLevel(settings.getLogLevel());
            if (settings.isLogDetails()) {
                detailLogStream = new FileOutputStream(romBasePath + LOG_FILE_EXTENSION);
                Logger.addStreamForAllLevels(detailLogStream);
            }

            if (!randomize(settings, actions)) {
                return false;
            }
        } finally {
            if (detailLogStream != null) {
                detailLogStream.close();
                Logger.clearAllStreams();
            }
        }
        RomIO.writePatch(romData, romFile);
        return true;
    }

    public boolean randomize(Settings settings, List<Action> actions) {
        if (!isRomLoaded() || !hasSelectedActions(actions)) {
            return false;
        }

        // get and store the base seed
        int seed = settings.getSeedValue();

        // Rebuild original from ROM bytes, reapply rules, then copy into modified so each
        // randomization is isolated from prior runs (and from any Lua writes to original)
        IssueTracker.clear();
        romData.prepareForModification();

        // Expose objects to be modified
        // TODO later: Add original vs modified and add more
        JavaContext context = new JavaContext();
        context.register("original", romData.original);
        context.register("modified", romData.modified);
        context.register("rules", romData.rules);

        // Enable lua based change detection. Setup of what is monitored is done in the
        // setup script
        context.setConfig("changeDetectionActive", true);

        // Prepare execution requests for each module using GUI config values
        List<ExecutionRequest> executionRequests = new LinkedList<>();
        for (Action action : actions) {
            ExecutionRequest request = action.toExecutionRequest();
            if (luaRandomizer.getModule(request.getModuleId()) == null) {
                IssueTracker.addError("Module not found: " + request.getModuleId());
                continue;
            }
            executionRequests.add(request);
        }

        if (executionRequests.isEmpty()) {
            IssuePresenter.finishPhase(popupParent, "randomize");
            return false;
        }

        // Execute modules — failures are logged immediately via IssueTracker.addError
        List<ExecutionResult> results =
                luaRandomizer.executeModules(executionRequests, context, seed);

        for (ExecutionResult result : results) {
            if (result.isSuccess()) {
                ExecutionRequest request = result.getRequest();
                if (request != null && request.usesSeed()) {
                    Logger.info("Module " + result.getModuleId() + " executed with seed "
                            + result.getSeedUsed());
                } else {
                    Logger.info("Module " + result.getModuleId() + " executed");
                }
            }
            // Failures already logged/collected when ModuleExecutor called IssueTracker.addError
        }

        boolean success = !IssueTracker.hasErrors();
        IssuePresenter.finishPhase(popupParent, "randomize");
        return success;
    }

    private static boolean hasSelectedActions(List<Action> actions) {
        return actions != null && !actions.isEmpty();
    }
}

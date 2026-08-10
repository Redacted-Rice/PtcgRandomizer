package redactedrice.ptcgr.randomizer.gui;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.IOException;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;

import java.awt.FlowLayout;

import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.gui.dualselector.table.DualTableSelector;
import redactedrice.ptcgr.randomizer.gui.rules.RulesPanel;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.IssuePresenter;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.configs.AppPreferences;
import redactedrice.ptcgr.configs.Config;
import redactedrice.randomizer.utils.IssueTracker;
import redactedrice.randomizer.utils.LogLevel;
import redactedrice.randomizer.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RandomizerApp {

    // TODO: Make configurable for functional testing support?
    private static final String DEFAULT_ROM_NAME = "ptcg.gbc";
    private static final String OPEN_ROM_TEXT = "Open ROM";
    private static final String OPEN_NEW_ROM_TEXT = "Open New ROM";

    private JFrame frmTradingCard;
    private JFileChooser openRomChooser;
    private JFileChooser saveRomChooser;
    private JFileChooser saveConfigChooser;
    private JFileChooser loadConfigChooser;
    private JButton openRomButton;

    private RandomizerCore randomizer;
    private AppPreferences appPreferences;
    private String lastOpenedRomPath;
    private JComboBox<LogLevel> logLevelCombo;
    private JCheckBox saveLogDetailsBox;
    private JCheckBox saveSettingsBox;
    private JTextField saveSetSeedVal;
    private DualTableSelector dualPanel;
    private RulesPanel rulesPanel;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    RandomizerApp window = new RandomizerApp();
                    window.frmTradingCard.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public RandomizerApp() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmTradingCard = new JFrame();
        frmTradingCard.setTitle("Pokemon Trading Card Game Randomizer");
        frmTradingCard.setBounds(100, 100, 1024, 768);
        frmTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));

        randomizer = new RandomizerCore(frmTradingCard);
        appPreferences = loadAppPreferences();
        lastOpenedRomPath = appPreferences.getLastRomPath();

        openRomChooser = new JFileChooser();
        saveRomChooser = new JFileChooser();
        saveConfigChooser = new JFileChooser();
        loadConfigChooser = new JFileChooser();
        saveConfigChooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));
        loadConfigChooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));

        applyFileChooserPreferences();
        applyWindowPreferences();

        JPanel saveRomPanel = new JPanel();
        saveRomPanel.setBorder(new EmptyBorder(4, 7, 4, 7));
        frmTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
        saveRomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 0));

        JPanel logLevelPanel = new JPanel(new BorderLayout(0, 0));
        saveRomPanel.add(logLevelPanel);
        JLabel logLevelLbl = new JLabel("Log level: ");
        logLevelPanel.add(logLevelLbl, BorderLayout.WEST);
        logLevelCombo = new JComboBox<>(LogLevel.values());
        logLevelCombo.setSelectedItem(appPreferences.getLogLevel());
        logLevelCombo.setToolTipText(
                "Minimum log level for console and detail log file. DEBUG includes verbose module output.");
        logLevelCombo.addActionListener(e -> {
            applyLogLevelFromUi();
            saveAppPreferencesQuietly();
        });
        logLevelPanel.add(logLevelCombo, BorderLayout.CENTER);
        applyLogLevelFromUi();

        saveLogDetailsBox = new JCheckBox("Log Randomizations");
        saveLogDetailsBox.setToolTipText(
                "Write a detailed log of randomization changes alongside the patch file.");
        saveRomPanel.add(saveLogDetailsBox);
        saveLogDetailsBox.setSelected(appPreferences.isLogDetails());
        saveLogDetailsBox.addActionListener(e -> saveAppPreferencesQuietly());

        saveSettingsBox = new JCheckBox("Save Randomization Config");
        saveSettingsBox.setToolTipText(
                "Save the randomization config to a YAML file that can be loaded to repeat the same randomization again later.");
        saveRomPanel.add(saveSettingsBox);
        saveSettingsBox.setSelected(appPreferences.isSaveSettings());
        saveSettingsBox.addActionListener(e -> saveAppPreferencesQuietly());

        JPanel saveSetSeedPanel = new JPanel();
        saveRomPanel.add(saveSetSeedPanel);
        saveSetSeedPanel.setLayout(new BorderLayout(0, 0));

        JLabel saveSetSeedLbl = new JLabel("Seed: ");
        saveSetSeedPanel.add(saveSetSeedLbl, BorderLayout.WEST);
        saveSetSeedLbl.setHorizontalAlignment(SwingConstants.TRAILING);

        saveSetSeedVal = new JTextField();
        saveSetSeedVal.setToolTipText(
                "Leave blank or put \"random\" for a random seed to be chosen. If the seed is a valid int, it will be used; Otherwise it is treated as a string and hashed into an int. The seed will be changed each time the rom is saved");
        saveSetSeedPanel.add(saveSetSeedVal);
        saveSetSeedVal.setText("Random");
        saveSetSeedVal.setColumns(10);

        JPanel randomizeBtnPanel = new JPanel();
        saveRomPanel.add(randomizeBtnPanel);
        randomizeBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton randomizeButton = new JButton("Randomize!");
        randomizeBtnPanel.add(randomizeButton);
        randomizeButton.addActionListener(event -> {
            try {
                if (!randomizer.isRomLoaded()) {
                    JOptionPane.showMessageDialog(frmTradingCard,
                            "A ROM must be loaded before randomizing.\nUse Open ROM to load one.",
                            "No ROM Loaded", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (dualPanel.getSelectedActions().isEmpty()) {
                    JOptionPane.showMessageDialog(frmTradingCard,
                            "Add at least one module to the selected list on the Actions tab.",
                            "No Modules Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Settings settings = createSettingsFromState();

                if (saveSettingsBox.isSelected()) {
                    int configReturnVal = saveConfigChooser.showSaveDialog(frmTradingCard);
                    if (configReturnVal != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    File configFile = FileExtensionUtils.ensureExtension(
                            saveConfigChooser.getSelectedFile(), YamlIO.FILE_EXTENSION);
                    try {
                        Config config = Config.fromAppState(settings.getSeedString(),
                                dualPanel.getSelectedActions(), randomizer.getActionBank(),
                                randomizer.getRules(), randomizer.getReferenceMonsterCards());
                        YamlIO.save(configFile, config.convertToYamlMap());
                    } catch (IOException configError) {
                        configError.printStackTrace();
                        JOptionPane.showMessageDialog(frmTradingCard, configError.getMessage(),
                                "Config Save Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    saveAppPreferencesQuietly();
                }

                int returnVal = saveRomChooser.showSaveDialog(frmTradingCard);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    saveAppPreferencesQuietly();
                    File saveFile = FileExtensionUtils.ensureExtension(
                            saveRomChooser.getSelectedFile(), RandomizerCore.PATCH_FILE_EXTENSION);
                    if (!randomizer.randomizeAndSaveRom(saveFile, settings,
                            dualPanel.getSelectedActions())) {
                        JOptionPane.showMessageDialog(frmTradingCard,
                                "Randomization failed. See the log for module errors; no patch was written.",
                                "Randomization Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e1) {
                // TODO later: Auto-generated catch block
                e1.printStackTrace();
                JOptionPane.showMessageDialog(frmTradingCard, e1.getMessage(),
                        "Randomization Failed", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(frmTradingCard, e1.getMessage(),
                        "Randomization Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel openRomPanel = new JPanel();
        frmTradingCard.getContentPane().add(openRomPanel, BorderLayout.NORTH);
        openRomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 0));

        openRomButton = new JButton(OPEN_ROM_TEXT);
        openRomButton.addActionListener(event -> {
            int returnVal = openRomChooser.showOpenDialog(frmTradingCard);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                if (!randomizer.openRom(openRomChooser.getSelectedFile(), frmTradingCard)) {
                    JOptionPane.showMessageDialog(frmTradingCard,
                            "Could not open the selected ROM file.", "Open ROM Failed",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    lastOpenedRomPath = openRomChooser.getSelectedFile().getAbsolutePath();
                    updateRomLoadedState();
                    saveAppPreferencesQuietly();
                }
            }
        });
        openRomPanel.add(openRomButton);

        JButton importConfigsButton = new JButton("Import Configs");
        importConfigsButton.addActionListener(event -> importConfigsFromFile());
        openRomPanel.add(importConfigsButton);

        JButton resetConfigsButton = new JButton("Reset Configs");
        resetConfigsButton.setToolTipText(
                "Reset selected config sections back to their default values.");
        resetConfigsButton.addActionListener(event -> resetConfigsToDefaults());
        openRomPanel.add(resetConfigsButton);

        JTabbedPane actionsTab = new JTabbedPane(JTabbedPane.TOP);
        frmTradingCard.getContentPane().add(actionsTab, BorderLayout.CENTER);

        rulesPanel = new RulesPanel(randomizer, appPreferences, this);
        actionsTab.addTab("Rules", null, rulesPanel,
                "Move exclusions and assignments applied during randomization");

        dualPanel = new DualTableSelector(randomizer.getActionBank(), appPreferences, this);
        actionsTab.addTab("Actions", null, dualPanel, null);
        actionsTab.setSelectedComponent(dualPanel);

        actionsTab.addChangeListener(e -> {
            if (actionsTab.getSelectedComponent() == rulesPanel) {
                rulesPanel.refresh();
            }
        });

        frmTradingCard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAppPreferencesQuietly();
            }
        });

        openRomIfExists();
    }

    private AppPreferences loadAppPreferences() {
        try {
            IssueTracker.clear();
            AppPreferences prefs = AppPreferences.load();
            IssuePresenter.finishPhase(frmTradingCard, "app preferences load");
            return prefs;
        } catch (IOException e) {
            e.printStackTrace();
            IssuePresenter.finishPhase(frmTradingCard, "app preferences load");
            return AppPreferences.loadDefaults();
        }
    }

    private void applyFileChooserPreferences() {
        applyChooserDirectory(openRomChooser, appPreferences.getOpenRomDirectory());
        openRomChooser.setSelectedFile(appPreferences.resolveOpenRomFile(DEFAULT_ROM_NAME));

        applyChooserDirectory(saveRomChooser, appPreferences.getPatchDirectory());
        saveRomChooser.setSelectedFile(appPreferences.resolvePatchFile());

        applyChooserDirectory(saveConfigChooser, appPreferences.getSaveConfigDirectory());
        saveConfigChooser.setSelectedFile(appPreferences.resolveSaveConfigFile());

        applyChooserDirectory(loadConfigChooser, appPreferences.getLoadConfigDirectory());
        loadConfigChooser.setSelectedFile(appPreferences.resolveLoadConfigFile());
    }

    private void applyChooserDirectory(JFileChooser chooser, String directoryPath) {
        AppPreferences.applyChooserDirectory(chooser, directoryPath);
    }

    private void applyWindowPreferences() {
        Integer x = appPreferences.getWindowX();
        Integer y = appPreferences.getWindowY();
        Integer width = appPreferences.getWindowWidth();
        Integer height = appPreferences.getWindowHeight();
        if (x != null && y != null && width != null && height != null && width > 0
                && height > 0) {
            frmTradingCard.setBounds(clampToVisibleScreen(new Rectangle(x, y, width, height)));
        }
    }

    private static Rectangle clampToVisibleScreen(Rectangle bounds) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(bounds.width, screen.width);
        int height = Math.min(bounds.height, screen.height);
        int x = Math.max(screen.x, Math.min(bounds.x, screen.x + screen.width - width));
        int y = Math.max(screen.y, Math.min(bounds.y, screen.y + screen.height - height));
        return new Rectangle(x, y, width, height);
    }

    public void saveAppPreferencesQuietly() {
        try {
            captureAppPreferencesFromUi().save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AppPreferences captureAppPreferencesFromUi() {
        Rectangle bounds = clampToVisibleScreen(frmTradingCard.getBounds());
        return AppPreferences.fromAppState(getSelectedLogLevel(), saveLogDetailsBox.isSelected(),
                saveSettingsBox.isSelected(), bounds.x, bounds.y, bounds.width, bounds.height,
                lastOpenedRomPath, openRomChooser.getCurrentDirectory(),
                openRomChooser.getSelectedFile(), saveRomChooser.getCurrentDirectory(),
                saveRomChooser.getSelectedFile(), saveConfigChooser.getCurrentDirectory(),
                saveConfigChooser.getSelectedFile(), loadConfigChooser.getCurrentDirectory(),
                loadConfigChooser.getSelectedFile(),
                rulesPanel.getExportUserRulesDirectory(),
                rulesPanel.getExportUserRulesSelectedFile(),
                dualPanel.getExportActionsDirectory(),
                dualPanel.getExportActionsSelectedFile());
    }

    private Settings createSettingsFromState() {
        Settings settings = new Settings();
        settings.setSeed(saveSetSeedVal.getText());
        settings.setLogDetails(saveLogDetailsBox.isSelected());
        settings.setLogLevel(getSelectedLogLevel());
        applyLogLevelFromUi();
        return settings;
    }

    private LogLevel getSelectedLogLevel() {
        Object selected = logLevelCombo.getSelectedItem();
        return selected instanceof LogLevel level ? level : LogLevel.INFO;
    }

    private void applyLogLevelFromUi() {
        Logger.setMinLogLevel(getSelectedLogLevel());
    }

    private void importConfigsFromFile() {
        int returnVal = loadConfigChooser.showOpenDialog(frmTradingCard);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File configFile = FileExtensionUtils.ensureExtension(loadConfigChooser.getSelectedFile(),
                YamlIO.FILE_EXTENSION);
        try {
            IssueTracker.clear();
            Map<String, Object> yaml = YamlIO.load(configFile);
            String sourceLabel = configFile.getName();
            Config loaded = Config.readFromLoadedYamlMap(yaml, sourceLabel);
            if (!loaded.isValid()) {
                IssuePresenter.finishPhase(frmTradingCard, "config import");
                return;
            }
            if (!loaded.hasRules() && !loaded.hasActionsSection() && !loaded.hasSeed()) {
                JOptionPane.showMessageDialog(frmTradingCard,
                        "The selected file does not contain any importable config sections.",
                        "Nothing to Import", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Optional<ConfigSectionsDialog.Selection> selection =
                    ConfigSectionsDialog.showImport(frmTradingCard, loaded);
            if (selection.isEmpty()) {
                return;
            }
            applyImportedConfig(loaded, selection.get());
            IssuePresenter.finishPhase(frmTradingCard, "config import");
            saveAppPreferencesQuietly();
        } catch (IOException ioError) {
            ioError.printStackTrace();
            JOptionPane.showMessageDialog(frmTradingCard, ioError.getMessage(),
                    "Config Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyImportedConfig(Config loaded, ConfigSectionsDialog.Selection selection) {
        if (selection.generalSettings() && loaded.hasSeed()) {
            saveSetSeedVal.setText(loaded.getSeed());
        }
        if (selection.actions()) {
            if (loaded.hasActions() || loaded.hasPreScripts() || loaded.hasPostScripts()) {
                loaded.checkScripts(randomizer.getActionBank());
            }
            if (loaded.hasActions()) {
                dualPanel.setSelectedActions(loaded.getActions(randomizer.getActionBank()));
            }
        }
        if (selection.rules() && loaded.hasRules()) {
            randomizer.mergeRulesFromConfig(loaded.getRulesConfig());
        }
        rulesPanel.refresh();
    }

    private void resetConfigsToDefaults() {
        Optional<ConfigSectionsDialog.Selection> selection =
                ConfigSectionsDialog.showReset(frmTradingCard);
        if (selection.isEmpty()) {
            return;
        }

        ConfigSectionsDialog.Selection chosen = selection.get();
        if (chosen.generalSettings()) {
            saveSetSeedVal.setText("Random");
            logLevelCombo.setSelectedItem(LogLevel.INFO);
            saveLogDetailsBox.setSelected(true);
            saveSettingsBox.setSelected(true);
            applyLogLevelFromUi();
        }
        if (chosen.actions()) {
            dualPanel.setSelectedActions(List.of());
        }
        if (chosen.rules()) {
            randomizer.resetRulesToBundledDefaults(frmTradingCard);
        }
        rulesPanel.refresh();
        saveAppPreferencesQuietly();
    }

    private void openRomIfExists() {
        File rom = appPreferences.resolveLastRomFile();
        if (rom == null) {
            rom = new File(DEFAULT_ROM_NAME);
        }
        if (rom.exists() && randomizer.openRom(rom, frmTradingCard)) {
            lastOpenedRomPath = rom.getAbsolutePath();
            updateRomLoadedState();
        }
    }

    private void updateRomLoadedState() {
        openRomButton.setText(randomizer.isRomLoaded() ? OPEN_NEW_ROM_TEXT : OPEN_ROM_TEXT);
        rulesPanel.refresh();
    }
}

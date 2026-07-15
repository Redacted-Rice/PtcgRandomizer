package redactedrice.ptcgr.randomizer.gui;

import java.awt.EventQueue;

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
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;

import java.awt.FlowLayout;

import redactedrice.ptcgr.randomizer.RandomizerCore;
import redactedrice.ptcgr.randomizer.Settings;
import redactedrice.ptcgr.randomizer.actions.Action;
import redactedrice.ptcgr.randomizer.gui.dualselector.DualTableSelector;
import redactedrice.ptcgr.utils.FileExtensionUtils;
import redactedrice.ptcgr.utils.WarningCollector;
import redactedrice.ptcgr.configs.YamlIO;
import redactedrice.ptcgr.configs.Config;

import java.util.List;
import java.util.Map;

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
    private JCheckBox saveLogDetailsBox;
    private JCheckBox saveSettingsBox;
    private JTextField saveSetSeedVal;
    private DualTableSelector dualPanel;

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

        openRomChooser = new JFileChooser();
        openRomChooser.setCurrentDirectory(new File(".")); // Jar location by default
        openRomChooser.setSelectedFile(new File(DEFAULT_ROM_NAME));

        saveRomChooser = new JFileChooser();
        saveRomChooser.setCurrentDirectory(new File(".")); // Jar location by default
        saveRomChooser.setSelectedFile(new File(RandomizerCore.DEFAULT_PATCH_BASE_NAME));

        saveConfigChooser = new JFileChooser();
        saveConfigChooser.setCurrentDirectory(new File(".")); // Jar location by default
        saveConfigChooser.setSelectedFile(new File(YamlIO.DEFAULT_FILE_NAME));
        saveConfigChooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));

        loadConfigChooser = new JFileChooser();
        loadConfigChooser.setCurrentDirectory(new File("."));
        loadConfigChooser.setSelectedFile(new File(YamlIO.DEFAULT_FILE_NAME));
        loadConfigChooser.setFileFilter(new FileNameExtensionFilter("YAML files", "yaml", "yml"));

        JPanel saveRomPanel = new JPanel();
        saveRomPanel.setBorder(new EmptyBorder(4, 7, 4, 7));
        frmTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
        saveRomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 0));

        saveLogDetailsBox = new JCheckBox("Log Randomizations");
        saveLogDetailsBox.setToolTipText(
                "Write a detailed log of randomization changes alongside the patch file.");
        saveRomPanel.add(saveLogDetailsBox);
        saveLogDetailsBox.setSelected(true);

        saveSettingsBox = new JCheckBox("Save Settings");
        saveSettingsBox.setToolTipText(
                "Save the config settings to a file that can be loaded to repeat the same randomization again later.");
        saveRomPanel.add(saveSettingsBox);
        saveSettingsBox.setSelected(true);

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
                                randomizer.getPendingRules());
                        YamlIO.save(configFile, config.convertToYamlMap());
                    } catch (IOException configError) {
                        configError.printStackTrace();
                        JOptionPane.showMessageDialog(frmTradingCard, configError.getMessage(),
                                "Config Save Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                int returnVal = saveRomChooser.showSaveDialog(frmTradingCard);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File saveFile = FileExtensionUtils.ensureExtension(
                            saveRomChooser.getSelectedFile(), RandomizerCore.PATCH_FILE_EXTENSION);
                    randomizer.randomizeAndSaveRom(saveFile, settings,
                            dualPanel.getSelectedActions());
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
                }
                updateRomLoadedState();
            }
        });
        openRomPanel.add(openRomButton);

        JButton loadConfigsButton = new JButton("Load Configs");
        loadConfigsButton.addActionListener(event -> loadConfigsFromFile());
        openRomPanel.add(loadConfigsButton);

        JTabbedPane actionsTab = new JTabbedPane(JTabbedPane.TOP);
        frmTradingCard.getContentPane().add(actionsTab, BorderLayout.CENTER);

        dualPanel = new DualTableSelector(randomizer.getActionBank());
        actionsTab.addTab("Actions", null, dualPanel, null);

        openRomIfExists();
    }

    private Settings createSettingsFromState() {
        Settings settings = new Settings();
        settings.setSeed(saveSetSeedVal.getText());
        settings.setLogDetails(saveLogDetailsBox.isSelected());
        return settings;
    }

    private void loadConfigsFromFile() {
        int returnVal = loadConfigChooser.showOpenDialog(frmTradingCard);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File configFile = FileExtensionUtils.ensureExtension(loadConfigChooser.getSelectedFile(),
                YamlIO.FILE_EXTENSION);
        try {
            WarningCollector warnings = new WarningCollector(frmTradingCard);
            Map<String, Object> yaml = YamlIO.load(configFile, warnings);
            Config config = Config.readFromLoadedYamlMap(yaml, configFile.getName(), warnings);
            saveSetSeedVal.setText(config.getSeed());
            config.checkScripts(randomizer.getActionBank(), warnings);
            randomizer.replacePendingRules(config.getRulesConfig());
            List<Action> actions = config.getActions(randomizer.getActionBank(), warnings);
            dualPanel.setSelectedActions(actions);
            warnings.logAndDisplay("config load", true);
        } catch (IOException ioError) {
            ioError.printStackTrace();
            JOptionPane.showMessageDialog(frmTradingCard, ioError.getMessage(),
                    "Config Load Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRomIfExists() {
        File defaultRom = new File(DEFAULT_ROM_NAME);
        if (defaultRom.exists() && randomizer.openRom(defaultRom, frmTradingCard)) {
            updateRomLoadedState();
        }
    }

    private void updateRomLoadedState() {
        openRomButton.setText(randomizer.isRomLoaded() ? OPEN_NEW_ROM_TEXT : OPEN_ROM_TEXT);
    }
}

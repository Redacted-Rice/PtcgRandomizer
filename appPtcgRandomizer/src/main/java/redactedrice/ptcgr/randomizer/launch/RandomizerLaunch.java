package redactedrice.ptcgr.randomizer.launch;

import java.awt.EventQueue;

import redactedrice.ptcgr.randomizer.gui.RandomizerApp;
import redactedrice.randomizer.utils.Logger;

/** Entry point routing for GUI, headless batch, and script tests. */
public final class RandomizerLaunch {
    private RandomizerLaunch() {
    }

    public static void main(String[] args) {
        CommandLineArgs launch = CommandLineArgs.parse(args);
        Logger.setMinLogLevel(launch.getLogLevel());

        // Check for an warn of unknown arguements
        if (!launch.getUnknownArgs().isEmpty()) {
            System.err.println("Unknown argument(s): " + String.join(" ", launch.getUnknownArgs()));
        }

        // If its script tests, it implies headless. Run and exit
        if (launch.isScriptTests()) {
            System.exit(ScriptTestRunner.run(launch));
        }

        // If its headless, run and exit
        if (launch.isHeadless()) {
            System.exit(HeadlessRunner.run(launch));
        }

        // Otherwise its a normal GUI launch
        EventQueue.invokeLater(() -> {
            try {
                new RandomizerApp(launch).showWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

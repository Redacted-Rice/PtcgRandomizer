package redactedrice.ptcgr.regression;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Regression harness application. Spawns PtcgRandomizer fat JAR in headless CLI mode per case.
 * Run from appPtcgrRegression module root (Gradle {@code runRegressionTests} does this).
 *
 * Usage: RegressionSuiteRunner &lt;appPtcgRandomizerDir&gt; [--rom path]
 */
public final class RegressionSuiteRunner {
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    // matches HeadlessRunner exit code
    public static final int ROM_NOT_FOUND = 2;
    public static final int USAGE_ERROR = 3;

    // CLI flags and child exit codes must match CommandLineArgs/HeadlessRunner
    // in PTCGR app
    private static final String HEADLESS_FLAG = "--headless";
    private static final String CONFIG_FLAG = "--config";
    private static final String ROM_FLAG = "--rom";
    private static final String LOG_LEVEL_FLAG = "--log-level";
    private static final String CLEAN_REINSTALL_RESOURCES_FLAG = "--clean-reinstall-resources";
    private static final int HEADLESS_EXIT_SUCCESS = 0;

    private static final String CASES_REL = "src/regression_tests/cases";
    private static final String RANDOMIZER_JAR_PREFIX = "PtcgRandomizer-";

    private RegressionSuiteRunner() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        try {
            return runSuite(RegressionOptions.fromArgs(args));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return USAGE_ERROR;
        } catch (IOException e) {
            System.err.println("Regression suite failed: " + e.getMessage());
            e.printStackTrace();
            return FAILURE;
        }
    }

    private static int runSuite(RegressionOptions options) throws IOException {
        // Find the ROM and JAR
        if (!options.rom.isFile()) {
            System.err.println("WARNING: Regression tests skipped (ROM not found: "
                    + options.rom.getAbsolutePath() + ")");
            return ROM_NOT_FOUND;
        }

        File randomizerJar = findRandomizerJar(new File(options.appDir, "app"));
        if (randomizerJar == null) {
            System.err.println("PtcgRandomizer JAR missing under " + options.appDir
                    + "/app. Run: gradlew :appPtcgRandomizer:fatJar");
            return FAILURE;
        }

        // Search and find all regression cases
        File casesDir = new File(options.regressionModuleDir, CASES_REL);
        if (!casesDir.isDirectory()) {
            System.err.println("Regression cases directory not found: " + casesDir);
            return FAILURE;
        }

        List<File> caseDirs = listCaseDirectories(casesDir);
        if (caseDirs.isEmpty()) {
            System.err.println("No regression cases under " + casesDir);
            return FAILURE;
        }
        File regressionBuild = new File(options.regressionModuleDir, "build/regression");
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();

        // Go through and execute each case
        for (File caseDir : caseDirs) {
            String caseName = caseDir.getName();
            File config = new File(caseDir, "config.yaml");
            File expectations = new File(caseDir, "expectations.yaml");
            if (!config.isFile() || !expectations.isFile()) {
                System.err.println("Case " + caseName + " needs config.yaml and expectations.yaml");
                return FAILURE;
            }

            File workDir = new File(regressionBuild, "cases/" + caseName + "/out");
            File log = new File(regressionBuild, "cases/" + caseName + "/headless.log");
            Files.createDirectories(workDir.toPath());
            Files.writeString(log.toPath(), "");

            System.out.println("Running regression case " + caseName + " (ROM: "
                    + options.rom.getAbsolutePath() + ")");

            int headlessExit =
                    runHeadlessCli(javaBin, randomizerJar, config, options.rom, workDir, log);
            if (headlessExit != HEADLESS_EXIT_SUCCESS) {
                System.err.println("Headless run failed for case " + caseName + " (exit "
                        + headlessExit + "). Log: " + log);
                Files.lines(log.toPath()).forEach(System.err::println);
                return FAILURE;
            }
            if (RegressionCaseVerifier.verify(expectations, log, workDir) != 0) {
                System.err.println("Expectations check failed for case " + caseName);
                return FAILURE;
            }
            System.out.println("Case " + caseName + " passed");
        }

        System.out.println("All regression cases passed");
        return SUCCESS;
    }

    private static int runHeadlessCli(String javaBin, File randomizerJar, File config, File rom,
            File workDir, File log) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(javaBin, "-Dfile.encoding=UTF-8", "-jar",
                randomizerJar.getAbsolutePath(), CLEAN_REINSTALL_RESOURCES_FLAG, HEADLESS_FLAG,
                CONFIG_FLAG, config.getAbsolutePath(), ROM_FLAG, rom.getAbsolutePath(),
                LOG_LEVEL_FLAG, "INFO");
        builder.directory(workDir);
        builder.redirectOutput(log);
        builder.redirectError(ProcessBuilder.Redirect.appendTo(log));
        Process process = builder.start();
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Headless run interrupted", e);
        }
    }

    private static List<File> listCaseDirectories(File casesDir) {
        List<File> dirs = new ArrayList<>();
        File[] children = casesDir.listFiles(File::isDirectory);
        if (children != null) {
            for (File child : children) {
                dirs.add(child);
            }
        }
        dirs.sort(Comparator.comparing(File::getName));
        return dirs;
    }

    static File findRandomizerJar(File appDir) throws IOException {
        if (!appDir.isDirectory()) {
            return null;
        }
        try (Stream<Path> jars = Files.list(appDir.toPath())) {
            return jars.filter(path -> {
                String name = path.getFileName().toString();
                return name.startsWith(RANDOMIZER_JAR_PREFIX) && name.endsWith(".jar");
            }).max(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> path.toFile().getAbsoluteFile()).orElse(null);
        }
    }
}

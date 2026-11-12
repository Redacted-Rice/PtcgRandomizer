import org.gradle.api.GradleException

plugins {
    java
}

group = "redactedrice"
version = "0.9.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.snakeyaml)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(20)
    }
}

val randomizerProject = project(":appPtcgRandomizer")
val regressionMainClass = "redactedrice.ptcgr.regression.RegressionSuiteRunner"

tasks.register<JavaExec>("runRegressionTests") {
    group = "verification"
    description =
        "Headless ROM regression cases. Optional: -Prom=path, -PregressionCase=name, " +
            "-PregressionCleanReinstall=first|every|never (requires :appPtcgRandomizer:fatJar)"
    dependsOn("classes", randomizerProject.tasks.named("fatJar"))
    mustRunAfter(
        randomizerProject.tasks.named("test"),
        randomizerProject.tasks.named("runScriptTests"),
    )

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(regressionMainClass)
    workingDir = layout.projectDirectory.asFile

    val appDir = randomizerProject.layout.projectDirectory.asFile.absolutePath
    args(appDir)

    val romPath = project.findProperty("rom") as String?
    if (romPath != null) {
        args("--rom", romPath)
    }

    val regressionCase = project.findProperty("regressionCase") as String?
    if (regressionCase != null) {
        args("--case", regressionCase)
    }

    val regressionCleanReinstall = project.findProperty("regressionCleanReinstall") as String?
    if (regressionCleanReinstall != null) {
        args("--clean-reinstall", regressionCleanReinstall)
    }

    isIgnoreExitValue = true
    doLast {
        val exitValue = executionResult.get().exitValue
        when (exitValue) {
            0 -> Unit
            2 -> logger.lifecycle(
                "ROM regression skipped (exit 2, ROM not found). Use -Prom= or PTCGR_ROM",
            )
            else -> throw GradleException("Regression tests failed (exit $exitValue)")
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

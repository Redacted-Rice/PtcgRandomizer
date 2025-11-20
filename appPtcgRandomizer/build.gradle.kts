plugins {
    application
}

group = "redactedrice"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    implementation("redactedrice:libGbcFramework:0.8.0")
    implementation("redactedrice:libGbz80Compiler:0.8.0")
    implementation("redactedrice:libGbcRomPacker:0.8.0")
    implementation("redactedrice:libBpsQueuedWriter:0.8.0")
    implementation("redactedrice:libUniversalRandomizerJava:0.5.0")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(20)
    }
}

application {
    // Define the main class for the application.
    mainClass = "redactedrice.ptcgr.randomizer.gui.RandomizerApp"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

// Task to generate manifest file for modules resource folder
tasks.register("generateModulesManifest") {
    group = "build"
    description = "Generates manifest file for modules resource folder"

    doLast {
        val manifestFile = file("${projectDir}/src/main/resources/modules/.manifest")
        val modulesDir = file("${projectDir}/src/main/resources/modules")

        // Walk the directory tree and collect all .lua files with relative paths
        val files = fileTree(modulesDir) {
            include("**/*.lua")
            exclude(".manifest")
        }.files.map { file ->
            modulesDir.toPath().relativize(file.toPath()).toString().replace('\\', '/')
        }.sorted()

        manifestFile.writeText(files.joinToString("\n"))
    }
}

// Make processResources depend on generateModulesManifest to ensure manifest is generated before packaging
tasks.named("processResources") {
    dependsOn("generateModulesManifest")
}

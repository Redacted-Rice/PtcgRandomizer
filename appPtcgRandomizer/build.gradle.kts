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

    val manifestFile = layout.projectDirectory.file("src/main/resources/modules/.manifest")
    val modulesDir = layout.projectDirectory.dir("src/main/resources/modules")

    doLast {
        // Walk the directory tree and collect all .lua files with relative paths
        val modulesDirFile = modulesDir.asFile
        val files = mutableListOf<String>()
        
        fun collectLuaFiles(dir: java.io.File, basePath: java.nio.file.Path) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    collectLuaFiles(file, basePath)
                } else if (file.isFile && file.name.endsWith(".lua") && file.name != ".manifest") {
                    val relativePath = basePath.relativize(file.toPath()).toString().replace('\\', '/')
                    files.add(relativePath)
                }
            }
        }
        
        if (modulesDirFile.exists() && modulesDirFile.isDirectory) {
            collectLuaFiles(modulesDirFile, modulesDirFile.toPath())
        }
        
        manifestFile.asFile.writeText(files.sorted().joinToString("\n"))
    }
}

// Make processResources depend on generateModulesManifest to ensure manifest is generated before packaging
tasks.named<ProcessResources>("processResources") {
    dependsOn("generateModulesManifest")
}

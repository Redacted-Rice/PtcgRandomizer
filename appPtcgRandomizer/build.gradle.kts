plugins {
    application
}

import org.gradle.jvm.tasks.Jar

group = "redactedrice"
version = "0.9.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation("org.luaj:luaj-jse:3.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.snakeyaml)

    implementation("redactedrice:libGbcFramework:0.8.0")
    implementation("redactedrice:libGbz80Compiler:0.8.0")
    implementation("redactedrice:libGbcRomPacker:0.8.0")
    implementation("redactedrice:libBpsQueuedWriter:0.8.0")
    implementation("redactedrice:libUniversalRandomizerJava:0.5.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(20)
    }
}

application {
    mainClass = "redactedrice.ptcgr.randomizer.gui.RandomizerApp"
}

val runnableJarName = "PtcgRandomizer-${project.version}.jar"

tasks.named<Jar>("jar") {
    enabled = false
}

val generateAppVersion = tasks.register("generateAppVersion") {
    group = "build"
    description = "Generates app-version.properties from the Gradle project version"

    val appVersion = version.toString()
    val outputFile = layout.buildDirectory.file(
        "generated/resources/redactedrice/ptcgr/constants/app-version.properties")

    outputs.file(outputFile)

    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText("version=$appVersion\n")
    }
}

sourceSets.main.get().resources.srcDir(
    layout.buildDirectory.dir("generated/resources"))

tasks.register("generateModulesManifest") {
    group = "build"
    description = "Generates manifest file for modules resource folder"

    val manifestFile = layout.projectDirectory.file("src/main/resources/modules/.manifest")
    val modulesDir = layout.projectDirectory.dir("src/main/resources/modules")

    doLast {
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

tasks.register("generateDevModulesManifest") {
    group = "build"
    description = "Generates manifest file for the dev only modules resource folder"

    val manifestFile = layout.projectDirectory.file("src/main/resources/devmodules/.manifest")
    val devModulesDir = layout.projectDirectory.dir("src/main/resources/devmodules")

    doLast {
        val devModulesDirFile = devModulesDir.asFile
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

        if (devModulesDirFile.exists() && devModulesDirFile.isDirectory) {
            collectLuaFiles(devModulesDirFile, devModulesDirFile.toPath())
        }

        manifestFile.asFile.writeText(files.sorted().joinToString("\n"))
    }
}

tasks.register("generateRulesManifest") {
    group = "build"
    description = "Generates manifest file for rules resource folder"

    val manifestFile = layout.projectDirectory.file("src/main/resources/rules/.manifest")
    val rulesDir = layout.projectDirectory.dir("src/main/resources/rules")

    doLast {
        val rulesDirFile = rulesDir.asFile
        val files = mutableListOf<String>()

        if (rulesDirFile.exists() && rulesDirFile.isDirectory) {
            rulesDirFile.listFiles()?.forEach { file ->
                if (file.isFile && file.name != ".manifest") {
                    files.add(file.name)
                }
            }
        }

        manifestFile.asFile.writeText(files.sorted().joinToString("\n"))
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("generateAppVersion", "generateModulesManifest", "generateDevModulesManifest",
        "generateRulesManifest")
}

tasks.register<Jar>("fatJar") {
    group = "application"
    description = "Builds the runnable application JAR with all dependencies and bundled resources"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn("classes", "processResources")

    archiveFileName.set(runnableJarName)
    destinationDirectory.set(layout.projectDirectory.dir("app"))

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Dev only test modules are never bundled into release packages. See the run task
    // and PtcgBundledResources.installDevAppResources() for how they're installed
    // for dev builds
    from(sourceSets.main.get().output) {
        exclude("devmodules/**")
    }
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    }) {
        exclude(
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
        )
    }
}

tasks.named<JavaExec>("run") {
    dependsOn("processResources")
    // run is the dev build/run path, so also install the dev only test modules
    // alongside the regular ones. Release packages (fatJar) never set this property.
    systemProperty("ptcgr.devModules", "true")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn("fatJar")
}

tasks.named("assemble") {
    dependsOn("fatJar")
}

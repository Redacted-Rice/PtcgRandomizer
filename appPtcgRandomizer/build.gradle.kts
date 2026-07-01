plugins {
    application
}

import org.gradle.jvm.tasks.Jar

group = "redactedrice"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.guava)
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

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn("fatJar")
}

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
    dependsOn("generateModulesManifest", "generateRulesManifest")
}

tasks.register<Jar>("fatJar") {
    group = "application"
    description = "Builds a single runnable JAR with all dependencies and bundled resources"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn("jar")

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)
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
}

tasks.named("build") {
    dependsOn("fatJar")
}

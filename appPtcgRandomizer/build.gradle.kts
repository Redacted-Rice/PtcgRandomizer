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
    mainClass = "redactedrice.randomizer.PtcgRandomizerApp"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

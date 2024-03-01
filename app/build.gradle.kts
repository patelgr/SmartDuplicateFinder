plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.yaml:snakeyaml:2.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
application {
    mainClass.set("net.hiralpatel.SDFApplication")
    applicationDefaultJvmArgs = listOf("-Dmode=server") // Set default JVM argument
}

// Define a task for running the server
tasks.register<JavaExec>("serverMode") {
    group = "Application"
    description = "Runs the application in server mode."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.hiralpatel.SDFApplication") // Set the main class
    jvmArgs("-Dmode=server") // Pass mode as an argument
}

// Define a task for running the client
tasks.register<JavaExec>("clientMode") {
    group = "Application"
    description = "Runs the application in client mode."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.hiralpatel.monitoring.MonitoringClient") // Set the client's main class
    jvmArgs("-Dmode=client") // Set a JVM system property
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


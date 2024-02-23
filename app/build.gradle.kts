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
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "net.hiralpatel.cli.AppCli"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


val kotlin_version: String by project
val logback_version: String by project

group = "org.openapitools"
version = "1.0.0"

plugins {
    kotlin("jvm") version "2.0.20"
    application
    kotlin("plugin.serialization") version "2.0.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    jacoco
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:3.0.2"))
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auto-head-response")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-hsts")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.dropwizard.metrics:metrics-core:4.1.18")
    implementation("io.ktor:ktor-server-metrics")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-status-pages")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("io.ktor:ktor-client-content-negotiation")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    ignoreFailures = true
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

ktlint {
    version.set("1.0.1")
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true)
}

detekt {
    toolVersion = "1.23.4"
    buildUponDefaultConfig = true
    ignoreFailures = true
}

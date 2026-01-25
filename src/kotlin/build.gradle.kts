
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
    id("com.github.johnrengelman.shadow") version "8.1.1"
    jacoco
}

application {
    mainClass.set("com.lampcontrol.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.shadowJar {
    archiveBaseName.set("kotlin-server")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "com.lampcontrol.ApplicationKt"
    }
}

// Configure run task to pass environment variables to the application
tasks.named<JavaExec>("run") {
    // Pass all environment variables to the application
    environment(System.getenv() as Map<String, Any>)
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

    // Exposed ORM for PostgreSQL
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Flyway for database migrations
    implementation("org.flywaydb:flyway-core:10.21.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.21.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

tasks.test {
    useJUnitPlatform()
    ignoreFailures = true

    // Allow JUnit Pioneer to modify environment variables via reflection
    jvmArgs(
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
    )
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/com/lampcontrol/Application*",
                    "**/com/lampcontrol/api/AppMain*",
                    "**/com/lampcontrol/api/Configuration*",
                    "**/com/lampcontrol/api/Paths*"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/com/lampcontrol/Application*",
                    "**/com/lampcontrol/api/AppMain*",
                    "**/com/lampcontrol/api/Configuration*",
                    "**/com/lampcontrol/api/Paths*"
                )
            }
        })
    )
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
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
    config.setFrom("$projectDir/detekt.yml")
    ignoreFailures = false
}

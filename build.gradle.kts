plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    implementation("org.postgresql:postgresql:42.7.4")

    // HttpServer
    implementation("io.vertx:vertx-core:4.5.11")
    implementation("io.vertx:vertx-web:4.5.11")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.5.11")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

detekt {
    toolVersion = "1.23.7"
    config.from("$rootDir/detekt.yml")
    buildUponDefaultConfig = true // Расширяем базовые правила Detekt
    allRules = false // Включаем только необходимые правила
}

// TODO добавить для CI
//tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
//    reports {
//        html.required.set(true) // Отчет в HTML
//        xml.required.set(true)  // Отчет в XML
//        txt.required.set(false) // Текстовый отчет не нужен
//    }
//}
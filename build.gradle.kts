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
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
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
plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.serialization)
}

group = "com.lizz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.mcp.kotlin)
    implementation(libs.slf4j.nop)
    implementation(libs.sqlite.jdbc)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.google.client.api)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}

application {
    mainClass.set("MainKt")
}
import org.jetbrains.kotlin.ir.backend.js.compile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("org.openapi.generator") version "5.3.0"
    id("de.undercouch.download") version "4.1.2"
}

group = "eu.bitwalker"
version = "0.0.1"
application {
    mainClass.set("eu.bitwalker.symasset.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-html-builder:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.dropwizard.metrics:metrics-core:4.2.7")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("ch.qos.logback:logback-classic:1.2.1")

}

task<de.undercouch.gradle.tasks.download.Download>("Download-Sympower-Metering-API") {
    description = "Download YAML file of the metering API"
    src("https://api.swaggerhub.com/apis/Sympower/Sympower-Metering-API/2.0.1")
    dest("$buildDir/openAPI/sympower-metering-api.yaml")
}

task<de.undercouch.gradle.tasks.download.Download>("Download-Sympower-Control-API") {
    description = "Download YAML file of the control API"
    src("https://api.swaggerhub.com/apis/Sympower/Sympower-Control-API/2.1.0")
    dest("$buildDir/openAPI/sympower-control-api.yaml")
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("Generate-Sympower-Metering-API-Server") {
    dependsOn("Download-Sympower-Metering-API")
    generatorName.set("kotlin-server")
    inputSpec.set("$buildDir/openAPI/sympower-metering-api.yaml").toString()
    outputDir.set("$buildDir/generated")
    configFile.set("src/main/resources/metering-api-config.json")
}

task<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("Generate-Sympower-Control-API-Server") {
    dependsOn("Download-Sympower-Control-API")
    generatorName.set("kotlin-server")
    inputSpec.set("$buildDir/openAPI/sympower-control-api.yaml").toString()
    outputDir.set("$buildDir/generated")
    configFile.set("src/main/resources/control-api-config.json")
}

tasks.named("compileKotlin") {
    dependsOn ("Generate-Sympower-Control-API-Server", "Generate-Sympower-Metering-API-Server")
}

tasks.create("stage") {
    dependsOn("installDist")
}

kotlin {
    sourceSets {
       main {
           kotlin.srcDir("$buildDir/generated/src/main")
       }
    }
}


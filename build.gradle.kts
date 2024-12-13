import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val main by extra("me.imoltres.bbu.BBU")
val ver by extra("0.1-BETA")
val apiVersion by extra("1.21")

val spigotVersion by extra("1.21-R0.1-SNAPSHOT")
val gsonVersion by extra("2.11.0")

val lombokVersion by extra("1.18.36")

val outputDir by extra(File(rootProject.projectDir, "dist"))
val outputName by extra(rootProject.name + "-" + ver + ".jar")
outputDir.mkdirs()

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.lombok") version "2.1.0"

    id("org.jetbrains.dokka") version "2.0.0-Beta"
    id("io.freefair.lombok") version "8.11"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "me.imoltres"
version = extra["ver"]!!

repositories {
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    mavenCentral()
}

dependencies {
    implementation(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.9.0")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "2.1.0")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.17.0")
    compileOnly(group = "org.purpurmc.purpur", name = "purpur-api", version = spigotVersion)

    implementation(platform("com.intellectualsites.bom:bom-newest:1.51"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.0.0-Beta")
}

dokka {
    moduleName.set("BBUReloaded")

    dokkaPublications.html {
        outputDirectory.set(File(projectDir, "docs"))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set(null as String?)
        archiveVersion.set(null as String?)
        destinationDirectory.set(outputDir)

        //BBUReloaded-0.1-DEV.jar
        archiveFileName.set(outputName)

        dependencies {

        }
    }

    processResources {
        doFirst {
            println("Replacing tokens in plugin.yml")
            println(rootProject.name)
        }

        filesMatching("plugin.yml") {
            expand(
                "main" to main,
                "name" to rootProject.name,
                "pluginVersion" to ver,
                "apiversion" to apiVersion
            )
        }

        outputs.upToDateWhen { false }
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

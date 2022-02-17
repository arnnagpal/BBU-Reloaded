val main by extra("me.imoltres.bbu.BBU")
val ver by extra("0.0.1-PRE")
val apiVersion by extra("1.17")

val spigotVersion by extra("1.18.1-R0.1-SNAPSHOT")
val gsonVersion by extra("2.8.9")
val faweVersion by extra("2.0.1")

val lombokVersion by extra("1.18.22")

val outputDir by extra(File(rootProject.projectDir, "dist"))
val outputName by extra(rootProject.name + "-" + ver + ".jar")
outputDir.mkdirs()

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.lombok") version "1.6.0"

    id("org.jetbrains.dokka") version "1.6.10"
    id("io.freefair.lombok") version "6.4.0"
}

group = "me.imoltres"
version = extra["ver"]!!

repositories {
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    mavenCentral()
}

dependencies {
    implementation(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.6.0")

    compileOnly(group = "org.purpurmc.purpur", name = "purpur-api", version = spigotVersion)
    compileOnly(group = "com.fastasyncworldedit", name = "FastAsyncWorldEdit-Bukkit", version = faweVersion)

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.10")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.8.1")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.8.1")
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

    dokkaHtml.configure {
        outputDirectory.set(File(projectDir, "docs"))
    }

    test {
        useJUnitPlatform()
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

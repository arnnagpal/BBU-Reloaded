val main by extra("me.imoltres.bbu.BBU")
val ver by extra("0.0.3-DEV")
val apiVersion by extra("1.19")

val spigotVersion by extra("1.19.3-R0.1-SNAPSHOT")
val gsonVersion by extra("2.10.1")

val lombokVersion by extra("1.18.22")

val outputDir by extra(File(rootProject.projectDir, "dist"))
val outputName by extra(rootProject.name + "-" + ver + ".jar")
outputDir.mkdirs()

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.lombok") version "1.8.0"

    id("org.jetbrains.dokka") version "1.7.20"
    id("io.freefair.lombok") version "6.6.1"
}

group = "me.imoltres"
version = extra["ver"]!!

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    mavenCentral()
}

dependencies {
    implementation(group = "com.google.code.gson", name = "gson", version = gsonVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "1.8.0")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.12.0")

    compileOnly(group = "org.purpurmc.purpur", name = "purpur-api", version = spigotVersion)

    implementation(platform("com.intellectualsites.bom:bom-1.18.x:1.23"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly(group = "com.fastasyncworldedit", name = "FastAsyncWorldEdit-Bukkit") {
        isTransitive = false
    }



    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")

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

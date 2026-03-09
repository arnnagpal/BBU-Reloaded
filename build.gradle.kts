val main by extra("me.imoltres.bbu.BBU")
val ver by extra("0.0.3-DEV")
val apiVersion by extra("1.21.11")
val kotlinVersion = extra("2.3.0")

val spigotVersion by extra("$apiVersion-R0.1-SNAPSHOT")
val gsonVersion by extra("2.13.2")
val coroutinesVersion by extra("1.10.2")
val commonsLangVersion by extra("3.20.0")
val bomVersion by extra("1.55")
val nbtLibVersion by extra("5.0.0")

val outputDir by extra(File(rootProject.projectDir, "dist"))
val outputName by extra(rootProject.name + "-" + ver + ".jar")
outputDir.mkdirs()

plugins {
    id("com.gradleup.shadow") version "9.2.1"
    java
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.lombok") version "2.3.10"

    id("org.jetbrains.dokka") version "1.7.20"
    id("io.freefair.lombok") version "9.1.0"
}

group = "me.imoltres"
version = extra["ver"]!!
kotlin.jvmToolchain(25)

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }

    maven {
        url = uri("https://repo.viaversion.com")
    }
}

dependencies {
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")

    compileOnly("org.purpurmc.purpur:dev-bundle:$spigotVersion")
//    compileOnly("org.purpurmc.purpur:purpur-api:${spigotVersion}")

//    implementation(platform("com.intellectualsites.bom:bom-newest:$bomVersion"))
//    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:$faweVersion")
//    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:$faweVersion") {
//        isTransitive = false
//    }

    // replacing fawe
    implementation("com.viaversion:nbt:$nbtLibVersion")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.2.0-Beta")
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

}

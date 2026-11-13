// Buildscript for OBFUSCATED Minecraft (1.21.11 and older).
// Same as build.gradle.kts, but Loom remaps: Mojang mappings + mod* configurations.
plugins {
    id("net.fabricmc.fabric-loom-remap")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
group = property("mod.group") as String
base { archivesName = property("mod.id") as String }

repositories {
    maven("https://maven.terraformersmc.com/") { name = "Terraformers (Mod Menu)" }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    // Only needed so ModMenuIntegration compiles (and so Mod Menu is there in runClient).
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

// 1.20.5 - 1.21.11 run on Java 21, older versions on Java 17.
val javaVersion = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5")) 21 else 17

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_range").toString(),
        "java" to javaVersion.toString(),
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "viewhands.mixins.json")) { expand(props) }
}

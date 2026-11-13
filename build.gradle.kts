// Buildscript for UNOBFUSCATED Minecraft (26.1 and newer). No mappings, no remapping.
plugins {
    id("net.fabricmc.fabric-loom")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
group = property("mod.group") as String
base { archivesName = property("mod.id") as String }

repositories {
    maven("https://maven.terraformersmc.com/") { name = "Terraformers (Mod Menu)" }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    // Only needed so ModMenuIntegration compiles (and so Mod Menu is there in runClient).
    implementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to project.property("mod.mc_range").toString(),
        "java" to "25",
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "viewhands.mixins.json")) { expand(props) }
}

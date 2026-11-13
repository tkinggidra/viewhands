pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.6"
}

stonecutter {
    create(rootProject) {
        // Obfuscated versions (1.21.11 and older) need Loom's remapping plugin -> separate buildscript.
        versions("1.21.11").buildscript("build.obf.gradle.kts")
        // Unobfuscated versions (26.1+) use build.gradle.kts.
        versions("26.1.2", "26.2")
        // The version the files in src/ are "written in" when committed to git.
        vcsVersion = "26.2"
    }
}

rootProject.name = "viewhands"

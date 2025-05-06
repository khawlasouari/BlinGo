pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.2.0" // ou celle que tu utilises
        id("com.google.gms.google-services") version "4.3.15"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

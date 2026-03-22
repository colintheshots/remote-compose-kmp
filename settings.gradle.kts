pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "remote-compose-kmp"

include(":remote-core")
include(":remote-creation-core")
include(":remote-creation")
include(":remote-player-core")
include(":remote-player-compose")
include(":sample:shared")
include(":sample:androidApp")

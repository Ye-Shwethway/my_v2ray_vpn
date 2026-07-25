pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        ivy {
            url = uri("https://github.com/2dust/AndroidLibV2rayLite/releases/download/")
            patternLayout {
                artifact("[revision]/[artifact].[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
}

rootProject.name = "VPN Project"
include(":app")

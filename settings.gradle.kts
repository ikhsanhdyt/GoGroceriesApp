pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "GoGroceriesApp"
include(":app")
include(":core:ui")
include(":core:util")
include(":domain")
include(":data")
include(":feature:home")
include(":feature:categories")
include(":feature:list-detail")
include(":feature:active-shopping")
include(":feature:trip-summary")
include(":feature:analytics")

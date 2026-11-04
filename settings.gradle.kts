pluginManagement {
    includeBuild("build-logic")
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

include(":core:common")
include(":core:entity")
include(":core:database")
include(":core:navigation")
include(":core:ui")

include(":api:category")
include(":api:grocerylist")

include(":feature:home")
include(":feature:categories")
include(":feature:list-detail")
include(":feature:active-shopping")
include(":feature:trip-summary")
include(":feature:analytics")

plugins {
    `kotlin-dsl`
}

group = "com.diavolo.gogroceriesapp.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.gogroceries.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidCore") {
            id = libs.plugins.gogroceries.android.core.get().pluginId
            implementationClass = "AndroidCoreConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.gogroceries.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.gogroceries.android.hilt.get().pluginId
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidApi") {
            id = libs.plugins.gogroceries.android.api.get().pluginId
            implementationClass = "AndroidApiConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.gogroceries.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.gogroceries.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}

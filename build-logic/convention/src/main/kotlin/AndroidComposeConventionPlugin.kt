import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.diavolo.gogroceriesapp.buildlogic.lib
import com.diavolo.gogroceriesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Adds Compose to a module that already applies an Android application or library plugin. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> { buildFeatures.compose = true }
        }

        dependencies {
            val bom = platform(libs.lib("compose-bom"))
            add("implementation", bom)
            add("implementation", libs.lib("compose-ui"))
            add("implementation", libs.lib("compose-material3"))
            add("implementation", libs.lib("compose-ui-tooling-preview"))
            add("debugImplementation", libs.lib("compose-ui-tooling"))

            add("androidTestImplementation", bom)
            add("androidTestImplementation", libs.lib("compose-ui-test-junit4"))
            add("androidTestImplementation", libs.lib("androidx-test-ext-junit"))
            add("androidTestImplementation", libs.lib("androidx-test-espresso-core"))
            add("debugImplementation", libs.lib("compose-ui-test-manifest"))
        }
    }
}

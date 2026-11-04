import com.diavolo.gogroceriesapp.buildlogic.lib
import com.diavolo.gogroceriesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** A `:feature:*` module: Compose screens, their view models and Hilt. */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("gogroceries.android.core")
        pluginManager.apply("gogroceries.android.compose")
        pluginManager.apply("gogroceries.android.hilt")

        dependencies {
            add("implementation", libs.lib("compose-material3-icon"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("hilt-navigation-compose"))
            add("implementation", libs.lib("kotlinx-coroutines-android"))

            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}

import com.diavolo.gogroceriesapp.buildlogic.lib
import com.diavolo.gogroceriesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** An `:api:*` module: one domain's data sources, repository, use cases and their Hilt bindings. */
class AndroidApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("gogroceries.android.core")
        pluginManager.apply("gogroceries.android.hilt")

        dependencies {
            add("implementation", libs.lib("kotlinx-coroutines-android"))

            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            add("testImplementation", libs.lib("mockk"))
        }
    }
}

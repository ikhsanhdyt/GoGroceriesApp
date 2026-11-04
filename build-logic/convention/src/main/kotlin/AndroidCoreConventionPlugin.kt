import com.android.build.api.dsl.LibraryExtension
import com.diavolo.gogroceriesapp.buildlogic.configureKotlinAndroid
import com.diavolo.gogroceriesapp.buildlogic.defaultNamespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Base for every Android library module: SDK levels, Java/Kotlin target and namespace. */
class AndroidCoreConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            namespace = defaultNamespace
            configureKotlinAndroid(this)
        }
    }
}

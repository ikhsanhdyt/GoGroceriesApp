import com.diavolo.gogroceriesapp.buildlogic.JVM_VERSION
import com.diavolo.gogroceriesapp.buildlogic.lib
import com.diavolo.gogroceriesapp.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** A pure Kotlin module with no Android dependency, such as `:core:entity`. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(JVM_VERSION)
        }

        dependencies {
            add("testImplementation", libs.lib("junit"))
        }
    }
}

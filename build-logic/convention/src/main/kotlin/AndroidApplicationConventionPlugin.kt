import com.android.build.api.dsl.ApplicationExtension
import com.diavolo.gogroceriesapp.buildlogic.TARGET_SDK
import com.diavolo.gogroceriesapp.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = TARGET_SDK
        }
    }
}

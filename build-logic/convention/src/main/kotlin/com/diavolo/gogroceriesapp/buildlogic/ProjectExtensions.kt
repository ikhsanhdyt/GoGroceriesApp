package com.diavolo.gogroceriesapp.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val COMPILE_SDK = 35
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 35
internal const val JVM_VERSION = 17

private const val BASE_NAMESPACE = "com.diavolo.gogroceriesapp"

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

/** `:feature:list-detail` becomes `com.diavolo.gogroceriesapp.feature.listdetail`. */
internal val Project.defaultNamespace: String
    get() = BASE_NAMESPACE + path.replace(":", ".").replace("-", "")

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    commonExtension.apply {
        compileSdk = COMPILE_SDK

        defaultConfig {
            minSdk = MIN_SDK
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(JVM_VERSION)
    }

    dependencies {
        add("testImplementation", libs.lib("junit"))
    }
}

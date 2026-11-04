plugins {
    alias(libs.plugins.gogroceries.android.core)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

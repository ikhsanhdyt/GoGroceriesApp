plugins {
    alias(libs.plugins.gogroceries.android.core)
    alias(libs.plugins.gogroceries.android.hilt)
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.kotlinx.coroutines.test)
}

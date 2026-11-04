plugins {
    alias(libs.plugins.gogroceries.android.feature)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:entity"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))

    implementation(project(":api:category"))
    implementation(project(":api:grocerylist"))

    implementation(libs.androidx.activity.compose)

    // Price scanner
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.mlkit.vision)
    implementation(libs.mlkit.text.recognition)
}

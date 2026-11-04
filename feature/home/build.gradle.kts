plugins {
    alias(libs.plugins.gogroceries.android.feature)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:entity"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))

    implementation(project(":api:grocerylist"))
}

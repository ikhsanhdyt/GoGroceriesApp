plugins {
    alias(libs.plugins.gogroceries.android.api)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:entity"))
}

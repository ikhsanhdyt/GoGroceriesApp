plugins {
    alias(libs.plugins.gogroceries.android.core)
    alias(libs.plugins.gogroceries.android.hilt)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    api(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.mockk)
}

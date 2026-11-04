plugins {
    alias(libs.plugins.gogroceries.android.application)
    alias(libs.plugins.gogroceries.android.compose)
    alias(libs.plugins.gogroceries.android.hilt)
}

android {
    namespace = "com.diavolo.gogroceriesapp"

    defaultConfig {
        applicationId = "com.diavolo.gogroceriesapp"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))

    implementation(project(":api:category"))
    implementation(project(":api:grocerylist"))

    implementation(project(":feature:home"))
    implementation(project(":feature:categories"))
    implementation(project(":feature:list-detail"))
    implementation(project(":feature:active-shopping"))
    implementation(project(":feature:trip-summary"))
    implementation(project(":feature:analytics"))

    implementation(libs.navigation.compose)
}

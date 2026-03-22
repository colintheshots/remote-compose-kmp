plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project(":sample:shared"))
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation("androidx.activity:activity-compose:1.9.3")
        }
    }
}

android {
    namespace = "androidx.compose.remote.sample.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "androidx.compose.remote.sample.android"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

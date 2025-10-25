plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cpe126L.mmcmspotfinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cpe126L.mmcmspotfinder"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.01 - Alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java 11 is fine for TFLite and your current setup
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        mlModelBinding = true
    }
}

dependencies {
    // Compose BOM to align versions
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Use BOM-managed versions (no explicit version strings)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // System splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Material Components for XML themes (Theme.Material3.*)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // OpenStreetMap (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // TensorFlow Lite runtime (required to run your exported .tflite model)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    // Optional delegates (uncomment if you decide to use them later):
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    // implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
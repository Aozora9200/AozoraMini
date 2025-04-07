plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.aozora.aozorabrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aozora.aozorabrowser"
        minSdk = 19
        targetSdk = 35
        versionCode = 1
        versionName = "1.00-AZB01C"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation ("com.android.support:appcompat-v7:28.0.0")
    implementation ("com.android.support:design:28.0.0")
    implementation ("com.android.support:support-v4:28.0.0")
    implementation ("com.android.support:recyclerview-v7:28.0.0")
    implementation ("com.android.support:cardview-v7:28.0.0")
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.android.support:preference-v7:28.0.0")
}
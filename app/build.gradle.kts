plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt") // << add kapt like this manually
    id("com.google.gms.google-services") // for Firebase
}


android {
    namespace = "com.example.quizappproject"
    compileSdk = 35 // You can keep 35 for now (or set 36 later when available)

    defaultConfig {
        applicationId = "com.example.quizappproject"
        minSdk = 24
        targetSdk = 35 // Keep it matching compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding=true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.material3.android)


    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.core:core-splashscreen:1.0.1")


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.0-alpha01")
    kapt("androidx.room:room-compiler:2.7.0-alpha01")
    implementation("androidx.room:room-ktx:2.7.0-alpha01")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")


    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

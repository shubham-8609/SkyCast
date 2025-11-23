plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.codeleg.skycast"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.codeleg.skycast"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        dataBinding = true
    }
}

dependencies {

    // Retrofit (Networking)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    //  Room (Local caching)
    implementation("androidx.room:room-runtime:2.8.3")
    add("kapt", "androidx.room:room-compiler:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")

    //  Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")

    // 🏗 WorkManager (background sync)
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    //  Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //  Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // 🖼 Glide (for weather icons)
    implementation("com.github.bumptech.glide:glide:5.0.5")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
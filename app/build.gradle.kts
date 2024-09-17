plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "project.alkautsar.simulasikredit"
    compileSdk = 34

    defaultConfig {
        applicationId = "project.alkautsar.simulasikredit"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.hilt.android)
    implementation("javax.inject:javax.inject:1")
    kapt("com.google.dagger:dagger-compiler:2.46")

    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
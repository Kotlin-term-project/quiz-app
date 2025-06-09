import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms)
}


val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

val openAiApiKey = localProperties.getProperty("OPENAI_API_KEY")
    ?: throw GradleException("OPENAI_API_KEY is missing")

println("OPENAI_API_KEY = $openAiApiKey")

android {
    namespace = "com.example.termproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.termproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiApiKey\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.ui.auth)
    implementation(libs.androidx.recyclerview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.material.dialogs.core)
    implementation(libs.material.dialogs.input)
    implementation(libs.material.dialogs.lifecycle)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
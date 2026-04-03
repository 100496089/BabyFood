import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(localFile.inputStream())
    }
}

val googleKey = localProperties.getProperty("GOOGLE_TRANSLATE_API_KEY") ?: ""
val spoonacularKey = localProperties.getProperty("SPOONACULAR_API_KEY") ?: ""
val spoonacularKey2 = localProperties.getProperty("SPOONACULAR_API_KEY_2") ?: ""
val spoonacularKey3 = localProperties.getProperty("SPOONACULAR_API_KEY_3") ?: ""

android {
    namespace = "com.uc3m.it.babyfood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.uc3m.it.babyfood"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_TRANSLATE_API_KEY", "\"$googleKey\"")
        buildConfigField("String", "SPOONACULAR_API_KEY", "\"$spoonacularKey\"")
        buildConfigField("String", "SPOONACULAR_API_KEY_2", "\"$spoonacularKey2\"")
        buildConfigField("String", "SPOONACULAR_API_KEY_3", "\"$spoonacularKey3\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // MPAndroidChart para las gráficas
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.redsocialaio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.redsocialaio"
        minSdk = 29
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core Android UI components
    implementation(libs.appcompat)           // Compatibilidad con versiones anteriores
    implementation(libs.material)            // Material Design components
    implementation(libs.constraintlayout)    // Layout flexible y eficiente
    
    // Architecture Components - MVVM Pattern
    implementation(libs.lifecycle.livedata.ktx)  // LiveData para observar cambios de datos
    implementation(libs.lifecycle.viewmodel.ktx) // ViewModel para manejar UI data
    
    // Navigation Component
    implementation(libs.navigation.fragment) // Navegación entre fragments
    implementation(libs.navigation.ui)       // UI helpers para navegación
    
    // Firebase - Authentication & Backend
    implementation(platform(libs.firebase.bom))        // Firebase Bill of Materials
    implementation(libs.firebase.auth)                 // Autenticación Firebase
    implementation(libs.credentials)                   // Credential Manager API
    implementation(libs.credentials.play.services.auth) // Google Sign-In integration
    implementation(libs.googleid)                      // Google ID token verification
    
    // Networking - Para APIs de redes sociales (Mastodon/BigBone)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")         // HTTP client
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")   // JSON parsing
    
    // Image Loading - Para avatares y media content
    implementation("com.github.bumptech.glide:glide:4.16.0")        // Carga y cache de imágenes
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Glide annotation processor
    
    // Testing
    testImplementation(libs.junit)           // Unit testing
    androidTestImplementation(libs.ext.junit) // Android unit testing
    androidTestImplementation(libs.espresso.core) // UI testing
}
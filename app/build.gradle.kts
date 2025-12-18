plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.wellnesshub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wellnesshub"
        minSdk = 26
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

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    
    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // MPAndroidChart for mood trends
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // RecyclerView for habit lists
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Fragment support
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // ViewPager2 for mood calendar
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Using built-in android.widget.CalendarView, no external calendar dependency needed
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
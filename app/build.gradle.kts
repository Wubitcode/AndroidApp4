plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.wubitcode.androidapp4"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.wubitcode.androidapp4"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Displays podcast search results and episodes in scrolling lists.
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Handles HTTP requests to the iTunes podcast search service
    // and individual podcast RSS feeds.
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Converts JSON responses from the iTunes API into Kotlin objects.
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Schedules reliable background tasks for checking
    // subscribed podcasts for new episodes.
    implementation("androidx.work:work-runtime-ktx:2.11.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
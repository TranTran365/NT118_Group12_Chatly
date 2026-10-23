plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.group12.chatly"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.group12.chatly"
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Multimedia: vị trí, bản đồ, ảnh, gọi Zego, emoji
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.photoview)
    implementation(libs.zego.call)
    implementation(libs.emoji2)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
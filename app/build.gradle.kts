plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.example.basemvvm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.basemvvm"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Core UI
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerView)

    //animation
    implementation(libs.lottie)

    //room local database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    annotationProcessor(libs.room.compiler)

    //local data
    implementation(libs.datastore)

    //Exoplayer - playing sound
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Image
    implementation(libs.glide)
    ksp(libs.compiler)
    implementation(libs.okhttp3.integration)
    implementation(libs.recyclerview.integration)

    //lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.process)

    //retrofit2 - networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp.dnsoverhttps)

    //other
    implementation(libs.installreferrer)
    implementation(libs.eventbus)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.annotations)
    implementation(libs.shimmer)
    implementation(libs.guava)
    implementation(libs.timber)
    implementation(libs.androidx.multidex)
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)
    implementation(libs.app.update.ktx)
    implementation(libs.relinker)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.palette.ktx)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //firebase
//    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
//    implementation("com.google.firebase:firebase-config-ktx")
//    implementation("com.google.firebase:firebase-crashlytics-ktx")
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-storage-ktx")
//    implementation ("com.google.firebase:firebase-messaging-ktx")

    //Ads Mediation
//    implementation 'com.google.android.gms:play-services-ads:24.3.0'
    implementation(libs.user.messaging.platform)
//    implementation 'com.google.firebase:firebase-ads:23.6.0'
//    implementation 'com.google.android.play:review-ktx:2.0.2'
//    implementation 'com.google.ads.mediation:vungle:7.5.0.0'
//    implementation 'com.google.ads.mediation:facebook:6.20.0.0'
//    implementation 'com.google.ads.mediation:mintegral:16.9.71.0'
//    implementation 'com.google.ads.mediation:applovin:13.2.0.1'
//    implementation 'com.unity3d.ads:unity-ads:4.14.2'
//    implementation 'com.google.ads.mediation:unity:4.14.2.0'
//    implementation 'com.google.ads.mediation:ironsource:8.8.0.1'
//    implementation 'com.google.ads.mediation:pangle:7.1.0.8.0'
}
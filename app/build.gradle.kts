plugins {
    id("com.android.application")
}

android {
    namespace = "it.unimib.worldnews"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.unimib.worldnews"
        minSdk = 23
        targetSdk = 34
        versionCode = 4
        versionName = "0.4"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    val nav_version = "2.7.5"

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-base:18.2.0")
    implementation("commons-validator:commons-validator:1.7")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-io:commons-io:2.15.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

// Release signing comes from keystore.properties (git-ignored, see docs/release-checklist.md).
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

/*
 * One Kotlin Multiplatform module: the game itself lives in src/commonMain and runs on both
 * Android (src/androidMain) and iOS (src/iosMain, packaged as the "Shared" framework that the
 * Xcode project in iosApp/ links). See docs/ios.md.
 */
kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.15.0")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            implementation("com.android.billingclient:billing-ktx:8.0.0")
        }
        getByName("androidUnitTest").dependencies {
            implementation("junit:junit:4.13.2")
            // VoiceCatalogTest calls every phrase by reflection.
            implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
        }
    }
}

android {
    namespace = "com.cojocari.alphabet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cojocari.alphabet"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "4.1"
    }

    signingConfigs {
        if (keystoreProps.getProperty("storeFile") != null) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Shrink and optimise the code and drop unused resources: a smaller, faster app.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

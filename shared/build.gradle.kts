// shared/build.gradle.kts - DÜZELTİLMİŞ VERSİYON

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()

    val iosTargets = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            // iOS'ta SQLite bağlantısı ve kaynakların düzgün çalışması için Dynamic olmalı
            isStatic = false
            linkerOpts("-lsqlite3")
        }
    }

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)

                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")


                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // İkonlar (Düzeltilmiş Multiplatform Versiyonu)
                implementation(compose.materialIconsExtended)

                // Peekaboo — galeri seçici ve kamera yakalama
                implementation(libs.peekaboo.image.picker)
                implementation(libs.peekaboo.ui)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                implementation(libs.sqldelight.android.driver)
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
            }
        }
        val androidUnitTest by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosTargets.forEach { target ->
                target.compilations.getByName("main").defaultSourceSet.dependsOn(this)
            }
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}

android {
    namespace = "com.example.gymappkmp"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// SQLDelight Yapılandırması
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.aigymapp.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
            // Şema doğrudan .sq dosyalarından üretilir
            deriveSchemaFromMigrations.set(false)
        }
    }
}
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.example.core.network"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    debug {
      buildConfigField("String", "API_BASE_URL", "\"https://api-dev.autobox.example.com/v1/\"")
      buildConfigField("boolean", "ENABLE_SAFE_HTTP_LOGS", "true")
    }
    release {
      buildConfigField("String", "API_BASE_URL", "\"https://api.autobox.example.com/v1/\"")
      buildConfigField("boolean", "ENABLE_SAFE_HTTP_LOGS", "false")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(project(":core:common"))
  api(libs.retrofit)
  api(libs.converter.moshi)
  api(libs.okhttp)
  implementation(libs.logging.interceptor)
  api(libs.moshi.kotlin)
  ksp(libs.moshi.kotlin.codegen)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}

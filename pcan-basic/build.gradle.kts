plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core"))
    api(libs.jna)
    implementation(libs.kotlinx.coroutines.core)
}

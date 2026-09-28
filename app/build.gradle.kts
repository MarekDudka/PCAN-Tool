import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":pcan-basic"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.pcantool.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Msi)
            packageName = "PCAN-Tool"
            packageVersion = "1.0.0"
            description = "Viewer, filter, recorder and CSV exporter for PEAK-System PCAN-Basic adapters"
            vendor = "PCAN-Tool"

            windows {
                menuGroup = "PCAN-Tool"
                perUserInstall = true
            }
        }
    }
}

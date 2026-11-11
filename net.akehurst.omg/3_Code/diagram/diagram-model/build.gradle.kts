plugins {
    id("project-conventions")
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.nal.agl.processor)
                api(libs.nak.compose.layout.graph)
                api(libs.nak.compose.components)
                implementation(compose.ui)
                implementation(compose.foundation)
            }
        }
    }
}

plugins {
    id("project-conventions")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.nal.agl.processor)
                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)
                implementation("org.slf4j:slf4j-api:2.0.16")
                runtimeOnly("org.slf4j:slf4j-simple:2.0.16") // console output
            }
        }
    }
}

// do not publish
tasks.withType<AbstractPublishToMaven> { onlyIf { false } }
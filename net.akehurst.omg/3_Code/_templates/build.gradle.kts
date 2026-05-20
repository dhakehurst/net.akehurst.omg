plugins {
    id("project-conventions")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.nak.kotlinx.collections)
                implementation(libs.nak.kotlinx.utils)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":_simple-mof-for-xmi"))
                implementation("org.slf4j:slf4j-api:2.0.16")
                runtimeOnly("org.slf4j:slf4j-simple:2.0.16") // console output
            }
        }
    }
}

// do not publish
tasks.withType<AbstractPublishToMaven> { onlyIf { false } }
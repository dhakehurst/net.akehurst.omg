plugins {
    id("project-conventions")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":uml-2_5_1-ram"))
                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)
                implementation(libs.xmlutil.core)
            }
        }
    }
}

// the large model test needs more than the default test heap
tasks.named<Test>("jvmTest") {
    maxHeapSize = "3g"
}

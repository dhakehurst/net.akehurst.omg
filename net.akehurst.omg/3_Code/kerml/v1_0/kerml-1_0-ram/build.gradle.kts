plugins {
    id("project-conventions")
    id("code-generator")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)

                api(project(":kerml-1_0-api"))
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    sourceXmi.set(layout.projectDirectory.file("../specs/KerML_1-0_ptc-25-04-04.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/ram"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.kerml.v1_0"
    )
}
plugins {
    id("project-conventions")
    id("code-generator")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":uml-2_5_1-api"))

                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    sourceXmi.set(layout.projectDirectory.file("../specs/2.5.1/UML_2-5-1_ASM_ptc-18-01-01.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/ram"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.uml.v2_5_1"
    )
}
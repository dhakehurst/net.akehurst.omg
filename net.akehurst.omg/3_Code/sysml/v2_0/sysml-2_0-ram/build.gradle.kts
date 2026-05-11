plugins {
    id("project-conventions")
    id("code-generator")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":sysml-2_0-api"))

                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    sourceXmi.set(layout.projectDirectory.file("../specs/SysML_2-0_ptc-25-02-15.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/ram"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.sysml.v2_0"
    )
}
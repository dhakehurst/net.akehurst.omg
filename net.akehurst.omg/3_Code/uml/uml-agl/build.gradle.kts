plugins {
    id("project-conventions")
    id("code-generator")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.nal.agl.processor)
                api(libs.nak.kotlinx.collections)
                api(libs.nak.kotlinx.utils)

                api(project(":uml-ram"))
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    sourceXmi.set(layout.projectDirectory.file("../specs/2.5.1/UML_2-5-1_ASM_ptc-18-01-01.xmi"))
}
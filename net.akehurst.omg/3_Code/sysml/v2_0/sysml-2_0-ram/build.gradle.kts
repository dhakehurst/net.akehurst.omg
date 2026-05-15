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
    modelName = "SysML"
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/SysML_2-0_ptc-25-02-15.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/ram"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.sysml.v2_0"
    )
    referencedTypeMapping = mapOf(
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long"
    )
}
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

                api(project(":uml-2_5_1-ram"))
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    modelName = "DiagramDefinition"
    sourceXmiPaths = listOf(
        layout.projectDirectory.file("../specs/DD_20131001_DiagramCommon.xmi"),
        layout.projectDirectory.file("../specs/DD_20131001DiagramGraph.xmi")
    )
    generateDir.set(layout.projectDirectory.dir("../../../_templates/agl-types"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.dd.v1_1"
    )
    referencedTypeMapping = mapOf(
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String" to "String",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Integer" to "Long",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real" to "Double",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#UnlimitedNatural" to "Long",
    )
}
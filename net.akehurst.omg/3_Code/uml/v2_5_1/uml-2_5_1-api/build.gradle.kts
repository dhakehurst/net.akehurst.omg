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
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    modelName = "UML"
    instanceRoots = listOf("Model")
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/UML_2-5-1_ASM_ptc-18-01-01.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.uml.v2_5_1",
        "COPYRIGHT" to file(layout.projectDirectory.dir("../../../_templates/text/COPYRIGHT.txt")).readText()
    )
    referencedTypeMapping = mapOf(
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String" to "String",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Integer" to "Long",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real" to "Double",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#UnlimitedNatural" to "Long",
    )
}
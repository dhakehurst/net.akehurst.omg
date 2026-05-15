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
    modelName = "SysModApi"
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/SysModApi-1_0-ptc-25-02-29.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.sysmodapi.v2_0"
    )
    referencedTypeMapping = mapOf(
        "http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
        "http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
        "http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
        "http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long",
        "KerML Abstract Syntax.xml#_18_5_3_12e503d9_1533160651703_306405_42199" to "net.akehurst.omg.kerml.v1_0.api.Element",
        "#Unspecified" to "Any"
    )
}
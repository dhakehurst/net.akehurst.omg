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
    modelName = "KerML"
    instanceRoots = listOf("Package", "LibraryPackage")
//    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/KerML_1-0_ptc-25-04-04.xmi"))
// original xmi does not specify aggregations!
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/kerml_patched.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.kerml.v1_0"
    )
    referencedTypeMapping = mapOf(
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long"
    )
}
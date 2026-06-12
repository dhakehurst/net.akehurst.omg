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
    modelName = "SysML"
    instanceRoots = listOf("Package", "LibraryPackage")
//    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/SysML_2-0_ptc-25-02-15.xmi"))
    // original xmi does not specify aggregations!
    sourceXmiPaths = listOf(
        layout.projectDirectory.file("../../../uml/v2_5_1/specs/UML_2-5-1_PT_ptc-18-01-02.xmi"), // for primitive types
        layout.projectDirectory.file("../../../kerml/v1_0/specs/kerml_patched.xmi"),
        layout.projectDirectory.file("../specs/sysml_patched.xmi"),
    )
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.sysml.v2_0",
        "COPYRIGHT" to file(layout.projectDirectory.dir("../../../_templates/text/COPYRIGHT.txt")).readText(),
        "EXCLUDE_PACKAGE" to listOf("PrimitiveTypes")
    )
    referencedTypeMapping = mapOf(
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long"
    )
}
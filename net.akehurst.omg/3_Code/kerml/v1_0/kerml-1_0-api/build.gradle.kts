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
    sourceXmiPaths = listOf(
        layout.projectDirectory.file("../specs/kerml_patched.xmi")
    )
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.kerml.v1_0",
        "COPYRIGHT" to file(layout.projectDirectory.dir("../../../_templates/text/COPYRIGHT.txt")).readText()
    )
    referencedTypeMapping = mapOf(
        "String" to "String",
        "Integer" to "Long",
        "Boolean" to "Boolean",
        "Real" to "Double",
        "UnlimitedNatural" to "Long"
    )
}
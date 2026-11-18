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

                api(project(":kerml-1_0-ram"))
            }
        }
    }
}

tasks.named<generator.GenerateTask>("generate") {
    modelName = "KerML"
    instanceRoots = listOf("Package", "LibraryPackage")
    // the same source as kerml-1_0-ram, so the types match the generated classes
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/kerml_patched.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/agl-types"))
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.kerml.v1_0",
        "COPYRIGHT" to file(layout.projectDirectory.dir("../../../_templates/text/COPYRIGHT.txt")).readText(),
        "EXCLUDE_PACKAGE" to emptyList<String>()
    )
    referencedTypeMapping = mapOf(
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
        "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long"
    )
}

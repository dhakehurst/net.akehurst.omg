package net.akehurst.omg.mof.gen

import net.akehurst.omg.mof.gen.template.SimpleMofToRam
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar mof-gen.jar <path_to_xmi_file> [output_directory]")
        return
    }
    val xmiFilePath = args[0]
    val outputDirPath = if (args.size > 1) args[1] else "generated_code"

    val xmiFile = File(xmiFilePath)
    val outputDir = File(outputDirPath)

    if (!xmiFile.exists()) {
        println("Error: XMI File not found at $xmiFilePath")
        return
    }

    try {
        println("Parsing XMI file: ${xmiFile.absolutePath}...")
        val parser = MofXmiParser()
       // val mdl = parser.parse(File("../../uml/specs/2.5.1/UML_2013-10-01_PrimitiveTypes.xmi"))
       // mdl.packages["PrimitiveTypes"] = mdl.packages["_0"]!!
        val mofModel = parser.parse(xmiFile)
        println("XMI parsing complete. Found ${mofModel.packages.size} root packages, ${mofModel.classes.size} classes total.")

        // Optional: Print model structure for debugging
       // printModelStructure(mofModel)


        println("\nGenerating Kotlin code to directory: ${outputDir.absolutePath}...")
        //val generator = KotlinCodeGenerator(mofModel)
        //generator.generateToFiles(outputDir)

        //Api
//        val generator = GeneratorFromTemplate(SimpleMofToApi.format, SimpleMof.types)
//        generator.generateToFiles(mofModel, outputDir)

        //Ram
        val generator = GeneratorFromTemplate(SimpleMofToRam.format, SimpleMof.types)
        generator.generateToFiles(mofModel, outputDir)

        println("\nKotlin code generation complete.")

    } catch (e: Exception) {
        println("\nAn error occurred:")
        e.printStackTrace()
    }
}

// Helper to print model structure (for debugging)
fun printModelStructure(model: MofModel) {
    println("\n--- Parsed Model Structure ---")
    model.packages.values.filter { it.parentPackage == null }.forEach { pkg ->
        printPackageRecursive(pkg, 0, model)
    }
}

fun printPackageRecursive(pkg: MofPackage, indentLevel: Int, model: MofModel) {
    val indent = "  ".repeat(indentLevel)
    println("${indent}Package: ${pkg.name} (ID: ${pkg.xmiId}, Path: ${model.getFullPackagePath(pkg)})")
    pkg.classes.forEach { cls ->
        println("${indent}  Class: ${cls.name} (Abstract: ${cls.isAbstract})")
        cls.ownedAttribute.forEach { attr ->
            println("${indent}    Attr: ${attr.name}: ${attr.typeHref ?: attr.typeXmiId} [${attr.lowerBound}..${if(attr.upperBound == -1) "*" else attr.upperBound.toString()}] ${if(attr.associationXmiId != null) "(Assoc: ${attr.associationXmiId})" else ""}")
        }
        cls.operations.forEach { op ->
            println("${indent}    Op: ${op.name}(${op.parameters.joinToString { it.name }}) : ${op.returnParameter?.typeHref ?: op.returnParameter?.typeXmiId ?: "Unit"}")
        }
    }
    pkg.associations.forEach { assoc ->
        println("${indent}  Association: ${assoc.name ?: "Unnamed"} (ID: ${assoc.xmiId})")
        assoc.memberEnds.forEach { end ->
            println("${indent}    End: ${end.name}: ${model.primitiveTypes[end.typeHref] ?: end.typeXmiId ?: "UnknownType"} (Assoc ID on Prop: ${end.associationXmiId})")
        }
    }
    pkg.subPackages.forEach { subPkg ->
        printPackageRecursive(subPkg, indentLevel + 1, model)
    }
}
package net.akehurst.omg.mof.gen.agl.types

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
        val parser = XMI2MOF()
        parser.parse(xmiFile)
//        println("XMI parsing complete. Found ${parser.metaElementTypes.size} metatypes.")

//        val tm = typeModel("MOF", true) {
//            namespace("omg.mof") {
//                for (mt in parser.metaElementTypes.values) {
//                    data(mt.name) {
//                        for (p in mt.property.values) {
//                            val chrs = emptySet<PropertyCharacteristic>()
//                            propertyOf(chrs, p.name, p.type.name)
//                        }
//                    }
//                }
//            }
//        }


//        println(tm.asString())

        println("\nKotlin code generation complete.")

    } catch (e: Exception) {
        println("\nAn error occurred:")
        e.printStackTrace()
    }


}
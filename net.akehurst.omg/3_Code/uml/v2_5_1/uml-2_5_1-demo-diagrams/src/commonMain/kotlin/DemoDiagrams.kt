package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.language.asm.builder.asmSimple
import net.akehurst.omg.uml.v2_5_1.agl.types.UMLTypesDomain
import net.akehurst.omg.uml.v2_5_1.ram.UMLFactoryRam

object DemoDiagrams {
    val umlTypesDomain = UMLTypesDomain(UMLFactoryRam).types

    val cd = asmSimple(umlTypesDomain) {
        element("Model") {
            propertyString("name", "Demo Model")
            propertyListOfElement("packagedElementSet") {
                element("Package") {
                    propertyString("name", "root")
                    propertyListOfElement("packagedElementSet") {
                        element("Class") {
                            propertyString("name", "Class_1")
                        }
                        element("Class") {
                            propertyString("name", "Class_2")
                        }
                    }
                }
            }
        }
    }

    val classDiagram = diagram("Demo Class Diagram", "ClassDiagram") {

    }

}
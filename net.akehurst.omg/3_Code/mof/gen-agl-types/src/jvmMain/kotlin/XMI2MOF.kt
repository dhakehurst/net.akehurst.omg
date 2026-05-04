package net.akehurst.omg.mof.gen.agl.types

import net.akehurst.omg.mof.ram.emof.EmofRamFactory
import  net.akehurst.omg.mof.api.emof.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XMI2MOF {

    fun parse(file: File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)
        doc.documentElement.normalize()

        val rootXmiElement = doc.documentElement
        if (rootXmiElement.tagName != "xmi:XMI") {
            throw IllegalArgumentException("Root element is not xmi:XMI")
        }
        extractMetaTypes(rootXmiElement)
    }

    fun extractMetaTypes(xmiElement: org.w3c.dom.Element): Pair<String, Any> {
        return when {
            xmiElement.hasAttributeNS(null,"xmi:type") -> {
                val elTypeName = xmiElement.getAttribute("xmi:type").substringAfter(":")

                val props = mutableMapOf<String, Any>()

                val nodes = xmiElement.childNodes
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i)
                    if (node is org.w3c.dom.Element) {
                       val (name,obj) =  extractMetaTypes(node)
                        props[name] = obj
                    }
                }

                val elName = xmiElement.tagName
                val emofEl: Element = EmofRamFactory.createByName(elTypeName, props)
                Pair(elName, emofEl)
            }
            else -> {
                val nodes = xmiElement.childNodes
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i)
                    if (node is org.w3c.dom.Element) {
                        extractMetaTypes(node)
                    }
                }

                return Pair("", "")
            }
        }
    }

}
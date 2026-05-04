package net.akehurst.omg.mof.gen

import org.w3c.dom.Attr
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.set

data class MetaType(
    val name: String
) {
    val property = mutableMapOf<String,MetaProperty>()

    fun addProperty(name:String, type:MetaType) {
        val existing = property[name]
        if (existing == null) {
            property[name] = MetaProperty(name, type)
        }
    }
}

data class MetaProperty(
    val name: String,
    val type:MetaType
) {

}

class MetaClassExtractor {

    val metaElementTypes = mutableMapOf<String,MetaType>()

    val STRING: MetaType = addType("String")

    fun addType(name:String):MetaType {
        val existing = metaElementTypes[name]
        return if (existing == null) {
            metaElementTypes[name] = MetaType(name)
            metaElementTypes[name]!!
        } else {
            existing
        }
    }

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

    fun extractMetaTypes(xmiElement: Element, parentType: MetaType? = null) {
        val elName = xmiElement.tagName
        val elTypeName = xmiElement.getAttribute("xmi:type").substringAfter(":")
        //if(elTypeName.isBlank().not()) {
            val elType = addType(elTypeName)
            parentType?.let { it.addProperty(elName, elType) }
        //}
            val atts = xmiElement.attributes
            for (i in 0 until atts.length) {
                val att = atts.item(i)
                when (att) {
                    is Attr -> when {
                        att.name.startsWith("xmlns:") -> Unit
                        att.name.startsWith("xmi:") -> Unit
                        else -> elType?.addProperty(att.name, STRING)
                    }

                    else -> Unit
                }
            }

            val nodes = xmiElement.childNodes
            for (i in 0 until nodes.length) {
                val node = nodes.item(i)
                if (node is Element) {
                    extractMetaTypes(node, elType)
                }
            }
    }

}
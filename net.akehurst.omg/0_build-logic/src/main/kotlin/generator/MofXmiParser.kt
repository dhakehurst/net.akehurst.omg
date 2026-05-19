/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generator

import net.akehurst.omg._simple_mof_for_xmi.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


class MofXmiParser(
    modelName: String,
    instanceRoots: List<String>,
    referencedTypeMapping: Map<String, String>
) {

    companion object {
        fun Element.getXmiId() = this.getAttribute("xmi:id")
        fun Element.getAttributeOrNull(name: String) = this.getAttributeNode(name)?.value
    }

    val refHandler = XmiReferenceHandler()
    val model: MofModel = MofModel(modelName, instanceRoots, refHandler)

    init {
        referencedTypeMapping.forEach { (k, v) ->
            ExternalReferenceClass(model, k, v).also {
                refHandler.setRef("EXTERNAL", k, it)
            }
        }

    }

    lateinit var currentFileName: String
    fun hasRef(ref: String) = refHandler.hasRef(currentFileName, ref)
    fun getRef(ref: String) = refHandler.getRef(currentFileName, ref)
    fun setRef(ref: String, value: Any) {
        refHandler.setRef(currentFileName, ref, value)
    }

    fun parse(file: File): MofModel {
        currentFileName = file.name
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)
        doc.documentElement.normalize()

        val rootXmiElement = doc.documentElement
        if (rootXmiElement.tagName != "xmi:XMI") {
            throw IllegalArgumentException("Root element is not xmi:XMI")
        }

        // First pass: Discover all top-level packagedElements (Packages, Classes, Associations)
        // and register them with their IDs.
        val rootPackagedElementNodes = rootXmiElement.childNodes
        for (i in 0 until rootPackagedElementNodes.length) {
            val node = rootPackagedElementNodes.item(i)
            if (node is Element && (node.tagName == "packagedElement" || node.tagName == "uml:Package" || node.tagName == "uml:Model")) {
                // This should be the root "MOF" package
                val rootMofPackage = preParsePackage(node)
                if (rootMofPackage != null) {
                    model.addPackage(currentFileName, null, rootMofPackage)
                    // Recursively pre-parse to discover all elements and their IDs
                    discoverElements(node, rootMofPackage)
                }
            }
        }

        // Second pass: Populate details (attributes, operations, generalizations, linking, etc.)
        model.packageList.forEach { pkg ->
            val packageElement = findDomElementById(doc, pkg.xmiId) // Helper to find the original DOM element
            packageElement?.let { populatePackageDetails(it, pkg) }
        }
        model.classList.forEach { cls ->
            val classElement = findDomElementById(doc, cls.xmiId)
            classElement?.let { populateClassDetails(it, cls) }
        }
        model.associationList.forEach { assoc ->
            val assocElement = findDomElementById(doc, assoc.xmiId)
            assocElement?.let { populateAssociationDetails(it, assoc) }
        }

        return model
    }

    private fun findDomElementById(doc: Document, id: String): Element? {
        // This is inefficient for large documents. A map built during a first pass would be better.
        // For now, we iterate.
        val nodeList = doc.getElementsByTagName("*")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node is Element && node.getAttribute("xmi:id") == id) {
                return node
            }
        }
        return null
    }

    private fun discoverElements(parentElement: Element, currentMofPackage: MofPackage) {
        val childNodes = parentElement.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node is Element) {
                val xmiId = node.getAttribute("xmi:id")
                if (xmiId.isEmpty()) continue // Skip elements without an ID if they are not meant to be referenced
                when (node.tagName) {
                    "packagedElement" -> packagedElement(node, currentMofPackage)
                    "packageImport" -> packageImport(node, currentMofPackage)
                }
            }
        }
    }

    private fun packagedElement(node: Element, currentMofPackage: MofPackage) {
        val xmiType = node.getAttribute("xmi:type")
        val xmiId = node.getAttribute("xmi:id")
        val name = node.getAttribute("name")
        when (xmiType) {
            "uml:Package" -> {
                if (!hasRef(xmiId)) {
                    val subPackage = MofPackage(model, name, xmiId, parentPackage = currentMofPackage)
                    model.addPackage(currentFileName, currentMofPackage, subPackage)
                    discoverElements(node, subPackage) // Recurse
                }
            }

            "uml:Class", "uml:DataType" -> {
                if (!hasRef(xmiId)) {
                    val mofClass = MofClass(model, name, xmiId, parentPackage = currentMofPackage)
                    mofClass.isAbstract = node.getAttribute("isAbstract") == "true"
                    mofClass.comment = getChildrenByTagName(node, "ownedComment").joinToString("\n") {
                        val annotated = getChildrenByTagName(it, "annotatedElement").firstOrNull()?.let {
                            it.getAttribute("xmi:idref")
                        }
                        if (xmiId == annotated) {
                            it.getAttribute("body")
                        } else {
                            ""
                        }
                    }
                    currentMofPackage.addClass(currentFileName, mofClass)
                    discoverElements(node, currentMofPackage) // Classes can contain nested elements in some UML profiles, but usually not for MOF structure
                }
            }

            "uml:Interface" -> {
                if (!hasRef(xmiId)) {
                    val mof = MofInterface(model, name, xmiId).apply { parentPackage = currentMofPackage }
                    mof.comment = getChildrenByTagName(node, "ownedComment").joinToString("\n") {
                        val annotated = getChildrenByTagName(it, "annotatedElement").firstOrNull()?.let {
                            it.getAttribute("xmi:idref")
                        }
                        if (xmiId == annotated) {
                            it.getAttribute("body")
                        } else {
                            ""
                        }
                    }
                    currentMofPackage.addInterface(currentFileName, mof)
                    discoverElements(node, currentMofPackage) // Classes can contain nested elements in some UML profiles, but usually not for MOF structure
                }
            }

            "uml:Association" -> {
                if (!hasRef(xmiId)) {
                    val mofAssociation = MofAssociation(model, name.ifEmpty { null }, xmiId, emptyList())
                    mofAssociation.parentPackage = currentMofPackage
                    model.associationList.add(mofAssociation)
                    setRef(xmiId, mofAssociation)
                    currentMofPackage.associations.add(mofAssociation)
                    // Associations have memberEnds and ownedEnds, discover them if needed for ID mapping
                }
            }

            "uml:PrimitiveType" -> {
                if (!hasRef(xmiId)) {
                    val mofClass = MofClass(model, name, xmiId, parentPackage = currentMofPackage)
                    mofClass.isAbstract = false
                    model.classList.add(mofClass)
                    setRef(xmiId, mofClass)
                    currentMofPackage.classes.add(mofClass)
                }
            }

            "uml:Enumeration" -> {
                if (!hasRef(xmiId)) {
                    val mofEnum = MofEnum(model, name, xmiId, parentPackage = currentMofPackage)
                    model.enumList.add(mofEnum)
                    setRef(xmiId, mofEnum)
                    currentMofPackage.enums.add(mofEnum)
                }
            }

            else -> println("Unknown element type '$xmiType' of packagedElement")
        }
    }

    private fun packageImport(node: Element, currentMofPackage: MofPackage) {
        val xmiType = node.getAttribute("xmi:type")
        val xmiId = node.getAttribute("xmi:id")
        when (xmiType) {
            "uml:PackageImport" -> {
                val importedPackageName = node.getAttribute("importedPackage")
                currentMofPackage.packageImport.add(importedPackageName)
            }

            else -> println("Unknown element type '$xmiType' of packageImport")
        }

    }

    private fun preParsePackage(packageElement: Element): MofPackage? {
        val xmiId = packageElement.getAttribute("xmi:id")
        val name = packageElement.getAttribute("name")
        if (xmiId.isNotEmpty() && name.isNotEmpty()) {
            return MofPackage(model, name, xmiId)
        }
        return null
    }

    private fun populatePackageDetails(packageElement: Element, mofPackage: MofPackage) {
        // Details like imports or merges would be processed here.
        // Child packages, classes, associations are already discovered and linked via parentPackage.
    }

    private fun populateClassDetails(classElement: Element, mofClass: MofClass) {
        val childNodes = classElement.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node !is Element) continue
            when (node.tagName) {
                "generalization" -> {
                    // try attribute
                    val general = node.getAttribute("general")
                    if (general.isNotBlank()) {
                        mofClass.generalizations.add(general)
                    } else {
                        //try child element
                        val gen2 = node.getElementsByTagName("general").item(0) as? Element
                        if (null != gen2) {
                            // try direct ref
                            val v = gen2.getAttribute("xmi:idref")
                            if (v.isNullOrBlank().not()) {
                                mofClass.generalizations.add(v)
                            } else {
                                //try href
                                val href = gen2.getAttribute("href")
                                if (href.isNotBlank()) {
                                    mofClass.generalizations.add(href.substringAfter("#"))
                                }
                            }
                        }
                    }
                }

                "ownedAttribute" -> {
                    val attr = parseProperty(node, mofClass)
                    mofClass.addAttribute(currentFileName, attr)
                }

                "ownedOperation" -> {
                    val op = parseOperation(node, mofClass)
                    mofClass.operations.add(op)
                }
            }
        }
    }

    private fun populateAssociationDetails(associationElement: Element, mofAssociation: MofAssociation) {
        val memberEnds = mutableListOf<MofProperty>()
        // Process 'ownedEnd' which are full property definitions
        getChildrenByTagName(associationElement, "ownedEnd").forEach { ownedEndElement ->
            // The parent class for an ownedEnd of an association is conceptually the type of the *other* end.
            // This is complex to resolve directly here. For now, parentClass on MofProperty for association ends might be null
            // or determined in a later linking step.
            val attr = parseProperty(ownedEndElement, null, mofAssociation.xmiId)
            setRef(attr.xmiId, attr) // Ensure property is also in the global map
            memberEnds.add(attr)
        }
        // Process 'memberEnd' which are references to properties defined elsewhere (typically on classes)
        associationElement.getAttributeOrNull("memberEnd")?.let {
            it.split(" ").forEach { idRef ->
                val referencedProperty = model.getElementById(idRef) as? MofProperty
                if (referencedProperty != null && !memberEnds.any { it.xmiId == referencedProperty.xmiId }) {
                    // Ensure it's correctly marked with its association
                    referencedProperty.associationXmiId = referencedProperty.associationXmiId ?: mofAssociation.xmiId
                    memberEnds.add(referencedProperty)
                    // Update the original property in the class's attribute list if necessary
                    // val ownerClass = referencedProperty.parentClass
                    // ownerClass?.attributes?.replaceAll { if (it.xmiId == updatedRefProperty.xmiId) updatedRefProperty else it }
                }
            }
        }
        getChildrenByTagName(associationElement, "memberEnd").forEach { memberEndRefElement ->
            val idRef = memberEndRefElement.getAttribute("xmi:idref")
            // Find the MofProperty by idRef. It should already be parsed as an ownedAttribute of a class.
            val referencedProperty = model.getElementById(idRef) as? MofProperty
            if (referencedProperty != null && !memberEnds.any { it.xmiId == referencedProperty.xmiId }) {
                // Ensure it's correctly marked with its association
                referencedProperty.associationXmiId = referencedProperty.associationXmiId ?: mofAssociation.xmiId
                memberEnds.add(referencedProperty)
                // Update the original property in the class's attribute list if necessary
                // val ownerClass = referencedProperty.parentClass
                // ownerClass?.attributes?.replaceAll { if (it.xmiId == updatedRefProperty.xmiId) updatedRefProperty else it }
            }
        }
        mofAssociation.memberEnds = memberEnds.distinctBy { it.xmiId }

        // set the parentClass of memberEnds if not set
        // ends 'owned' by the association are not navigable - so do not imply a property on the otherEnd class
        memberEnds.forEach { end ->
            val otherEnd = memberEnds.firstOrNull { it.xmiId != end.xmiId }
            end.opposite = otherEnd
            when {
                null == otherEnd -> {
                    error("Error, there must be an 'other' end for an association. end=$end, memberEnds = $memberEnds")
                }
                null == end.parentClass -> otherEnd.typeXmiId?.let { end.parentClass = getRef(it) as? MofClass }
                    ?: otherEnd.typeHref?.let { ExternalReferenceClass(model, it, it.substringAfter("#")) }

                else -> {
                    val otherClass = getRef(otherEnd.typeXmiId!!) as MofClass
                    check(end.parentClass == otherClass) { "parentClass is wrong" }
                }
            }
        }
    }

    private fun parseProperty(propertyElement: Element, ownerClass: MofClass?, assocXmiId: String? = null): MofProperty {
        val name = propertyElement.getAttribute("name")
        val xmiId = propertyElement.getAttribute("xmi:id")
        val comment = getChildrenByTagName(propertyElement, "ownedComment").joinToString("\n") {
            val annotated = getChildrenByTagName(it, "annotatedElement").firstOrNull()?.let {
                it.getAttribute("xmi:idref")
            }
            if (xmiId == annotated) {
                it.getAttribute("body")
            } else {
                ""
            }
        }

        var typeXmiId: String? = propertyElement.getAttribute("type")
        var typeHref: String? = null
        val typeNode = getChildrenByTagName(propertyElement, "type").firstOrNull()
        if (typeNode != null) {
            typeXmiId = typeNode.getAttribute("xmi:idref").ifEmpty { null }
            typeHref = typeNode.getAttribute("href").ifEmpty { null }
        }

        var lower = 0
        var upper = 1
        getChildrenByTagName(propertyElement, "lowerValue").firstOrNull()?.let { lv ->
            val valueStr = lv.getAttribute("value")
            lower = valueStr.toIntOrNull() ?: (if (valueStr == "*") -1 else 0) // Should not be * for lower
        }
        getChildrenByTagName(propertyElement, "upperValue").firstOrNull()?.let { uv ->
            val valueStr = uv.getAttribute("value")
            upper = valueStr.toIntOrNull() ?: (if (valueStr == "*") -1 else 1)
        }
        if (upper == -1 && getChildrenByTagName(propertyElement, "lowerValue").isEmpty()) {
            lower = 0 // Default lower to 0 for unbounded collections if not specified
        }

        val aggregationKind = when (propertyElement.getAttribute("aggregation")) {
            "composite" -> MofAggregationKind.composite
            "shared" -> MofAggregationKind.reference
            else -> MofAggregationKind.NONE
        }

        var isUnique = true //default
        propertyElement.getAttribute("isUnique").let {
            if (it.isNotEmpty()) isUnique = it.toBoolean()
        }
        var isOrdered = false //default
        propertyElement.getAttribute("isOrdered").let {
            if (it.isNotEmpty()) isOrdered = it.toBoolean()
        }
        val redefinedRef = getChildrenByTagName(propertyElement, "redefinedProperty").firstOrNull()?.let {
            it.getAttribute("xmi:idref")
        }
        val subsettedRef = getChildrenByTagName(propertyElement, "subsettedProperty").firstOrNull()?.let {
            it.getAttribute("xmi:idref")
        }
        val prop = MofProperty(
            model,
            name = name,
            xmiId = xmiId
        ).also { self ->
            self.typeXmiId = typeXmiId
            self.typeHref = typeHref
            self.comment = comment
            self.lowerBound = lower
            self.upperBound = upper
            self.isDerived = propertyElement.getAttribute("isDerived") == "true"
            self.isReadOnly = propertyElement.getAttribute("isReadOnly") == "true" || propertyElement.getAttribute("isLeaf") == "true" // isLeaf implies read-only in some contexts
            self.isUnique = isUnique
            self.isOrdered = isOrdered
            self.aggregation = aggregationKind
            self.associationXmiId = assocXmiId ?: propertyElement.getAttribute("association").ifEmpty { null }
            redefinedRef?.let { (self.redefinedPropertyRef as MutableSet).add(it) }
            subsettedRef?.let { (self.subsettedPropertyRef as MutableSet).add(it) }
        }
        return prop
    }

    private fun parseOperation(operationElement: Element, ownerClass: MofClass): MofOperation {
        val name = operationElement.getAttribute("name")
        val xmiId = operationElement.getAttribute("xmi:id")
        val params = mutableListOf<MofParameter>()
        var returnParam: MofParameter? = null

        getChildrenByTagName(operationElement, "ownedParameter").forEach { paramElement ->
            val paramName = paramElement.getAttribute("name")
            val paramXmiId = paramElement.getAttribute("xmi:id")
            val direction = paramElement.getAttribute("direction").ifEmpty { "in" }

            var paramTypeXmiId: String? = null
            var paramTypeHref: String? = null
            getChildrenByTagName(paramElement, "type").firstOrNull()?.let { typeElem ->
                paramTypeXmiId = typeElem.getAttribute("xmi:idref").ifEmpty { null }
                paramTypeHref = typeElem.getAttribute("href").ifEmpty { null }
            }

            val mofParam = MofParameter(model, paramName, paramXmiId, paramTypeXmiId, paramTypeHref, direction)
            setRef(paramXmiId, mofParam) // Register param

            if (direction == "return") {
                returnParam = mofParam
            } else {
                params.add(mofParam)
            }
        }

        val op = MofOperation(
            model,
            name = name,
            xmiId = xmiId,
            visibility = operationElement.getAttribute("visibility").ifEmpty { "public" },
            isQuery = operationElement.getAttribute("isQuery") == "true",
            parameters = params,
            returnParameter = returnParam
        )
        op.parentClass = ownerClass
        setRef(xmiId, op) // Register op
        return op
    }

    private fun getChildrenByTagName(element: Element, tagName: String): List<Element> {
        val list = mutableListOf<Element>()
        val childNodes = element.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node is Element && node.tagName == tagName) {
                list.add(node)
            }
        }
        return list
    }
}
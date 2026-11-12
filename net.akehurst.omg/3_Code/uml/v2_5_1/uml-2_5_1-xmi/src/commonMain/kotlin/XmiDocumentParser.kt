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

package net.akehurst.omg.uml.v2_5_1.xmi

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.core.KtXmlReader

/**
 * An XML element kept as it was read: stereotype applications before they are typed, and (if asked for)
 * xmi:Extension content. Attributes are keyed by local name, except those in a namespace, which are keyed
 * by '{namespace}localName'. An element whose xmi:type is a UML metaclass is read as a UML object, [umlObject].
 */
class XmiRawElement(
    val namespaceUri: String,
    val prefix: String,
    val localName: String,
    val xmiId: String?,
    val xmiUuid: String?,
    val xmiType: String?,
    val href: String?,
    val idref: String?,
    val attributes: Map<String, String>,
    val children: List<XmiRawElement>,
    val text: String?,
    val umlObject: Any?,
    val line: Int?,
) {
    val qualifiedName: String get() = if (prefix.isEmpty()) localName else "$prefix:$localName"
    override fun toString(): String = "<$qualifiedName${xmiId?.let { " xmi:id='$it'" } ?: ""}>"
}

/** A reference read from XMI, set once all documents are read. */
internal class XmiPendingReference(
    val document: XmiDocument,
    val owner: Any,
    val feature: XmiMetaFeature,
    val text: String,
    val isHref: Boolean,
    val line: Int?,
)

internal class XmiDocumentParser(
    private val document: XmiDocument,
    private val content: String,
    private val metamodel: XmiMetamodel,
    private val options: XmiReadOptions,
    private val issues: XmiIssueCollector,
    private val pending: MutableList<XmiPendingReference>,
) {
    companion object {
        const val XMLNS_NS = "http://www.w3.org/2000/xmlns/"
    }

    private val ns get() = options.namespaces
    private val reader = KtXmlReader(content, expandEntities = true)
    private var generatedIdCount = 0

    private val extensionCount = mutableMapOf<String, Int>()
    private val unknownNamespaceCount = mutableMapOf<String, Int>()

    // profiles enclosing the element being read, so stereotypes can be recorded against them
    private val profileStack = mutableListOf<XmiProfileInfo>()

    private val line: Int? get() = try { reader.getLineNumber() } catch (_: Throwable) { null }

    fun parse() {
        try {
            if (!moveToNextStartElement()) {
                issues.error(XmiIssueKind.XML_SYNTAX, "No root element", document.uri)
                return
            }
            if (ns.isXmi(reader.namespaceURI) && reader.localName == "XMI") {
                while (moveToNextChildStart()) topLevel()
            } else {
                topLevel()
            }
        } catch (e: XmiReadException) {
            throw e
        } catch (e: Exception) {
            issues.error(XmiIssueKind.XML_SYNTAX, "XML error: ${e.message}", document.uri, line)
        }
        for ((extender, n) in extensionCount) {
            issues.warning(XmiIssueKind.EXTENSION, "$n xmi:Extension element(s) from extender '$extender' ${if (options.keepExtensions) "kept raw" else "skipped"}", document.uri)
        }
        for ((nsUri, n) in unknownNamespaceCount) {
            issues.warning(XmiIssueKind.UNKNOWN_NAMESPACE, "$n element(s)/attribute(s) in namespace '$nsUri' skipped", document.uri)
        }
    }

    // --- XML navigation ---

    /** moves to the next start element (anywhere), returns false at the end of the document */
    private fun moveToNextStartElement(): Boolean {
        while (reader.hasNext()) {
            if (reader.next() == EventType.START_ELEMENT) return true
        }
        return false
    }

    /**
     * Moves to the next child start element of the current element; returns false (positioned on the
     * current element's end) when there are no more children. Text is ignored.
     */
    private fun moveToNextChildStart(): Boolean {
        while (reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> return true
                EventType.END_ELEMENT -> return false
                EventType.END_DOCUMENT -> return false
                else -> {}
            }
        }
        return false
    }

    /** reads the children of the current element, calling [onChild] positioned on each child start, and returns the text */
    private inline fun readContent(onChild: () -> Unit): String? {
        var sb: StringBuilder? = null
        while (reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> onChild()
                EventType.END_ELEMENT, EventType.END_DOCUMENT -> break
                EventType.TEXT, EventType.CDSECT, EventType.ENTITY_REF, EventType.IGNORABLE_WHITESPACE -> {
                    (sb ?: StringBuilder().also { sb = it }).append(reader.text)
                }

                else -> {}
            }
        }
        return sb?.toString()
    }

    /** skips the current element and its content, ending on its end tag */
    private fun skipElement() {
        var depth = 1
        while (reader.hasNext() && depth > 0) {
            when (reader.next()) {
                EventType.START_ELEMENT -> depth++
                EventType.END_ELEMENT -> depth--
                EventType.END_DOCUMENT -> return
                else -> {}
            }
        }
    }

    private class Attr(val nsUri: String, val localName: String, val value: String)

    private fun attributes(): List<Attr> {
        val n = reader.attributeCount
        val list = ArrayList<Attr>(n)
        for (i in 0 until n) {
            val aNs = reader.getAttributeNamespace(i)
            val aLn = reader.getAttributeLocalName(i)
            if (aNs == XMLNS_NS || reader.getAttributePrefix(i) == "xmlns" || (aNs.isEmpty() && aLn == "xmlns")) continue
            list.add(Attr(aNs, aLn, reader.getAttributeValue(i)))
        }
        return list
    }

    private fun countUnknownNamespace(nsUri: String) {
        unknownNamespaceCount[nsUri] = (unknownNamespaceCount[nsUri] ?: 0) + 1
    }

    // --- top level ---

    private fun topLevel() {
        val elNs = reader.namespaceURI
        val elName = reader.localName
        when {
            ns.isXmi(elNs) -> when (elName) {
                "Extension" -> extension()
                "Documentation", "documentation" -> skipElement()
                else -> {
                    issues.warning(XmiIssueKind.UNKNOWN_FEATURE, "Unknown XMI element 'xmi:$elName' skipped", document.uri, line)
                    skipElement()
                }
            }

            ns.isUml(elNs) -> {
                val obj = readObject(elName, null)
                if (null != obj) document._roots.add(obj)
            }

            ns.isIgnored(elNs) -> skipElement()
            elNs.isEmpty() -> {
                // e.g. <documentation> in OMG metamodel files
                issues.info(XmiIssueKind.UNKNOWN_NAMESPACE, "Element '$elName' with no namespace skipped", document.uri, line)
                skipElement()
            }

            else -> {
                // a stereotype application, or an element of a tool namespace; decided once profiles are known
                document._otherTopLevel.add(readRaw())
            }
        }
    }

    private fun extension() {
        val extender = attributes().firstOrNull { it.localName == "extender" }?.value ?: "?"
        extensionCount[extender] = (extensionCount[extender] ?: 0) + 1
        if (options.keepExtensions) {
            document._extensions.add(readRaw())
        } else {
            skipElement()
        }
    }

    // --- raw elements ---

    private fun readRaw(): XmiRawElement {
        val elNs = reader.namespaceURI
        val prefix = reader.prefix
        val elName = reader.localName
        val ln = line
        var xmiId: String? = null
        var xmiUuid: String? = null
        var xmiType: String? = null
        var href: String? = null
        var idref: String? = null
        val attrs = mutableMapOf<String, String>()
        for (a in attributes()) {
            when {
                a.nsUri.isNotEmpty() && ns.isXmi(a.nsUri) -> when (a.localName) {
                    "id" -> xmiId = a.value
                    "uuid" -> xmiUuid = a.value
                    "type" -> xmiType = a.value
                    "idref" -> idref = a.value
                    else -> {}
                }

                a.nsUri.isEmpty() && a.localName == "href" -> href = a.value
                a.nsUri.isEmpty() -> attrs[a.localName] = a.value
                else -> attrs["{${a.nsUri}}${a.localName}"] = a.value
            }
        }
        // an element whose xmi:type is a UML metaclass is a UML object, e.g. a ValueSpecification as a tag value
        val umlTypeName = xmiType?.let { umlMetaclassName(it) }
        if (null != umlTypeName) {
            val obj = readObjectBody(umlTypeName, xmiType, null, elName)
            return XmiRawElement(elNs, prefix, elName, xmiId, xmiUuid, xmiType, href, idref, attrs, emptyList(), null, obj, ln)
        }
        val children = mutableListOf<XmiRawElement>()
        val text = readContent { children.add(readRaw()) }
        val txt = if (children.isEmpty()) text else text?.takeIf { it.isNotBlank() }
        if (null != xmiId) document.registerRaw(xmiId, xmiUuid)
        return XmiRawElement(elNs, prefix, elName, xmiId, xmiUuid, xmiType, href, idref, attrs, children, txt, null, ln)
    }

    /** the metaclass name of an xmi:type value if it is in the UML namespace, else null */
    private fun umlMetaclassName(xmiType: String): String? {
        val p = xmiType.indexOf(':')
        val prefix = if (p < 0) "" else xmiType.substring(0, p)
        val name = if (p < 0) xmiType else xmiType.substring(p + 1)
        val typeNs = reader.namespaceContext.getNamespaceURI(prefix) ?: return null
        return if (ns.isUml(typeNs)) name else null
    }

    // --- objects ---

    /**
     * Reads the current element as an object.
     * The metaclass is that of xmi:type, else [defaultTypeName] (the element name at top level, or the type of the feature).
     */
    private fun readObject(defaultTypeName: String, ownerFeature: XmiMetaFeature?): Any? {
        var xmiType: String? = null
        for (i in 0 until reader.attributeCount) {
            if (reader.getAttributeLocalName(i) == "type" && ns.isXmi(reader.getAttributeNamespace(i))) {
                xmiType = reader.getAttributeValue(i)
                break
            }
        }
        val typeName = if (null != xmiType) {
            val n = umlMetaclassName(xmiType)
            if (null == n) {
                val p = xmiType.substringBefore(':', "")
                val typeNs = reader.namespaceContext.getNamespaceURI(p)
                if (null != typeNs && ns.isIgnored(typeNs)) {
                    skipElement()
                } else {
                    issues.warning(XmiIssueKind.UNKNOWN_METACLASS, "xmi:type '$xmiType' is not a UML metaclass; element '${reader.localName}' skipped", document.uri, line)
                    skipElement()
                }
                return null
            }
            n
        } else {
            defaultTypeName
        }
        return readObjectBody(typeName, xmiType, ownerFeature, reader.localName)
    }

    private fun readObjectBody(typeName: String, xmiType: String?, ownerFeature: XmiMetaFeature?, elementName: String): Any? {
        val ln = line
        val mc = metamodel.metaclass(typeName)
        if (null == mc || mc.isAbstract) {
            val what = if (null == mc) "unknown" else "abstract"
            issues.warning(XmiIssueKind.UNKNOWN_METACLASS, "Metaclass '$typeName' is $what; element '$elementName' skipped${if (null == xmiType) " (no xmi:type)" else ""}", document.uri, ln)
            skipElement()
            return null
        }
        val attrs = attributes()
        var xmiId: String? = null
        var xmiUuid: String? = null
        for (a in attrs) {
            if (a.nsUri.isNotEmpty() && ns.isXmi(a.nsUri)) when (a.localName) {
                "id" -> xmiId = a.value
                "uuid" -> xmiUuid = a.value
            }
        }
        val identity: Any = xmiId ?: xmiUuid ?: "${document.uri}#_gen${generatedIdCount++}"
        val obj = try {
            mc.construct(identity)
        } catch (e: Exception) {
            issues.error(XmiIssueKind.OTHER, "Could not create '${mc.name}' '$identity': ${e.message}", document.uri, ln)
            skipElement()
            return null
        }
        document.register(obj, xmiId, xmiUuid, issues, ln)

        val isProfile = mc.name == "Profile"
        if (isProfile) {
            val uri = attrs.firstOrNull { it.nsUri.isEmpty() && it.localName == "URI" }?.value
            val info = XmiProfileInfo(obj, uri, document)
            document._profiles.add(info)
            profileStack.add(info)
        }
        if (mc.name == "Stereotype") {
            val name = attrs.firstOrNull { it.nsUri.isEmpty() && it.localName == "name" }?.value
            for (p in profileStack) p.addStereotype(name, obj)
        }

        // values of many valued features, set once the element is read (a many valued DATA feature is set as a whole)
        val values = LinkedHashMap<XmiMetaFeature, MutableList<Any>>()
        for (a in attrs) {
            when {
                a.nsUri.isNotEmpty() && ns.isXmi(a.nsUri) -> {} // id, uuid, type, version, label ...
                a.nsUri.isNotEmpty() -> if (!ns.isIgnored(a.nsUri)) countUnknownNamespace(a.nsUri)
                a.localName == "href" -> issues.warning(XmiIssueKind.OTHER, "href on a contained element is not supported; '${a.value}' ignored", document.uri, ln)
                else -> setFromAttribute(obj, mc, a.localName, a.value, ln, values)
            }
        }

        readContent { readFeatureElement(obj, mc, values) }

        for ((f, vs) in values) {
            try {
                f.set(obj, vs)
            } catch (e: XmiReadException) {
                throw e
            } catch (e: Exception) {
                issues.error(XmiIssueKind.INVALID_VALUE, "Could not set '${mc.name}.${f.name}': ${e.message}", document.uri, ln)
            }
        }

        if (isProfile) profileStack.removeAt(profileStack.size - 1)
        return obj
    }

    private fun setFromAttribute(obj: Any, mc: XmiMetaclass, name: String, value: String, ln: Int?, values: MutableMap<XmiMetaFeature, MutableList<Any>>) {
        val f = mc.feature(name)
        when {
            null == f -> issues.warning(XmiIssueKind.UNKNOWN_FEATURE, "'${mc.name}' has no feature '$name'; attribute ignored", document.uri, ln)
            f.isDerived -> {} // computed, nothing to set
            else -> when (f.kind) {
                XmiFeatureKind.DATA -> setData(obj, mc, f, value, ln, values)
                XmiFeatureKind.REFERENCE -> value.split(' ', '\t', '\n', '\r').filter { it.isNotEmpty() }.forEach {
                    pending.add(XmiPendingReference(document, obj, f, it, false, ln))
                }

                XmiFeatureKind.COMPOSITE -> value.split(' ', '\t', '\n', '\r').filter { it.isNotEmpty() }.forEach {
                    // a contained object given by id, contained by this object but defined elsewhere
                    pending.add(XmiPendingReference(document, obj, f, it, false, ln))
                }
            }
        }
    }

    private fun setData(obj: Any, mc: XmiMetaclass, f: XmiMetaFeature, text: String, ln: Int?, values: MutableMap<XmiMetaFeature, MutableList<Any>>) {
        val v = f.convert(text)
        if (null == v) {
            issues.warning(XmiIssueKind.INVALID_VALUE, "Value '$text' is not valid for '${mc.name}.${f.name}' of type '${f.typeName}'", document.uri, ln)
            return
        }
        setValue(obj, mc, f, v, text, ln, values)
    }

    private fun setValue(obj: Any, mc: XmiMetaclass, f: XmiMetaFeature, v: Any, text: String, ln: Int?, values: MutableMap<XmiMetaFeature, MutableList<Any>>) {
        if (f.isMany) {
            values.getOrPut(f) { mutableListOf() }.add(v)
            return
        }
        try {
            f.set(obj, v)
        } catch (e: XmiReadException) {
            throw e
        } catch (e: Exception) {
            issues.error(XmiIssueKind.INVALID_VALUE, "Could not set '${mc.name}.${f.name}' to '$text': ${e.message}", document.uri, ln)
        }
    }

    /** positioned on the start of a child element of an object: reads it as the value of a feature */
    private fun readFeatureElement(obj: Any, mc: XmiMetaclass, values: MutableMap<XmiMetaFeature, MutableList<Any>>) {
        val elNs = reader.namespaceURI
        val elName = reader.localName
        val ln = line
        when {
            elNs.isEmpty() -> {}
            ns.isXmi(elNs) -> {
                if (elName == "Extension") extension() else skipElement()
                return
            }

            ns.isIgnored(elNs) -> {
                skipElement(); return
            }

            else -> {
                countUnknownNamespace(elNs)
                skipElement()
                return
            }
        }
        val f = mc.feature(elName)
        if (null == f) {
            issues.warning(XmiIssueKind.UNKNOWN_FEATURE, "'${mc.name}' has no feature '$elName'; element skipped", document.uri, ln)
            skipElement()
            return
        }
        if (f.isDerived) {
            skipElement()
            return
        }
        var href: String? = null
        var idref: String? = null
        for (a in attributes()) {
            if (a.nsUri.isEmpty() && a.localName == "href") href = a.value
            else if (a.localName == "idref" && ns.isXmi(a.nsUri)) idref = a.value
        }
        when {
            null != href -> {
                pending.add(XmiPendingReference(document, obj, f, href, true, ln))
                skipElement()
            }

            null != idref -> {
                pending.add(XmiPendingReference(document, obj, f, idref, false, ln))
                skipElement()
            }

            else -> when (f.kind) {
                XmiFeatureKind.DATA -> {
                    val text = readContent { skipElement() } ?: ""
                    setData(obj, mc, f, text, ln, values)
                }

                XmiFeatureKind.COMPOSITE -> {
                    val child = readObject(f.typeName, f)
                    if (null != child) setValue(obj, mc, f, child, elName, ln, values)
                }

                XmiFeatureKind.REFERENCE -> {
                    issues.warning(XmiIssueKind.INVALID_VALUE, "Reference '${mc.name}.${f.name}' has neither href nor xmi:idref; element skipped", document.uri, ln)
                    skipElement()
                }
            }
        }
    }
}

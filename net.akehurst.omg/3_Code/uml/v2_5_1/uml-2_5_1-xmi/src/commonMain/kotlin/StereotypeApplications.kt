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

import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.Property
import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.Element
import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.NamedElement
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Profile
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Stereotype
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.Enumeration
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.EnumerationLiteral
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.PrimitiveType
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralInteger
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralUnlimitedNatural

/**
 * The value(s) of one tag (stereotype attribute) of a [StereotypeApplication].
 *
 * Each value is one of:
 * - String, Long, Double or Boolean, for a primitive type (Integer and UnlimitedNatural are Long, '*' is -1)
 * - an [EnumerationLiteral], for an enumeration (a String if the literal is not found)
 * - an [Element], for a reference to a model element, or an [XmiProxy] if it is not resolved
 * - a UML object (e.g. a ValueSpecification), for a value written as a contained element with xmi:type
 * - a String, as written, when the tag has no definition (unknown profile or attribute)
 */
class TagValue(
    val name: String,
    /** the stereotype attribute that defines this tag, if the profile is loaded */
    val definition: Property?,
    val values: List<Any>,
    val isMany: Boolean,
) {
    /** the first (or only) value */
    val value: Any? get() = values.firstOrNull()

    /** the first value as a String: the literal name for an enumeration literal, the name of a named element */
    val stringValue: String?
        get() = when (val v = value) {
            null -> null
            is EnumerationLiteral -> v.name
            is NamedElement -> v.name
            else -> v.toString()
        }

    val isTyped: Boolean get() = null != definition
    override fun toString(): String = "$name=${if (isMany) values.toString() else value.toString()}"
}

/**
 * A stereotype application, e.g. `<sysml:Block xmi:id="_b1" base_Class="_c1"/>`.
 * If the profile (or the stereotype in it) is not loaded, [stereotype] is null and the tags are untyped.
 */
class StereotypeApplication(
    val raw: XmiRawElement,
    val document: XmiDocument,
    /** the profile URI, i.e. the namespace of the application element mapped by [XmiNamespaceTable.profileAliases] */
    val profileUri: String,
    /** the stereotype name, i.e. the local name of the application element */
    val name: String,
    val profile: Profile?,
    val stereotype: Stereotype?,
    /** the extended elements, from the base_* attributes: [Element]s, or [XmiProxy]s when not resolved */
    val baseElements: List<Any>,
    val tags: Map<String, TagValue>,
) {
    val xmiId: String? get() = raw.xmiId

    /** 'ProfileName::StereotypeName' if the profile is loaded, else 'prefix:name' as written */
    val qualifiedName: String get() = if (null != profile) "${profile.name}::$name" else raw.qualifiedName

    val isTyped: Boolean get() = null != stereotype

    fun tag(name: String): TagValue? = tags[name]
    override fun toString(): String = "«$qualifiedName» on ${baseElements.joinToString { (it as? NamedElement)?.name ?: it.toString() }}"
}

/**
 * All stereotype applications of the documents in a resource set, indexed by base element.
 * Profiles are found by their URI (see [XmiNamespaceTable.profileAliases] to match other URIs).
 */
class StereotypeApplications internal constructor(
    private val resourceSet: XmiResourceSet,
) {
    private val ns get() = resourceSet.options.namespaces
    private val issues get() = resourceSet.issueCollector
    private val processedDocuments = mutableSetOf<XmiDocument>()
    private val _applications = mutableListOf<StereotypeApplication>()
    private val _byElement = HashMap<Any, MutableList<StereotypeApplication>>()
    private val _other = mutableListOf<XmiRawElement>()

    // the profile URIs each loaded stereotype belongs to (a stereotype in a nested profile belongs to both)
    private val profileUrisOfStereotype = HashMap<Any, MutableSet<String>>()
    private val profilesByUri = mutableMapOf<String, MutableList<XmiProfileInfo>>()

    val applications: List<StereotypeApplication> get() = _applications

    /** top level elements that are not stereotype applications (no base_ attribute), e.g. tool data */
    val otherElements: List<XmiRawElement> get() = _other

    /** reads the stereotype applications of documents not yet processed */
    internal fun update() {
        for (p in resourceSet.profiles) {
            val uri = p.uri ?: continue
            if (profilesByUri[uri]?.contains(p) == true) continue
            profilesByUri.getOrPut(uri) { mutableListOf() }.add(p)
            for (sts in p.stereotypesByName.values) for (s in sts) profileUrisOfStereotype.getOrPut(s) { mutableSetOf() }.add(uri)
        }
        for (doc in resourceSet.documents.toList()) {
            if (doc in processedDocuments) continue
            processedDocuments.add(doc)
            for (raw in doc.otherTopLevel) readApplication(doc, raw)
        }
    }

    // --- queries ---

    /** the applications on [element] */
    fun applicationsOf(element: Any): List<StereotypeApplication> = _byElement[element] ?: emptyList()

    /** the (loaded) stereotypes applied to [element] */
    fun appliedStereotypes(element: Any): List<Stereotype> = applicationsOf(element).mapNotNull { it.stereotype }

    /** the profile URIs that [stereotype] belongs to */
    fun profileUrisOf(stereotype: Stereotype): Set<String> = profileUrisOfStereotype[stereotype] ?: emptySet()

    /** [stereotype] and all stereotypes it specialises, directly or indirectly */
    fun generalisationClosure(stereotype: Stereotype): Set<Stereotype> {
        val result = linkedSetOf<Stereotype>()
        val queue = ArrayDeque<Stereotype>()
        queue.add(stereotype)
        while (queue.isNotEmpty()) {
            val s = queue.removeFirst()
            if (result.add(s)) {
                for (g in s.generalizationSet) {
                    val general = g.generalReference.resolved
                    if (general is Stereotype) queue.add(general)
                }
            }
        }
        return result
    }

    /** true if the application is of [stereotype], or (with [includeSubStereotypes]) of a stereotype that specialises it */
    fun isApplicationOf(app: StereotypeApplication, stereotype: Stereotype, includeSubStereotypes: Boolean = true): Boolean {
        val s = app.stereotype ?: return false
        return if (includeSubStereotypes) stereotype in generalisationClosure(s) else s == stereotype
    }

    /**
     * true if the application is of the stereotype named [stereotypeName] in the profile with URI [profileUri]
     * (mapped through [XmiNamespaceTable.profileAliases]), or of a stereotype that specialises it.
     * This also works for applications whose profile is not loaded, but then without sub-stereotypes.
     */
    fun isApplicationOf(app: StereotypeApplication, profileUri: String, stereotypeName: String, includeSubStereotypes: Boolean = true): Boolean {
        val uri = ns.profileUriFor(profileUri)
        val s = app.stereotype
        return when {
            null == s -> app.profileUri == uri && app.name == stereotypeName
            includeSubStereotypes -> generalisationClosure(s).any { it.name == stereotypeName && uri in profileUrisOf(it) }
            else -> s.name == stereotypeName && uri in profileUrisOf(s)
        }
    }

    /** true if [element] has [stereotype] applied, or a stereotype that specialises it */
    fun hasStereotype(element: Any, stereotype: Stereotype, includeSubStereotypes: Boolean = true): Boolean =
        applicationsOf(element).any { isApplicationOf(it, stereotype, includeSubStereotypes) }

    /** true if [element] has the stereotype [stereotypeName] of profile [profileUri] applied, or a stereotype that specialises it */
    fun hasStereotype(element: Any, profileUri: String, stereotypeName: String, includeSubStereotypes: Boolean = true): Boolean =
        applicationsOf(element).any { isApplicationOf(it, profileUri, stereotypeName, includeSubStereotypes) }

    /** the applications on [element] of the stereotype, or of stereotypes that specialise it */
    fun applicationsOf(element: Any, profileUri: String, stereotypeName: String, includeSubStereotypes: Boolean = true): List<StereotypeApplication> =
        applicationsOf(element).filter { isApplicationOf(it, profileUri, stereotypeName, includeSubStereotypes) }

    /** all applications of the stereotype, or of stereotypes that specialise it */
    fun applicationsOf(profileUri: String, stereotypeName: String, includeSubStereotypes: Boolean = true): List<StereotypeApplication> =
        _applications.filter { isApplicationOf(it, profileUri, stereotypeName, includeSubStereotypes) }

    /** all elements that have the stereotype applied, or a stereotype that specialises it */
    fun elementsWithStereotype(profileUri: String, stereotypeName: String, includeSubStereotypes: Boolean = true): List<Any> =
        applicationsOf(profileUri, stereotypeName, includeSubStereotypes).flatMap { it.baseElements }.distinct()

    /** all elements that have [stereotype] applied, or a stereotype that specialises it */
    fun elementsWithStereotype(stereotype: Stereotype, includeSubStereotypes: Boolean = true): List<Any> =
        _applications.filter { isApplicationOf(it, stereotype, includeSubStereotypes) }.flatMap { it.baseElements }.distinct()

    /** the tag [tagName] of the first application on [element] of the stereotype (or a sub-stereotype of it) */
    fun tagValue(element: Any, profileUri: String, stereotypeName: String, tagName: String): TagValue? =
        applicationsOf(element, profileUri, stereotypeName).firstNotNullOfOrNull { it.tags[tagName] }

    /** the loaded stereotype [stereotypeName] of the profile [profileUri], or null */
    fun stereotype(profileUri: String, stereotypeName: String): Stereotype? =
        profilesByUri[ns.profileUriFor(profileUri)]?.firstNotNullOfOrNull { it.stereotypesByName[stereotypeName]?.firstOrNull() as? Stereotype }

    // --- reading ---

    private fun isBase(name: String) = name.startsWith("base_")

    private fun readApplication(doc: XmiDocument, raw: XmiRawElement) {
        if (ns.isIgnored(raw.namespaceUri)) return
        val hasBase = raw.attributes.keys.any { isBase(it) } || raw.children.any { isBase(it.localName) }
        if (!hasBase) {
            _other.add(raw)
            issues.warning(XmiIssueKind.UNKNOWN_NAMESPACE, "Top level element '${raw.qualifiedName}' (namespace '${raw.namespaceUri}') is not a stereotype application; kept raw", doc.uri, raw.line)
            return
        }
        val profileUri = ns.profileUriFor(raw.namespaceUri)
        val profileInfos = profilesByUri[profileUri]
        val stereotype = profileInfos?.firstNotNullOfOrNull { it.stereotypesByName[raw.localName]?.firstOrNull() } as? Stereotype
        val profile = profileInfos?.firstOrNull()?.profile as? Profile
        when {
            null == profileInfos -> issues.warning(XmiIssueKind.MISSING_PROFILE, "No profile with URI '$profileUri' is loaded; '${raw.qualifiedName}' kept untyped", doc.uri, raw.line)
            null == stereotype -> issues.warning(XmiIssueKind.UNKNOWN_STEREOTYPE, "Profile '$profileUri' has no stereotype '${raw.localName}'; kept untyped", doc.uri, raw.line)
        }
        val tagDefs = stereotype?.let { tagDefinitions(it) } ?: emptyMap()

        val bases = mutableListOf<Any>()
        val values = linkedMapOf<String, MutableList<Any>>()
        for ((name, text) in raw.attributes) {
            if (name.startsWith("{")) continue // an attribute in another namespace
            if (isBase(name)) {
                ids(text).forEach { bases.add(resourceSet.resolveReference(doc, it, false, raw.line)) }
            } else {
                val def = tagDefs[name]
                if (null != stereotype && null == def) unknownTag(doc, raw, name)
                values.getOrPut(name) { mutableListOf() }.addAll(attributeValues(doc, raw, def, text))
            }
        }
        for (child in raw.children) {
            val name = child.localName
            if (child.namespaceUri.isNotEmpty()) continue // e.g. an xmi:Extension
            if (isBase(name)) {
                childReference(doc, child)?.let { bases.add(it) }
            } else {
                val def = tagDefs[name]
                if (null != stereotype && null == def) unknownTag(doc, raw, name)
                childValue(doc, raw, def, child)?.let { values.getOrPut(name) { mutableListOf() }.add(it) }
            }
        }
        val tags = values.mapValues { (name, vs) ->
            val def = tagDefs[name]
            TagValue(name, def, vs, def?.let { isMany(it) } ?: (vs.size > 1))
        }
        val app = StereotypeApplication(raw, doc, profileUri, raw.localName, profile, stereotype, bases, tags)
        _applications.add(app)
        for (b in bases) _byElement.getOrPut(b) { mutableListOf() }.add(app)
    }

    private fun unknownTag(doc: XmiDocument, raw: XmiRawElement, name: String) {
        issues.warning(XmiIssueKind.UNKNOWN_FEATURE, "Stereotype '${raw.qualifiedName}' has no attribute '$name'; kept as written", doc.uri, raw.line)
    }

    private fun ids(text: String) = text.split(' ', '\t', '\n', '\r').filter { it.isNotEmpty() }

    private fun childReference(doc: XmiDocument, child: XmiRawElement): Any? = when {
        null != child.href -> resourceSet.resolveReference(doc, child.href, true, child.line)
        null != child.idref -> resourceSet.resolveReference(doc, child.idref, false, child.line)
        else -> child.text?.trim()?.takeIf { it.isNotEmpty() }?.let { resourceSet.resolveReference(doc, it, false, child.line) }
    }

    private enum class TagKind { PRIMITIVE, ENUM, REFERENCE, UNTYPED }

    private fun kindOf(def: Property?): TagKind {
        if (null == def) return TagKind.UNTYPED
        val ref = def.typeReference
        return when (val t = ref.resolved) {
            is PrimitiveType -> TagKind.PRIMITIVE
            is Enumeration -> TagKind.ENUM
            null -> {
                val proxy = ref.reference as? XmiProxy
                when {
                    null == proxy -> TagKind.UNTYPED
                    XmiStandardResources.isUmlPrimitiveTypes(proxy.uri ?: "") -> TagKind.PRIMITIVE
                    else -> TagKind.REFERENCE // e.g. a UML metaclass (UML.xmi#NamedElement)
                }
            }

            else -> TagKind.REFERENCE
        }
    }

    private fun primitiveTypeName(def: Property): String {
        val ref = def.typeReference
        return (ref.resolved as? NamedElement)?.name ?: (ref.reference as? XmiProxy)?.fragment ?: "String"
    }

    private fun convertPrimitive(doc: XmiDocument, raw: XmiRawElement, def: Property, text: String): Any {
        val tn = primitiveTypeName(def)
        return if (XmiDataConversion.isPrimitive(tn)) {
            XmiDataConversion.convertPrimitive(tn, text) ?: run {
                issues.warning(XmiIssueKind.INVALID_VALUE, "Value '$text' of tag '${def.name}' of '${raw.qualifiedName}' is not a valid $tn; kept as String", doc.uri, raw.line)
                text
            }
        } else text // a primitive type of a library (e.g. SysML Number): kept as written
    }

    private fun enumLiteral(doc: XmiDocument, raw: XmiRawElement, def: Property, text: String): Any {
        val e = def.typeReference.resolved as Enumeration
        return e.ownedLiteralOrderedSet.firstOrNull { it.name == text } ?: run {
            issues.warning(XmiIssueKind.INVALID_VALUE, "'$text' is not a literal of enumeration '${e.name}' (tag '${def.name}' of '${raw.qualifiedName}'); kept as String", doc.uri, raw.line)
            text
        }
    }

    private fun attributeValues(doc: XmiDocument, raw: XmiRawElement, def: Property?, text: String): List<Any> = when (kindOf(def)) {
        TagKind.UNTYPED -> listOf(text)
        TagKind.PRIMITIVE -> listOf(convertPrimitive(doc, raw, def!!, text))
        TagKind.ENUM -> listOf(enumLiteral(doc, raw, def!!, text))
        TagKind.REFERENCE -> ids(text).map { resourceSet.resolveReference(doc, it, false, raw.line) }
    }

    private fun childValue(doc: XmiDocument, raw: XmiRawElement, def: Property?, child: XmiRawElement): Any? {
        child.umlObject?.let { return it }
        if (null != child.href || null != child.idref) return childReference(doc, child)
        val text = child.text ?: ""
        return when (kindOf(def)) {
            TagKind.UNTYPED -> text
            TagKind.PRIMITIVE -> convertPrimitive(doc, raw, def!!, text)
            TagKind.ENUM -> enumLiteral(doc, raw, def!!, text.trim())
            TagKind.REFERENCE -> childReference(doc, child)
        }
    }

    /** the attributes of [stereotype] and of the stereotypes it specialises (nearest first), except base_ */
    fun tagDefinitions(stereotype: Stereotype): Map<String, Property> {
        val result = linkedMapOf<String, Property>()
        for (s in generalisationClosure(stereotype)) {
            for (p in s.ownedAttributeOrderedSet) {
                val n = p.name ?: continue
                if (!isBase(n) && n !in result) result[n] = p
            }
        }
        return result
    }

    private fun isMany(p: Property): Boolean = when (val u = p.upperValue) {
        is LiteralUnlimitedNatural -> (u.value ?: 1L).let { it < 0 || it > 1 }
        is LiteralInteger -> (u.value ?: 1L) > 1
        else -> false
    }
}

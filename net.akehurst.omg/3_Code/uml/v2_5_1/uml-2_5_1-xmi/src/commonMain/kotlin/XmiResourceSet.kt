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

import net.akehurst.kotlinx.utils.ReferenceImmutable

data class XmiReadOptions(
    /** collect problems in the result (true), or throw [XmiReadException] on the first error (false) */
    val lenient: Boolean = true,
    val namespaces: XmiNamespaceTable = XmiNamespaceTable.DEFAULT,
    /** supplies documents referenced by hrefs; the UML PrimitiveTypes library is always available */
    val resolver: XmiResourceResolver = XmiResourceResolver.NONE,
    /** load documents referenced by hrefs (through [resolver]) */
    val loadReferencedDocuments: Boolean = true,
    /** keep xmi:Extension elements as [XmiRawElement]s in [XmiDocument.extensions]; otherwise they are skipped */
    val keepExtensions: Boolean = false,
    /** at most this many issues of each kind are kept; [XmiIssueCollector.counts] has the totals */
    val maxIssuesPerKind: Int = 200,
)

/** A profile found in a document, with the stereotypes it (or a package nested in it) owns, by name. */
class XmiProfileInfo(
    val profile: Any,
    /** the value of the profile's URI attribute, which is the namespace of its stereotype applications */
    val uri: String?,
    val document: XmiDocument,
) {
    private val _stereotypes = mutableMapOf<String, MutableList<Any>>()
    val stereotypesByName: Map<String, List<Any>> get() = _stereotypes

    internal fun addStereotype(name: String?, stereotype: Any) {
        if (null != name) _stereotypes.getOrPut(name) { mutableListOf() }.add(stereotype)
    }
}

/** One XMI document that has been read. */
class XmiDocument(
    val uri: String,
) {
    internal val _roots = mutableListOf<Any>()
    internal val _otherTopLevel = mutableListOf<XmiRawElement>()
    internal val _extensions = mutableListOf<XmiRawElement>()
    internal val _profiles = mutableListOf<XmiProfileInfo>()
    private val _byId = HashMap<String, Any>()
    private val _byUuid = HashMap<String, Any>()
    private val _rawIds = HashSet<String>()

    /** the xmi:id and xmi:uuid each element was read with, by element identity (not equals) */
    private val _idsOf = HashMap<XmiIdentityKey, XmiElementIds>()

    /** the top level model elements, e.g. the uml:Model or uml:Profile */
    val roots: List<Any> get() = _roots

    /** top level elements that are not UML, i.e. stereotype applications and tool elements, as read */
    val otherTopLevel: List<XmiRawElement> get() = _otherTopLevel

    /** xmi:Extension elements, if [XmiReadOptions.keepExtensions] */
    val extensions: List<XmiRawElement> get() = _extensions

    val profiles: List<XmiProfileInfo> get() = _profiles

    /** elements by xmi:id */
    val elementsById: Map<String, Any> get() = _byId

    /** elements by xmi:uuid */
    val elementsByUuid: Map<String, Any> get() = _byUuid

    fun elementById(id: String): Any? = _byId[id] ?: _byUuid[id]

    /** true if [element] (this very object, not one that is equal to it) was read from this document */
    fun contains(element: Any): Boolean = _idsOf.containsKey(XmiIdentityKey(element))

    /**
     * the xmi:id [element] was read with in this document; null if it has none, if its xmi:id was a duplicate
     * (reported as [XmiIssueKind.DUPLICATE_ID]), or if it was not read from this document.
     * Found by object identity: an element that is only equal to one read from this document gives null.
     */
    fun idOf(element: Any): String? = _idsOf[XmiIdentityKey(element)]?.id

    /** the xmi:uuid [element] was read with in this document; null if it has none or was not read from this document */
    fun uuidOf(element: Any): String? = _idsOf[XmiIdentityKey(element)]?.uuid

    internal fun register(obj: Any, xmiId: String?, xmiUuid: String?, issues: XmiIssueCollector, line: Int?) {
        var id: String? = null
        if (null != xmiId) {
            if (_byId.containsKey(xmiId) || _rawIds.contains(xmiId)) issues.error(XmiIssueKind.DUPLICATE_ID, "Duplicate xmi:id '$xmiId'", uri, line)
            else {
                _byId[xmiId] = obj
                id = xmiId
            }
        }
        if (null != xmiUuid) _byUuid[xmiUuid] = obj
        _idsOf[XmiIdentityKey(obj)] = XmiElementIds(id, xmiUuid)
    }

    internal fun registerRaw(xmiId: String, xmiUuid: String?) {
        _rawIds.add(xmiId)
    }

    override fun toString(): String = "XmiDocument($uri)"
}

/** the xmi:id and xmi:uuid an element was read with */
internal class XmiElementIds(val id: String?, val uuid: String?)

/**
 * A map key that compares by identity (===), so that distinct elements that are equal are kept apart
 * (the ram classes are data classes, equal when created by the same factory with the same identity).
 * Kotlin common has no identity hash code, so the element's own hashCode is used; it must not change while
 * the element is a key, which holds for the ram classes (their hashCode depends only on factory and identity).
 */
internal class XmiIdentityKey(val obj: Any) {
    override fun equals(other: Any?) = other is XmiIdentityKey && other.obj === obj
    override fun hashCode() = obj.hashCode()
}

/**
 * The documents read together: a model and the profiles, libraries and model parts it references.
 * References between them are resolved when a document is read with [read].
 *
 * Use one resource set per model: read the profiles (or let the resolver supply them), then the model.
 */
class XmiResourceSet(
    val metamodel: XmiMetamodel,
    val options: XmiReadOptions = XmiReadOptions(),
) {
    val issueCollector = XmiIssueCollector(options.lenient, options.maxIssuesPerKind)
    private val _documents = linkedMapOf<String, XmiDocument>()
    private val pending = mutableListOf<XmiPendingReference>()
    private val missing = mutableSetOf<String>()
    private val resolver = XmiResourceResolver.firstOf(options.resolver, XmiStandardResources.resolver)

    val documents: Collection<XmiDocument> get() = _documents.values
    val issues: List<XmiIssue> get() = issueCollector.issues

    fun document(uri: String): XmiDocument? = _documents[uri]

    /** the document [element] (this very object, not one that is equal to it) was read from; null if it was not read in this set */
    fun documentOf(element: Any): XmiDocument? = _documents.values.firstOrNull { it.contains(element) }

    /** the xmi:id [element] was read with, in whichever document of this set it was read from; see [XmiDocument.idOf] */
    fun idOf(element: Any): String? = documentOf(element)?.idOf(element)

    /** the xmi:uuid [element] was read with, in whichever document of this set it was read from; see [XmiDocument.uuidOf] */
    fun uuidOf(element: Any): String? = documentOf(element)?.uuidOf(element)

    /** all profiles in all documents read */
    val profiles: List<XmiProfileInfo> get() = _documents.values.flatMap { it.profiles }

    /**
     * Reads [content] as the document [uri], loads the documents it references (through the resolver),
     * and resolves references. A document already read with this uri is returned as it is.
     */
    fun read(uri: String, content: String): XmiDocument {
        val doc = parse(uri, content)
        resolvePending()
        return doc
    }

    private fun parse(uri: String, content: String): XmiDocument {
        _documents[uri]?.let { return it }
        val doc = XmiDocument(uri)
        _documents[uri] = doc
        XmiDocumentParser(doc, content, metamodel, options, issueCollector, pending).parse()
        return doc
    }

    private fun loadReferenced(uri: String, href: String, from: XmiDocument, line: Int?): XmiDocument? {
        _documents[uri]?.let { return it }
        if (!options.loadReferencedDocuments || uri in missing) return null
        val content = resolver.resolve(XmiResourceRequest(uri, href, from.uri))
        return if (null == content) {
            missing.add(uri)
            if (!isUmlMetamodel(uri)) {
                issueCollector.warning(XmiIssueKind.MISSING_RESOURCE, "Referenced document '$uri' is not available; references into it are kept as proxies", from.uri, line)
            }
            null
        } else {
            parse(uri, content)
        }
    }

    /** hrefs into the UML metamodel itself (e.g. the metaclass types of base_ properties of stereotypes) */
    fun isUmlMetamodel(uri: String) = Regex("omg\\.org/spec/UML/[0-9.]+/UML\\.xmi$").containsMatchIn(uri)

    /**
     * Finds the object for an xmi:idref/id (isHref=false) or href (isHref=true) written in [from].
     * Loads the referenced document if needed. Returns the object, or an [XmiProxy] if it is not found.
     */
    fun resolveReference(from: XmiDocument, text: String, isHref: Boolean, line: Int? = null, warn: Boolean = true): Any {
        if (!isHref) {
            return from.elementById(text) ?: run {
                if (warn) issueCollector.warning(XmiIssueKind.UNRESOLVED_REFERENCE, "xmi:id '$text' not found; kept as proxy", from.uri, line)
                XmiProxy(text, from.uri, text)
            }
        }
        val docPart = XmiUri.documentPart(text)
        val frag = XmiUri.fragment(text)
        val uri = if (docPart.isEmpty()) from.uri else XmiUri.resolve(from.uri, docPart)
        val target = if (docPart.isEmpty()) from else loadReferenced(uri, text, from, line)
        val found = if (null != target && null != frag) target.elementById(frag) else null
        return found ?: run {
            if (warn && null != target) {
                issueCollector.warning(XmiIssueKind.UNRESOLVED_REFERENCE, "href '$text': '$frag' not found in '$uri'; kept as proxy", from.uri, line)
            }
            XmiProxy(text, uri, frag)
        }
    }

    private fun resolvePending() {
        // resolving may load more documents, which add to pending; so resolve first, then set
        val resolvedValues = ArrayList<Any>(pending.size)
        var i = 0
        while (i < pending.size) {
            val p = pending[i]
            resolvedValues.add(resolveReference(p.document, p.text, p.isHref, p.line))
            i++
        }
        // group by owner and feature, keeping the order written
        val groups = LinkedHashMap<Pair<Any, XmiMetaFeature>, MutableList<Pair<XmiPendingReference, Any>>>()
        for ((idx, p) in pending.withIndex()) {
            groups.getOrPut(Pair(XmiIdentityKey(p.owner), p.feature)) { mutableListOf() }.add(Pair(p, resolvedValues[idx]))
        }
        pending.clear()
        for ((key, entries) in groups) {
            val owner = (key.first as XmiIdentityKey).obj
            val f = key.second
            val first = entries.first().first
            try {
                when (f.kind) {
                    XmiFeatureKind.COMPOSITE -> {
                        // contained elements given by id: added to those already read inline
                        val found = entries.mapNotNull { (p, r) ->
                            if (r is XmiProxy) {
                                issueCollector.warning(XmiIssueKind.UNRESOLVED_REFERENCE, "Contained element '${p.text}' of '${f.name}' not found; ignored", p.document.uri, p.line)
                                null
                            } else r
                        }
                        // added to the elements already read inline
                        if (f.isMany) f.set(owner, found.distinct())
                        else if (found.isNotEmpty()) f.set(owner, found.last())
                    }

                    else -> {
                        val refs = entries.map { (_, r) ->
                            if (r is XmiProxy) ReferenceImmutable<Any, Any>(r, null) else ReferenceImmutable<Any, Any>(identityOf(r), r)
                        }
                        if (f.isMany) f.set(owner, refs) else f.set(owner, refs.last())
                    }
                }
            } catch (e: XmiReadException) {
                throw e
            } catch (e: Exception) {
                issueCollector.error(XmiIssueKind.INVALID_VALUE, "Could not set '${f.name}' to ${entries.map { it.first.text }}: ${e.message}", first.document.uri, first.line)
            }
        }
    }

    /** the identity an object was created with */
    fun identityOf(obj: Any): Any = (metamodel as? XmiIdentityProvider)?.identityOf(obj) ?: obj
}

/** implemented by a metamodel that can give the identity an object was created with */
interface XmiIdentityProvider {
    fun identityOf(obj: Any): Any?
}

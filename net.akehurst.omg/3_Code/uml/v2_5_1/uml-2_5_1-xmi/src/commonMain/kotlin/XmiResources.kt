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

/**
 * A request for the content of a document referenced by an href.
 * @param uri the document part of the href, resolved against [fromDocumentUri] when it is relative
 * @param href the href as written, including its fragment
 * @param fromDocumentUri the URI of the document that holds the href
 */
data class XmiResourceRequest(
    val uri: String,
    val href: String,
    val fromDocumentUri: String,
)

/**
 * Supplies the XMI text of documents referenced by hrefs (profiles, libraries, other model parts).
 * Returns null when the document is not available; the hrefs into it are then kept as [XmiProxy]s.
 */
fun interface XmiResourceResolver {
    fun resolve(request: XmiResourceRequest): String?

    companion object {
        val NONE = XmiResourceResolver { null }

        /** a resolver that looks [XmiResourceRequest.uri] up in [contentByUri], also trying the last path segment */
        fun fromMap(contentByUri: Map<String, String>) = XmiResourceResolver { req ->
            contentByUri[req.uri] ?: contentByUri[req.uri.substringAfterLast('/')]
        }

        /** tries each resolver in turn */
        fun firstOf(vararg resolvers: XmiResourceResolver) = XmiResourceResolver { req ->
            resolvers.firstNotNullOfOrNull { it.resolve(req) }
        }
    }
}

object XmiUri {

    /** the document part of an href, i.e. without the '#fragment' */
    fun documentPart(href: String): String = href.substringBefore('#')

    /** the fragment of an href, i.e. after the '#', or null if there is none */
    fun fragment(href: String): String? = href.indexOf('#').let { if (it < 0) null else href.substring(it + 1) }

    fun hasScheme(uri: String): Boolean {
        val c = uri.indexOf(':')
        if (c <= 0) return false
        val s = uri.indexOf('/')
        return (s < 0 || c < s) && uri.substring(0, c).all { it.isLetterOrDigit() || it == '+' || it == '-' || it == '.' }
    }

    /** resolves [uri] against [base], for the usual cases of relative paths ('a.xmi', '../lib/a.xmi', '/x/a.xmi') */
    fun resolve(base: String?, uri: String): String {
        if (uri.isEmpty()) return base ?: uri
        if (null == base || hasScheme(uri)) return normalise(uri)
        val baseDoc = documentPart(base)
        if (uri.startsWith("/")) {
            // keep the scheme and authority of the base, if any
            val schemeEnd = baseDoc.indexOf("://")
            return if (schemeEnd >= 0) {
                val authEnd = baseDoc.indexOf('/', schemeEnd + 3).let { if (it < 0) baseDoc.length else it }
                normalise(baseDoc.substring(0, authEnd) + uri)
            } else {
                val c = if (hasScheme(baseDoc)) baseDoc.indexOf(':') + 1 else 0
                normalise(baseDoc.substring(0, c) + uri)
            }
        }
        val dir = baseDoc.substringBeforeLast('/', "")
        return normalise(if (dir.isEmpty()) uri else "$dir/$uri")
    }

    private fun normalise(uri: String): String {
        if (!uri.contains("./")) return uri
        val frag = fragment(uri)
        val doc = documentPart(uri)
        val prefixEnd = doc.indexOf("://").let { if (it < 0) 0 else doc.indexOf('/', it + 3).let { e -> if (e < 0) doc.length else e } }
        val prefix = doc.substring(0, prefixEnd)
        val parts = doc.substring(prefixEnd).split('/')
        val out = mutableListOf<String>()
        for ((i, p) in parts.withIndex()) {
            when (p) {
                "." -> {}
                ".." -> if (out.isNotEmpty() && out.last() != ".." && out.last() != "") out.removeAt(out.size - 1) else out.add(p)
                else -> if (i == 0 || p.isNotEmpty() || i == parts.size - 1) out.add(p)
            }
        }
        val path = out.joinToString("/")
        return prefix + path + (frag?.let { "#$it" } ?: "")
    }
}

/**
 * Documents that are part of the UML standard, supplied by the reader itself, so that a model that
 * references them reads without the caller having to supply them.
 */
object XmiStandardResources {

    /** URIs of the UML PrimitiveTypes library, as written by tools for the different UML versions */
    fun isUmlPrimitiveTypes(uri: String): Boolean =
        uri.endsWith("/PrimitiveTypes.xmi") && (uri.contains("omg.org/spec/UML/") || uri.contains("omg.org/spec/PrimitiveTypes/"))

    val resolver = XmiResourceResolver { req ->
        when {
            isUmlPrimitiveTypes(req.uri) -> UML_PRIMITIVE_TYPES
            else -> null
        }
    }

    /** The UML 2.5.1 PrimitiveTypes library (ptc/18-01-02), without its comments. */
    val UML_PRIMITIVE_TYPES = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
          <uml:Package xmi:type="uml:Package" xmi:id="_0" name="PrimitiveTypes" URI="http://www.omg.org/spec/PrimitiveTypes/20161101">
            <packagedElement xmi:type="uml:PrimitiveType" xmi:id="Boolean" name="Boolean"/>
            <packagedElement xmi:type="uml:PrimitiveType" xmi:id="Integer" name="Integer"/>
            <packagedElement xmi:type="uml:PrimitiveType" xmi:id="Real" name="Real"/>
            <packagedElement xmi:type="uml:PrimitiveType" xmi:id="String" name="String"/>
            <packagedElement xmi:type="uml:PrimitiveType" xmi:id="UnlimitedNatural" name="UnlimitedNatural"/>
          </uml:Package>
        </xmi:XMI>
    """.trimIndent()
}

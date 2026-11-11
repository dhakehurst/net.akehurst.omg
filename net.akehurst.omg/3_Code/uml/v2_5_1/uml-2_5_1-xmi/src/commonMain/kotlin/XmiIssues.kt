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

enum class XmiIssueSeverity { INFO, WARNING, ERROR }

enum class XmiIssueKind {
    /** an XML syntax error; the rest of the document is not read */
    XML_SYNTAX,

    /** an xmi:Extension element (tool specific data), which is skipped or kept raw */
    EXTENSION,

    /** an element or attribute from a namespace that is not mapped, e.g. a vendor namespace */
    UNKNOWN_NAMESPACE,

    /** an xmi:type, element or attribute that the metamodel does not define */
    UNKNOWN_METACLASS,
    UNKNOWN_FEATURE,

    /** a value that cannot be converted to the type of its feature */
    INVALID_VALUE,

    /** a reference (xmi:idref, id list or href) that could not be resolved; it is kept as an [XmiProxy] */
    UNRESOLVED_REFERENCE,

    /** a referenced document that the resource resolver could not supply */
    MISSING_RESOURCE,

    /** a stereotype application whose profile or stereotype is not loaded; it is kept untyped */
    MISSING_PROFILE,
    UNKNOWN_STEREOTYPE,

    /** a duplicate xmi:id in one document */
    DUPLICATE_ID,

    /** anything else */
    OTHER
}

/**
 * A problem found while reading XMI. Reading is lenient: problems are collected, not thrown.
 * [documentUri] and [line] say where it was found, when known.
 */
data class XmiIssue(
    val severity: XmiIssueSeverity,
    val kind: XmiIssueKind,
    val message: String,
    val documentUri: String? = null,
    val line: Int? = null,
) {
    override fun toString(): String {
        val loc = listOfNotNull(documentUri, line?.let { "line $it" }).joinToString(":")
        return if (loc.isEmpty()) "$severity $kind: $message" else "$severity $kind ($loc): $message"
    }
}

/** Thrown only when [XmiReadOptions.lenient] is false, for the first ERROR found. */
class XmiReadException(val issue: XmiIssue) : RuntimeException(issue.toString())

class XmiIssueCollector(
    private val lenient: Boolean,
    private val maxIssuesPerKind: Int
) {
    private val _issues = mutableListOf<XmiIssue>()
    private val countPerKind = mutableMapOf<XmiIssueKind, Int>()

    val issues: List<XmiIssue> get() = _issues

    /** number of issues of each kind, including those not kept because of [XmiReadOptions.maxIssuesPerKind] */
    val counts: Map<XmiIssueKind, Int> get() = countPerKind

    fun add(issue: XmiIssue) {
        val n = (countPerKind[issue.kind] ?: 0) + 1
        countPerKind[issue.kind] = n
        if (n <= maxIssuesPerKind) _issues.add(issue)
        if (!lenient && issue.severity == XmiIssueSeverity.ERROR) throw XmiReadException(issue)
    }

    fun info(kind: XmiIssueKind, message: String, documentUri: String?, line: Int? = null) = add(XmiIssue(XmiIssueSeverity.INFO, kind, message, documentUri, line))
    fun warning(kind: XmiIssueKind, message: String, documentUri: String?, line: Int? = null) = add(XmiIssue(XmiIssueSeverity.WARNING, kind, message, documentUri, line))
    fun error(kind: XmiIssueKind, message: String, documentUri: String?, line: Int? = null) = add(XmiIssue(XmiIssueSeverity.ERROR, kind, message, documentUri, line))
}

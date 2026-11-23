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
 * Says what each XML namespace URI of an XMI document means.
 *
 * Tools write different URIs for the same thing (e.g. UML 2.5 and 2.5.1, several SysML 1.x versions),
 * so each kind of namespace is given by a list of exact URIs and a list of patterns.
 *
 * - [xmi]: the XMI namespace (xmi:id, xmi:type, xmi:Extension, ...)
 * - [uml]: the UML metamodel namespace; `xmi:type="uml:Class"` and `<uml:Model>` use it
 * - [profileAliases]: maps the namespace URI of stereotype applications (e.g. `<sysml:Block>`) to the URI
 *   of a loaded `uml:Profile` (its `URI` attribute). A namespace that is not in this map is matched to a
 *   profile with exactly that URI. Use it when a model uses another version of a profile than the one loaded.
 * - [ignored]: namespaces whose elements and attributes are skipped without a warning, e.g. tool namespaces
 *   known to hold only tool data.
 */
data class XmiNamespaceTable(
    val xmi: Set<String> = emptySet(),
    val xmiPatterns: List<Regex> = emptyList(),
    val uml: Set<String> = emptySet(),
    val umlPatterns: List<Regex> = emptyList(),
    val profileAliases: Map<String, String> = emptyMap(),
    val ignored: Set<String> = emptySet(),
    val ignoredPatterns: List<Regex> = emptyList(),
) {
    companion object {
        /** URIs written by common tools for XMI 2.x and UML 2.x. */
        val DEFAULT = XmiNamespaceTable(
            xmi = setOf(
                "http://www.omg.org/XMI",
                "http://schema.omg.org/spec/XMI/2.1",
                "http://schema.omg.org/spec/XMI/2.1.1",
                "http://schema.omg.org/spec/XMI/2.2",
            ),
            xmiPatterns = listOf(Regex("^https?://www\\.omg\\.org/spec/XMI/[0-9.]+/?$")),
            uml = setOf(
                "http://schema.omg.org/spec/UML/2.1",
                "http://schema.omg.org/spec/UML/2.1.1",
                "http://schema.omg.org/spec/UML/2.2",
                "http://www.eclipse.org/uml2/2.1.0/UML",
                "http://www.eclipse.org/uml2/3.0.0/UML",
                "http://www.eclipse.org/uml2/4.0.0/UML",
                "http://www.eclipse.org/uml2/5.0.0/UML",
            ),
            // e.g. http://www.omg.org/spec/UML/20131001 but not http://www.omg.org/spec/UML/20131001/StandardProfile
            umlPatterns = listOf(Regex("^https?://www\\.omg\\.org/spec/UML/[0-9.]+/?$")),
            // mofext:Tag elements hold metamodel tags (e.g. org.omg.xmi.nsPrefix), not model content
            ignoredPatterns = listOf(Regex("^https?://www\\.omg\\.org/spec/MOF/[0-9.]+/?$")),
            // the UML 2.5.1 StandardProfile has an http URI; some files (e.g. the SysML 1.7 profile) refer to it by https
            profileAliases = mapOf("https://www.omg.org/spec/UML/20161101/StandardProfile" to "http://www.omg.org/spec/UML/20161101/StandardProfile"),
        )
    }

    fun isXmi(nsUri: String): Boolean = nsUri in xmi || xmiPatterns.any { it.matches(nsUri) }
    fun isUml(nsUri: String): Boolean = nsUri in uml || umlPatterns.any { it.matches(nsUri) }
    fun isIgnored(nsUri: String): Boolean = nsUri in ignored || ignoredPatterns.any { it.matches(nsUri) }

    /** the URI of the profile that applications in namespace [nsUri] belong to */
    fun profileUriFor(nsUri: String): String = profileAliases[nsUri] ?: nsUri

    fun withXmi(vararg nsUri: String) = copy(xmi = xmi + nsUri)
    fun withUml(vararg nsUri: String) = copy(uml = uml + nsUri)
    fun withProfileAlias(applicationNsUri: String, profileUri: String) = copy(profileAliases = profileAliases + (applicationNsUri to profileUri))
    fun withIgnored(vararg nsUri: String) = copy(ignored = ignored + nsUri)
}

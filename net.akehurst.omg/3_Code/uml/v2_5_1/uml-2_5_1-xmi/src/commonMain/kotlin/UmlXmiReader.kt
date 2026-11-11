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

import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Model
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Profile
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelFactory
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam

/**
 * Reads UML 2.5.1 models (model instances, not metamodels) from XMI into the UML ram classes.
 *
 * ```
 * val reader = UmlXmiReader(XmiReadOptions(resolver = XmiResourceResolver.fromMap(mapOf("SysML.xmi" to sysmlText))))
 * reader.load("http://www.omg.org/spec/SysML/20181001/SysML.xmi", sysmlText) // optional: profiles, libraries
 * val result = reader.read("model.xmi", modelText)
 * val model = result.model
 * result.stereotypes.hasStereotype(element, SYSML_URI, "Requirement")
 * ```
 * Profiles and libraries can be loaded before the model with [load], or supplied on demand by
 * [XmiReadOptions.resolver] when the model references them by href.
 * One reader holds one set of documents; use a new reader for an unrelated model.
 */
class UmlXmiReader(
    val options: XmiReadOptions = XmiReadOptions(),
    val factory: UML_ModelFactory = UML_ModelFactoryRam("UML"),
) {
    val metamodel = UmlXmiMetamodel(factory)
    val resourceSet = XmiResourceSet(metamodel, options)
    val stereotypes = StereotypeApplications(resourceSet)

    /** reads a profile, library or model part that other documents reference, e.g. by href */
    fun load(uri: String, content: String): XmiDocument = resourceSet.read(uri, content).also { stereotypes.update() }

    /** reads the model [content] as document [uri] */
    fun read(uri: String, content: String): UmlXmiReadResult {
        val doc = resourceSet.read(uri, content)
        stereotypes.update()
        return UmlXmiReadResult(doc, this)
    }
}

class UmlXmiReadResult internal constructor(
    val document: XmiDocument,
    val reader: UmlXmiReader,
) {
    /** the top level UML elements of the document */
    val roots: List<Any> get() = document.roots

    /** the first top level uml:Model, if any */
    val model: Model? get() = document.roots.firstOrNull { it is Model } as Model?

    /** the first top level uml:Package (including Model and Profile), if any */
    val rootPackage: Package? get() = document.roots.firstOrNull { it is Package } as Package?

    /** the top level uml:Profiles */
    val profiles: List<Profile> get() = document.roots.filterIsInstance<Profile>()

    /** stereotype applications of all documents read by the reader */
    val stereotypes: StereotypeApplications get() = reader.stereotypes

    /** the issues found in all documents read by the reader */
    val issues: List<XmiIssue> get() = reader.resourceSet.issues
    val errors: List<XmiIssue> get() = issues.filter { it.severity == XmiIssueSeverity.ERROR }
    val warnings: List<XmiIssue> get() = issues.filter { it.severity == XmiIssueSeverity.WARNING }

    /** the number of issues of each kind, including those not kept (see [XmiReadOptions.maxIssuesPerKind]) */
    val issueCounts: Map<XmiIssueKind, Int> get() = reader.resourceSet.issueCollector.counts

    fun elementById(id: String): Any? = document.elementById(id)
}

/** Reads a UML 2.5.1 model from XMI; see [UmlXmiReader] to load profiles first or read several models. */
fun readUmlXmi(content: String, uri: String = "model.xmi", options: XmiReadOptions = XmiReadOptions()): UmlXmiReadResult =
    UmlXmiReader(options).read(uri, content)

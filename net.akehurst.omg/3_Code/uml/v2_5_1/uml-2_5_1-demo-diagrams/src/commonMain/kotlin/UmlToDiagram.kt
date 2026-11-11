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


package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.syntaxAnalyser.LocationMapDefault
import net.akehurst.language.api.processor.M2mTransformString
import net.akehurst.language.base.api.Import
import net.akehurst.language.expressions.processor.ObjectGraphAccessorMutatorByReflection
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.m2mTransform.api.DomainReference
import net.akehurst.language.types.api.TypeInstance
import net.akehurst.language.types.api.TypesDomain
import net.akehurst.language.objectgraph.api.TypedObject
import net.akehurst.language.types.asm.StdLibDefault
import net.akehurst.language.types.builder.typesDomain
import net.akehurst.omg.diagram.Diagram
import net.akehurst.omg.diagram.DiagramTypesDomain
import net.akehurst.omg.uml.v2_5_1.agl.types.UMLTypesDomain
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelFactory

/**
 * Runs an agl-m2m transform from UML model elements (domain 'uml') to the generic diagram model (domain 'dgm').
 */
class UmlToDiagram(factory: UML_ModelFactory) {

    class Result(val diagrams: List<Diagram>, val issues: IssueHolder)

    val umlTypes: TypesDomain = UMLTypesDomain(factory).types.also { importAllNamespaces(it) }

    /**
     * The 'dgm' side also needs the UML types, because the target templates contain type tests of UML elements
     * (e.g. `when { e is Interface -> ... }`) and agl resolves them in the domain of the template.
     * The diagram namespace comes first so that simple names like 'Node' resolve to the diagram type.
     */
    val diagramTypes: TypesDomain = typesDomain("Diagram", true) {}.also { td ->
        DiagramTypesDomain.types.namespace.filter { it.qualifiedName.value == DiagramTypesDomain.NAMESPACE }.forEach { td.addNamespace(it) }
        umlTypes.namespace.filter { it.qualifiedName.value.startsWith("UML") }.forEach { td.addNamespace(it) }
        td.resolveImports()
    }

    fun transform(transform: String, source: List<Any>): Result {
        val issues = IssueHolder(LanguageProcessorPhase.INTERPRET)
        val umlOg = UmlObjectGraph(umlTypes, issues)
        val dgmOg = UmlObjectGraph(diagramTypes, issues)
        val res = Agl.transform(
            M2mTransformString(transform),
            mapOf(UML to umlTypes, DGM to diagramTypes),
            mapOf(umlTypes.name to umlOg, diagramTypes.name to dgmOg),
            mapOf(UML to source.map { umlOg.toTypedObject(it, StdLibDefault.AnyType) }),
            emptyMap(),
            DGM
        )
        issues.addAllFrom(res.issues)
        val diagrams = res.targets.mapNotNull { it.self as? Diagram }
        diagrams.forEach { d ->
            // a diagram shows only the edges between its own nodes (e.g. not a profile's metaclass references from the profile itself)
            val ids = d.allNode.map { it.id }.toSet()
            d.edge = d.edge.filter { it.sourceId in ids && it.targetId in ids }
            // relationships often have no name, so give each edge an id from its ends
            d.edge.forEachIndexed { i, e -> if (e.id.isBlank()) e.id = "${e.shape}[$i]:${e.sourceId}->${e.targetId}" }
        }
        return Result(diagrams, issues)
    }

    /**
     * agl's reflection object graph can not yet create 'OrderedSet' or 'Collection' values (used by the UML types);
     * until it can, treat an OrderedSet as a List and a Collection as a Set.
     */
    private class UmlObjectGraph(typesDomain: TypesDomain, issues: IssueHolder) :
        ObjectGraphAccessorMutatorByReflection(typesDomain, issues, LocationMapDefault()) {
        override fun createCollection(collectionType: TypeInstance, collection: Iterable<TypedObject>): TypedObject =
            when (collectionType.qualifiedTypeName) {
                StdLibDefault.OrderedSet.qualifiedName -> super.createCollection(StdLibDefault.List.type(collectionType.typeArguments), collection)
                StdLibDefault.Collection.qualifiedName -> super.createCollection(StdLibDefault.Set.type(collectionType.typeArguments), collection)
                else -> super.createCollection(collectionType, collection)
            }
    }

    companion object {
        val UML = DomainReference("uml")
        val DGM = DomainReference("dgm")

        /**
         * The generated UML types refer to types in other UML namespaces by qualified name,
         * which agl resolves only through imports. Until the template adds them, import every namespace into every other.
         */
        private fun importAllNamespaces(td: TypesDomain) {
            val umlNamespaces = td.namespace.filter { it.qualifiedName.value.startsWith("UML") }
            val names = umlNamespaces.map { it.qualifiedName.value }
            umlNamespaces.forEach { ns ->
                names.filter { it != ns.qualifiedName.value && ns.import.none { i -> i.value == it } }.forEach { ns.addImport(Import(it)) }
            }
            td.resolveImports()
        }
    }
}

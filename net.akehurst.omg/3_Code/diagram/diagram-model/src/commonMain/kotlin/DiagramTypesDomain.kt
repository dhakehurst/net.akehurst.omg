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


package net.akehurst.omg.diagram

import net.akehurst.language.types.builder.typesDomain

/**
 * The agl types of the generic diagram model, bound to the Kotlin classes in DiagramModel.kt,
 * so that agl expressions and agl-m2m transforms can read, create and set them.
 */
object DiagramTypesDomain {
    // The mutators accept any value: an m2m expression that finds nothing gives Unit, which becomes "" or an empty list.

    const val NAMESPACE = "net.akehurst.omg.diagram"

    val types = typesDomain("Diagram", true) {
        namespace(NAMESPACE) {
            data("Diagram", implementation = Diagram::class) {
                constructor_ { execution { Diagram() } }
                propertyOfWithBinding<Diagram, Any?>(setOf(CMP, VAR, STR), "name", "String", accessor = { s: Diagram -> s.name }, mutator = { s: Diagram, v: Any? -> s.name = (v as? String) ?: "" })
                propertyOfWithBinding<Diagram, Any?>(setOf(CMP, VAR, STR), "kind", "String", accessor = { s: Diagram -> s.kind }, mutator = { s: Diagram, v: Any? -> s.kind = (v as? String) ?: "" })
                propertyOfWithBinding<Diagram, Any?>(setOf(CMP, VAR, STR), "node", "List", accessor = { s: Diagram -> s.node }, mutator = { s: Diagram, v: Any? -> s.node = (v as? Collection<*>)?.filterIsInstance<Node>() ?: emptyList() }) { typeArgument("Node") }
                propertyOfWithBinding<Diagram, Any?>(setOf(CMP, VAR, STR), "edge", "List", accessor = { s: Diagram -> s.edge }, mutator = { s: Diagram, v: Any? -> s.edge = (v as? Collection<*>)?.filterIsInstance<Edge>() ?: emptyList() }) { typeArgument("Edge") }
            }
            data("DiagramElement", implementation = DiagramElement::class) {
                propertyOfWithBinding<DiagramElement, Any?>(setOf(CMP, VAR, STR), "id", "String", accessor = { s: DiagramElement -> s.id }, mutator = { s: DiagramElement, v: Any? -> s.id = (v as? String) ?: "" })
                propertyOfWithBinding<DiagramElement, Any?>(setOf(CMP, VAR, STR), "shape", "String", accessor = { s: DiagramElement -> s.shape }, mutator = { s: DiagramElement, v: Any? -> s.shape = (v as? String) ?: "" })
                propertyOfWithBinding<DiagramElement, Any?>(setOf(CMP, VAR, STR), "label", "String", accessor = { s: DiagramElement -> s.label }, mutator = { s: DiagramElement, v: Any? -> s.label = (v as? String) ?: "" })
                propertyOfWithBinding<DiagramElement, Any?>(setOf(CMP, VAR, STR), "keyword", "List", accessor = { s: DiagramElement -> s.keyword }, mutator = { s: DiagramElement, v: Any? -> s.keyword = (v as? Collection<*>)?.filterIsInstance<String>() ?: emptyList() }) { typeArgument("String") }
                propertyOfWithBinding<DiagramElement, Any?>(setOf(CMP, VAR, STR), "option", "List", accessor = { s: DiagramElement -> s.option }, mutator = { s: DiagramElement, v: Any? -> s.option = (v as? Collection<*>)?.filterIsInstance<String>() ?: emptyList() }) { typeArgument("String") }
                propertyOfWithBinding<DiagramElement, Any?>(setOf(REF, VAR, STR), "modelElement", "Any", true, accessor = { s: DiagramElement -> s.modelElement }, mutator = { s: DiagramElement, v: Any? -> s.modelElement = v.takeIf { it != Unit } })
            }
            data("Node", implementation = Node::class) {
                supertypes("DiagramElement")
                constructor_ { execution { Node() } }
                propertyOfWithBinding<Node, Any?>(setOf(CMP, VAR, STR), "compartment", "List", accessor = { s: Node -> s.compartment }, mutator = { s: Node, v: Any? -> s.compartment = (v as? Collection<*>)?.filterIsInstance<Compartment>() ?: emptyList() }) { typeArgument("Compartment") }
                propertyOfWithBinding<Node, Any?>(setOf(CMP, VAR, STR), "child", "List", accessor = { s: Node -> s.child }, mutator = { s: Node, v: Any? -> s.child = (v as? Collection<*>)?.filterIsInstance<Node>() ?: emptyList() }) { typeArgument("Node") }
                propertyOfWithBinding<Node, Any?>(setOf(CMP, VAR, STR), "port", "List", accessor = { s: Node -> s.port }, mutator = { s: Node, v: Any? -> s.port = (v as? Collection<*>)?.filterIsInstance<Node>() ?: emptyList() }) { typeArgument("Node") }
            }
            data("Compartment", implementation = Compartment::class) {
                constructor_ { execution { Compartment() } }
                propertyOfWithBinding<Compartment, Any?>(setOf(CMP, VAR, STR), "name", "String", accessor = { s: Compartment -> s.name }, mutator = { s: Compartment, v: Any? -> s.name = (v as? String) ?: "" })
                propertyOfWithBinding<Compartment, Any?>(setOf(CMP, VAR, STR), "item", "List", accessor = { s: Compartment -> s.item }, mutator = { s: Compartment, v: Any? -> s.item = (v as? Collection<*>)?.filterIsInstance<String>() ?: emptyList() }) { typeArgument("String") }
            }
            data("Edge", implementation = Edge::class) {
                supertypes("DiagramElement")
                constructor_ { execution { Edge() } }
                propertyOfWithBinding<Edge, Any?>(setOf(CMP, VAR, STR), "sourceId", "String", accessor = { s: Edge -> s.sourceId }, mutator = { s: Edge, v: Any? -> s.sourceId = (v as? String) ?: "" })
                propertyOfWithBinding<Edge, Any?>(setOf(CMP, VAR, STR), "targetId", "String", accessor = { s: Edge -> s.targetId }, mutator = { s: Edge, v: Any? -> s.targetId = (v as? String) ?: "" })
                propertyOfWithBinding<Edge, Any?>(setOf(CMP, VAR, STR), "sourceEnd", "EdgeEnd", accessor = { s: Edge -> s.sourceEnd }, mutator = { s: Edge, v: Any? -> if (v is EdgeEnd) s.sourceEnd = v })
                propertyOfWithBinding<Edge, Any?>(setOf(CMP, VAR, STR), "targetEnd", "EdgeEnd", accessor = { s: Edge -> s.targetEnd }, mutator = { s: Edge, v: Any? -> if (v is EdgeEnd) s.targetEnd = v })
            }
            data("EdgeEnd", implementation = EdgeEnd::class) {
                constructor_ { execution { EdgeEnd() } }
                propertyOfWithBinding<EdgeEnd, Any?>(setOf(CMP, VAR, STR), "decoration", "String", accessor = { s: EdgeEnd -> s.decoration }, mutator = { s: EdgeEnd, v: Any? -> s.decoration = (v as? String) ?: "" })
                propertyOfWithBinding<EdgeEnd, Any?>(setOf(CMP, VAR, STR), "label", "List", accessor = { s: EdgeEnd -> s.label }, mutator = { s: EdgeEnd, v: Any? -> s.label = (v as? Collection<*>)?.filterIsInstance<String>() ?: emptyList() }) { typeArgument("String") }
            }
        }
    }
}

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

/**
 * A generic diagram: nodes (some of which contain other nodes) and edges between nodes.
 * It knows nothing about the modelling language. Each element names the shape that draws it,
 * and carries the texts and options that shape needs. A [ShapeRegistry] turns shape names into drawings.
 *
 * The classes are deliberately simple and mutable, so that an agl-m2m transform can create and fill them
 * through [DiagramTypesDomain].
 */
class Diagram {
    /** Shown as the diagram's title. */
    var name: String = ""

    /** The kind of diagram, e.g. 'ClassDiagram'. Used to choose how to lay it out. */
    var kind: String = ""

    /** The top level nodes. Nested nodes are in [Node.child]. */
    var node: List<Node> = emptyList()

    /** All edges of the diagram, whatever nodes they connect. */
    var edge: List<Edge> = emptyList()

    /** All nodes, including nested nodes and ports. */
    val allNode: List<Node> get() = node.flatMap { it.allNode }
}

/**
 * Something that is drawn and shows (part of) a model element.
 */
abstract class DiagramElement {
    /** Unique within the diagram; also used as the layout id. */
    var id: String = ""

    /** Name of the shape that draws this element, looked up in a [ShapeRegistry]. */
    var shape: String = ""

    /** The main text, usually the element's name. */
    var label: String = ""

    /** Extra texts shown with the label, e.g. keywords and stereotypes. */
    var keyword: List<String> = emptyList()

    /** Shape specific on/off options, e.g. 'abstract', 'dashed'. */
    var option: List<String> = emptyList()

    /** The model element that is shown, if any. */
    var modelElement: Any? = null
}

/**
 * A node: a box-like shape. A node with [child] nodes is a container. [port] nodes sit on its border.
 */
class Node : DiagramElement() {
    /** Named lists of texts, e.g. the attributes and operations of a class. */
    var compartment: List<Compartment> = emptyList()

    /** Contained nodes. */
    var child: List<Node> = emptyList()

    /** Nodes on the border of this node. */
    var port: List<Node> = emptyList()

    /** This node and all nodes nested in it, including ports. */
    val allNode: List<Node> get() = listOf(this) + port.flatMap { it.allNode } + child.flatMap { it.allNode }
}

/** A named list of texts in a node. */
class Compartment {
    var name: String = ""
    var item: List<String> = emptyList()
}

/**
 * An edge: a line from the node [sourceId] to the node [targetId], with optional decorations and texts at each end.
 */
class Edge : DiagramElement() {
    /** [Node.id] of the source node (or port). */
    var sourceId: String = ""

    /** [Node.id] of the target node (or port). */
    var targetId: String = ""
    var sourceEnd: EdgeEnd = EdgeEnd()
    var targetEnd: EdgeEnd = EdgeEnd()
}

/** What is drawn at one end of an edge. */
class EdgeEnd {
    /** Name of the symbol at this end, e.g. 'openArrow', 'filledDiamond'; empty for none. */
    var decoration: String = ""

    /** Texts near this end, e.g. role name and multiplicity. */
    var label: List<String> = emptyList()
}

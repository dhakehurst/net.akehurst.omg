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

import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundEdge
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundGraph
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundGraphState
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundNode
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundPort
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Draws diagram elements: maps the [DiagramElement.shape] name to the Compose content for a node,
 * or the line style of an edge.
 */
interface ShapeRegistry {

    /**
     * The content of a node. For a container, [children] draws the contained nodes and must be placed by the content.
     */
    fun nodeContent(node: Node): @Composable (children: @Composable () -> Unit) -> Unit

    /** The line style, end symbols and texts of an edge. */
    fun edgeContent(edge: Edge): GraphLayoutEdgeContent
}

/**
 * Converts a [Diagram] into the state shown by layout-graph's CompoundGraphLayoutView.
 * Nodes with children become nested graphs; ports become border nodes;
 * each edge is placed in the innermost graph that contains both of its ends.
 */
fun Diagram.toCompoundGraphState(shapes: ShapeRegistry, stateId: String = name): GraphLayoutCompoundGraphState {
    val root = GraphLayoutCompoundGraph(id = "root")
    val graphOfNode = mutableMapOf<String, String>() // node (or port) id -> id of the graph that owns it
    val parentOfGraph = mutableMapOf<String, String?>(root.id to null)

    fun addNode(owner: GraphLayoutCompoundGraph, node: Node) {
        owner.nodes[node.id] = GraphLayoutCompoundNode(id = node.id, ports = node.port.map { it.toPort() })
        graphOfNode[node.id] = owner.id
        node.port.flatMap { it.allNode }.forEach { graphOfNode[it.id] = owner.id }
        if (node.child.isNotEmpty()) {
            val g = GraphLayoutCompoundGraph(id = node.id)
            owner.children[node.id] = g
            parentOfGraph[g.id] = owner.id
            node.child.forEach { addNode(g, it) }
        }
    }
    node.forEach { addNode(root, it) }

    val graphById = mutableMapOf<String, GraphLayoutCompoundGraph>()
    fun index(g: GraphLayoutCompoundGraph) {
        graphById[g.id] = g; g.children.values.forEach { index(it) }
    }
    index(root)

    edge.forEach { e ->
        val s = graphOfNode[e.sourceId] ?: error("Edge '${e.id}': no node with id '${e.sourceId}'")
        val t = graphOfNode[e.targetId] ?: error("Edge '${e.id}': no node with id '${e.targetId}'")
        val owner = lowestCommonGraph(s, t, parentOfGraph, root.id)
        graphById.getValue(owner).edges[e.id] = GraphLayoutCompoundEdge(id = e.id, sourceId = e.sourceId, targetId = e.targetId)
    }

    return GraphLayoutCompoundGraphState(id = stateId, root = root).also { state ->
        allNode.forEach { n -> state.addNodeContent(n.id, shapes.nodeContent(n)) }
        edge.forEach { e -> state.addEdgeContent(e.id, shapes.edgeContent(e)) }
    }
}

private fun Node.toPort(): GraphLayoutCompoundPort = GraphLayoutCompoundPort(id = id, ports = port.map { it.toPort() })

private fun lowestCommonGraph(a: String, b: String, parentOf: Map<String, String?>, rootId: String): String {
    val ancestorsOfA = generateSequence(a) { parentOf[it] }.toSet()
    return generateSequence(b) { parentOf[it] }.firstOrNull { it in ancestorsOfA } ?: rootId
}

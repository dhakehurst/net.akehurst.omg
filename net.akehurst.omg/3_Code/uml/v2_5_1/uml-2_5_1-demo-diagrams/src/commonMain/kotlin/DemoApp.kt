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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.components.tree.TreeView
import net.akehurst.kotlin.compose.components.tree.TreeViewNode
import net.akehurst.kotlin.compose.components.tree.TreeViewStateHolder
import net.akehurst.kotlin.compose.layout.graph.CompoundGraphLayoutView
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundGraphState
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutViewState
import net.akehurst.kotlin.compose.layout.graph.collapsibleChildGraphs
import net.akehurst.kotlin.compose.layout.graph.toggleCollapsed
import net.akehurst.kotlin.compose.layout.ladder.LadderLayoutState
import net.akehurst.kotlin.compose.layout.ladder.LadderLayoutView
import net.akehurst.omg.diagram.Table
import net.akehurst.omg.diagram.TableModelView
import net.akehurst.omg.diagram.toCompoundGraphState
import net.akehurst.omg.diagram.toTreeViewNodes
import net.akehurst.omg.diagram.toLadderLayoutState
import net.akehurst.omg.uml.gui.compose.UmlLadderShapeRegistry
import net.akehurst.omg.uml.gui.compose.UmlShapeRegistry

/**
 * Shows one diagram per UML diagram type, plus a tree and a table. Each is made from an example UML model (XMI)
 * by an agl-m2m transform: [UmlDiagramRules], [UmlSequenceRules] for the sequence diagram, or [UmlTreeTableRules] for the tree and the table.
 * Diagrams are drawn with the shapes of [UmlShapeRegistry], the tree with the components TreeView and the table with its TableView.
 */
@Composable
fun DemoApp() {
    val examples = DemoDiagrams.examples
    var selected by remember { mutableStateOf(examples.first()) }
    val shown = remember(selected) { DemoDiagramView.make(selected) }
    // incremented to make the view lay out again after a container is collapsed or expanded
    var collapseVersion by remember(selected) { mutableStateOf(0) }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F6))) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .fillMaxSize()
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFD8D8D8))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("UML view", style = MaterialTheme.typography.titleMedium)
            examples.forEach { ex ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == ex, onClick = { selected = ex })
                    Text(ex.title)
                }
            }
            val state = shown.graph
            val containers = if (null != state && collapseVersion >= 0) state.collapsibleChildGraphs().sortedBy { it.id } else emptyList()
            if (containers.isNotEmpty()) {
                Text("Containers", style = MaterialTheme.typography.titleMedium)
                containers.forEach { g ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = g.isCollapsed, onCheckedChange = {
                            state!!.toggleCollapsed(g.id)
                            collapseVersion++
                        })
                        Text(g.id, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFD8D8D8))
                .padding(8.dp)
        ) {
            DiagramCanvas(selected.title, shown, collapseVersion)
        }
    }
}

@Composable
private fun DiagramCanvas(title: String, shown: DemoDiagramView, collapseVersion: Int) {
    var viewState by remember(shown) { mutableStateOf(GraphLayoutViewState()) }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        val graph = shown.graph
        val ladder = shown.ladder
        when {
            null != graph -> CompoundGraphLayoutView(
                state = graph,
                layoutKey = collapseVersion,
                viewState = viewState,
                updateView = { offset: Offset, zoom: Float -> viewState = GraphLayoutViewState(zoom = zoom, offset = offset) },
                modifier = Modifier.weight(1f).fillMaxSize()
            )

            null != ladder -> LadderLayoutView(
                state = ladder,
                viewState = viewState,
                updateView = { offset: Offset, zoom: Float -> viewState = GraphLayoutViewState(zoom = zoom, offset = offset) },
                modifier = Modifier.weight(1f).fillMaxSize()
            )

            null != shown.tree -> TreeView(stateHolder = shown.tree, modifier = Modifier.weight(1f).fillMaxSize())

            null != shown.table -> TableModelView(table = shown.table, modifier = Modifier.weight(1f))

            else -> {
                Text("The diagram could not be made:", style = MaterialTheme.typography.bodyMedium)
                shown.problems.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

/** The view state for one example (a layout-graph, ladder, tree or table), or the problems that stopped it being made. */
class DemoDiagramView(
    val graph: GraphLayoutCompoundGraphState? = null,
    val ladder: LadderLayoutState? = null,
    val tree: TreeViewStateHolder? = null,
    val table: Table? = null,
    val problems: List<String> = emptyList(),
) {
    val isMade: Boolean get() = null != graph || null != ladder || null != tree || null != table

    companion object {
        private val umlToDiagram by lazy { UmlToDiagram(DemoDiagrams.reader().factory) }

        fun make(example: DemoDiagrams.Example): DemoDiagramView = try {
            val read = DemoDiagrams.read(example)
            when (example.view) {
                DemoDiagrams.View.LADDER -> {
                    val res = umlToDiagram.transform(UmlSequenceRules.transform, listOf(read.element))
                    when (val l = res.ladders.firstOrNull()) {
                        null -> DemoDiagramView(problems = read.issues + res.issues.errors.map { it.toString() })
                        else -> DemoDiagramView(ladder = l.toLadderLayoutState(UmlLadderShapeRegistry))
                    }
                }

                DemoDiagrams.View.TREE -> {
                    val res = umlToDiagram.transform(UmlTreeTableRules.containmentTree, listOf(read.element))
                    when (val t = res.trees.firstOrNull()) {
                        null -> DemoDiagramView(problems = read.issues + res.issues.errors.map { it.toString() })
                        else -> DemoDiagramView(tree = TreeViewStateHolder().also { h ->
                            val nodes = t.toTreeViewNodes()
                            nodes.forEach { expand(it) }
                            h.updateItems(nodes)
                        })
                    }
                }

                DemoDiagrams.View.TABLE -> {
                    val res = umlToDiagram.transform(UmlTreeTableRules.attributeTable, listOf(read.element))
                    when (val t = res.tables.firstOrNull()) {
                        null -> DemoDiagramView(problems = read.issues + res.issues.errors.map { it.toString() })
                        else -> DemoDiagramView(table = t)
                    }
                }

                else -> {
                    val res = umlToDiagram.transform(UmlDiagramRules.transform, listOf(read.element))
                    when (val d = res.diagrams.firstOrNull()) {
                        null -> DemoDiagramView(problems = read.issues + res.issues.errors.map { it.toString() })
                        else -> DemoDiagramView(graph = d.toCompoundGraphState(UmlShapeRegistry))
                    }
                }
            }
        } catch (t: Throwable) {
            DemoDiagramView(problems = listOf(t.toString()))
        }

        /** the demo tree starts fully open */
        private fun expand(node: TreeViewNode) {
            node.expanded = node.hasChildren
            node.children.value.forEach { expand(it) }
        }
    }
}

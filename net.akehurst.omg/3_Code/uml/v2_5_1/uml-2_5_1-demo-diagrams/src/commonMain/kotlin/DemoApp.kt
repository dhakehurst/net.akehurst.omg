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
import net.akehurst.kotlin.compose.layout.graph.CompoundGraphLayoutView
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutCompoundGraphState
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutViewState
import net.akehurst.kotlin.compose.layout.graph.collapsibleChildGraphs
import net.akehurst.kotlin.compose.layout.graph.toggleCollapsed
import net.akehurst.omg.diagram.toCompoundGraphState
import net.akehurst.omg.uml.gui.compose.UmlShapeRegistry

/**
 * Shows one diagram per UML diagram type. Each diagram is made from an example UML model (XMI)
 * by the agl-m2m transform [UmlDiagramRules], drawn with the shapes of [UmlShapeRegistry].
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
            Text("UML diagram", style = MaterialTheme.typography.titleMedium)
            examples.forEach { ex ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == ex, onClick = { selected = ex })
                    Text(ex.title)
                }
            }
            val state = shown.state
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
        Text("$title diagram", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
        val state = shown.state
        if (null == state) {
            Text("The diagram could not be made:", style = MaterialTheme.typography.bodyMedium)
            shown.problems.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        } else {
            CompoundGraphLayoutView(
                state = state,
                layoutKey = collapseVersion,
                viewState = viewState,
                updateView = { offset: Offset, zoom: Float -> viewState = GraphLayoutViewState(zoom = zoom, offset = offset) },
                modifier = Modifier.weight(1f).fillMaxSize()
            )
        }
    }
}

/** The layout-graph state for one example, or the problems that stopped it being made. */
class DemoDiagramView(val state: GraphLayoutCompoundGraphState?, val problems: List<String>) {
    companion object {
        private val umlToDiagram by lazy { UmlToDiagram(DemoDiagrams.reader().factory) }

        fun make(example: DemoDiagrams.Example): DemoDiagramView = try {
            val read = DemoDiagrams.read(example)
            val res = umlToDiagram.transform(UmlDiagramRules.transform, listOf(read.element))
            val d = res.diagrams.firstOrNull()
            when (d) {
                null -> DemoDiagramView(null, read.issues + res.issues.errors.map { it.toString() })
                else -> DemoDiagramView(d.toCompoundGraphState(UmlShapeRegistry), emptyList())
            }
        } catch (t: Throwable) {
            DemoDiagramView(null, listOf(t.toString()))
        }
    }
}

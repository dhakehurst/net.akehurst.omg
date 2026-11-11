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


package net.akehurst.omg.uml.gui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML component diagrams (UML 2.5.1, 11.6 Components).
 * Ports and connectors are in [CompositeStructureDiagram].
 */
object ComponentDiagram : StructureDiagram {

    /**
     * A Component is shown as a Classifier rectangle with the keyword «component»
     * and the component icon in the upper right corner.
     * Its realizing classifiers or parts can be shown inside it ([children]).
     */
    @Composable
    fun ComponentView(
        name: String,
        compartments: List<List<String>> = emptyList(),
        stereotypes: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = Column(modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.align(Alignment.Center).padding(end = 20.dp)) {
                NameCompartment(name, listOf("component") + stereotypes, stroke)
            }
            ComponentIcon(stroke, fill, Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp))
        }
        compartments.forEach { ListCompartment(it, stroke) }
        if (children != null) ChildrenCompartment(children)
    }

    /**
     * A provided interface shown as a “ball” (lollipop). The node is meant to be placed beside, or
     * on the border of, the component or port that provides it, and connected to it by a plain line.
     */
    @Composable
    fun ProvidedInterfaceView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(14.dp)) {
                val sw = UmlStyle.strokeWidth.toPx()
                drawCircle(fill, radius = size.minDimension / 2f - sw)
                drawCircle(stroke, radius = size.minDimension / 2f - sw, style = Stroke(sw))
            }
            UmlText(name, stroke = stroke)
        }

    /**
     * A required interface shown as a “socket” (half circle), opening to the right, towards the provider.
     */
    @Composable
    fun RequiredInterfaceView(name: String, stroke: Color = UmlStyle.stroke) =
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(18.dp)) {
                val sw = UmlStyle.strokeWidth.toPx()
                val d = size.minDimension - 2 * sw
                drawArc(stroke, startAngle = 90f, sweepAngle = 180f, useCenter = false, topLeft = Offset(sw + d / 4f, sw), size = Size(d, d), style = Stroke(sw))
            }
            UmlText(name, stroke = stroke)
        }

    /** The plain solid line connecting a component (or port) to a ball or socket. */
    fun InterfaceConnectionEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = umlEdge(stroke = stroke)

    /** A ComponentRealization is a realization: a dashed line with a hollow triangle at the component. */
    fun ComponentRealizationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = ClassDiagram.RealizationEdge(stroke)

    /** An InterfaceRealization is a realization: a dashed line with a hollow triangle at the interface. */
    fun InterfaceRealizationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = ClassDiagram.RealizationEdge(stroke)

    /** A required interface shown as a «use» dependency to the interface. */
    fun UsageEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = ClassDiagram.UsageEdge(stroke)

    /** An assembly connector, see [CompositeStructureDiagram.AssemblyConnectorEdge]. */
    fun AssemblyConnectorEdge(name: String = "", stroke: Color = UmlStyle.stroke) = CompositeStructureDiagram.AssemblyConnectorEdge(name, stroke)

    /** A delegation connector, see [CompositeStructureDiagram.DelegationConnectorEdge]. */
    fun DelegationConnectorEdge(stroke: Color = UmlStyle.stroke) = CompositeStructureDiagram.DelegationConnectorEdge(stroke)

    /** The component icon: a rectangle with two small rectangles on its left side. */
    @Composable
    fun ComponentIcon(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill, modifier: Modifier = Modifier) =
        Canvas(modifier.size(width = 14.dp, height = 12.dp)) {
            val sw = 1.dp.toPx()
            val tab = Size(size.width * 0.45f, size.height * 0.22f)
            drawRect(fill, topLeft = Offset(size.width * 0.22f, 0f), size = Size(size.width * 0.78f, size.height))
            drawRect(stroke, topLeft = Offset(size.width * 0.22f, 0f), size = Size(size.width * 0.78f, size.height), style = Stroke(sw))
            listOf(0.2f, 0.58f).forEach { y ->
                drawRect(fill, topLeft = Offset(0f, size.height * y), size = tab)
                drawRect(stroke, topLeft = Offset(0f, size.height * y), size = tab, style = Stroke(sw))
            }
        }
}

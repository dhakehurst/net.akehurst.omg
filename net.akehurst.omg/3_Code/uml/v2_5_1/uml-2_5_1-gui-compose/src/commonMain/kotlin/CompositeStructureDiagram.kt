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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML composite structure diagrams (UML 2.5.1, 11.2 to 11.7).
 */
object CompositeStructureDiagram : StructureDiagram {

    /**
     * A StructuredClassifier showing its internal structure: the name compartment above an
     * internal structure compartment that holds its parts ([children]).
     */
    @Composable
    fun StructuredClassifierView(
        name: String,
        keywords: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: @Composable () -> Unit,
    ) = Column(modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)) {
        NameCompartment(name, keywords, stroke)
        ListCompartment(emptyList(), stroke)
        ChildrenCompartment(children)
    }

    /**
     * A part (a composite Property) is shown as a rectangle with “name : Type [multiplicity]”.
     * A non-composite property (a reference) is shown with a dashed outline.
     * A part can itself show internal structure ([children]).
     */
    @Composable
    fun PartView(
        name: String,
        type: String = "",
        multiplicity: String = "",
        isComposite: Boolean = true,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = Column(
        modifier = Modifier.umlNode().then(
            if (isComposite) Modifier.umlOutline(RectangleShape, stroke, fill) else Modifier.dashedOutline(stroke, fill, ellipse = false)
        )
    ) {
        val label = buildString {
            append(name)
            if (type.isNotEmpty()) append(" : ").append(type)
            if (multiplicity.isNotEmpty()) append(" [").append(multiplicity).append("]")
        }
        NameCompartment(label, emptyList(), stroke, bold = false)
        if (children != null) ChildrenCompartment(children)
    }

    /**
     * A Port is shown as a small square, normally placed on the border of its classifier or part.
     * Its name and type are shown next to it. A behavior port is shown with [isBehavior].
     */
    @Composable
    fun PortView(
        name: String = "",
        type: String = "",
        isBehavior: Boolean = false,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Box(
        modifier = Modifier
            .umlOutline(RectangleShape, stroke, fill)
            .padding(horizontal = 3.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        val label = listOf(name, type).filter { it.isNotEmpty() }.joinToString(" : ")
        UmlText(if (isBehavior) "$label ${guillemets("behavior")}".trim() else label.ifEmpty { " " }, stroke = stroke)
    }

    /** A Connector is a solid line between connectable elements (parts or ports), with optional name and end labels. */
    fun ConnectorEdge(
        name: String = "",
        sourceEnd: String = "",
        targetEnd: String = "",
        stroke: Color = UmlStyle.stroke,
    ): GraphLayoutEdgeContent = umlEdge(startText = listOf(sourceEnd), middleText = listOf(name), endText = listOf(targetEnd), stroke = stroke)

    /** An assembly connector: a connector between a required and a provided interface. */
    fun AssemblyConnectorEdge(name: String = "", stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(middleText = listOf(name), stroke = stroke)

    /** A delegation connector: from a port to the part that realizes it, drawn with an open arrow and «delegate». */
    fun DelegationConnectorEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.openArrow(stroke), middleText = listOf(guillemets("delegate")), stroke = stroke)

    /** A Collaboration is shown as a dashed ellipse containing its name and its roles ([children]). */
    @Composable
    fun CollaborationView(
        name: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = Column(
        modifier = Modifier.umlNode().dashedOutline(stroke, fill, ellipse = true).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NameCompartment(name, emptyList(), stroke)
        if (children != null) ChildrenCompartment(children)
    }

    /** A CollaborationUse is shown as a dashed ellipse containing “name : Collaboration”. */
    @Composable
    fun CollaborationUseView(
        name: String,
        collaboration: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Box(
        modifier = Modifier.umlNode().dashedOutline(stroke, fill, ellipse = true).padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        UmlText("$name : $collaboration", stroke = stroke)
    }

    /** A role binding is a dashed line from a CollaborationUse to the element bound to a role, labelled with the role name. */
    fun RoleBindingEdge(role: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(dashed = true, endText = listOf(role), stroke = stroke)

    /** Outline drawn with a dashed line, as a rectangle or an ellipse. */
    fun Modifier.dashedOutline(stroke: Color, fill: Color, ellipse: Boolean) = this.drawBehind {
        val sw = UmlStyle.strokeWidth.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
        val inset = sw / 2f
        val tl = Offset(inset, inset)
        val sz = Size(size.width - sw, size.height - sw)
        if (ellipse) {
            drawOval(fill, tl, sz)
            drawOval(stroke, tl, sz, style = Stroke(sw, pathEffect = dash))
        } else {
            drawRect(fill, tl, sz)
            drawRect(stroke, tl, sz, style = Stroke(sw, pathEffect = dash))
        }
    }
}

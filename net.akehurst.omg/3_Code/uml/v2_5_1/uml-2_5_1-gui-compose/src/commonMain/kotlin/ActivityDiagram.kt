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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML activity diagrams (UML 2.5.1, clauses 15 and 16).
 */
object ActivityDiagram : BehaviorDiagram {

    private val actionCorner = RoundedCornerShape(10.dp)
    private val activityCorner = RoundedCornerShape(16.dp)

    /**
     * An Activity is shown as a large round-cornered rectangle with its name at the top left,
     * and its nodes and edges inside ([children]).
     */
    @Composable
    fun ActivityView(
        name: String,
        parameters: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: @Composable () -> Unit,
    ) = Column(modifier = Modifier.umlNode().umlOutline(activityCorner, stroke, fill).padding(6.dp)) {
        UmlText(name, Modifier.align(Alignment.Start), stroke, bold = true)
        parameters.forEach { UmlText(it, Modifier.align(Alignment.Start), stroke) }
        ChildrenCompartment(children)
    }

    /** An Action is shown as a round-cornered rectangle containing its name or body. */
    @Composable
    fun ActionView(name: String, keywords: List<String> = emptyList(), stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        Column(
            modifier = Modifier.umlNode().umlOutline(actionCorner, stroke, fill).padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NameCompartment(name, keywords, stroke, bold = false)
        }

    /** A CallBehaviorAction is an Action with a rake symbol in the lower right corner. */
    @Composable
    fun CallBehaviorActionView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        Box(modifier = Modifier.umlNode().umlOutline(actionCorner, stroke, fill)) {
            UmlText(name, Modifier.align(Alignment.Center).padding(start = 8.dp, end = 20.dp, top = 4.dp, bottom = 4.dp), stroke)
            Canvas(Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 4.dp).size(10.dp)) {
                val sw = 1.dp.toPx()
                val w = size.width
                val h = size.height
                drawLine(stroke, Offset(w / 2f, 0f), Offset(w / 2f, h), sw)
                drawLine(stroke, Offset(0f, h / 2f), Offset(w, h / 2f), sw)
                drawLine(stroke, Offset(0f, h / 2f), Offset(0f, h), sw)
                drawLine(stroke, Offset(w, h / 2f), Offset(w, h), sw)
            }
        }

    /** A SendSignalAction is shown as a convex pentagon (an arrow pointing right) containing the signal name. */
    @Composable
    fun SendSignalActionView(signal: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = Box(
        modifier = Modifier.umlNode().umlOutline(sendShape, stroke, fill).padding(start = 8.dp, end = 18.dp, top = 6.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) { UmlText(signal, stroke = stroke) }

    /** An AcceptEventAction is shown as a concave pentagon (a notch on the left) containing the event name. */
    @Composable
    fun AcceptEventActionView(event: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = Box(
        modifier = Modifier.umlNode().umlOutline(acceptShape, stroke, fill).padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) { UmlText(event, stroke = stroke) }

    /** An InitialNode is shown as a filled circle. */
    @Composable
    fun InitialNodeView(stroke: Color = UmlStyle.stroke) = Canvas(Modifier.size(16.dp)) {
        drawCircle(stroke, radius = size.minDimension / 2f)
    }

    /** An ActivityFinalNode is shown as a circle with a filled circle inside it (a bull's eye). */
    @Composable
    fun ActivityFinalNodeView(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = Canvas(Modifier.size(18.dp)) {
        val sw = UmlStyle.strokeWidth.toPx()
        val r = size.minDimension / 2f - sw / 2f
        drawCircle(fill, radius = r)
        drawCircle(stroke, radius = r, style = Stroke(sw))
        drawCircle(stroke, radius = r * 0.6f)
    }

    /** A FlowFinalNode is shown as a circle with a cross inside it. */
    @Composable
    fun FlowFinalNodeView(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = Canvas(Modifier.size(18.dp)) {
        val sw = UmlStyle.strokeWidth.toPx()
        val r = size.minDimension / 2f - sw / 2f
        val c = center
        val d = r * 0.7071f
        drawCircle(fill, radius = r)
        drawCircle(stroke, radius = r, style = Stroke(sw))
        drawLine(stroke, Offset(c.x - d, c.y - d), Offset(c.x + d, c.y + d), sw)
        drawLine(stroke, Offset(c.x - d, c.y + d), Offset(c.x + d, c.y - d), sw)
    }

    /** A DecisionNode or MergeNode is shown as a diamond, with an optional «decisionInput» note text. */
    @Composable
    fun DecisionNodeView(decisionInput: String = "", stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = 26.dp, height = 20.dp).umlOutline(diamondShape, stroke, fill))
            if (decisionInput.isNotEmpty()) UmlText("${guillemets("decisionInput")} $decisionInput", stroke = stroke)
        }

    /** A MergeNode is shown as a diamond. */
    @Composable
    fun MergeNodeView(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = DecisionNodeView("", stroke, fill)

    /** A ForkNode or JoinNode is shown as a thick bar; [horizontal] chooses its orientation. */
    @Composable
    fun ForkNodeView(horizontal: Boolean = true, stroke: Color = UmlStyle.stroke) = Canvas(
        if (horizontal) Modifier.size(width = 60.dp, height = 5.dp) else Modifier.size(width = 5.dp, height = 60.dp)
    ) {
        drawRect(stroke)
    }

    /** A JoinNode is shown as a thick bar, with an optional {joinSpec = ...}. */
    @Composable
    fun JoinNodeView(horizontal: Boolean = true, joinSpec: String = "", stroke: Color = UmlStyle.stroke) =
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ForkNodeView(horizontal, stroke)
            if (joinSpec.isNotEmpty()) UmlText("{joinSpec = $joinSpec}", stroke = stroke)
        }

    /** An ObjectNode is shown as a rectangle with “name : Type” and an optional [state] in brackets. */
    @Composable
    fun ObjectNodeView(
        name: String,
        type: String = "",
        state: String = "",
        keywords: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Column(
        modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill).padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val label = if (type.isEmpty()) name else "$name : $type"
        NameCompartment(label, keywords, stroke, bold = false)
        if (state.isNotEmpty()) UmlText("[$state]", stroke = stroke)
    }

    /** A CentralBufferNode is an ObjectNode with the keyword «centralBuffer». */
    @Composable
    fun CentralBufferNodeView(name: String, type: String = "", stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ObjectNodeView(name, type, keywords = listOf("centralBuffer"), stroke = stroke, fill = fill)

    /** A DataStoreNode is an ObjectNode with the keyword «datastore». */
    @Composable
    fun DataStoreNodeView(name: String, type: String = "", stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ObjectNodeView(name, type, keywords = listOf("datastore"), stroke = stroke, fill = fill)

    /**
     * An ActivityParameterNode is an ObjectNode shown on the border of its Activity
     * (place it as a port of the activity node).
     */
    @Composable
    fun ActivityParameterNode(name: String, type: String = "", stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ObjectNodeView(name, type, stroke = stroke, fill = fill)

    /** A Pin is a small rectangle on the border of its Action, with its name next to it. */
    @Composable
    fun PinView(name: String = "", stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(10.dp).umlOutline(RectangleShape, stroke, fill))
            if (name.isNotEmpty()) UmlText(name, stroke = stroke)
        }

    /**
     * An ActivityPartition (swimlane) is shown as a rectangle with its name in a header
     * and the nodes in the partition inside it ([children]).
     */
    @Composable
    fun ActivityPartitionView(
        name: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: @Composable () -> Unit,
    ) = Column(modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)) {
        UmlText(name, Modifier.fillMaxWidth().padding(vertical = 2.dp), stroke, bold = true)
        HorizontalDivider(thickness = UmlStyle.strokeWidth, color = stroke)
        ChildrenCompartment(children)
    }

    /** A ControlFlow is a solid line with an open arrow at the target, with an optional [guard] and [weight]. */
    fun ControlFlowEdge(guard: String = "", weight: String = "", name: String = "", stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        flowEdge(guard, weight, name, stroke)

    /** An ObjectFlow is drawn like a ControlFlow; it connects object nodes or pins. */
    fun ObjectFlowEdge(guard: String = "", weight: String = "", name: String = "", stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        flowEdge(guard, weight, name, stroke)

    /** An ExceptionHandler is shown as a lightning-bolt line; here a solid line with an arrow and the exception type. */
    fun ExceptionHandlerEdge(exceptionType: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.openArrow(stroke), middleText = listOf("↯ $exceptionType"), stroke = stroke)

    @Composable
    fun CompositionView() {
    }

    private fun flowEdge(guard: String, weight: String, name: String, stroke: Color) = umlEdge(
        end = UmlEdgeSymbols.openArrow(stroke),
        middleText = listOf(name, if (guard.isEmpty()) "" else "[$guard]", if (weight.isEmpty()) "" else "{weight = $weight}"),
        stroke = stroke,
    )

    private val diamondShape = GenericShape { size, _ ->
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height / 2f)
        lineTo(size.width / 2f, size.height)
        lineTo(0f, size.height / 2f)
        close()
    }

    private val sendShape = GenericShape { size, _ ->
        val p = size.height / 2f
        moveTo(0f, 0f)
        lineTo(size.width - p, 0f)
        lineTo(size.width, size.height / 2f)
        lineTo(size.width - p, size.height)
        lineTo(0f, size.height)
        close()
    }

    private val acceptShape = GenericShape { size, _ ->
        val p = size.height / 2f
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        lineTo(p, size.height / 2f)
        close()
    }
}

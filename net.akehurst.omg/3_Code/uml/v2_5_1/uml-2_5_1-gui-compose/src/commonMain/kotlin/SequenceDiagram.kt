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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML sequence diagrams (UML 2.5.1, 17.8 Interaction Diagrams), drawn on the ladder layout:
 * lifeline headers, message lines, and the labels of combined fragments.
 */
object SequenceDiagram : BehaviorDiagram {

    /** The head of a lifeline: a rectangle with “name : Type”. The dashed line below it is drawn by the layout. */
    @Composable
    fun LifelineHeaderView(
        label: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Box(modifier = Modifier.umlOutline(RectangleShape, stroke, fill).padding(horizontal = 8.dp, vertical = 4.dp)) {
        UmlText(label, stroke = stroke)
    }

    /** A synchronous call: solid line, filled arrow head. */
    fun SynchCallMessage(name: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.filledArrow(stroke), middleText = listOf(name), stroke = stroke)

    /** An asynchronous call or signal: solid line, open arrow head. */
    fun AsynchMessage(name: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.openArrow(stroke), middleText = listOf(name), stroke = stroke)

    /** A reply: dashed line, open arrow head. */
    fun ReplyMessage(name: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.openArrow(stroke), dashed = true, middleText = listOf(name), stroke = stroke)

    /** A create message: dashed line, open arrow head. */
    fun CreateMessage(name: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.openArrow(stroke), dashed = true, middleText = listOf(guillemets("create"), name), stroke = stroke)

    /** A delete message: solid line, filled arrow head. (The X at the end of the lifeline is not drawn yet.) */
    fun DeleteMessage(name: String, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.filledArrow(stroke), middleText = listOf(guillemets("destroy"), name), stroke = stroke)

    /** The interaction operator in the tab of a combined fragment, e.g. “alt” or “loop”. */
    @Composable
    fun CombinedFragmentLabel(operator: String, stroke: Color = UmlStyle.stroke) =
        UmlText(operator, stroke = stroke, bold = true)

    /** The guard of an interaction operand, “[condition]”. */
    @Composable
    fun OperandGuard(guard: String, stroke: Color = UmlStyle.stroke) =
        UmlText("[$guard]", stroke = stroke)
}

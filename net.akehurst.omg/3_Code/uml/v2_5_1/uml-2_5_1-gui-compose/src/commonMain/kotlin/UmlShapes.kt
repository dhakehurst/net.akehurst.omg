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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.EdgeContentPosition
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeSymbol
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeText

/**
 * Default colours and sizes shared by all UML shapes.
 */
object UmlStyle {
    val stroke = Color.Black
    val fill = Color.White
    val strokeWidth: Dp = 1.5.dp
    val padding: Dp = 4.dp

    /** Edge line dash pattern for dashed UML lines (dependencies, realizations, ...), in dp. */
    val dashed = listOf(6f, 4f)
}

/** «keyword» text, as used for keywords and stereotypes. */
fun guillemets(keyword: String) = "«$keyword»"

/**
 * A single line of UML text inside a shape.
 */
@Composable
fun UmlText(
    text: String,
    modifier: Modifier = Modifier,
    stroke: Color = UmlStyle.stroke,
    bold: Boolean = false,
    italic: Boolean = false,
    underline: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
) = Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.labelSmall,
    color = stroke,
    fontWeight = if (bold) FontWeight.Bold else null,
    fontStyle = if (italic) FontStyle.Italic else null,
    textDecoration = if (underline) TextDecoration.Underline else null,
    textAlign = textAlign,
    softWrap = false,
)

/**
 * The name compartment of a classifier-like shape: keywords/stereotypes on their own lines above the name.
 */
@Composable
fun ColumnScope.NameCompartment(
    name: String,
    keywords: List<String>,
    stroke: Color,
    italic: Boolean = false,
    underline: Boolean = false,
    bold: Boolean = true,
) {
    keywords.forEach { kw ->
        UmlText(guillemets(kw), Modifier.align(Alignment.CenterHorizontally).padding(horizontal = UmlStyle.padding), stroke)
    }
    UmlText(
        name,
        Modifier.align(Alignment.CenterHorizontally).padding(horizontal = UmlStyle.padding, vertical = 2.dp),
        stroke, bold = bold, italic = italic, underline = underline
    )
}

/**
 * A list compartment (attributes, operations, literals, slots, ...) separated from what is above it by a line.
 */
@Composable
fun ColumnScope.ListCompartment(items: List<String>, stroke: Color) {
    HorizontalDivider(thickness = UmlStyle.strokeWidth, color = stroke)
    Column(Modifier.fillMaxWidth().padding(horizontal = UmlStyle.padding, vertical = 2.dp)) {
        items.forEach { item ->
            UmlText(item, Modifier.align(Alignment.Start), stroke, textAlign = TextAlign.Start)
        }
    }
}

/**
 * Box holding the contained (child) nodes of a container shape, below the rest of the shape.
 */
@Composable
fun ColumnScope.ChildrenCompartment(children: @Composable () -> Unit) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(UmlStyle.padding)
) {
    children()
}

/** A shape outline filled with [fill] and stroked with [stroke]. */
fun Modifier.umlOutline(shape: Shape, stroke: Color, fill: Color) =
    this.background(fill, shape).border(UmlStyle.strokeWidth, stroke, shape)

/** Fills the bounds the layout gives a node. */
fun Modifier.umlNode() = this.fillMaxSize()

/**
 * Symbols drawn at the ends of edges.
 *
 * Each symbol is given for the END of an edge: its tip is at (0,0), pointing in +X towards the node.
 * Use [atStart] to get the same symbol for the START of an edge.
 */
object UmlEdgeSymbols {

    /** Open (stick) arrow head: navigability, dependency, flow. */
    fun openArrow(stroke: Color = UmlStyle.stroke) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(-10f, 6f), Offset(0f, 0f), Offset(-10f, -6f)),
        isClosed = false,
        strokeColor = stroke,
    )

    /** Filled arrow head. */
    fun filledArrow(stroke: Color = UmlStyle.stroke) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(0f, 0f), Offset(-10f, 5f), Offset(-10f, -5f)),
        fillColor = stroke,
        strokeColor = stroke,
    )

    /** Hollow triangle: generalization, realization. */
    fun hollowTriangle(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(0f, 0f), Offset(-14f, 8f), Offset(-14f, -8f)),
        fillColor = fill,
        strokeColor = stroke,
    )

    /** Filled triangle: extension (profiles). */
    fun filledTriangle(stroke: Color = UmlStyle.stroke) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(0f, 0f), Offset(-14f, 8f), Offset(-14f, -8f)),
        fillColor = stroke,
        strokeColor = stroke,
    )

    /** Hollow diamond: shared aggregation, drawn at the end of the whole. */
    fun hollowDiamond(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(0f, 0f), Offset(-9f, 5f), Offset(-18f, 0f), Offset(-9f, -5f)),
        fillColor = fill,
        strokeColor = stroke,
    )

    /** Filled diamond: composite aggregation, drawn at the end of the whole. */
    fun filledDiamond(stroke: Color = UmlStyle.stroke) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(0f, 0f), Offset(-9f, 5f), Offset(-18f, 0f), Offset(-9f, -5f)),
        fillColor = stroke,
        strokeColor = stroke,
    )

    /** A small cross: explicitly non-navigable association end. */
    fun cross(stroke: Color = UmlStyle.stroke) = GraphLayoutEdgeSymbol(
        pathPoints = listOf(Offset(-14f, -5f), Offset(-4f, 5f), Offset(-9f, 0f), Offset(-14f, 5f), Offset(-4f, -5f)),
        isClosed = false,
        strokeColor = stroke,
    )

    /** Circle with a plus: namespace containment (owner end). */
    fun circledPlus(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill): GraphLayoutEdgeSymbol {
        val r = 6f
        val n = 16
        val circle = (0..n).map { i ->
            val a = (2.0 * kotlin.math.PI * i / n)
            Offset((-r + r * kotlin.math.cos(a)).toFloat(), (r * kotlin.math.sin(a)).toFloat())
        }
        // circle, then the plus drawn by going through the centre
        val plus = listOf(Offset(0f, 0f), Offset(-r, 0f), Offset(-2 * r, 0f), Offset(-r, 0f), Offset(-r, -r), Offset(-r, r))
        return GraphLayoutEdgeSymbol(
            pathPoints = circle + plus,
            isClosed = false,
            fillColor = fill,
            strokeColor = stroke,
        )
    }

    /** The same symbol, for the START of an edge (mirrored so that it points back at the source node). */
    fun GraphLayoutEdgeSymbol.atStart() = this.copy(pathPoints = pathPoints.map { Offset(-it.x, it.y) })
}

/**
 * Builds the visual content of an edge. [start] and [end] symbols are given in END form (see [UmlEdgeSymbols]);
 * the start one is mirrored here.
 */
fun umlEdge(
    start: GraphLayoutEdgeSymbol? = null,
    end: GraphLayoutEdgeSymbol? = null,
    dashed: Boolean = false,
    startText: List<String> = emptyList(),
    middleText: List<String> = emptyList(),
    endText: List<String> = emptyList(),
    stroke: Color = UmlStyle.stroke,
    underlineMiddle: Boolean = false,
): GraphLayoutEdgeContent {
    val texts = buildList {
        if (startText.isNotEmpty()) add(edgeText(EdgeContentPosition.START, startText, stroke, false))
        if (middleText.isNotEmpty()) add(edgeText(EdgeContentPosition.MIDDLE, middleText, stroke, underlineMiddle))
        if (endText.isNotEmpty()) add(edgeText(EdgeContentPosition.END, endText, stroke, false))
    }
    return GraphLayoutEdgeContent(
        startSymbol = start?.let { with(UmlEdgeSymbols) { it.atStart() } },
        endSymbol = end,
        texts = texts,
        lineColor = stroke,
        lineWidth = UmlStyle.strokeWidth.value,
        lineDash = if (dashed) UmlStyle.dashed else null,
    )
}

private fun edgeText(position: EdgeContentPosition, lines: List<String>, stroke: Color, underline: Boolean) =
    GraphLayoutEdgeText(position) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            lines.filter { it.isNotEmpty() }.forEach { UmlText(it, stroke = stroke, underline = underline) }
        }
    }

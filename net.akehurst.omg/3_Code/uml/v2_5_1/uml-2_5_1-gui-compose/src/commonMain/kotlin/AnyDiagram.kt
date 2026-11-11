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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes that can appear on any UML diagram (UML 2.5.1 clause 7, Common Structure).
 */
object AnyDiagram : StructureDiagram {

    /**
     * A Comment is shown as a rectangle with the upper right corner bent
     * (this is also known as a “note symbol”).
     * The rectangle contains the body of the Comment.
     */
    @Composable
    fun CommentView(contentText: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) = NoteView(contentText, stroke, fill)

    /**
     * A Constraint is shown as its text in braces, {text}, here inside a note symbol
     * (the form used when it is attached to elements by dashed lines).
     */
    @Composable
    fun ConstraintView(specification: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        NoteView("{$specification}", stroke, fill)

    /** The dashed line attaching a Comment or Constraint to the elements it annotates. */
    fun AnnotationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(dashed = true, stroke = stroke)

    /**
     * A Dependency is a dashed arrow from the client (source) to the supplier (target),
     * optionally labelled with a keyword and a name.
     */
    fun DependencyEdge(keyword: String? = null, name: String = "", stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(
            end = UmlEdgeSymbols.openArrow(stroke),
            dashed = true,
            middleText = listOfNotNull(keyword?.let { guillemets(it) }, name),
            stroke = stroke,
        )

    /** An Abstraction is a Dependency with the keyword «abstraction» or a more specific stereotype. */
    fun AbstractionEdge(keyword: String = "abstraction", stroke: Color = UmlStyle.stroke) = DependencyEdge(keyword, stroke = stroke)

    /**
     * Ownership of an element by a Namespace: a solid line with a circled plus at the owner (target) end.
     */
    fun ContainmentEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.circledPlus(stroke), stroke = stroke)

    @Composable
    private fun NoteView(contentText: String, stroke: Color, fill: Color) = Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val corner = 14.dp.toPx().coerceAtMost(size.minDimension / 2f)
                val strokeWidth = UmlStyle.strokeWidth.toPx()

                val notePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width - corner, 0f)
                    lineTo(size.width, corner)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                drawPath(path = notePath, color = fill, style = Fill)
                drawPath(path = notePath, color = stroke, style = Stroke(width = strokeWidth))

                // Draw the fold seam to make the bent corner explicit.
                drawLine(color = stroke, start = Offset(size.width - corner, 0f), end = Offset(size.width - corner, corner), strokeWidth = strokeWidth)
                drawLine(color = stroke, start = Offset(size.width - corner, corner), end = Offset(size.width, corner), strokeWidth = strokeWidth)
            }
    ) {
        UmlText(
            text = contentText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 8.dp, top = 8.dp, end = 24.dp, bottom = 8.dp),
            stroke = stroke,
            textAlign = TextAlign.Start,
        )
    }
}

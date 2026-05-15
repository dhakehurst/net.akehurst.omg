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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp

object AnyDiagram : StructureDiagram {

    @Composable
    fun DiagramView() {
    }

    /**
     * A Comment is shown as a rectangle with the upper right corner bent
     * (this is also known as a “note symbol”).
     * The rectangle contains the body of the Comment.
     */
    @Composable
    fun CommentView(contentText:String, stroke:Color, fill: Color) = Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val corner = 14.dp.toPx().coerceAtMost(size.minDimension / 2f)
                val strokeWidth = 1.5.dp.toPx()

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
                drawLine(
                    color = stroke,
                    start = androidx.compose.ui.geometry.Offset(size.width - corner, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width - corner, corner),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = stroke,
                    start = androidx.compose.ui.geometry.Offset(size.width - corner, corner),
                    end = androidx.compose.ui.geometry.Offset(size.width, corner),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Text(
            text = contentText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 8.dp, top = 8.dp, end = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = stroke
        )
    }


    @Composable
    fun DependencyView() {
    }

}
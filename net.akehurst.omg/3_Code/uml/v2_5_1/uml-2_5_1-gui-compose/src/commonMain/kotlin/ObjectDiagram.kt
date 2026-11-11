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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML object diagrams (UML 2.5.1, 9.8 Instances).
 */
object ObjectDiagram : StructureDiagram {

    /**
     * An InstanceSpecification is shown as a rectangle with its name and classifiers, “name : Type”, underlined,
     * and an optional compartment listing its slots, “feature = value”.
     */
    @Composable
    fun InstanceSpecificationView(
        name: String,
        classifiers: List<String> = emptyList(),
        slots: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Column(modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)) {
        NameCompartment(instanceName(name, classifiers), emptyList(), stroke, underline = true, bold = false)
        if (slots.isNotEmpty()) ListCompartment(slots, stroke)
    }

    /**
     * A link (an InstanceSpecification of an Association) is a solid line between the instances,
     * with its name underlined and optional end names.
     */
    fun LinkEdge(
        name: String = "",
        association: String = "",
        sourceEnd: String = "",
        targetEnd: String = "",
        stroke: Color = UmlStyle.stroke,
    ): GraphLayoutEdgeContent = umlEdge(
        startText = listOf(sourceEnd),
        middleText = listOf(instanceName(name, listOfNotNull(association.ifEmpty { null }))),
        endText = listOf(targetEnd),
        stroke = stroke,
        underlineMiddle = true,
    )

    private fun instanceName(name: String, classifiers: List<String>) = when {
        classifiers.isEmpty() -> name
        else -> "$name : ${classifiers.joinToString(", ")}"
    }
}

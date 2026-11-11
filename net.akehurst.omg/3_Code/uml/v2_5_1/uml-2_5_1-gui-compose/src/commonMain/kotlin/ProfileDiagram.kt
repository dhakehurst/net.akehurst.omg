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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML profile diagrams (UML 2.5.1, 12.3 Profiles).
 */
object ProfileDiagram : StructureDiagram {

    /** A Profile is shown as a Package with the keyword «profile». */
    @Composable
    fun ProfileView(
        name: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = PackageDiagram.PackageView(name, listOf("profile"), stroke, fill, children)

    /** A Stereotype is shown as a Class with the keyword «stereotype». */
    @Composable
    fun StereotypeView(
        name: String,
        attributes: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = ClassDiagram.ClassifierView(name, listOf(attributes), stroke, fill, listOf("stereotype"))

    /** A referenced metaclass is shown as a Class with the keyword «metaclass». */
    @Composable
    fun MetaclassView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ClassDiagram.ClassifierView(name, emptyList(), stroke, fill, listOf("metaclass"))

    /**
     * An Extension is a solid line with a filled triangle at the extended metaclass (the target),
     * with {required} when the extension is required.
     */
    fun ExtensionEdge(isRequired: Boolean = false, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = umlEdge(
        end = UmlEdgeSymbols.filledTriangle(stroke),
        middleText = if (isRequired) listOf("{required}") else emptyList(),
        stroke = stroke,
    )

    /** A ProfileApplication is a dashed arrow from the applying package to the profile, «apply». */
    fun ProfileApplicationEdge(isStrict: Boolean = false, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = umlEdge(
        end = UmlEdgeSymbols.openArrow(stroke),
        dashed = true,
        middleText = listOf(guillemets("apply")) + if (isStrict) listOf("{strict}") else emptyList(),
        stroke = stroke,
    )

    /** A metaclass reference (ElementImport of a metaclass) is a dashed arrow with «reference». */
    fun MetaclassReferenceEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        AnyDiagram.DependencyEdge("reference", stroke = stroke)
}

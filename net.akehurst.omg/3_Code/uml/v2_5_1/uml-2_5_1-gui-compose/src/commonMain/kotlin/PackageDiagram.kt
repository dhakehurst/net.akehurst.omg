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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML package diagrams (UML 2.5.1, 12.2 Packages).
 */
object PackageDiagram : StructureDiagram {

    /**
     * A Package is shown as a large rectangle with a small rectangle (a “tab”) attached to its upper left.
     * When members are shown inside it ([children]), the name is placed in the tab;
     * otherwise ([children] null) the name is placed in the large rectangle.
     */
    @Composable
    fun PackageView(
        name: String,
        keywords: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = Column(modifier = Modifier.umlNode()) {
        Box(
            modifier = Modifier
                .background(fill)
                .border(UmlStyle.strokeWidth, stroke)
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            if (children != null) {
                Column { NameCompartment(name, keywords, stroke) }
            } else {
                UmlText(" ", stroke = stroke)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .umlOutline(RectangleShape, stroke, fill)
        ) {
            if (children != null) {
                ChildrenCompartment(children)
            } else {
                NameCompartment(name, keywords, stroke)
            }
        }
    }

    /** A Model is a Package shown with a small triangle in the name, here the keyword «model». */
    @Composable
    fun ModelView(
        name: String,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = PackageView(name, listOf("model"), stroke, fill, children)

    /** A PackageImport is a dashed arrow from the importing namespace to the imported package, «import» or «access». */
    fun PackageImportEdge(isPublic: Boolean = true, stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        AnyDiagram.DependencyEdge(if (isPublic) "import" else "access", stroke = stroke)

    /** An ElementImport is a dashed arrow to the imported element, «import» or «access», with an optional alias. */
    fun ElementImportEdge(isPublic: Boolean = true, alias: String = "", stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        AnyDiagram.DependencyEdge(if (isPublic) "import" else "access", alias, stroke)

    /** A PackageMerge is a dashed arrow from the receiving package to the merged package, «merge». */
    fun PackageMergeEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        AnyDiagram.DependencyEdge("merge", stroke = stroke)

    /** Package membership shown with a line and a circled plus at the owning package. */
    fun MembershipEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = AnyDiagram.ContainmentEdge(stroke)
}

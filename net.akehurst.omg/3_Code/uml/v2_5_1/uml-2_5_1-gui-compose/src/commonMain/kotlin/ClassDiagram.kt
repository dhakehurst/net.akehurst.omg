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
 * Shapes for UML class diagrams (UML 2.5.1 clauses 9, 10 and 11).
 */
object ClassDiagram : StructureDiagram {

    /** Kind of aggregation shown at one end of an association line (UML 2.5.1, 11.5.4). */
    enum class Aggregation { NONE, SHARED, COMPOSITE }

    /**
     * What is drawn at one end of an association line.
     * The diamond for [aggregation] is drawn at the end attached to the whole.
     * [navigable] true draws an open arrow, false a cross, null nothing (unspecified).
     */
    data class AssociationEnd(
        val roleName: String = "",
        val multiplicity: String = "",
        val aggregation: Aggregation = Aggregation.NONE,
        val navigable: Boolean? = null,
        /** Property modifiers such as {ordered}, {unique}, {readOnly}, {subsets x}, {redefines y}. */
        val modifiers: List<String> = emptyList(),
    )

    /**
     * A Classifier is shown as a solid-outline rectangle containing the classifier's name,
     * and with optional compartments separated by horizontal lines.
     * Abstract classifiers have their name in italics.
     */
    @Composable
    fun ClassifierView(
        titleBoxText: String,
        compartments: List<List<String>>,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        keywords: List<String> = emptyList(),
        isAbstract: Boolean = false,
    ) = Column(
        modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)
    ) {
        NameCompartment(titleBoxText, keywords, stroke, italic = isAbstract)
        compartments.forEach { ListCompartment(it, stroke) }
    }

    /** A Class with attribute and operation compartments. */
    @Composable
    fun ClassView(
        name: String,
        attributes: List<String> = emptyList(),
        operations: List<String> = emptyList(),
        isAbstract: Boolean = false,
        stereotypes: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = ClassifierView(name, listOf(attributes, operations), stroke, fill, stereotypes, isAbstract)

    /** An Interface: a classifier rectangle with the keyword «interface». */
    @Composable
    fun InterfaceView(
        name: String,
        attributes: List<String> = emptyList(),
        operations: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = ClassifierView(name, listOf(attributes, operations), stroke, fill, listOf("interface"))

    /** A DataType: a classifier rectangle with the keyword «dataType». */
    @Composable
    fun DataTypeView(
        name: String,
        attributes: List<String> = emptyList(),
        operations: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = ClassifierView(name, listOf(attributes, operations), stroke, fill, listOf("dataType"))

    /** A PrimitiveType: a classifier rectangle with the keyword «primitive». */
    @Composable
    fun PrimitiveTypeView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ClassifierView(name, emptyList(), stroke, fill, listOf("primitive"))

    /** An Enumeration: keyword «enumeration» and a compartment listing its literals. */
    @Composable
    fun EnumerationView(
        name: String,
        literals: List<String>,
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = ClassifierView(name, listOf(literals), stroke, fill, listOf("enumeration"))

    /**
     * A binary Association is a solid line between the two classifiers.
     * The name is shown near the middle and the end names and multiplicities near each end.
     * The source end is the first member end, the target end the second.
     */
    fun AssociationEdge(
        name: String = "",
        source: AssociationEnd = AssociationEnd(),
        target: AssociationEnd = AssociationEnd(),
        stroke: Color = UmlStyle.stroke,
    ): GraphLayoutEdgeContent = umlEdge(
        start = endSymbol(source, stroke),
        end = endSymbol(target, stroke),
        startText = endTexts(source),
        middleText = listOf(name),
        endText = endTexts(target),
        stroke = stroke,
    )

    /** A Generalization is a solid line with a hollow triangle at the general classifier (the target). */
    fun GeneralizationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.hollowTriangle(stroke), stroke = stroke)

    /** A Realization (or InterfaceRealization) is a dashed line with a hollow triangle at the realized element. */
    fun RealizationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent =
        umlEdge(end = UmlEdgeSymbols.hollowTriangle(stroke), dashed = true, stroke = stroke)

    /** A Dependency, see [AnyDiagram.DependencyEdge]. */
    fun DependencyEdge(keyword: String? = null, name: String = "", stroke: Color = UmlStyle.stroke) =
        AnyDiagram.DependencyEdge(keyword, name, stroke)

    /** A Usage is a Dependency with the keyword «use». */
    fun UsageEdge(stroke: Color = UmlStyle.stroke) = AnyDiagram.DependencyEdge("use", stroke = stroke)

    private fun endSymbol(end: AssociationEnd, stroke: Color) = when {
        end.aggregation == Aggregation.COMPOSITE -> UmlEdgeSymbols.filledDiamond(stroke)
        end.aggregation == Aggregation.SHARED -> UmlEdgeSymbols.hollowDiamond(stroke)
        end.navigable == true -> UmlEdgeSymbols.openArrow(stroke)
        end.navigable == false -> UmlEdgeSymbols.cross(stroke)
        else -> null
    }

    private fun endTexts(end: AssociationEnd) =
        listOf(end.roleName, end.multiplicity) + end.modifiers.map { "{$it}" }
}

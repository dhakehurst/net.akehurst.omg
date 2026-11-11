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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent

/**
 * Shapes for UML deployment diagrams (UML 2.5.1, clause 19).
 */
object DeploymentDiagram : StructureDiagram {

    private val depth = 8.dp

    /**
     * A Node is shown as a three-dimensional box (a cube) with its name inside.
     * Devices and execution environments are Nodes with the keywords «device» and «executionEnvironment».
     * Deployed artifacts and nested nodes can be shown inside it ([children]).
     */
    @Composable
    fun NodeView(
        name: String,
        keywords: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
        children: (@Composable () -> Unit)? = null,
    ) = Column(
        modifier = Modifier
            .umlNode()
            .drawBehind {
                val d = depth.toPx()
                val sw = UmlStyle.strokeWidth.toPx()
                val w = size.width
                val h = size.height
                // front face is the rectangle (0,d)-(w-d,h); the top and right faces go back by d
                val top = Path().apply { moveTo(0f, d); lineTo(d, 0f); lineTo(w, 0f); lineTo(w - d, d); close() }
                val side = Path().apply { moveTo(w - d, d); lineTo(w, 0f); lineTo(w, h - d); lineTo(w - d, h); close() }
                val front = Path().apply { moveTo(0f, d); lineTo(w - d, d); lineTo(w - d, h); lineTo(0f, h); close() }
                listOf(top, side, front).forEach {
                    drawPath(it, fill, style = Fill)
                    drawPath(it, stroke, style = Stroke(sw))
                }
            }
            .padding(top = depth, end = depth)
    ) {
        NameCompartment(name, keywords, stroke)
        if (children != null) ChildrenCompartment(children)
    }

    /** A Device is a Node with the keyword «device». */
    @Composable
    fun DeviceView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill, children: (@Composable () -> Unit)? = null) =
        NodeView(name, listOf("device"), stroke, fill, children)

    /** An ExecutionEnvironment is a Node with the keyword «executionEnvironment». */
    @Composable
    fun ExecutionEnvironmentView(name: String, stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill, children: (@Composable () -> Unit)? = null) =
        NodeView(name, listOf("executionEnvironment"), stroke, fill, children)

    /** An Artifact is shown as a classifier rectangle with the keyword «artifact» and a document icon in the upper right. */
    @Composable
    fun ArtifactView(
        name: String,
        keywords: List<String> = listOf("artifact"),
        properties: List<String> = emptyList(),
        stroke: Color = UmlStyle.stroke,
        fill: Color = UmlStyle.fill,
    ) = Column(modifier = Modifier.umlNode().umlOutline(RectangleShape, stroke, fill)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.align(Alignment.Center).padding(end = 16.dp)) {
                NameCompartment(name, keywords, stroke)
            }
            DocumentIcon(stroke, fill, Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp))
        }
        if (properties.isNotEmpty()) ListCompartment(properties, stroke)
    }

    /** A DeploymentSpecification is an Artifact with the keyword «deployment spec» and its properties. */
    @Composable
    fun DeploymentSpecificationView(name: String, properties: List<String> = emptyList(), stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill) =
        ArtifactView(name, listOf("deployment spec"), properties, stroke, fill)

    /** A Deployment is a dashed arrow from the artifact to the deployment target, «deploy». */
    fun DeploymentEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = AnyDiagram.DependencyEdge("deploy", stroke = stroke)

    /** A Manifestation is a dashed arrow from the artifact to the element it manifests, «manifest». */
    fun ManifestationEdge(stroke: Color = UmlStyle.stroke): GraphLayoutEdgeContent = AnyDiagram.DependencyEdge("manifest", stroke = stroke)

    /** A CommunicationPath is a solid line between nodes, with optional name and multiplicities. */
    fun CommunicationPathEdge(
        name: String = "",
        sourceMultiplicity: String = "",
        targetMultiplicity: String = "",
        stroke: Color = UmlStyle.stroke,
    ): GraphLayoutEdgeContent = umlEdge(startText = listOf(sourceMultiplicity), middleText = listOf(name), endText = listOf(targetMultiplicity), stroke = stroke)

    /** The artifact (document) icon: a page with a folded corner. */
    @Composable
    fun DocumentIcon(stroke: Color = UmlStyle.stroke, fill: Color = UmlStyle.fill, modifier: Modifier = Modifier) =
        Canvas(modifier.size(width = 10.dp, height = 13.dp)) {
            val sw = 1.dp.toPx()
            val c = size.width * 0.4f
            val page = Path().apply {
                moveTo(0f, 0f); lineTo(size.width - c, 0f); lineTo(size.width, c); lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(page, fill, style = Fill)
            drawPath(page, stroke, style = Stroke(sw))
            val fold = Path().apply { moveTo(size.width - c, 0f); lineTo(size.width - c, c); lineTo(size.width, c) }
            drawPath(fold, stroke, style = Stroke(sw))
        }
}

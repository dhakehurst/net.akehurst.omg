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
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent
import net.akehurst.omg.diagram.Edge
import net.akehurst.omg.diagram.EdgeEnd
import net.akehurst.omg.diagram.Node
import net.akehurst.omg.diagram.ShapeRegistry

/**
 * Draws generic diagram elements with the UML shapes, by [Node.shape] / [Edge.shape] name.
 *
 * How the generic data is used:
 * - [Node.label] is the name text; [Node.keyword] are keywords or stereotypes (shown in guillemets);
 *   [Node.compartment] are the list compartments; [Node.option] holds flags such as 'abstract' or 'reference'.
 * - [Edge.label] is the middle text, [EdgeEnd.label] the texts at each end,
 *   [EdgeEnd.decoration] names an end symbol (see [decorationSymbol]) for shapes that let the ends vary.
 */
object UmlShapeRegistry : ShapeRegistry {

    /** Node shape names this registry knows. */
    val nodeShapes = setOf(
        "Comment", "Constraint",
        "Class", "Interface", "DataType", "PrimitiveType", "Enumeration",
        "Package", "Model",
        "InstanceSpecification",
        "Component", "ProvidedInterface", "RequiredInterface",
        "StructuredClassifier", "Part", "Port", "Collaboration", "CollaborationUse",
        "Node", "Device", "ExecutionEnvironment", "Artifact", "DeploymentSpecification",
        "Profile", "Stereotype", "Metaclass",
        "Activity", "Action", "CallBehaviorAction", "SendSignalAction", "AcceptEventAction",
        "InitialNode", "ActivityFinalNode", "FlowFinalNode", "DecisionNode", "MergeNode", "ForkNode", "JoinNode",
        "ObjectNode", "CentralBufferNode", "DataStoreNode", "ActivityParameterNode", "Pin", "ActivityPartition",
    )

    /** Edge shape names this registry knows. */
    val edgeShapes = setOf(
        "Annotation", "Dependency", "Abstraction", "Containment",
        "Association", "Generalization", "Realization", "Usage",
        "PackageImport", "ElementImport", "PackageMerge",
        "Link",
        "InterfaceConnection", "ComponentRealization", "InterfaceRealization",
        "Connector", "AssemblyConnector", "DelegationConnector", "RoleBinding",
        "Deployment", "Manifestation", "CommunicationPath",
        "Extension", "ProfileApplication", "MetaclassReference",
        "ControlFlow", "ObjectFlow", "ExceptionHandler",
    )

    override fun nodeContent(node: Node): @Composable (children: @Composable () -> Unit) -> Unit {
        val label = node.label
        val kw = node.keyword
        // classifier shapes always show their attribute and operation compartments; other compartments only when not empty
        val alwaysShown = when (node.shape) {
            "Class", "Interface", "DataType", "PrimitiveType", "Stereotype" -> setOf("attributes", "operations")
            "Enumeration" -> setOf("literals")
            else -> emptySet()
        }
        val comps = node.compartment.filter { it.item.isNotEmpty() || it.name in alwaysShown }.map { it.item }
        val has = { o: String -> o in node.option }
        val hasChildren = node.child.isNotEmpty()
        return when (node.shape) {
            "Comment" -> { _ -> AnyDiagram.CommentView(label) }
            "Constraint" -> { _ -> AnyDiagram.ConstraintView(label) }

            // a class with parts is drawn with its internal structure
            "Class" -> when {
                hasChildren -> { children -> CompositeStructureDiagram.StructuredClassifierView(label, kw, children = children) }
                else -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = kw, isAbstract = has("abstract")) }
            }
            "Interface" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("interface") + kw) }
            "DataType" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("dataType") + kw) }
            "PrimitiveType" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("primitive") + kw) }
            "Enumeration" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("enumeration") + kw) }

            "Package" -> { children -> PackageDiagram.PackageView(label, kw, children = if (hasChildren) children else null) }
            "Model" -> { children -> PackageDiagram.PackageView(label, listOf("model") + kw, children = if (hasChildren) children else null) }

            "InstanceSpecification" -> { _ -> ObjectDiagram.InstanceSpecificationView(label, slots = comps.flatten()) }

            "Component" -> { children -> ComponentDiagram.ComponentView(label, comps, kw, children = if (hasChildren) children else null) }
            "ProvidedInterface" -> { _ -> ComponentDiagram.ProvidedInterfaceView(label) }
            "RequiredInterface" -> { _ -> ComponentDiagram.RequiredInterfaceView(label) }

            "StructuredClassifier" -> { children -> CompositeStructureDiagram.StructuredClassifierView(label, kw, children = children) }
            "Part" -> { children -> CompositeStructureDiagram.PartView(label, isComposite = !has("reference"), children = if (hasChildren) children else null) }
            "Port" -> { _ -> CompositeStructureDiagram.PortView(label, isBehavior = has("behavior")) }
            "Collaboration" -> { children -> CompositeStructureDiagram.CollaborationView(label, children = if (hasChildren) children else null) }
            "CollaborationUse" -> { _ -> CompositeStructureDiagram.CollaborationUseView(label, kw.firstOrNull() ?: "") }

            "Node" -> { children -> DeploymentDiagram.NodeView(label, kw, children = if (hasChildren) children else null) }
            "Device" -> { children -> DeploymentDiagram.NodeView(label, listOf("device") + kw, children = if (hasChildren) children else null) }
            "ExecutionEnvironment" -> { children -> DeploymentDiagram.NodeView(label, listOf("executionEnvironment") + kw, children = if (hasChildren) children else null) }
            "Artifact" -> { _ -> DeploymentDiagram.ArtifactView(label, listOf("artifact") + kw, comps.flatten()) }
            "DeploymentSpecification" -> { _ -> DeploymentDiagram.DeploymentSpecificationView(label, comps.flatten()) }

            "Profile" -> { children -> ProfileDiagram.ProfileView(label, children = if (hasChildren) children else null) }
            "Stereotype" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("stereotype") + kw) }
            "Metaclass" -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("metaclass") + kw) }

            "Activity" -> { children -> ActivityDiagram.ActivityView(label, comps.flatten(), children = children) }
            "Action" -> { _ -> ActivityDiagram.ActionView(label, kw) }
            "CallBehaviorAction" -> { _ -> ActivityDiagram.CallBehaviorActionView(label) }
            "SendSignalAction" -> { _ -> ActivityDiagram.SendSignalActionView(label) }
            "AcceptEventAction" -> { _ -> ActivityDiagram.AcceptEventActionView(label) }
            "InitialNode" -> { _ -> ActivityDiagram.InitialNodeView() }
            "ActivityFinalNode" -> { _ -> ActivityDiagram.ActivityFinalNodeView() }
            "FlowFinalNode" -> { _ -> ActivityDiagram.FlowFinalNodeView() }
            "DecisionNode" -> { _ -> ActivityDiagram.DecisionNodeView(label) }
            "MergeNode" -> { _ -> ActivityDiagram.MergeNodeView() }
            "ForkNode" -> { _ -> ActivityDiagram.ForkNodeView(!has("vertical")) }
            "JoinNode" -> { _ -> ActivityDiagram.JoinNodeView(!has("vertical"), label) }
            "ObjectNode" -> { _ -> ActivityDiagram.ObjectNodeView(label, keywords = kw) }
            "CentralBufferNode" -> { _ -> ActivityDiagram.CentralBufferNodeView(label) }
            "DataStoreNode" -> { _ -> ActivityDiagram.DataStoreNodeView(label) }
            "ActivityParameterNode" -> { _ -> ActivityDiagram.ActivityParameterNode(label) }
            "Pin" -> { _ -> ActivityDiagram.PinView(label) }
            "ActivityPartition" -> { children -> ActivityDiagram.ActivityPartitionView(label, children = children) }

            else -> { _ -> ClassDiagram.ClassifierView(label, comps, keywords = listOf("unknown shape '${node.shape}'") + kw) }
        }
    }

    override fun edgeContent(edge: Edge): GraphLayoutEdgeContent {
        val label = edge.label
        val kw = edge.keyword.firstOrNull()
        val has = { o: String -> o in edge.option }
        val s = edge.sourceEnd.label
        val t = edge.targetEnd.label
        return when (edge.shape) {
            "Annotation" -> AnyDiagram.AnnotationEdge()
            "Dependency" -> AnyDiagram.DependencyEdge(kw, label)
            "Abstraction" -> AnyDiagram.AbstractionEdge(kw ?: "abstraction")
            "Containment" -> AnyDiagram.ContainmentEdge()

            "Association" -> ClassDiagram.AssociationEdge(label, edge.sourceEnd.toAssociationEnd(), edge.targetEnd.toAssociationEnd())
            "Generalization" -> ClassDiagram.GeneralizationEdge()
            "Realization" -> ClassDiagram.RealizationEdge()
            "Usage" -> ClassDiagram.UsageEdge()

            "PackageImport" -> PackageDiagram.PackageImportEdge(!has("private"))
            "ElementImport" -> PackageDiagram.ElementImportEdge(!has("private"), label)
            "PackageMerge" -> PackageDiagram.PackageMergeEdge()

            "Link" -> ObjectDiagram.LinkEdge(label, kw ?: "", s.firstOrNull() ?: "", t.firstOrNull() ?: "")

            "InterfaceConnection" -> ComponentDiagram.InterfaceConnectionEdge()
            "ComponentRealization" -> ComponentDiagram.ComponentRealizationEdge()
            "InterfaceRealization" -> ComponentDiagram.InterfaceRealizationEdge()

            "Connector" -> CompositeStructureDiagram.ConnectorEdge(label, s.firstOrNull() ?: "", t.firstOrNull() ?: "")
            "AssemblyConnector" -> CompositeStructureDiagram.AssemblyConnectorEdge(label)
            "DelegationConnector" -> CompositeStructureDiagram.DelegationConnectorEdge()
            "RoleBinding" -> CompositeStructureDiagram.RoleBindingEdge(label)

            "Deployment" -> DeploymentDiagram.DeploymentEdge()
            "Manifestation" -> DeploymentDiagram.ManifestationEdge()
            "CommunicationPath" -> DeploymentDiagram.CommunicationPathEdge(label, s.firstOrNull() ?: "", t.firstOrNull() ?: "")

            "Extension" -> ProfileDiagram.ExtensionEdge(has("required"))
            "ProfileApplication" -> ProfileDiagram.ProfileApplicationEdge(has("strict"))
            "MetaclassReference" -> ProfileDiagram.MetaclassReferenceEdge()

            "ControlFlow" -> ActivityDiagram.ControlFlowEdge(guard = kw ?: "", name = label)
            "ObjectFlow" -> ActivityDiagram.ObjectFlowEdge(guard = kw ?: "", name = label)
            "ExceptionHandler" -> ActivityDiagram.ExceptionHandlerEdge(label)

            else -> umlEdge(
                start = decorationSymbol(edge.sourceEnd.decoration),
                end = decorationSymbol(edge.targetEnd.decoration),
                dashed = has("dashed"),
                startText = s,
                middleText = edge.keyword.map { guillemets(it) } + label,
                endText = t,
            )
        }
    }

    /** End symbol by name: openArrow, filledArrow, hollowTriangle, filledTriangle, hollowDiamond, filledDiamond, cross, circledPlus. */
    fun decorationSymbol(name: String) = when (name) {
        "openArrow" -> UmlEdgeSymbols.openArrow()
        "filledArrow" -> UmlEdgeSymbols.filledArrow()
        "hollowTriangle" -> UmlEdgeSymbols.hollowTriangle()
        "filledTriangle" -> UmlEdgeSymbols.filledTriangle()
        "hollowDiamond" -> UmlEdgeSymbols.hollowDiamond()
        "filledDiamond" -> UmlEdgeSymbols.filledDiamond()
        "cross" -> UmlEdgeSymbols.cross()
        "circledPlus" -> UmlEdgeSymbols.circledPlus()
        else -> null
    }

    /**
     * Association end from generic data: label[0] = role name, label[1] = multiplicity, further labels are modifiers;
     * decoration 'hollowDiamond' / 'filledDiamond' = shared / composite aggregation,
     * 'openArrow' = navigable, 'cross' = not navigable.
     */
    private fun EdgeEnd.toAssociationEnd() = ClassDiagram.AssociationEnd(
        roleName = label.getOrElse(0) { "" },
        multiplicity = label.getOrElse(1) { "" },
        aggregation = when (decoration) {
            "filledDiamond" -> ClassDiagram.Aggregation.COMPOSITE
            "hollowDiamond" -> ClassDiagram.Aggregation.SHARED
            else -> ClassDiagram.Aggregation.NONE
        },
        navigable = when (decoration) {
            "openArrow" -> true
            "cross" -> false
            else -> null
        },
        modifiers = label.drop(2),
    )
}

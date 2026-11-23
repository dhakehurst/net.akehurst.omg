package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.omg.diagram.Diagram
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_UmlDiagramRules {

    private fun Diagram.dump() = buildString {
        appendLine("$name ($kind)")
        allNode.forEach { n -> appendLine("  node '${n.id}' ${n.shape} '${n.label}' kw=${n.keyword} opt=${n.option} comps=${n.compartment.filter { it.item.isNotEmpty() }.map { it.name + it.item }} children=${n.child.map { it.id }} ports=${n.port.map { it.id }}") }
        edge.forEach { e -> appendLine("  edge ${e.shape} ${e.sourceId}->${e.targetId} '${e.label}' kw=${e.keyword} opt=${e.option} ends=${e.sourceEnd.decoration}${e.sourceEnd.label}/${e.targetEnd.decoration}${e.targetEnd.label}") }
    }

    private fun run(example: DemoDiagrams.Example): Diagram {
        val read = DemoDiagrams.read(example)
        println("--- ${example.title}: XMI issues ${read.issues}")
        val res = UmlToDiagram(read.reader.factory).transform(UmlDiagramRules.transform, listOf(read.element))
        println("m2m issues: ${res.issues}")
        res.diagrams.forEach { println(it.dump()) }
        assertEquals(1, res.diagrams.size)
        val d = res.diagrams[0]
        val ids = d.allNode.map { it.id }.toSet()
        d.edge.forEach { e -> assertTrue(e.sourceId in ids && e.targetId in ids, "edge ends not in diagram: ${e.id}") }
        return d
    }

    @Test
    fun class_diagram() {
        val d = run(DemoDiagrams.classDiagram)
        assertEquals(setOf("Vehicle", "Car", "Bicycle", "Engine", "Person", "Colour", "Drivable"), d.node.map { it.id }.toSet())
        assertEquals(listOf("Association", "Association", "Generalization", "Generalization", "InterfaceRealization"), d.edge.map { it.shape }.sorted())
    }

    @Test
    fun package_diagram() {
        val d = run(DemoDiagrams.packageDiagram)
        assertEquals(setOf("Catalog", "Orders", "Payments", "Offers"), d.node.map { it.id }.toSet())
        assertEquals(listOf("Dependency", "PackageImport", "PackageImport", "PackageMerge"), d.edge.map { it.shape }.sorted())
    }

    @Test
    fun object_diagram() {
        val d = run(DemoDiagrams.objectDiagram)
        assertEquals(setOf("myCar", "bob"), d.node.map { it.id }.toSet())
        assertEquals(listOf("Link"), d.edge.map { it.shape })
    }

    @Test
    fun component_diagram() {
        val d = run(DemoDiagrams.componentDiagram)
        assertEquals(5, d.node.size)
        assertEquals(listOf("InterfaceRealization", "InterfaceRealization", "Usage", "Usage"), d.edge.map { it.shape }.sorted())
    }

    @Test
    fun composite_structure_diagram() {
        val d = run(DemoDiagrams.compositeStructureDiagram)
        assertEquals(listOf("Car"), d.node.map { it.id })
        assertEquals(3, d.node[0].child.size)
        assertEquals(1, d.node[0].port.size)
        assertEquals(3, d.edge.size)
    }

    @Test
    fun deployment_diagram() {
        val d = run(DemoDiagrams.deploymentDiagram)
        assertEquals(setOf("AppServer", "DbServer", "shop.jar", "schema.sql"), d.node.map { it.id }.toSet())
        assertEquals(listOf("CommunicationPath", "Deployment", "Deployment"), d.edge.map { it.shape }.sorted())
    }

    @Test
    fun profile_diagram() {
        val d = run(DemoDiagrams.profileDiagram)
        assertEquals(setOf("Entity", "Service", "Class", "Component"), d.node.map { it.id }.toSet())
        assertEquals(listOf("Extension", "Extension"), d.edge.map { it.shape })
    }

    @Test
    fun activity_diagram() {
        val d = run(DemoDiagrams.activityDiagram)
        assertEquals(listOf("ProcessOrder"), d.node.map { it.id })
        assertEquals(2, d.node[0].child.size)
        assertEquals(11, d.edge.size)
    }
}

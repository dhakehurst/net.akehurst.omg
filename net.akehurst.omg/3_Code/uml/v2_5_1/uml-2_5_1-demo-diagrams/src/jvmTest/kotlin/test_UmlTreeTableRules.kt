package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.omg.diagram.TreeItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_UmlTreeTableRules {

    private fun TreeItem.dump(indent: String): String = buildString {
        appendLine("$indent$shape '$id' '$label' $keyword")
        child.forEach { append(it.dump("$indent  ")) }
    }

    @Test
    fun containment_tree() {
        val read = DemoDiagrams.read(DemoDiagrams.classDiagram)
        val res = UmlToDiagram(read.reader.factory).transform(UmlTreeTableRules.containmentTree, listOf(read.element))
        println("m2m issues: ${res.issues}")
        res.trees.forEach { t -> println("${t.name} (${t.kind})"); t.item.forEach { print(it.dump("  ")) } }
        assertEquals(1, res.trees.size)
        val t = res.trees[0]
        assertTrue(t.item.isNotEmpty())
        assertTrue(t.allItem.any { it.shape == "Class" })
        assertTrue(t.allItem.any { it.shape == "Property" })
    }

    @Test
    fun attribute_table() {
        val read = DemoDiagrams.read(DemoDiagrams.classDiagram)
        val res = UmlToDiagram(read.reader.factory).transform(UmlTreeTableRules.attributeTable, listOf(read.element))
        println("m2m issues: ${res.issues}")
        res.tables.forEach { t -> println("${t.name} (${t.kind}) ${t.column}"); t.row.forEach { println("  ${it.id}: ${it.cell}") } }
        assertEquals(1, res.tables.size)
        val t = res.tables[0]
        assertEquals(listOf("Classifier", "Attribute", "Type", "Multiplicity", "Aggregation"), t.column)
        assertTrue(t.row.isNotEmpty())
        t.row.forEach { assertEquals(5, it.cell.size, "row ${it.id}") }
        assertEquals(listOf("Vehicle", "wheels", "Integer", "1..1", ""), t.row.first { it.id == "Vehicle::wheels" }.cell)
        assertEquals(listOf("Car", "engine", "Engine", "1..1", "composite"), t.row.first { it.id == "Car::engine" }.cell)
    }
}

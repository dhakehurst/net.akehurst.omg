package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.omg.diagram.Ladder
import net.akehurst.omg.diagram.Step
import kotlin.test.Test
import kotlin.test.assertEquals

class test_UmlSequenceRules {

    private fun Step.dump(indent: String): String = buildString {
        appendLine("$indent${shape} '${id}' '${label}' ${sourceId}->${targetId} covered=$covered")
        operand.forEach { o ->
            appendLine("$indent  [${o.guard}]")
            o.step.forEach { append(it.dump("$indent    ")) }
        }
    }

    private fun Ladder.dump() = buildString {
        appendLine("$name ($kind)")
        rail.forEach { appendLine("  rail '${it.id}' ${it.shape} '${it.label}'") }
        step.forEach { append(it.dump("  ")) }
        bar.forEach { appendLine("  bar '${it.id}' ${it.shape} on ${it.railId}: ${it.startId} .. ${it.finishId}") }
    }

    @Test
    fun sequence_diagram() {
        val read = DemoDiagrams.read(DemoDiagrams.sequenceDiagram)
        println("XMI issues ${read.issues}")
        val res = UmlToDiagram(read.reader.factory).transform(UmlSequenceRules.transform, listOf(read.element))
        println("m2m issues: ${res.issues}")
        res.ladders.forEach { println(it.dump()) }
        assertEquals(1, res.ladders.size)
        val l = res.ladders[0]
        assertEquals(listOf("customer", "shop", "payment"), l.rail.map { it.id })
        assertEquals(listOf("customer : Customer", "shop : Shop", "payment : PaymentService"), l.rail.map { it.label })
        assertEquals(listOf("loop1", "placeOrder", "checkStock", "alt1", "confirm"), l.step.map { it.id })
        assertEquals(listOf("CombinedFragment", "SynchCall", "SynchCall", "CombinedFragment", "Reply"), l.step.map { it.shape })
        assertEquals(listOf("loop", "alt"), l.step.filter { it.shape == "CombinedFragment" }.map { it.label })
        assertEquals(listOf("shop", "shop"), l.step[2].let { listOf(it.sourceId, it.targetId) }) // self message
        val alt = l.step[3]
        assertEquals(listOf("customer", "shop", "payment"), alt.covered)
        assertEquals(listOf("in stock", "else"), alt.operand.map { it.guard })
        assertEquals(listOf(listOf("pay", "paid"), listOf("outOfStock")), alt.operand.map { o -> o.step.map { it.id } })
        assertEquals(listOf("AsynchSignal", "AsynchSignal"), alt.operand[0].step.map { it.shape })
        assertEquals(1, l.bar.size)
        assertEquals(listOf("shop", "placeOrder", "confirm"), l.bar[0].let { listOf(it.railId, it.startId, it.finishId) })
    }
}

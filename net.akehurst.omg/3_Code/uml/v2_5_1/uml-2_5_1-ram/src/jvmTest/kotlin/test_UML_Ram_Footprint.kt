package net.akehurst.omg.uml.v2_5_1

import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import kotlin.test.Test

/**
 * Prints the heap used by empty UML ram objects, and a class histogram of what that memory is made of.
 * Not an assertion: it is a measurement to compare template changes against.
 */
class test_UML_Ram_Footprint {

    private fun used(): Long {
        val rt = Runtime.getRuntime()
        repeat(3) { System.gc(); Thread.sleep(100) }
        return rt.totalMemory() - rt.freeMemory()
    }

    private fun histogram(top: Int) {
        val pid = ProcessHandle.current().pid()
        val jcmd = System.getProperty("java.home") + "/bin/jcmd"
        val p = ProcessBuilder(jcmd, pid.toString(), "GC.class_histogram").redirectErrorStream(true).start()
        p.inputStream.bufferedReader().readLines().also { java.io.File(System.getProperty("java.io.tmpdir"), "uml-histo.txt").writeText(it.joinToString("\n")) }.take(top + 3).forEach { println(it) }
        p.waitFor()
    }

    private fun measure(label: String, n: Int, construct: (UML_ModelFactoryRam, Int) -> Any) {
        val factory = UML_ModelFactoryRam("footprint")
        val keep = ArrayList<Any>(n)
        val before = used()
        for (i in 0 until n) keep.add(construct(factory, i))
        val after = used()
        println("$label: $n objects, ${(after - before) / n} bytes each (including factory store)")
        keep.size
    }

    @Test
    fun footprint() {
        val n = 20_000
        measure("Class", n) { f, i -> f.UML.StructuredClassifiers.Class_construct("c$i") }
        measure("Property", n) { f, i -> f.UML.Classification.Property_construct("p$i") }
        measure("Port", n) { f, i -> f.UML.StructuredClassifiers.Port_construct("q$i") }
        measure("Comment", n) { f, i -> f.UML.CommonStructure.Comment_construct("k$i") }

        // 25k Class + 50k Property + 25k Port, as in the XMI reader large test
        val factory = UML_ModelFactoryRam("footprint")
        val keep = ArrayList<Any>()
        val before = used()
        for (i in 0 until 25_000) {
            keep.add(factory.UML.StructuredClassifiers.Class_construct("c$i"))
            keep.add(factory.UML.Classification.Property_construct("a$i"))
            keep.add(factory.UML.Classification.Property_construct("b$i"))
            keep.add(factory.UML.StructuredClassifiers.Port_construct("p$i"))
        }
        val after = used()
        println("Mix: 100k objects, ${(after - before) / (1024 * 1024)} MiB")
        histogram(40)
        keep.size
    }
}

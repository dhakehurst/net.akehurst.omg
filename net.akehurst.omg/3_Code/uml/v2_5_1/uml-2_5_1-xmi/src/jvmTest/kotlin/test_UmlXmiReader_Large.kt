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

package net.akehurst.omg.uml.v2_5_1.xmi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTimedValue

class test_UmlXmiReader_Large {

    /** a generated model with [blocks] blocks, each with 3 properties and a Block application */
    private fun largeModel(blocks: Int): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20131001" xmlns:sysml="${TestsatModel.SYSML_NS}">
<uml:Model xmi:type="uml:Model" xmi:id="_m" name="Large">
""")
        for (i in 0 until blocks) {
            sb.append("""<packagedElement xmi:type="uml:Class" xmi:id="_c$i" name="Block$i">
  <ownedAttribute xmi:type="uml:Property" xmi:id="_c$i.a" name="a" aggregation="composite" type="_c${(i + 1) % blocks}"/>
  <ownedAttribute xmi:type="uml:Property" xmi:id="_c$i.b" name="b"><type href="http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real"/></ownedAttribute>
  <ownedAttribute xmi:type="uml:Port" xmi:id="_c$i.p" name="p" type="_c0"/>
</packagedElement>
""")
        }
        sb.append("</uml:Model>\n")
        for (i in 0 until blocks) sb.append("""<sysml:Block xmi:id="_s$i" base_Class="_c$i"/>""").append('\n')
        sb.append("</xmi:XMI>\n")
        return sb.toString()
    }

    @Test
    fun read_100k_elements() {
        val blocks = 25_000 // 100k UML elements + 25k applications
        val text = largeModel(blocks)
        val rt = Runtime.getRuntime()
        System.gc()
        val before = rt.totalMemory() - rt.freeMemory()
        val (r, t) = measureTimedValue {
            UmlXmiReader(XmiReadOptions(resolver = XmiResourceResolver.fromMap(mapOf("SysML.xmi" to TestsatModel.SYSML_STANDIN))))
                .also { it.load("SysML.xmi", TestsatModel.SYSML_STANDIN) }
                .read("large.xmi", text)
        }
        System.gc()
        val after = rt.totalMemory() - rt.freeMemory()
        println("Read ${text.length / 1024} KiB, ${r.document.elementsById.size} elements in $t, heap +${(after - before) / (1024 * 1024)} MiB")
        assertEquals(emptyList(), r.errors)
        assertEquals(blocks * 4 + 1, r.document.elementsById.size)
        assertEquals(blocks, r.stereotypes.elementsWithStereotype(TestsatModel.SYSML_NS, "Block").size)
    }
}

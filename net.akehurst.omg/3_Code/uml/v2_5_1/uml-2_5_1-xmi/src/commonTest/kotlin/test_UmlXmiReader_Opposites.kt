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

import net.akehurst.omg.uml.v2_5_1.api.UML.Interactions.Lifeline
import net.akehurst.omg.uml.v2_5_1.api.UML.Interactions.MessageOccurrenceSpecification
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tools (e.g. Cameo) write both ends of an opposite pair of references, e.g. 'covered' on each fragment
 * and 'coveredBy' on each lifeline. Both must be read without errors and without duplicate values.
 */
class test_UmlXmiReader_Opposites {

    private fun interaction(fragmentsFirst: Boolean): String {
        val fragments = """
            <fragment xmi:type="uml:MessageOccurrenceSpecification" xmi:id="send" message="msg"><covered xmi:idref="l_obc"/></fragment>
            <fragment xmi:type="uml:MessageOccurrenceSpecification" xmi:id="recv" message="msg"><covered xmi:idref="l_eps"/></fragment>
        """
        val lifelines = """
            <lifeline xmi:type="uml:Lifeline" xmi:id="l_obc" name="obc" represents="p_obc"><coveredBy xmi:idref="send"/></lifeline>
            <lifeline xmi:type="uml:Lifeline" xmi:id="l_eps" name="eps" represents="p_eps"><coveredBy xmi:idref="recv"/></lifeline>
        """
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
              <uml:Model xmi:id="m" name="Testsat">
                <packagedElement xmi:type="uml:Class" xmi:id="obc" name="OBC">
                  <ownedAttribute xmi:type="uml:Property" xmi:id="p_eps" name="eps"/>
                  <ownedAttribute xmi:type="uml:Property" xmi:id="p_obc" name="obc"/>
                  <ownedBehavior xmi:type="uml:Interaction" xmi:id="i1" name="Power on">
                    ${if (fragmentsFirst) fragments + lifelines else lifelines + fragments}
                    <message xmi:type="uml:Message" xmi:id="msg" name="switchOn" sendEvent="send" receiveEvent="recv"/>
                  </ownedBehavior>
                </packagedElement>
              </uml:Model>
            </xmi:XMI>
        """.trimIndent()
    }

    private fun check(r: UmlXmiReadResult) {
        r.issues.forEach { println(it) }
        assertEquals(emptyList(), r.errors)
        val lObc = r.elementById("l_obc") as Lifeline
        val lEps = r.elementById("l_eps") as Lifeline
        val send = r.elementById("send") as MessageOccurrenceSpecification
        val recv = r.elementById("recv") as MessageOccurrenceSpecification
        assertEquals(listOf<Any>(send), lObc.coveredBySet.toList())
        assertEquals(listOf<Any>(recv), lEps.coveredBySet.toList())
        assertEquals(lObc, send.covered)
        assertEquals(lEps, recv.covered)
        assertEquals(listOf<Any>(lObc), send.coveredSet.toList())
        assertEquals(listOf<Any>(lEps), recv.coveredSet.toList())
    }

    @Test
    fun covered_and_coveredBy_fragments_first() = check(readUmlXmi(interaction(fragmentsFirst = true)))

    @Test
    fun covered_and_coveredBy_lifelines_first() = check(readUmlXmi(interaction(fragmentsFirst = false)))
}

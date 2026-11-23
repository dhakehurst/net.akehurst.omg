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

import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.Classifier
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Port
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Port::required and Port::provided of ports typed by a Class that uses one Interface (a Usage)
 * and realizes another (an InterfaceRealization). The Class also inherits a Usage from its parent.
 */
class test_UmlXmiReader_Ports {

    private val xmi = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
          <uml:Model xmi:id="m" name="Testsat">
            <packagedElement xmi:type="uml:Interface" xmi:id="if_cmd" name="Commanding"/>
            <packagedElement xmi:type="uml:Interface" xmi:id="if_tm" name="Telemetry"/>
            <packagedElement xmi:type="uml:Interface" xmi:id="if_pwr" name="Power"/>
            <packagedElement xmi:type="uml:Class" xmi:id="c_unit" name="Unit"/>
            <packagedElement xmi:type="uml:Usage" xmi:id="u_pwr" client="c_unit" supplier="if_pwr"/>
            <packagedElement xmi:type="uml:Class" xmi:id="c_obc" name="OBC">
              <generalization xmi:type="uml:Generalization" xmi:id="g_obc" general="c_unit"/>
              <interfaceRealization xmi:type="uml:InterfaceRealization" xmi:id="ir_tm" client="c_obc" supplier="if_tm" contract="if_tm" implementingClassifier="c_obc"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Usage" xmi:id="u_cmd" client="c_obc" supplier="if_cmd"/>
            <packagedElement xmi:type="uml:Class" xmi:id="c_sat" name="Satellite">
              <ownedAttribute xmi:type="uml:Port" xmi:id="p_obc" name="obcPort" type="c_obc"/>
              <ownedAttribute xmi:type="uml:Port" xmi:id="p_obc_conj" name="obcPortConjugated" type="c_obc" isConjugated="true"/>
              <ownedAttribute xmi:type="uml:Port" xmi:id="p_tm" name="tmPort" type="if_tm"/>
            </packagedElement>
          </uml:Model>
        </xmi:XMI>
    """.trimIndent()

    private val r by lazy { readUmlXmi(xmi).also { res -> res.issues.forEach { println(it) } } }
    private fun port(id: String) = r.elementById(id) as Port
    private fun names(c: Iterable<Any?>) = c.map { (it as Classifier).name }.toSet()

    @Test
    fun no_errors() = assertEquals(emptyList(), r.errors)

    @Test
    fun directlyUsedInterfaces() {
        val obc = r.elementById("c_obc") as Classifier
        assertEquals(setOf("Commanding"), names(obc.directlyUsedInterfaces()))
        assertEquals(setOf("Commanding", "Power"), names(obc.allUsedInterfaces()))
    }

    @Test
    fun port_typed_by_class() {
        val p = port("p_obc")
        assertEquals(setOf("Commanding", "Power"), names(p.requiredSet))
        assertEquals(setOf("Telemetry"), names(p.providedSet))
    }

    @Test
    fun conjugated_port_typed_by_class() {
        val p = port("p_obc_conj")
        assertEquals(setOf("Telemetry"), names(p.requiredSet))
        assertEquals(setOf("Commanding", "Power"), names(p.providedSet))
    }

    @Test
    fun port_typed_by_interface() {
        val p = port("p_tm")
        assertEquals(emptySet(), names(p.requiredSet))
        assertEquals(setOf("Telemetry"), names(p.providedSet))
    }
}

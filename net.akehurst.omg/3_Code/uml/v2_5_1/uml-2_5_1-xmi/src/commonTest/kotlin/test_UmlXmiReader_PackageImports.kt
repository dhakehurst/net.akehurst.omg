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

import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.NamedElement
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Package::member, importedMember, visibleMembers and makesVisible with element and package imports,
 * public and private, including packages that import each other (as the packages of the UML metamodel do).
 */
class test_UmlXmiReader_PackageImports {

    private val xmi = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
          <uml:Model xmi:id="m" name="Testsat">
            <packagedElement xmi:type="uml:Package" xmi:id="p_power" name="Power">
              <packageImport xmi:type="uml:PackageImport" xmi:id="pi_power_data" importedPackage="p_data"/>
              <packagedElement xmi:type="uml:Class" xmi:id="c_battery" name="Battery"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Package" xmi:id="p_data" name="Data">
              <packageImport xmi:type="uml:PackageImport" xmi:id="pi_data_power" importedPackage="p_power"/>
              <packageImport xmi:type="uml:PackageImport" xmi:id="pi_data_thermal" importedPackage="p_thermal" visibility="private"/>
              <elementImport xmi:type="uml:ElementImport" xmi:id="ei_data_obc" importedElement="c_obc" alias="Computer"/>
              <packagedElement xmi:type="uml:Class" xmi:id="c_store" name="Store"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Package" xmi:id="p_thermal" name="Thermal">
              <packagedElement xmi:type="uml:Class" xmi:id="c_heater" name="Heater"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Package" xmi:id="p_avionics" name="Avionics">
              <packagedElement xmi:type="uml:Class" xmi:id="c_obc" name="OBC"/>
            </packagedElement>
          </uml:Model>
        </xmi:XMI>
    """.trimIndent()

    private val r by lazy { readUmlXmi(xmi).also { res -> res.issues.forEach { println(it) } } }
    private fun pkg(id: String) = r.elementById(id) as Package
    private fun el(id: String) = r.elementById(id) as NamedElement
    private fun names(c: Iterable<Any?>) = c.map { (it as NamedElement).name }.toSet()

    @Test
    fun no_errors() = assertEquals(emptyList(), r.errors)

    @Test
    fun visibleMembers_follow_public_imports_through_a_cycle() {
        // Data: its own Store, its public element import OBC, and Power's Battery (public import); not Thermal's Heater (private)
        assertEquals(setOf("Store", "OBC", "Battery"), names(pkg("p_data").visibleMembers()))
        // Power: its Battery, and what Data makes visible
        assertEquals(setOf("Battery", "Store", "OBC"), names(pkg("p_power").visibleMembers()))
        assertTrue(pkg("p_power").makesVisible(el("c_obc")))
        assertFalse(pkg("p_power").makesVisible(el("c_heater")))
    }

    @Test
    fun importedMember_and_member() {
        // a private import still imports into the importing package itself
        assertEquals(setOf("OBC", "Battery", "Heater"), names(pkg("p_data").importedMemberSet))
        assertEquals(setOf("Store", "OBC", "Battery", "Heater"), names(pkg("p_data").memberSet))
        assertEquals(setOf("Store", "OBC"), names(pkg("p_power").importedMemberSet))
        assertEquals(setOf("Battery", "Store", "OBC"), names(pkg("p_power").memberSet))
    }

    @Test
    fun getNamesOfMember() {
        assertEquals(setOf("Computer"), pkg("p_data").getNamesOfMember(el("c_obc")))
        assertEquals(setOf("Computer"), pkg("p_power").getNamesOfMember(el("c_obc")))
        assertEquals(setOf("Battery"), pkg("p_data").getNamesOfMember(el("c_battery")))
        assertEquals(emptySet(), pkg("p_avionics").getNamesOfMember(el("c_battery")))
    }
}

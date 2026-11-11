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
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Profile
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Stereotype
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** reads the OMG SysML 1.7 profile (ptc/24-01-02) as an ordinary UML model, and the Testsat model against it */
class test_UmlXmiReader_SysML17 {

    companion object {
        const val SYSML17 = "https://www.omg.org/spec/SysML/20240101"
        val profileText: String by lazy { File("../../../sysml/v1_7/specs/SysML_1-7_ptc-24-01-02.xmi").readText() }
    }

    @Test
    fun read_profile() {
        val r = UmlXmiReader().read("https://www.omg.org/spec/SysML/20240101/SysML.xmi", profileText)
        r.issues.forEach { println(it) }
        assertEquals(emptyList(), r.errors)
        val profile = assertIs<Profile>(r.rootPackage)
        assertEquals("SysML", profile.name)
        assertEquals(SYSML17, profile.URI)

        val st = r.stereotypes
        val requirement = assertNotNull(st.stereotype(SYSML17, "Requirement"))
        // id and text are owned by the abstract AbstractRequirement
        val tags = st.tagDefinitions(requirement).keys
        assertTrue(tags.containsAll(listOf("id", "text")), "$tags")
        assertTrue(st.generalisationClosure(requirement).any { it.name == "AbstractRequirement" })
        val satisfy = assertNotNull(st.stereotype(SYSML17, "Satisfy"))
        assertTrue(st.generalisationClosure(satisfy).any { it.name == "Trace" })
        val names = r.document.elementsById.values.filterIsInstance<Stereotype>().map { it.name }.toSet()
        assertTrue(names.containsAll(listOf("Block", "InterfaceBlock", "ValueType", "Requirement", "Verify", "DeriveReqt", "TestCase", "FlowProperty", "ProxyPort")), "$names")
    }

    @Test
    fun read_testsat_with_sysml17() {
        // the Testsat model was written against the stand-in profile namespace; map it to SysML 1.7
        val options = XmiReadOptions(
            namespaces = XmiNamespaceTable.DEFAULT.withProfileAlias(TestsatModel.SYSML_NS, SYSML17),
            resolver = XmiResourceResolver.fromMap(mapOf("SysML.xmi" to profileText, "TestsatProfile.xmi" to TestsatModel.TEAM_PROFILE))
        )
        val r = UmlXmiReader(options).read("file:/models/testsat.xmi", TestsatModel.MODEL)
        assertEquals(emptyList(), r.errors)
        val st = r.stereotypes
        fun named(id: String) = r.elementById(id) as NamedElement
        assertTrue(st.hasStereotype(named("_testsat"), SYSML17, "Block"))
        assertEquals("TS-001", st.tagValue(named("_req1"), SYSML17, "Requirement", "id")?.value)
        assertTrue(st.tagValue(named("_req1"), SYSML17, "Requirement", "text")!!.isTyped)
        // the team stereotype specialises the real SysML Requirement and inherits id/text from AbstractRequirement
        assertTrue(st.hasStereotype(named("_req3"), SYSML17, "Requirement"))
        assertTrue(st.hasStereotype(named("_req3"), SYSML17, "AbstractRequirement"))
        assertEquals("TS-003", st.tagValue(named("_req3"), SYSML17, "Requirement", "id")?.value)
        assertEquals(3, st.elementsWithStereotype(SYSML17, "Trace").size)
    }
}

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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** the UML StandardProfile is built in, so models that apply it need no resolver */
class test_UmlXmiReader_StandardProfile {

    companion object {
        const val STD = "http://www.omg.org/spec/UML/20161101/StandardProfile"
        val MODEL = """
            <?xml version="1.0" encoding="UTF-8"?>
            <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101"
                     xmlns:StandardProfile="http://www.omg.org/spec/UML/20161101/StandardProfile">
              <uml:Model xmi:type="uml:Model" xmi:id="_m" name="Testsat">
                <packagedElement xmi:type="uml:Class" xmi:id="_a" name="Bus"/>
                <packagedElement xmi:type="uml:Class" xmi:id="_b" name="BusDesign"/>
                <packagedElement xmi:type="uml:Abstraction" xmi:id="_t" client="_b" supplier="_a"/>
                <packagedElement xmi:type="uml:Artifact" xmi:id="_f" name="bus.kt"/>
                <profileApplication xmi:type="uml:ProfileApplication" xmi:id="_pa">
                  <appliedProfile href="http://www.omg.org/spec/UML/20161101/StandardProfile.xmi#_0"/>
                </profileApplication>
              </uml:Model>
              <StandardProfile:Trace xmi:id="_s1" base_Abstraction="_t"/>
              <StandardProfile:Source xmi:id="_s2" base_Artifact="_f"/>
            </xmi:XMI>
        """.trimIndent()
    }

    @Test
    fun applications_of_the_standard_profile() {
        val r = readUmlXmi(MODEL, "file:/models/std.xmi")
        assertEquals(emptyList(), r.errors)
        assertEquals(emptyList(), r.issues.filter { it.kind == XmiIssueKind.MISSING_RESOURCE || it.kind == XmiIssueKind.MISSING_PROFILE })
        val st = r.stereotypes
        assertNotNull(st.stereotype(STD, "Trace"))
        assertTrue(st.hasStereotype(r.elementById("_t") as NamedElement, STD, "Trace"))
        // Source specialises File
        assertTrue(st.hasStereotype(r.elementById("_f") as NamedElement, STD, "File"))
    }
}

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

import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Class
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** the xmi:id and xmi:uuid of an element read from XMI, found from the element */
class test_UmlXmiReader_Ids {

    private fun xmi(body: String) = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
          $body
        </xmi:XMI>
    """.trimIndent()

    private val MODEL = xmi(
        """
          <uml:Model xmi:id="_m" name="Testsat">
            <packagedElement xmi:type="uml:Package" xmi:id="_pkg" xmi:uuid="uuid-pkg" name="Structure">
              <packagedElement xmi:type="uml:Class" xmi:id="_unit" name="Unit"/>
              <packagedElement xmi:type="uml:Class" xmi:id="_obc" name="OBC">
                <generalization xmi:type="uml:Generalization" xmi:id="_gen" general="_unit"/>
              </packagedElement>
            </packagedElement>
          </uml:Model>
        """
    )

    @Test
    fun idOf_gives_the_xmi_id_of_each_element() {
        val r = readUmlXmi(MODEL)
        assertEquals(emptyList(), r.errors)
        val model = r.model!!
        val pkg = model.packagedElementSet.single() as Package
        val obc = pkg.packagedElementSet.first { (it as Class).name == "OBC" } as Class
        val unit = pkg.packagedElementSet.first { (it as Class).name == "Unit" } as Class
        val gen = obc.generalizationSet.single()

        assertEquals("_m", r.idOf(model))
        assertEquals("_pkg", r.idOf(pkg))
        assertEquals("_obc", r.idOf(obc))
        assertEquals("_unit", r.idOf(unit))
        assertEquals("_gen", r.idOf(gen))
        // the target of the generalization, as reached through the reference
        assertEquals("_unit", r.idOf(gen.general!!))
        for (e in listOf(model, pkg, obc, unit, gen)) {
            assertSame(e, r.elementById(r.idOf(e)!!))
            assertEquals(r.idOf(e), r.document.idOf(e))
        }
    }

    @Test
    fun uuidOf_gives_the_xmi_uuid() {
        val r = readUmlXmi(MODEL)
        val pkg = r.elementById("_pkg")!!
        assertEquals("uuid-pkg", r.uuidOf(pkg))
        assertEquals("uuid-pkg", r.document.uuidOf(pkg))
        assertSame(pkg, r.document.elementsByUuid["uuid-pkg"])
        // an element without xmi:uuid
        assertNull(r.uuidOf(r.elementById("_obc")!!))
    }

    @Test
    fun ids_of_an_element_of_a_referenced_profile() {
        val profile = xmi(
            """
              <uml:Profile xmi:id="_prof" name="TestsatProfile" URI="http://example.org/testsat/profile">
                <packagedElement xmi:type="uml:Class" xmi:id="_prof_mass" xmi:uuid="uuid-mass" name="Mass"/>
              </uml:Profile>
            """
        )
        val model = xmi(
            """
              <uml:Model xmi:id="_m" name="Testsat">
                <packagedElement xmi:type="uml:Class" xmi:id="_obc" name="OBC">
                  <ownedAttribute xmi:type="uml:Property" xmi:id="_obc_mass" name="mass">
                    <type href="TestsatProfile.xmi#_prof_mass"/>
                  </ownedAttribute>
                </packagedElement>
              </uml:Model>
            """
        )
        val reader = UmlXmiReader(XmiReadOptions(resolver = XmiResourceResolver.fromMap(mapOf("TestsatProfile.xmi" to profile))))
        val r = reader.read("file:/models/testsat.xmi", model)
        assertEquals(emptyList(), r.errors)
        val obc = r.elementById("_obc") as Class
        val type = obc.ownedAttributeOrderedSet.single().type!!

        val profileDoc = reader.resourceSet.document("file:/models/TestsatProfile.xmi")!!
        assertSame(profileDoc, reader.resourceSet.documentOf(type))
        assertSame(r.document, reader.resourceSet.documentOf(obc))
        assertEquals("_prof_mass", reader.resourceSet.idOf(type))
        assertEquals("uuid-mass", reader.resourceSet.uuidOf(type))
        assertEquals("_prof_mass", r.idOf(type))
        assertEquals("uuid-mass", r.uuidOf(type))
        // the model document itself does not give ids of elements of other documents
        assertNull(r.document.idOf(type))
        assertEquals("_prof_mass", profileDoc.idOf(type))
    }

    @Test
    fun uuid_equal_to_another_id_gives_distinct_elements() {
        // the identity of a read element is its xmi:id, else its xmi:uuid; B's would be A's, so B's is generated
        // (the ram classes are equal when their identities are)
        val r = readUmlXmi(
            xmi(
                """
                  <uml:Model xmi:id="_m" name="Testsat">
                    <packagedElement xmi:type="uml:Class" xmi:id="X" name="A"/>
                    <packagedElement xmi:type="uml:Class" xmi:uuid="X" name="B"/>
                  </uml:Model>
                """
            )
        )
        assertEquals(emptyList(), r.errors)
        val classes = r.model!!.packagedElementSet.map { it as Class }
        assertEquals(listOf("A", "B"), classes.map { it.name })
        val (a, b) = classes
        assertNotEquals<Any>(a, b)
        assertEquals("X", a._identity)
        assertSame(b, r.document.elementsByUuid["X"])

        assertEquals("X", r.idOf(a))
        assertNull(r.uuidOf(a))
        assertNull(r.idOf(b))
        assertEquals("X", r.uuidOf(b))
    }

    @Test
    fun duplicate_id_gives_no_id_to_the_rejected_element() {
        val r = readUmlXmi(
            xmi(
                """
                  <uml:Model xmi:id="_m" name="Testsat">
                    <packagedElement xmi:type="uml:Class" xmi:id="_dup" name="First"/>
                    <packagedElement xmi:type="uml:Class" xmi:id="_dup" name="Second"/>
                  </uml:Model>
                """
            )
        )
        assertTrue(r.errors.any { it.kind == XmiIssueKind.DUPLICATE_ID && it.message.contains("_dup") })
        // the second is given a generated identity, so it is not equal to the first and is kept in the model
        val classes = r.model!!.packagedElementSet.map { it as Class }
        assertEquals(listOf("First", "Second"), classes.map { it.name })
        val (first, second) = classes
        assertNotEquals<Any>(first, second)
        assertSame(first, r.elementById("_dup"))
        assertEquals("_dup", r.idOf(first))
        assertNull(r.idOf(second))
        // still known as read from the document
        assertSame(r.document, r.reader.resourceSet.documentOf(second))
    }

    @Test
    fun same_id_in_two_documents_gives_distinct_elements() {
        // the UML PrimitiveTypes library (supplied by the reader) has the package xmi:id '_0' too
        val r = readUmlXmi(
            xmi(
                """
                  <uml:Model xmi:id="_m" name="Testsat">
                    <packagedElement xmi:type="uml:Package" xmi:id="_0" name="Structure">
                      <packagedElement xmi:type="uml:Class" xmi:id="_obc" name="OBC">
                        <ownedAttribute xmi:type="uml:Property" xmi:id="_obc_name" name="name">
                          <type href="http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String"/>
                        </ownedAttribute>
                      </packagedElement>
                    </packagedElement>
                  </uml:Model>
                """
            )
        )
        assertEquals(emptyList(), r.errors)
        val structure = r.elementById("_0") as Package
        assertEquals("Structure", structure.name)
        val obc = r.elementById("_obc") as Class
        val string = obc.ownedAttributeOrderedSet.single().type!!
        val primitiveTypes = string.owner as Package
        assertEquals("PrimitiveTypes", primitiveTypes.name)

        assertNotEquals<Any>(structure, primitiveTypes)
        assertEquals("_0", r.idOf(structure))
        assertEquals("_0", r.idOf(primitiveTypes))
        assertSame(r.document, r.reader.resourceSet.documentOf(structure))
        val ptDoc = r.reader.resourceSet.documentOf(primitiveTypes)!!
        assertTrue(ptDoc !== r.document)
        assertSame(primitiveTypes, ptDoc.elementById("_0"))
        assertSame(structure, r.document.elementById("_0"))
    }

    @Test
    fun element_created_after_reading_gives_null() {
        val r = readUmlXmi(MODEL)
        val created = r.reader.metamodel.reflective.construct("Class", "_new")!!
        assertNull(r.idOf(created))
        assertNull(r.uuidOf(created))
        assertNull(r.reader.resourceSet.documentOf(created))
        // also when it is created with the identity of a read element, and so is equal to it
        val sameIdentity = r.reader.metamodel.reflective.construct("Class", "_obc")!!
        assertEquals<Any>(r.elementById("_obc")!!, sameIdentity)
        assertNull(r.idOf(sameIdentity))
        assertNull(r.reader.resourceSet.documentOf(sameIdentity))
    }
}

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

import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.Abstraction
import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.NamedElement
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.EnumerationLiteral
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.PrimitiveType
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Class
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Port
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralReal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class test_UmlXmiReader_Testsat {

    private val SYSML = TestsatModel.SYSML_NS
    private val TEAM = TestsatModel.TEAM_NS

    private fun read(): UmlXmiReadResult {
        val resolver = XmiResourceResolver.fromMap(
            mapOf(
                "SysML.xmi" to TestsatModel.SYSML_STANDIN,
                "TestsatProfile.xmi" to TestsatModel.TEAM_PROFILE,
            )
        )
        return UmlXmiReader(XmiReadOptions(resolver = resolver)).read("file:/models/testsat.xmi", TestsatModel.MODEL)
    }

    private fun UmlXmiReadResult.named(id: String) = elementById(id) as NamedElement

    @Test
    fun model_structure() {
        val r = read()
        r.issues.forEach { println(it) }
        assertEquals(emptyList(), r.errors)
        val model = assertNotNull(r.model)
        assertEquals("Testsat Model", model.name)
        val pkgs = model.packagedElementSet.filterIsInstance<Package>().map { it.name }.toSet()
        assertEquals(setOf("Structure", "Requirements"), pkgs)

        val testsat = r.elementById("_testsat") as Class
        assertEquals(listOf("power", "obc", "tmPort", "mass", "calibration"), testsat.ownedAttributeOrderedSet.map { it.name })
        val power = testsat.ownedAttributeOrderedSet.first { it.name == "power" }
        assertEquals("composite", power.aggregation?.name)
        assertEquals("PowerSubsystem", (power.type as NamedElement).name)
        val port = testsat.ownedAttributeOrderedSet.first { it.name == "tmPort" }
        assertIs<Port>(port)
        assertEquals("TelemetryInterface", port.type?.name)

        // href to the UML primitive types, supplied by the reader itself
        val mass = testsat.ownedAttributeOrderedSet.first { it.name == "mass" }
        assertIs<PrimitiveType>(mass.type)
        assertEquals("Real", mass.type?.name)
        assertEquals(11.5, (mass.defaultValue as LiteralReal).value)

        val connector = testsat.ownedConnectorSet.single()
        assertEquals(listOf("power", "obc"), connector.endOrderedSet.map { it.role?.name })

        val der = r.elementById("_der1") as Abstraction
        assertEquals(listOf("Power Budget"), der.clientSet.map { it.name })
        assertEquals(listOf("Mass Limit"), der.supplierSet.map { it.name })
    }

    @Test
    fun unresolvable_href_is_a_proxy() {
        val r = read()
        val testsat = r.elementById("_testsat") as Class
        val cal = testsat.ownedAttributeOrderedSet.first { it.name == "calibration" }
        assertNull(cal.typeReference.resolved)
        val proxy = assertIs<XmiProxy>(cal.typeReference.reference)
        assertEquals("Missing_Library.xmi#_lib_calibration", proxy.href)
        assertEquals("file:/models/Missing_Library.xmi", proxy.uri)
        assertTrue(r.issues.any { it.kind == XmiIssueKind.MISSING_RESOURCE && it.message.contains("Missing_Library.xmi") })
    }

    @Test
    fun enumeration_literals() {
        val xmi = """
            <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
              <uml:Model xmi:id="_m" name="M">
                <packagedElement xmi:type="uml:Class" xmi:id="_c" name="C" visibility="private"/>
                <packagedElement xmi:type="uml:Class" xmi:id="_d" name="D" visibility="nowhere"/>
              </uml:Model>
            </xmi:XMI>
        """.trimIndent()
        val r = readUmlXmi(xmi)
        assertEquals("private", (r.elementById("_c") as Class).visibility?.name)
        assertNull((r.elementById("_d") as Class).visibility)
        // an unknown literal is a warning, not an error
        assertTrue(r.errors.isEmpty(), r.errors.toString())
        assertTrue(r.warnings.any { it.kind == XmiIssueKind.INVALID_VALUE && it.message.contains("'nowhere'") }, r.warnings.toString())
    }

    @Test
    fun extensions_and_tool_namespaces_are_tolerated() {
        val r = read()
        assertTrue(r.warnings.any { it.kind == XmiIssueKind.EXTENSION && it.message.contains("3 xmi:Extension") })
        assertTrue(r.warnings.any { it.kind == XmiIssueKind.UNKNOWN_NAMESPACE && it.message.contains(TestsatModel.TOOL_NS) })
        // the tool element is not a stereotype application, but is kept
        assertEquals(listOf("Settings"), r.stereotypes.otherElements.map { it.localName })
    }

    @Test
    fun stereotypes_and_tags() {
        val r = read()
        val st = r.stereotypes
        val testsat = r.named("_testsat")
        assertTrue(st.hasStereotype(testsat, SYSML, "Block"))
        assertFalse(st.hasStereotype(testsat, SYSML, "Requirement"))
        assertEquals(setOf("Testsat", "PowerSubsystem", "OnBoardComputer"), st.elementsWithStereotype(SYSML, "Block").map { (it as NamedElement).name }.toSet())

        val power = r.named("_power")
        assertEquals(true, st.tagValue(power, SYSML, "Block", "isEncapsulated")?.value)

        val req1 = r.named("_req1")
        assertEquals("TS-001", st.tagValue(req1, SYSML, "Requirement", "id")?.value)
        assertEquals("The Testsat mass shall be below 12 kg.", st.tagValue(req1, SYSML, "Requirement", "text")?.value)
        val req2 = r.named("_req2")
        assertEquals("The power budget shall have 10 % margin & more.", st.tagValue(req2, SYSML, "Requirement", "text")?.value)

        // a sub-stereotype of Requirement counts as Requirement, and inherits its tags
        val req3 = r.named("_req3")
        assertTrue(st.hasStereotype(req3, SYSML, "Requirement"))
        assertFalse(st.hasStereotype(req3, SYSML, "Requirement", includeSubStereotypes = false))
        assertTrue(st.hasStereotype(req3, TEAM, "SafetyRequirement"))
        assertEquals(setOf("Mass Limit", "Power Budget", "Battery Safety"), st.elementsWithStereotype(SYSML, "Requirement").map { (it as NamedElement).name }.toSet())
        val app = st.applicationsOf(req3).single()
        assertEquals("TestsatProfile::SafetyRequirement", app.qualifiedName)
        assertEquals("TS-003", app.tag("id")?.value)
        assertTrue(app.tag("id")!!.isTyped)
        val hazard = assertIs<EnumerationLiteral>(app.tag("hazardLevel")?.value)
        assertEquals("catastrophic", hazard.name)
        assertEquals(0.25, app.tag("margin")?.value)
        assertEquals(listOf("Reviewer A", "Reviewer B"), app.tag("reviewer")?.values)
        assertTrue(app.tag("reviewer")!!.isMany)
        assertEquals(power, app.tag("owner")?.value)

        // relationships: Satisfy/Verify/DeriveReqt all specialise Trace
        val sat = st.elementsWithStereotype(SYSML, "Satisfy").single() as Abstraction
        assertEquals("PowerSubsystem", sat.clientSet.single().name)
        assertEquals("Power Budget", sat.supplierSet.single().name)
        assertEquals(3, st.elementsWithStereotype(SYSML, "Trace").size)
        assertEquals("Mass Test", (st.elementsWithStereotype(SYSML, "TestCase").single() as NamedElement).name)
    }

    @Test
    fun application_with_missing_profile_is_kept_untyped() {
        val r = read()
        val testsat = r.named("_testsat")
        val legacy = r.stereotypes.applicationsOf(testsat).single { !it.isTyped }
        assertEquals("legacy:Tracked", legacy.qualifiedName)
        assertEquals(TestsatModel.LEGACY_NS, legacy.profileUri)
        assertEquals("T-77", legacy.tag("trackingId")?.value)
        assertEquals("kept raw", legacy.tag("note")?.value)
        assertTrue(r.stereotypes.hasStereotype(testsat, TestsatModel.LEGACY_NS, "Tracked"))
        assertTrue(r.warnings.any { it.kind == XmiIssueKind.MISSING_PROFILE && it.message.contains(TestsatModel.LEGACY_NS) })
    }

    @Test
    fun strict_mode_throws_on_first_error() {
        val bad = TestsatModel.MODEL.replace("xmi:id=\"_obc\"", "xmi:id=\"_power\"")
        val lenient = readUmlXmi(bad)
        assertTrue(lenient.errors.any { it.kind == XmiIssueKind.DUPLICATE_ID })
        var thrown: XmiReadException? = null
        try {
            readUmlXmi(bad, options = XmiReadOptions(lenient = false))
        } catch (e: XmiReadException) {
            thrown = e
        }
        assertEquals(XmiIssueKind.DUPLICATE_ID, thrown?.issue?.kind)
    }

    @Test
    fun profile_alias_maps_another_profile_version() {
        // a model written against another version of the profile namespace
        val other = "http://example.org/testsat/sysml-standin/v2"
        val model = TestsatModel.MODEL.replace("xmlns:sysml=\"$SYSML\"", "xmlns:sysml=\"$other\"")
        val options = XmiReadOptions(
            namespaces = XmiNamespaceTable.DEFAULT.withProfileAlias(other, SYSML),
            resolver = XmiResourceResolver.fromMap(mapOf("SysML.xmi" to TestsatModel.SYSML_STANDIN, "TestsatProfile.xmi" to TestsatModel.TEAM_PROFILE))
        )
        val r = UmlXmiReader(options).read("testsat.xmi", model)
        assertTrue(r.stereotypes.hasStereotype(r.named("_testsat"), SYSML, "Block"))
        assertTrue(r.stereotypes.hasStereotype(r.named("_testsat"), other, "Block"))
    }
}

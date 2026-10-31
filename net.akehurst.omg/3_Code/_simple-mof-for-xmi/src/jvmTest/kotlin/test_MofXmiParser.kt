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


package net.akehurst.omg._simple_mof_for_xmi

import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.cls
import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.model
import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.op
import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.prop
import kotlin.test.*

class test_MofXmiParser {

    @Test
    fun classes_and_generalizations() {
        assertTrue(cls("Element").isAbstract)
        assertFalse(cls("Package").isAbstract)
        assertEquals(setOf("Element"), cls("Relationship").superTypes.map { it.name }.toSet())
        assertEquals("Test.Package", cls("Package").qualifiedName)
    }

    @Test
    fun enumeration_literals() {
        val kind = model.enumList.single { it.name == "Kind" }
        assertEquals(listOf("a", "b", "in"), kind.literals.map { it.name })
        assertEquals(listOf("a", "b", "in_"), kind.literals.map { it.validName })
        assertEquals("Test-Kind-in", kind.literals.last().xmiId)
    }

    @Test
    fun property_multiplicity_and_flags() {
        prop("Element", "declaredName").let {
            assertEquals("String", it.type.name)
            assertEquals(0, it.lowerBound)
            assertEquals(1, it.upperBound)
            assertTrue(it.isOptional)
            assertFalse(it.isDerived)
        }
        prop("Element", "kind").let {
            assertEquals("Kind", it.type.name)
            assertFalse(it.isOptional)
        }
        // upper bound '-1' (as in the KerML XMI) and '*' both mean unbounded
        prop("Element", "ownedRelationship").let {
            assertEquals(-1, it.upperBound)
            assertTrue(it.isCollection)
            assertTrue(it.isOrdered)
            assertEquals(MofAggregationKind.composite, it.aggregation)
        }
        prop("Element", "ownedElement").let {
            assertEquals(-1, it.upperBound)
            assertTrue(it.isDerived)
            assertTrue(it.isDerivedOnly)
        }
        prop("Relationship", "relatedElement").let {
            assertTrue(it.isDerivedUnion)
            assertFalse(it.isDerivedOnly)
            assertFalse(it.isUnique)
        }
    }

    @Test
    fun subsetting() {
        assertEquals(setOf("relatedElement"), prop("Relationship", "ownedRelatedElement").subsettedProperty.map { it.nameInXmi }.toSet())
        assertEquals(setOf("relatedElement"), prop("Relationship", "target").subsettedProperty.map { it.nameInXmi }.toSet())
    }

    @Test
    fun association_opposites() {
        assertSame(prop("Relationship", "owningRelatedElement"), prop("Element", "ownedRelationship").opposite)
        assertSame(prop("Element", "ownedRelationship"), prop("Relationship", "owningRelatedElement").opposite)
        // an association owned end is held by the class at the other end, but is not one of its attributes
        val targetOf = cls("Element").associationOwnedAttribute.single()
        assertEquals("targetOf", targetOf.nameInXmi)
        assertTrue(targetOf.isAssociationOwned)
        assertSame(prop("Relationship", "target"), targetOf.opposite)
        assertFalse(cls("Element").ownedAttribute.contains(targetOf))
    }

    @Test
    fun derivations_are_found() {
        assertEquals("effectiveName()", prop("Element", "name").derivationOcl)
        assertEquals("ownedRelationship.ownedRelatedElement", prop("Element", "ownedElement").derivationOcl)
        assertNull(prop("Element", "declaredName").derivationOcl)
        // the expression of a multi line body is kept as written
        assertTrue(prop("Element", "qualifiedName").derivationOcl!!.startsWith("if name = null then null\n"))
    }

    @Test
    fun operations() {
        op("Element", "isNamed").let {
            assertEquals(listOf("aName"), it.parameters.map { p -> p.name })
            assertEquals("Boolean", it.returnParameter?.type?.name)
            assertFalse(it.isAbstract)
            assertEquals("name = aName", it.bodyOcl)
            assertEquals(true, it.bodyCondition?.isOcl)
        }
        op("Element", "namedOwned").let {
            assertTrue(it.returnParameter!!.isCollection)
            assertEquals("OrderedSet<Element>", it.returnTypeName)
        }
        assertTrue(op("Element", "describe").isAbstract)
        assertNull(op("Element", "describe").bodyCondition)
        assertEquals(false, op("Element", "informal").bodyCondition?.isOcl, "language is English")
        assertEquals(setOf("Test-Element-describe_"), op("Package", "describe").redefinedOperationRef)
        assertTrue(op("Package", "describe").isOverride)
        assertFalse(op("Element", "describe").isOverride)
    }
}

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


import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.utils.ReferenceImmutable
import net.akehurst.kotlinx.utils.ValueExt.mutable
import net.akehurst.omg.testmodel.api.Test_ModelBuilder
import net.akehurst.omg.testmodel.api.Test.Element
import net.akehurst.omg.testmodel.api.Test.Kind
import net.akehurst.omg.testmodel.api.Test.Package
import net.akehurst.omg.testmodel.api.Test.Relationship
import net.akehurst.omg.testmodel.api.Test.Element_NN
import net.akehurst.omg.testmodel.ram.Test_ModelFactoryRam
import kotlin.test.*

/**
 * Tests the code generated from specs/testmodel.xmi by the api and ram templates:
 * derivations translated from OCL, operations, derived unions, enumerations and opposites.
 * Each test names the OCL it exercises.
 */
class test_TestModel {

    /**
     * Root 'root'
     *   r1: Relationship
     *     a: Package 'A'
     *       r2: Relationship
     *         b: Package 'B'
     *         c: Relationship (no name)
     */
    private fun model(): Package {
        val factory = Test_ModelFactoryRam("TestFactoryRam")
        return Test_ModelBuilder.Package(factory, "root") {
            declaredName("Root")
            kind(Kind.a)
            ownedRelationshipOrderedSet {
                Relationship("r1") {
                    ownedRelatedElementOrderedSet {
                        Package("a") {
                            declaredName("A")
                            kind(Kind.b)
                            ownedRelationshipOrderedSet {
                                Relationship("r2") {
                                    ownedRelatedElementOrderedSet {
                                        Package("b") { declaredName("B") }
                                        Relationship("c") { }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Element.child(name: String): Element = ownedElementOrderedSet.first { it.name == name }

    @Test
    fun opposites_are_set() {
        val root = model()
        val r1 = root.ownedRelationshipOrderedSet.single()
        assertSame(root, r1.owningRelatedElement)
        val a = r1.ownedRelatedElementOrderedSet.single()
        assertSame(r1, a.owningRelationship)
    }

    // name = effectiveName(); Element::effectiveName() = declaredName
    // Package::effectiveName() = if declaredName = null then 'unnamed' else self.Element::effectiveName() endif
    @Test
    fun name_from_redefined_operation() {
        val root = model()
        assertEquals("Root", root.name)
        val c = root.allOwnedOrderedSet.first { it is Relationship && it.owningRelationship != null }
        // Relationship::effectiveName() = self.oclAsType(Element).effectiveName(), which must not call itself
        assertNull(c.name)
        c.declaredNameValue.mutable.set("C")
        assertEquals("C", c.name)
        val p = root.allOwnedOrderedSet.filterIsInstance<Package>().first { it.declaredName == "B" }
        p.declaredNameValue.mutable.set(null)
        assertEquals("unnamed", p.name)
    }

    // owner = if owningRelationship = null then null else owningRelationship.owningRelatedElement endif
    @Test
    fun owner() {
        val root = model()
        assertNull(root.owner)
        val a = root.child("A")
        assertSame(root, a.owner)
        assertSame(a, a.child("B").owner)
    }

    // ownedElement = ownedRelationship.ownedRelatedElement
    @Test
    fun ownedElement_collects_over_relationships() {
        val root = model()
        assertEquals(listOf("A"), root.ownedElementOrderedSet.map { it.name })
        val a = root.child("A")
        assertEquals(listOf("B", null), a.ownedElementOrderedSet.map { it.name })
    }

    // allOwned = ownedElement->closure(ownedElement): the source elements and all reached from them, not self
    @Test
    fun allOwned_is_closure_including_source() {
        val root = model()
        val a = root.child("A")
        val b = a.child("B")
        val c = a.ownedElementOrderedSet.last()
        assertEquals(setOf(a, b, c), root.allOwnedOrderedSet.toSet())
        assertEquals(setOf(b, c), a.allOwnedOrderedSet.toSet())
        assertTrue(b.allOwnedOrderedSet.isEmpty())
    }

    // ownedPackage = ownedElement->selectByKind(Package)
    @Test
    fun ownedPackage_selectByKind() {
        val root = model()
        val a = root.child("A")
        assertEquals(listOf("B"), a.ownedPackageOrderedSet.map { it.name })
    }

    // isA = kind = Kind::a
    @Test
    fun isA_compares_enum_literal() {
        val root = model()
        assertTrue(root.isA)
        assertFalse(root.child("A").isA)
        assertFailsWith<IllegalStateException>("kind is required and not set") { root.child("A").child("B").isA }
    }

    // relationshipCount = ownedRelationship->size()
    @Test
    fun relationshipCount_size() {
        val root = model()
        assertEquals(1L, root.relationshipCount)
        assertEquals(0L, root.child("A").child("B").relationshipCount)
    }

    // qualifiedName = if name = null then null else if owner = null or owner.qualifiedName = null then name
    //                 else owner.qualifiedName + '::' + name endif endif
    @Test
    fun qualifiedName_concatenates_strings() {
        val root = model()
        val a = root.child("A")
        assertEquals("Root", root.qualifiedName)
        assertEquals("Root::A", a.qualifiedName)
        assertEquals("Root::A::B", a.child("B").qualifiedName)
        assertNull(a.ownedElementOrderedSet.last().qualifiedName)
    }

    // derived values are computed on each access, so they follow changes to the stored values
    @Test
    fun derived_values_follow_changes() {
        val root = model()
        val a = root.child("A")
        val b = a.child("B")
        a.declaredNameValue.mutable.set("A2")
        assertEquals("Root::A2::B", b.qualifiedName)

        val r2 = a.ownedRelationshipOrderedSet.single()
        r2.ownedRelatedElementOrderedSet.mutable.remove(b)
        assertNull(b.owner)
        assertEquals("B", b.qualifiedName)
        assertFalse(root.allOwnedOrderedSet.contains(b))
        assertEquals(1L, a.relationshipCount)
    }

    // isNamed(aName) = name = aName
    @Test
    fun operation_with_parameter() {
        val root = model()
        assertTrue(root.isNamed("Root"))
        assertFalse(root.isNamed("A"))
    }

    // namedOwned() = ownedElement->select(e | e.name <> null)
    @Test
    fun operation_select() {
        val a = model().child("A")
        assertEquals(listOf("B"), a.namedOwned().map { it.name })
    }

    // Element::describe() is abstract; Package::describe() = 'package';
    // Relationship::describe() = 'relationship to ' + target->size().toString()
    @Test
    fun operation_redefinitions() {
        val root = model()
        assertEquals("package", root.describe())
        assertEquals("relationship to 0", root.ownedRelationshipOrderedSet.single().describe())
    }

    // operations that are not translated are generated with a TODO body
    @Test
    fun untranslated_operations_and_derivations_are_todo() {
        val root = model()
        // depth(): 'owner.depth() + 1' is arithmetic on a possibly null value
        assertFailsWith<NotImplementedError> { root.depth() }
        // informal(): the body is not OCL
        assertFailsWith<NotImplementedError> { root.informal() }
        // everything = Element.allInstances()
        assertFailsWith<NotImplementedError> { root.everythingSet.size }
    }

    // Relationship::relatedElement is a derived union of owningRelatedElement, ownedRelatedElement and target
    @Test
    fun derived_union() {
        val root = model()
        val r1 = root.ownedRelationshipOrderedSet.single()
        val a = root.child("A")
        assertEquals(listOf(root, a), r1.relatedElementList)

        val b = a.child("B")
        r1.targetOrderedSetReference.mutable.add(ReferenceImmutable("b", b))
        assertEquals(listOf(root, a, b), r1.relatedElementList)
        assertEquals("relationship to 1", r1.describe())
    }

    // Relationship::target is opposite to an association owned end, which is kept in the ram but not in the api
    @Test
    fun association_owned_opposite() {
        val root = model()
        val r1 = root.ownedRelationshipOrderedSet.single()
        val b = root.child("A").child("B")
        r1.targetOrderedSetReference.mutable.add(ReferenceImmutable("b", b))
        assertEquals(setOf(r1), (b as Element_NN).A_target_targetOf_targetOfSet)
        r1.targetOrderedSetReference.mutable.remove(ReferenceImmutable("b", b))
        assertTrue(b.A_target_targetOf_targetOfSet.isEmpty())
    }

    @Test
    fun enumeration_literals() {
        // 'in' is a Kotlin keyword, so the literal is 'in_'
        assertEquals(listOf("a", "b", "in_"), Kind.entries.map { it.name })
    }
}

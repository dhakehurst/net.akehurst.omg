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

import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.model
import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.op
import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.prop
import kotlin.test.*

/**
 * The OCL to Kotlin translation, for the properties of Element in the test model.
 * The target property sets the expected Kotlin type: qualifiedName String?, isA Boolean,
 * relationshipCount Long, ownedElement OrderedSet<Element>, owner Element?.
 */
class test_OclToKotlin {

    private fun translate(property: String, ocl: String): String {
        val (code, reason) = OclToKotlin(model).translateOrReason(prop("Element", property), ocl)
        return code ?: "UNTRANSLATABLE: $reason"
    }

    private fun check(property: String, ocl: String, expected: String) = assertEquals(expected, translate(property, ocl), ocl)

    @Test
    fun navigation_and_names() {
        check("qualifiedName", "declaredName", "self.declaredName")
        check("qualifiedName", "self.declaredName", "self.declaredName")
        check("qualifiedName", "_'declaredName'", "self.declaredName")
        check("qualifiedName", "owner.name", "self.owner?.name")
        check("owner", "noSuchThing", "UNTRANSLATABLE: unknown name 'noSuchThing'")
    }

    @Test
    fun strings() {
        check("qualifiedName", "'a' + 'b'", "(\"a\" + \"b\")")
        check("qualifiedName", "declaredName.concat('x')", "((self.declaredName ?: error(\"concat with null\")) + \"x\")")
    }

    @Test
    fun let_and_if() {
        check("qualifiedName", "let n : String = declaredName in if n = null then 'x' else n endif", "run { val _n = self.declaredName; (if ((_n == null)) \"x\" else _n) }")
        check("owner", "if ownedElement->isEmpty() then null else ownedElement->first() endif", "(if (self.ownedElementOrderedSet.none()) null else self.ownedElementOrderedSet.firstOrNull())")
    }

    @Test
    fun booleans() {
        check("isA", "true and not false", "(true && !false)")
        check("isA", "declaredName <> null implies kind = Kind::b", "(!(self.declaredName != null) || (self.kind == Kind.b))")
    }

    @Test
    fun enumerations() {
        check("isA", "kind = Kind::a", "(self.kind == Kind.a)")
        check("isA", "kind = 'a'", "UNTRANSLATABLE: comparison of 'Kind' with 'String'")
    }

    @Test
    fun integers() {
        check("isA", "relationshipCount > 1 or relationshipCount <= 0", "((self.relationshipCount > 1L) || (self.relationshipCount <= 0L))")
        check("relationshipCount", "ownedElement->size() + 1", "(self.ownedElementOrderedSet.count().toLong() + 1L)")
        check("relationshipCount", "Sequence{1..3}->size()", "(1L..3L).toList().count().toLong()")
        check("relationshipCount", "2 * 3", "UNTRANSLATABLE: multiplication")
    }

    @Test
    fun types() {
        check("isA", "owner.oclIsKindOf(Package)", "(self.owner is Package)")
        check("isA", "oclIsKindOf(Package)", "(self is Package)")
        check("owner", "owner.oclAsType(Package)", "(self.owner as Package?)")
        check("owner", "ownedElement->selectByKind(Package)->first()", "self.ownedElementOrderedSet.filterIsInstance<Package>().firstOrNull()")
        check("isA", "owner.oclIsUndefined()", "UNTRANSLATABLE: unknown operation 'oclIsUndefined' of 'Element'")
    }

    @Test
    fun collection_queries() {
        check("isA", "ownedElement->isEmpty()", "self.ownedElementOrderedSet.none()")
        check("isA", "ownedElement->notEmpty()", "self.ownedElementOrderedSet.any()")
        check("isA", "ownedElement->forAll(e | e.isA)", "self.ownedElementOrderedSet.all { _e -> _e.isA }")
        check("isA", "ownedElement->exists(isA)", "self.ownedElementOrderedSet.any { _e1 -> _e1.isA }")
        check("isA", "ownedElement->includes(self)", "self.ownedElementOrderedSet.contains(self)")
        check("isA", "ownedElement->excludes(self)", "!self.ownedElementOrderedSet.contains(self)")
        check("isA", "ownedElement->includesAll(allOwned)", "self.allOwnedOrderedSet.all { it in self.ownedElementOrderedSet }")
        check("owner", "ownedElement->first()", "self.ownedElementOrderedSet.firstOrNull()")
        check("owner", "ownedElement->last()", "self.ownedElementOrderedSet.lastOrNull()")
        check("owner", "ownedElement->at(1)", "self.ownedElementOrderedSet.elementAtOrNull((1L).toInt() - 1)")
        check("owner", "ownedElement->any(isA)", "self.ownedElementOrderedSet.firstOrNull { _e1 -> _e1.isA }")
        check("relationshipCount", "ownedElement->iterate(e; acc : Integer = 0 | acc + e.relationshipCount)", "self.ownedElementOrderedSet.fold(0L) { _acc, _e -> (_acc + _e.relationshipCount) }")
    }

    @Test
    fun collection_results() {
        check("ownedElement", "ownedRelationship.ownedRelatedElement", "self.ownedRelationshipOrderedSet.flatMap { it.ownedRelatedElementOrderedSet }.toOrderedSet()")
        check("ownedElement", "ownedRelationship->collect(ownedRelatedElement)->flatten()", "self.ownedRelationshipOrderedSet.flatMap { _e1 -> _e1.ownedRelatedElementOrderedSet }.toOrderedSet()")
        check("ownedElement", "ownedElement->reject(isA)", "self.ownedElementOrderedSet.filterNot { _e1 -> _e1.isA }.toOrderedSet()")
        check("ownedElement", "ownedElement->including(self)", "(self.ownedElementOrderedSet + listOf(self)).toOrderedSet()")
        check("ownedElement", "ownedElement->excluding(self)", "self.ownedElementOrderedSet.filter { it != self }.toOrderedSet()")
        check("ownedElement", "ownedElement->union(allOwned)", "(self.ownedElementOrderedSet + self.allOwnedOrderedSet).toOrderedSet()")
        check("ownedElement", "ownedElement->intersection(allOwned)", "self.ownedElementOrderedSet.filter { it in self.allOwnedOrderedSet }.distinct().toOrderedSet()")
        check("ownedElement", "ownedElement->asOrderedSet()", "self.ownedElementOrderedSet.distinct().toOrderedSet()")
        check("ownedElement", "Sequence{self, owner}", "listOfNotNull(self, self.owner).toOrderedSet()")
        check("ownedElement", "OrderedSet{}", "emptyList<Element>().toOrderedSet()")
        check("ownedElement", "Element.allInstances()", "UNTRANSLATABLE: unknown name 'Element'")
    }

    // OCL closure: the source elements and everything reached from them by the body
    @Test
    fun closure() {
        check("ownedElement", "ownedElement->closure(ownedElement)", "self.ownedElementOrderedSet.toList().distinct().transitiveClosure { _e1 -> (_e1.ownedElementOrderedSet).toList() }.toOrderedSet()")
        check("ownedElement", "OrderedSet{self}->closure(owner)", "listOfNotNull(self).toList().distinct().transitiveClosure { _e1 -> listOfNotNull(_e1.owner) }.toOrderedSet()")
        check("ownedElement", "self->closure(e | e.ownedElement)", "listOf(self).toList().distinct().transitiveClosure { _e -> (_e.ownedElementOrderedSet).toList() }.toOrderedSet()")
    }

    @Test
    fun derivations_of_the_test_model() {
        assertEquals("self.effectiveName()", prop("Element", "name").derivationKotlin)
        assertEquals("(if ((self.owningRelationship == null)) null else self.owningRelationship?.owningRelatedElement)", prop("Element", "owner").derivationKotlin)
        assertEquals("self.ownedElementOrderedSet.filterIsInstance<Package>().toOrderedSet()", prop("Element", "ownedPackage").derivationKotlin)
        assertNull(prop("Element", "everything").derivationKotlin)
        assertTrue(prop("Element", "everything").derivationDoc.contains("Not translated: unknown name 'Element'."))
    }

    // operation bodies are default methods of the api interface: 'self' is 'this', parameters are in scope
    @Test
    fun operations_of_the_test_model() {
        assertEquals("this.declaredName", op("Element", "effectiveName").bodyKotlin)
        assertEquals("(this.name == aName)", op("Element", "isNamed").bodyKotlin)
        assertEquals("this.ownedElementOrderedSet.filter { _e -> (_e.name != null) }.toOrderedSet()", op("Element", "namedOwned").bodyKotlin)
        // self.T::op() calls the redefined operation
        assertEquals("(if ((this.declaredName == null)) \"unnamed\" else super<Element>.effectiveName())", op("Package", "effectiveName").bodyKotlin)
        assertEquals("(\"relationship to \" + this.targetOrderedSet.count().toLong().toString())", op("Relationship", "describe").bodyKotlin)
        // self.oclAsType(T).op() also calls the operation as defined by T, not the override (which would recurse)
        assertEquals("super<Element>.effectiveName()", op("Relationship", "effectiveName").bodyKotlin)
        // not translated: abstract, not OCL, or outside the subset handled
        assertEquals("TODO(\"Operation 'Test.Element.describe' is not implemented\")", op("Element", "describe").bodyKotlin)
        assertEquals("TODO(\"Operation 'Test.Element.informal' is not implemented\")", op("Element", "informal").bodyKotlin)
        assertTrue(op("Element", "informal").bodyDoc.contains("Not translated: the body is not OCL."))
        assertEquals("TODO(\"Operation 'Test.Element.depth' is not implemented\")", op("Element", "depth").bodyKotlin)
        assertTrue(op("Element", "depth").bodyDoc.contains("Not translated: arithmetic on non integers."))
    }
}

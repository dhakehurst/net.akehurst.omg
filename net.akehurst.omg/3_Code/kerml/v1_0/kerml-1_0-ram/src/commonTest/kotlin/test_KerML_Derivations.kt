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


package net.akehurst.omg.kerml.v1_0.api

import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Classifiers.Classifier
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Classifiers.Subclassification
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.Feature
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.FeatureTyping
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.Redefinition
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.Subsetting
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Types.FeatureMembership
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.kerml.v1_0.api.KerML.Root.Namespaces.OwningMembership
import net.akehurst.omg.kerml.v1_0.ram.KerML_ModelFactoryRam
import kotlin.test.*

/**
 * Derived properties of KerML computed by code translated from the OCL in the spec,
 * on a model built from settable properties only:
 *
 *   package P
 *     classifier A { feature x; }
 *     classifier B specializes A { feature y : A subsets x; feature x redefines A::x; }
 */
class test_KerML_Derivations {

    private class Model(val p: Package, val a: Classifier, val b: Classifier, val x: Feature, val y: Feature, val bx: Feature)

    private fun model(): Model {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val p = KerML_ModelBuilder.Package(factory, "P") {
            declaredName("P")
            ownedRelationshipOrderedSet {
                OwningMembership("mA") {
                    ownedRelatedElementOrderedSet {
                        Classifier("A") {
                            declaredName("A")
                            ownedRelationshipOrderedSet {
                                FeatureMembership("fmAx") { ownedRelatedElementOrderedSet { Feature("A::x") { declaredName("x") } } }
                            }
                        }
                    }
                }
                OwningMembership("mB") {
                    ownedRelatedElementOrderedSet {
                        Classifier("B") {
                            declaredName("B")
                            ownedRelationshipOrderedSet {
                                Subclassification("B<A") { }
                                FeatureMembership("fmBy") {
                                    ownedRelatedElementOrderedSet {
                                        Feature("B::y") {
                                            declaredName("y")
                                            ownedRelationshipOrderedSet {
                                                FeatureTyping("y:A") { }
                                                Subsetting("y<x") { }
                                            }
                                        }
                                    }
                                }
                                FeatureMembership("fmBx") {
                                    ownedRelatedElementOrderedSet {
                                        Feature("B::x") {
                                            declaredName("x")
                                            ownedRelationshipOrderedSet { Redefinition("x<x") { } }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val a = p.ownedElementOrderedSet.first() as Classifier
        val b = p.ownedElementOrderedSet.last() as Classifier
        val x = a.ownedElementOrderedSet.single() as Feature
        val (y, bx) = b.ownedElementOrderedSet.filterIsInstance<Feature>().let { Pair(it[0], it[1]) }
        // the references, which the builder does not set
        (b.ownedRelationshipOrderedSet.first() as Subclassification).let {
            it.subclassifierReference.mutable.set("B", b)
            it.superclassifierReference.mutable.set("A", a)
        }
        (y.ownedRelationshipOrderedSet.first() as FeatureTyping).let {
            it.typedFeatureReference.mutable.set("B::y", y)
            it.typeReference.mutable.set("A", a)
        }
        (y.ownedRelationshipOrderedSet.last() as Subsetting).let {
            it.subsettingFeatureReference.mutable.set("B::y", y)
            it.subsettedFeatureReference.mutable.set("A::x", x)
        }
        (bx.ownedRelationshipOrderedSet.single() as Redefinition).let {
            it.redefiningFeatureReference.mutable.set("B::x", bx)
            it.redefinedFeatureReference.mutable.set("A::x", x)
        }
        return Model(p, a, b, x, y, bx)
    }

    // name = effectiveName(); Feature::effectiveName() is declaredName when it is set
    @Test
    fun name() {
        val m = model()
        assertEquals("P", m.p.name)
        assertEquals("B", m.b.name)
        assertEquals("y", m.y.name)
    }

    // owner = owningRelationship.owningRelatedElement; ownedElement = ownedRelationship.ownedRelatedElement
    @Test
    fun owner_and_ownedElement() {
        val m = model()
        assertNull(m.p.owner)
        assertSame(m.p, m.b.owner)
        assertSame(m.b, m.y.owner)
        assertEquals(listOf(m.a, m.b), m.p.ownedElementOrderedSet.toList())
        assertEquals(listOf(m.y, m.bx), m.b.ownedElementOrderedSet.toList())
    }

    // ownedMembership = ownedRelationship->selectByKind(Membership)
    // ownedFeatureMembership = ownedRelationship->selectByKind(FeatureMembership)
    @Test
    fun memberships() {
        val m = model()
        assertEquals(listOf("mA", "mB"), m.p.ownedMembershipOrderedSet.map { it._identity })
        assertTrue(m.p.ownedMembershipOrderedSet.all { it is OwningMembership })
        assertEquals(listOf("fmBy", "fmBx"), m.b.ownedFeatureMembershipOrderedSet.map { it._identity })
        assertEquals(listOf("fmBy", "fmBx"), m.b.ownedMembershipOrderedSet.map { it._identity })
    }

    // ownedSpecialization = ownedRelationship->selectByKind(Specialization)->select(s | s.specific = self)
    // Subclassification::subclassifier redefines specific and superclassifier redefines general
    @Test
    fun ownedSpecialization() {
        val m = model()
        val s = m.b.ownedSpecializationOrderedSet.single()
        assertSame(m.b, s.specific)
        assertSame(m.a, s.general)
        assertTrue(m.a.ownedSpecializationOrderedSet.isEmpty())
        // a Specialization owned by B whose specific is not B is not one of its ownedSpecializations
        (s as Subclassification).subclassifierReference.mutable.set("A", m.a)
        assertTrue(m.b.ownedSpecializationOrderedSet.isEmpty())
    }

    // ownedTyping = ownedSpecialization->selectByKind(FeatureTyping)
    // ownedSubsetting = ownedSpecialization->selectByKind(Subsetting)
    // ownedRedefinition = ownedSubsetting->selectByKind(Redefinition)
    @Test
    fun feature_specializations() {
        val m = model()
        assertSame(m.a, m.y.ownedTypingOrderedSet.single().type)
        assertSame(m.x, m.y.ownedSubsettingSet.single().subsettedFeature)
        assertTrue(m.y.ownedRedefinitionSet.isEmpty())
        // a Redefinition is a Subsetting
        assertSame(m.x, m.bx.ownedRedefinitionSet.single().redefinedFeature)
        assertEquals(m.bx.ownedRedefinitionSet, m.bx.ownedSubsettingSet)
        assertTrue(m.bx.ownedTypingOrderedSet.isEmpty())
    }
}

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


import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.omg.sysml.v2_0.api.SysML_ModelBuilder
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Classifiers.Subclassification
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Features.FeatureTyping
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Enumerations.EnumerationDefinition
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Parts.PartDefinition
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Parts.PartUsage
import net.akehurst.omg.sysml.v2_0.ram.SysML_ModelFactoryRam
import kotlin.test.*

/**
 * Derived properties of SysML computed by code translated from the OCL in the spec,
 * on a model built from settable properties only:
 *
 *   package P
 *     part def Vehicle;
 *     part def Engine;
 *     part def Car :> Vehicle { part engine : Engine; ref part driver; }
 *     enum def Colour { red; green; }
 */
class test_SysML_Derivations {

    private class Model(val p: Package, val vehicle: PartDefinition, val engineDef: PartDefinition, val car: PartDefinition, val engine: PartUsage, val driver: PartUsage, val colour: EnumerationDefinition)

    private fun model(): Model {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val p = SysML_ModelBuilder.Package(factory, "P") {
            declaredName("P")
            ownedRelationshipOrderedSet {
                OwningMembership("mVehicle") { ownedRelatedElementOrderedSet { PartDefinition("Vehicle") { declaredName("Vehicle") } } }
                OwningMembership("mEngine") { ownedRelatedElementOrderedSet { PartDefinition("Engine") { declaredName("Engine") } } }
                OwningMembership("mCar") {
                    ownedRelatedElementOrderedSet {
                        PartDefinition("Car") {
                            declaredName("Car")
                            ownedRelationshipOrderedSet {
                                Subclassification("Car:>Vehicle") { }
                                FeatureMembership("fmEngine") {
                                    ownedRelatedElementOrderedSet {
                                        PartUsage("Car::engine") {
                                            declaredName("engine")
                                            isComposite(true)
                                            ownedRelationshipOrderedSet { FeatureTyping("engine:Engine") { } }
                                        }
                                    }
                                }
                                FeatureMembership("fmDriver") {
                                    ownedRelatedElementOrderedSet {
                                        PartUsage("Car::driver") {
                                            declaredName("driver")
                                            isComposite(false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                OwningMembership("mColour") {
                    ownedRelatedElementOrderedSet {
                        EnumerationDefinition("Colour") {
                            declaredName("Colour")
                            ownedRelationshipOrderedSet {
                                VariantMembership("vRed") { ownedRelatedElementOrderedSet { EnumerationUsage("red") { declaredName("red") } } }
                                VariantMembership("vGreen") { ownedRelatedElementOrderedSet { EnumerationUsage("green") { declaredName("green") } } }
                            }
                        }
                    }
                }
            }
        }
        val (vehicle, engineDef, car) = p.ownedElementOrderedSet.filterIsInstance<PartDefinition>()
        val (engine, driver) = car.ownedElementOrderedSet.filterIsInstance<PartUsage>()
        val colour = p.ownedElementOrderedSet.filterIsInstance<EnumerationDefinition>().single()
        // the references, which the builder does not set
        (car.ownedRelationshipOrderedSet.first() as Subclassification).let {
            it.subclassifierReference.mutable.set("Car", car)
            it.superclassifierReference.mutable.set("Vehicle", vehicle)
        }
        (engine.ownedRelationshipOrderedSet.single() as FeatureTyping).let {
            it.typedFeatureReference.mutable.set("Car::engine", engine)
            it.typeReference.mutable.set("Engine", engineDef)
        }
        return Model(p, vehicle, engineDef, car, engine, driver, colour)
    }

    @Test
    fun names_and_owners() {
        val m = model()
        assertEquals(listOf("Vehicle", "Engine", "Car", "Colour"), m.p.ownedElementOrderedSet.map { it.name })
        assertSame(m.car, m.engine.owner)
        assertSame(m.p, m.car.owner)
    }

    // ownedSpecialization = ownedRelationship->selectByKind(Specialization)->select(s | s.specific = self)
    @Test
    fun specialization() {
        val m = model()
        val s = m.car.ownedSpecializationOrderedSet.single()
        assertSame(m.car, s.specific)
        assertSame(m.vehicle, s.general)
        assertTrue(m.vehicle.ownedSpecializationOrderedSet.isEmpty())
    }

    // ownedTyping = ownedSpecialization->selectByKind(FeatureTyping)
    @Test
    fun typing() {
        val m = model()
        assertSame(m.engineDef, m.engine.ownedTypingOrderedSet.single().type)
        assertTrue(m.driver.ownedTypingOrderedSet.isEmpty())
    }

    // Usage::isReference = not isComposite
    @Test
    fun isReference() {
        val m = model()
        assertEquals(false, m.engine.isReference)
        assertEquals(true, m.driver.isReference)
    }

    // ownedFeatureMembership = ownedRelationship->selectByKind(FeatureMembership)
    // variantMembership = ownedMembership->selectByKind(VariantMembership)
    @Test
    fun memberships() {
        val m = model()
        assertEquals(listOf("fmEngine", "fmDriver"), m.car.ownedFeatureMembershipOrderedSet.map { it._identity })
        assertTrue(m.car.variantMembershipSet.isEmpty())
        assertEquals(setOf("vRed", "vGreen"), m.colour.variantMembershipSet.map { it._identity }.toSet())
    }
}

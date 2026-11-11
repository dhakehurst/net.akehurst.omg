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

package net.akehurst.omg.uml.v2_5_1

import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.AggregationKind
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Model
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.api.UML.StateMachines.State
import net.akehurst.omg.uml.v2_5_1.api.UML.StateMachines.StateMachine
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Class
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelBuilder
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import kotlin.test.*

/**
 * Derived properties and operations of UML computed by code translated from the OCL in the spec,
 * on a model built from settable properties only:
 *
 *   model M
 *     package P
 *       class Vehicle { a : [2..*] (composite); b; }
 *       class Car specializes Vehicle
 *       state machine SM { region R { state S { region R1; region R2; } state T; } }
 */
class test_UML_Derivations {

    private fun model(): Model {
        val factory = UML_ModelFactoryRam("TestFactoryRam")
        val m = UML_ModelBuilder.Model(factory, "M") {
            name("M")
            packagedElementSet {
                Package("P") {
                    name("P")
                    packagedElementSet {
                        Class("Vehicle") {
                            name("Vehicle")
                            ownedAttributeOrderedSet {
                                Property("Vehicle::a") {
                                    name("a")
                                    aggregation(AggregationKind.composite)
                                    lowerValue { LiteralInteger("a.lower") { value(2) } }
                                    upperValue { LiteralUnlimitedNatural("a.upper") { value(-1) } }
                                }
                                Property("Vehicle::b") { name("b") }
                            }
                        }
                        Class("Car") {
                            name("Car")
                            generalizationSet { Generalization("Car->Vehicle") { } }
                        }
                        StateMachine("SM") {
                            name("SM")
                            regionSet {
                                Region("R") {
                                    name("R")
                                    subvertexSet {
                                        State("S") {
                                            name("S")
                                            regionSet {
                                                Region("R1") { name("R1") }
                                                Region("R2") { name("R2") }
                                            }
                                        }
                                        State("T") { name("T") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // the reference, which the builder does not set
        val car = m.allOwnedElements().filterIsInstance<Class>().first { it.name == "Car" }
        val vehicle = m.allOwnedElements().filterIsInstance<Class>().first { it.name == "Vehicle" }
        car.generalizationSet.single().generalReference.mutable.set("Vehicle", vehicle)
        return m
    }

    private fun Model.cls(name: String) = allOwnedElements().filterIsInstance<Class>().first { it.name == name }
    private fun Model.state(name: String) = allOwnedElements().filterIsInstance<State>().first { it.name == name }

    // nestedPackage = packagedElement->select(oclIsKindOf(Package))->collect(oclAsType(Package))->asSet()
    // ownedType = packagedElement->select(oclIsKindOf(Type))->collect(oclAsType(Type))->asSet()
    @Test
    fun package_contents() {
        val m = model()
        val p = m.nestedPackageSet.single()
        assertEquals("P", p.name)
        assertEquals(setOf("Vehicle", "Car", "SM"), p.ownedTypeSet.map { it.name }.toSet())
        assertTrue(p.nestedPackageSet.isEmpty())
    }

    // qualifiedName = if self.name <> null and self.allNamespaces()->select(ns | ns.name = null)->isEmpty()
    //   then self.allNamespaces()->iterate(ns : Namespace; agg: String = self.name | ns.name.concat(self.separator()).concat(agg))
    //   else null endif
    @Test
    fun qualifiedName() {
        val m = model()
        assertEquals("M", m.qualifiedName)
        assertEquals("M::P", m.nestedPackageSet.single().qualifiedName)
        assertEquals("M::P::Vehicle", m.cls("Vehicle").qualifiedName)
        assertEquals("M::P::Vehicle::a", m.cls("Vehicle").ownedAttributeOrderedSet.first().qualifiedName)
    }

    // isComposite = aggregation = AggregationKind::composite
    // lower = lowerBound(); upper = upperBound(); a missing bound is 1
    @Test
    fun property_isComposite_and_multiplicity() {
        val (a, b) = model().cls("Vehicle").ownedAttributeOrderedSet.toList()
        assertEquals(true, a.isComposite)
        assertEquals(false, b.isComposite)
        assertEquals(2L, a.lower)
        assertEquals(-1L, a.upper, "* is -1")
        assertEquals(1L, b.lower)
        assertEquals(1L, b.upper)
        assertTrue(a.isMultivalued())
        assertFalse(b.isMultivalued())
    }

    // general = self.parents(); parents() = generalization.general->asSet()
    // superClass = self.general()->select(oclIsKindOf(Class))->collect(oclAsType(Class))->asSet()
    @Test
    fun generalization() {
        val m = model()
        val car = m.cls("Car")
        val vehicle = m.cls("Vehicle")
        assertSame(car, car.generalizationSet.single().specific)
        assertEquals(setOf(vehicle), car.generalSet)
        assertEquals(setOf(vehicle), car.superClassSet)
        assertTrue(vehicle.generalSet.isEmpty())
    }

    // isComposite = region->notEmpty(); isOrthogonal = region->size() > 1; isSimple = region->isEmpty() and not isSubmachineState
    @Test
    fun state_kinds() {
        val m = model()
        val s = m.state("S")
        val t = m.state("T")
        assertEquals(true, s.isComposite)
        assertEquals(true, s.isOrthogonal)
        assertEquals(false, s.isSimple)
        assertEquals(false, t.isComposite)
        assertEquals(false, t.isOrthogonal)
        assertEquals(true, t.isSimple)
    }

    // containingStateMachine(): through the container regions of a vertex
    @Test
    fun containingStateMachine() {
        val m = model()
        val sm = m.allOwnedElements().filterIsInstance<StateMachine>().single()
        assertSame(sm, m.state("S").containingStateMachine())
        assertSame(sm, m.state("S").regionSet.first().containingStateMachine())
    }
}

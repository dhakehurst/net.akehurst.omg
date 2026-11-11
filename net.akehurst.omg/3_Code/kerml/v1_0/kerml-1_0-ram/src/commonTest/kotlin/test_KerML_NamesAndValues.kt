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
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.Feature
import net.akehurst.omg.kerml.v1_0.api.KerML.Core.Features.Redefinition
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Expressions.ConstructorExpression
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Expressions.LiteralInteger
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.FeatureValues.FeatureValue
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Functions.Function_
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.kerml.v1_0.api.KerML.Root.Namespaces.Membership
import net.akehurst.omg.kerml.v1_0.ram.KerML_ModelFactoryRam
import kotlin.test.*

/**
 * Element::escapedName (a Kotlin body in the patched XMI), Element::qualifiedName,
 * Function::isModelLevelEvaluable, Feature::valuation and InstantiationExpression::argument.
 */
class test_KerML_NamesAndValues {

    private fun factory() = KerML_ModelFactoryRam("TestFactoryRam")

    private fun feature(name: String?, shortName: String? = null): Feature {
        val p = KerML_ModelBuilder.Package(factory(), "P") {
            ownedRelationshipOrderedSet {
                OwningMembership("m") {
                    ownedRelatedElementOrderedSet {
                        Feature("f") {
                            declaredName(name)
                            declaredShortName(shortName)
                        }
                    }
                }
            }
        }
        return p.ownedElementOrderedSet.single() as Feature
    }

    // name, else shortName; as is if a basic name, else quoted with special characters escaped
    @Test
    fun escapedName() {
        assertEquals("x", feature("x").escapedName())
        assertEquals("_x1", feature("_x1").escapedName())
        assertEquals("'1x'", feature("1x").escapedName())
        assertEquals("'a b'", feature("a b").escapedName())
        assertEquals("'+'", feature("+").escapedName())
        assertEquals("'it\\'s'", feature("it's").escapedName())
        assertEquals("'a\\\\b\\n'", feature("a\\b\n").escapedName())
        assertEquals("s", feature(null, "s").escapedName())
        assertEquals("x", feature("x", "s").escapedName())
        assertNull(feature(null).escapedName())
    }

    /**
     *   package R { package DataFunctions { function '+'; function foo; } }
     */
    private fun library(): Triple<Package, Function_, Function_> {
        val r = KerML_ModelBuilder.Package(factory(), "R") {
            declaredName("R")
            ownedRelationshipOrderedSet {
                OwningMembership("mDF") {
                    ownedRelatedElementOrderedSet {
                        Package("DF") {
                            declaredName("DataFunctions")
                            ownedRelationshipOrderedSet {
                                OwningMembership("mPlus") { ownedRelatedElementOrderedSet { Function_("plus") { declaredName("+") } } }
                                OwningMembership("mFoo") { ownedRelatedElementOrderedSet { Function_("foo") { declaredName("foo") } } }
                            }
                        }
                    }
                }
            }
        }
        val df = r.ownedElementOrderedSet.single() as Package
        val (plus, foo) = df.ownedElementOrderedSet.map { it as Function_ }
        return Triple(df, plus, foo)
    }

    // a root namespace has no qualifiedName; its members are not qualified by it
    @Test
    fun qualifiedName() {
        val (df, plus, foo) = library()
        assertNull(df.owningNamespace?.qualifiedName)
        assertEquals("DataFunctions", df.qualifiedName)
        assertEquals("DataFunctions::'+'", plus.qualifiedName)
        assertEquals("DataFunctions::foo", foo.qualifiedName)
    }

    // true for the functions marked 'Yes' in KerML Tables 5 and 7
    @Test
    fun isModelLevelEvaluable() {
        val (_, plus, foo) = library()
        assertEquals(true, plus.isModelLevelEvaluable)
        assertEquals(false, foo.isModelLevelEvaluable)
    }

    // valuation = ownedMembership->selectByKind(FeatureValue)->first()
    @Test
    fun valuation() {
        val p = KerML_ModelBuilder.Package(factory(), "P") {
            ownedRelationshipOrderedSet {
                OwningMembership("m") {
                    ownedRelatedElementOrderedSet {
                        Feature("f") {
                            declaredName("f")
                            ownedRelationshipOrderedSet {
                                FeatureValue("fv") { ownedRelatedElementOrderedSet { LiteralInteger("five") { value(5) } } }
                            }
                        }
                        Feature("g") { declaredName("g") }
                    }
                }
            }
        }
        val (f, g) = p.ownedElementOrderedSet.map { it as Feature }
        val fv = f.valuation
        assertNotNull(fv)
        assertEquals("fv", fv._identity)
        assertSame(f, fv.featureWithValue)
        assertEquals(5L, (fv.value as LiteralInteger).value)
        assertNull(g.valuation)
    }

    /**
     *   package P {
     *     classifier C { feature x; feature y; }
     *     new C(x = 5)   // ConstructorExpression e, with result r { feature redefines C::x = 5; }
     *   }
     */
    @Test
    fun argument_of_ConstructorExpression() {
        val p = KerML_ModelBuilder.Package(factory(), "P") {
            ownedRelationshipOrderedSet {
                OwningMembership("mC") {
                    ownedRelatedElementOrderedSet {
                        Classifier("C") {
                            declaredName("C")
                            ownedRelationshipOrderedSet {
                                FeatureMembership("fmx") { ownedRelatedElementOrderedSet { Feature("C::x") { declaredName("x") } } }
                                FeatureMembership("fmy") { ownedRelatedElementOrderedSet { Feature("C::y") { declaredName("y") } } }
                            }
                        }
                    }
                }
                OwningMembership("mE") {
                    ownedRelatedElementOrderedSet {
                        ConstructorExpression("e") {
                            ownedRelationshipOrderedSet {
                                Membership("mInst") { }
                                ReturnParameterMembership("rpm") {
                                    ownedRelatedElementOrderedSet {
                                        Feature("r") {
                                            ownedRelationshipOrderedSet {
                                                FeatureMembership("fmrx") {
                                                    ownedRelatedElementOrderedSet {
                                                        Feature("r::x") {
                                                            ownedRelationshipOrderedSet {
                                                                Redefinition("rx<x") { }
                                                                FeatureValue("fv") { ownedRelatedElementOrderedSet { LiteralInteger("five") { value(5) } } }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val c = p.ownedElementOrderedSet.first() as Classifier
        val e = p.ownedElementOrderedSet.last() as ConstructorExpression
        val cx = c.ownedElementOrderedSet.first() as Feature
        (e.ownedRelationshipOrderedSet.first() as Membership).memberElementReference.mutable.set("C", c)
        val rx = e.result.ownedFeatureOrderedSet.single()
        (rx.ownedRelationshipOrderedSet.first() as Redefinition).let {
            it.redefiningFeatureReference.mutable.set("r::x", rx)
            it.redefinedFeatureReference.mutable.set("C::x", cx)
        }

        assertSame(c, e.instantiatedType)
        val arg = e.argumentOrderedSet.single()
        assertEquals("five", arg._identity)
        assertEquals(5L, (arg as LiteralInteger).value)
    }
}

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
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Features.Feature
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Features.ReferenceSubsetting
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Connectors.BindingConnector
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Expressions.FeatureReferenceExpression
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.sysml.v2_0.api.KerML.Root.Namespaces.Membership
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Parts.PartUsage
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Requirements.SatisfyRequirementUsage
import net.akehurst.omg.sysml.v2_0.ram.SysML_ModelFactoryRam
import kotlin.test.*

/**
 * SatisfyRequirementUsage::satisfyingFeature: the other end of the BindingConnector to the subjectParameter
 * (the spec rule, corrected); otherwise the referent of the subject's FeatureValue (as the pilot does).
 *
 *   package P {
 *     part x; part y;
 *     satisfy s by x;   // subject subj = x  (a FeatureValue holding a FeatureReferenceExpression to x)
 *   }
 * and, for the BindingConnector case, s also owns 'binding subj = y'.
 */
class test_SysML_SatisfyingFeature {

    private class Model(val p: Package, val x: PartUsage, val y: PartUsage, val s: SatisfyRequirementUsage)

    private fun model(withBinding: Boolean): Model {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val p = SysML_ModelBuilder.Package(factory, "P") {
            declaredName("P")
            ownedRelationshipOrderedSet {
                OwningMembership("mx") { ownedRelatedElementOrderedSet { PartUsage("x") { declaredName("x") } } }
                OwningMembership("my") { ownedRelatedElementOrderedSet { PartUsage("y") { declaredName("y") } } }
                OwningMembership("ms") {
                    ownedRelatedElementOrderedSet {
                        SatisfyRequirementUsage("s") {
                            declaredName("s")
                            ownedRelationshipOrderedSet {
                                SubjectMembership("sm") {
                                    ownedRelatedElementOrderedSet {
                                        ReferenceUsage("subj") {
                                            ownedRelationshipOrderedSet {
                                                FeatureValue("fv") {
                                                    ownedRelatedElementOrderedSet {
                                                        FeatureReferenceExpression("ref") {
                                                            ownedRelationshipOrderedSet { Membership("mRef") { } }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (withBinding) {
                                    OwningMembership("mb") {
                                        ownedRelatedElementOrderedSet {
                                            BindingConnector("b") {
                                                ownedRelationshipOrderedSet {
                                                    FeatureMembership("fmEnd1") {
                                                        ownedRelatedElementOrderedSet {
                                                            Feature("end1") { isEnd(true); ownedRelationshipOrderedSet { ReferenceSubsetting("rs1") { } } }
                                                        }
                                                    }
                                                    FeatureMembership("fmEnd2") {
                                                        ownedRelatedElementOrderedSet {
                                                            Feature("end2") { isEnd(true); ownedRelationshipOrderedSet { ReferenceSubsetting("rs2") { } } }
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
        val (x, y) = p.ownedElementOrderedSet.filterIsInstance<PartUsage>()
        val s = p.ownedElementOrderedSet.filterIsInstance<SatisfyRequirementUsage>().single()
        val subj = s.subjectParameter
        val ref = subj.valuation!!.value as FeatureReferenceExpression
        (ref.ownedRelationshipOrderedSet.single() as Membership).memberElementReference.mutable.set("x", x)
        if (withBinding) {
            val b = s.ownedMemberOrderedSet.filterIsInstance<BindingConnector>().single()
            val (end1, end2) = b.ownedElementOrderedSet.map { it as Feature }
            (end1.ownedRelationshipOrderedSet.single() as ReferenceSubsetting).let {
                it.referencedFeatureReference.mutable.set("subj", subj)
            }
            (end2.ownedRelationshipOrderedSet.single() as ReferenceSubsetting).let {
                it.referencedFeatureReference.mutable.set("y", y)
            }
        }
        return Model(p, x, y, s)
    }

    // no BindingConnector, as read from the textual notation: the referent of the subject's FeatureValue
    @Test
    fun from_FeatureValue() {
        val m = model(withBinding = false)
        assertSame(m.x, m.s.satisfyingFeature)
    }

    // a BindingConnector to the subjectParameter: its other end, as the spec rule says
    @Test
    fun from_BindingConnector() {
        val m = model(withBinding = true)
        assertSame(m.y, m.s.satisfyingFeature)
    }
}

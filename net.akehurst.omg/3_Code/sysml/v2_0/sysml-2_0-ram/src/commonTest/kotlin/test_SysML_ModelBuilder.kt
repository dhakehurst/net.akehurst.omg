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

import net.akehurst.omg.sysml.v2_0.api.SysML_ModelAsString
import net.akehurst.omg.sysml.v2_0.api.SysML_ModelBuilder
import net.akehurst.omg.sysml.v2_0.ram.SysML_ModelFactoryRam
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Features.Feature
import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Types.FeatureDirectionKind
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Behaviors.ParameterMembership
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Functions.ReturnParameterMembership
import kotlin.test.*

// Models are built only from settable (non derived) properties.
// In KerML/SysML ownership is held by relationships, e.g. Package -ownedRelationship-> OwningMembership -ownedRelatedElement-> Package,
// and the name is set as declaredName; owner, ownedElement, ownedMember and name are derived from those.
class test_SysML_ModelBuilder {

    @Test
    fun empty() {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val model = SysML_ModelBuilder.Package(factory, "RootPackage") {

        }
        val actual = SysML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun named() {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val model = SysML_ModelBuilder.Package(factory, "RootPackage") {
            declaredName("Root Package")
        }
        val actual = SysML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              declaredName 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun named_subpackage() {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val model = SysML_ModelBuilder.Package(factory, "RootPackage") {
            declaredName("Root Package")
            ownedRelationshipOrderedSet {
                OwningMembership("SubPackageMembership") {
                    ownedRelatedElementOrderedSet {
                        Package("SubPackage") {
                            declaredName("Sub Package")
                        }
                    }
                }
            }
        }
        val actual = SysML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              ownedRelationshipOrderedSet = [
                OwningMembership 'SubPackageMembership'
                  owningRelatedElement Package 'RootPackage'
                  ownedRelatedElementOrderedSet = [
                    Package 'SubPackage'
                      owningRelationship OwningMembership 'SubPackageMembership'
                      declaredName 'Sub Package'
                  ]
              ]
              declaredName 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)

        // derived from the relationships
        val sub = model.ownedElementOrderedSet.single() as Package
        assertEquals("Sub Package", sub.declaredName)
        assertSame(model, sub.owner)
    }

    @Test
    fun subpackage_with_content_x1() {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val model = SysML_ModelBuilder.Package(factory, "RootPackage") {
            declaredName("Root Package")
            ownedRelationshipOrderedSet {
                OwningMembership("SubPackageMembership") {
                    ownedRelatedElementOrderedSet {
                        Package("SubPackage") {
                            declaredName("Sub Package")
                            ownedRelationshipOrderedSet {
                                OwningMembership("Enum1Membership") {
                                    ownedRelatedElementOrderedSet {
                                        EnumerationDefinition("Enum1") {
                                            declaredName("Enum1")
                                            // enumerated values are owned through VariantMemberships
                                            ownedRelationshipOrderedSet {
                                                VariantMembership("REDMembership") {
                                                    ownedRelatedElementOrderedSet { EnumerationUsage("RED") { declaredName("RED") } }
                                                }
                                                VariantMembership("GREENMembership") {
                                                    ownedRelatedElementOrderedSet { EnumerationUsage("GREEN") { declaredName("GREEN") } }
                                                }
                                                VariantMembership("BLUEMembership") {
                                                    ownedRelatedElementOrderedSet { EnumerationUsage("BLUE") { declaredName("BLUE") } }
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
        val actual = SysML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              ownedRelationshipOrderedSet = [
                OwningMembership 'SubPackageMembership'
                  owningRelatedElement Package 'RootPackage'
                  ownedRelatedElementOrderedSet = [
                    Package 'SubPackage'
                      owningRelationship OwningMembership 'SubPackageMembership'
                      ownedRelationshipOrderedSet = [
                        OwningMembership 'Enum1Membership'
                          owningRelatedElement Package 'SubPackage'
                          ownedRelatedElementOrderedSet = [
                            EnumerationDefinition 'Enum1'
                              owningRelationship OwningMembership 'Enum1Membership'
                              ownedRelationshipOrderedSet = [
                                VariantMembership 'REDMembership'
                                  owningRelatedElement EnumerationDefinition 'Enum1'
                                  ownedRelatedElementOrderedSet = [
                                    EnumerationUsage 'RED'
                                      owningRelationship VariantMembership 'REDMembership'
                                      declaredName 'RED'
                                  ]
                                VariantMembership 'GREENMembership'
                                  owningRelatedElement EnumerationDefinition 'Enum1'
                                  ownedRelatedElementOrderedSet = [
                                    EnumerationUsage 'GREEN'
                                      owningRelationship VariantMembership 'GREENMembership'
                                      declaredName 'GREEN'
                                  ]
                                VariantMembership 'BLUEMembership'
                                  owningRelatedElement EnumerationDefinition 'Enum1'
                                  ownedRelatedElementOrderedSet = [
                                    EnumerationUsage 'BLUE'
                                      owningRelationship VariantMembership 'BLUEMembership'
                                      declaredName 'BLUE'
                                  ]
                              ]
                              declaredName 'Enum1'
                          ]
                      ]
                      declaredName 'Sub Package'
                  ]
              ]
              declaredName 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    // enumerations: a settable enum property, and spec operations whose OCL returns an enum literal
    @Test
    fun enumerations() {
        val factory = SysML_ModelFactoryRam("TestFactoryRam")
        val model = SysML_ModelBuilder.Package(factory, "RootPackage") {
            ownedRelationshipOrderedSet {
                ParameterMembership("pm") {
                    ownedRelatedElementOrderedSet {
                        Feature("f") {
                            direction(FeatureDirectionKind.inout)
                        }
                    }
                }
                ReturnParameterMembership("rpm") { }
            }
        }
        val (pm, rpm) = model.ownedRelationshipOrderedSet.toList()
        assertEquals(FeatureDirectionKind.in_, (pm as ParameterMembership).parameterDirection())
        assertEquals(FeatureDirectionKind.out, (rpm as ReturnParameterMembership).parameterDirection())
        assertEquals(FeatureDirectionKind.inout, (pm.ownedRelatedElementOrderedSet.single() as Feature).direction)
        assertEquals(listOf("in", "inout", "out"), FeatureDirectionKind.entries.map { it.name.removeSuffix("_") })
    }

}

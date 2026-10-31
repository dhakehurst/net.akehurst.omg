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

import net.akehurst.omg.kerml.v1_0.api.KerML_ModelAsString
import net.akehurst.omg.kerml.v1_0.api.KerML_ModelBuilder
import net.akehurst.omg.kerml.v1_0.ram.KerML_ModelFactoryRam
import net.akehurst.omg.kerml.v1_0.api.KerML.Kernel.Packages.Package
import kotlin.test.*

// Models are built only from settable (non derived) properties.
// In KerML ownership is held by relationships, e.g. Package -ownedRelationship-> OwningMembership -ownedRelatedElement-> Package,
// and the name is set as declaredName; owner, ownedElement, ownedMember and name are derived from those.
class test_KerML_ModelBuilder {

    @Test
    fun empty() {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val model = KerML_ModelBuilder.Package(factory, "RootPackage") {

        }
        val actual = KerML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun named() {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val model = KerML_ModelBuilder.Package(factory, "RootPackage") {
            declaredName("Root Package")
        }
        val actual = KerML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              declaredName 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun named_subpackage() {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val model = KerML_ModelBuilder.Package(factory, "RootPackage") {
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
        val actual = KerML_ModelAsString.Package_asString(model)
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
}

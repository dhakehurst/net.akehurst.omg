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

import net.akehurst.omg.uml.v2_5_1.api.UML_ModelAsString
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelBuilder
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals

class test_UML_Model_Builder {

    // nestedPackage and ownedType are derived from packagedElement, so the model is built through packagedElement
    @Test
    fun package_with_class() {
        val factory = UML_ModelFactoryRam("TestFactoryRam")
        val model = UML_ModelBuilder.Model(factory, "test") {
            packagedElementSet {
                Package("P1") {
                    name("P1")
                    packagedElementSet {
                        Class("C1") {
                            name("C1")
                        }
                    }
                }
            }
        }

        val actual = UML_ModelAsString.Model_asString(model)
        println(actual)
        val expected = """
            Model 'test'
              packagedElementSet = [
                Package 'P1'
                  packagedElementSet = [
                    Class 'C1'
                      name 'C1'
                  ]
                  name 'P1'
              ]

        """.trimIndent()
        assertEquals(expected, actual)

        // derived from packagedElement
        val p1 = model.nestedPackageSet.single()
        assertEquals("P1", p1.name)
        assertEquals(listOf("C1"), p1.ownedTypeSet.map { it.name })
    }

}

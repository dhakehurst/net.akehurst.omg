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

import net.akehurst.omg.kerml.v1_0.ram.KerML_ModelFactoryRam
import kotlin.test.*

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
            name("Root Package")
        }
        val actual = KerML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              name 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)
    }

    @Test
    fun named_subpackage() {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val model = KerML_ModelBuilder.Package(factory, "RootPackage") {
            name("Root Package")
            ownedElementOrderedSet {
                Package("SubPackage") {
                    name("Sub Package")
                }
            }
        }
        val actual = KerML_ModelAsString.Package_asString(model)
        println(actual)
        val expected = """
            Package 'RootPackage'
              ownedElementOrderedSet = [
                Package 'SubPackage'
                  name 'Sub Package'
                  
              ]
              name 'Root Package'

        """.trimIndent()

        assertEquals(expected, actual)
    }

}
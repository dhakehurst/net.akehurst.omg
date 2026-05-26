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

package net.akehurst.omg.templates.examples

import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.kotlinx.utils.cast
import net.akehurst.kotlinx.utils.resolve
import kotlin.test.*

class test_SingleRefRedefDiffNameSameTypeAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.SingleRefRedefDiffNameSameTypeAttribute_construct()
        assertNotNull(obj)
        try {
            obj.redefinesProp1
            fail("Exception expected")
        } catch (e: IllegalStateException) {
            assertEquals("prop1 not resolved", e.message)
        }
        assertNull(obj.redefinesProp2)
    }

    @Test
    fun property_by_value_and_reference() {
        val factory = ExamplesFactoryRam()
        val obj = factory.SingleRefRedefDiffNameSameTypeAttribute_construct()

        val p1 = factory.PropType_construct("p1")
        val p2 = factory.PropType_construct("p2")

        obj.redefinesProp1Reference.mutable.set(p1._identity, p1)
        assertEquals(p1, obj.redefinesProp1)

        obj.redefinesProp2Reference.mutable.set(p2._identity, p2)
        assertEquals(p2, obj.redefinesProp2)

        // by reference id and resolve
        val obj2 = factory.SingleRefRedefDiffNameSameTypeAttribute_construct()
        val pv1 = factory.PropType_construct("rv1")
        obj2.redefinesProp1Reference.mutable.reference = "rv1"
        factory.resolve(obj2.redefinesProp1Reference)
        assertEquals(pv1, obj2.redefinesProp1)
    }

    @Test
    fun builder() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "Test") {
            content {
                PropType("p1")
                PropType("p2")
                SingleRefRedefDiffNameSameTypeAttribute("obj") {
                    redefinesProp1("p1")
                    redefinesProp2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(3, actual.contentList.size)
        val inst = actual.contentList[2].cast<SingleRefRedefDiffNameSameTypeAttribute>()
        assertEquals("p1", inst.redefinesProp1._identity)
        assertEquals("p2", inst.redefinesProp2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = ExamplesFactoryRam()
        val a = factory.SingleRefRedefDiffNameSameTypeAttribute_construct()
        val b = factory.SingleRefRedefDiffNameSameTypeAttribute_construct()
        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString() {
        val factory = ExamplesFactoryRam()
        val model = Examples(factory, "Test") {
            content {
                PropType("p1")
                SingleRefRedefDiffNameSameTypeAttribute("obj") {
                    redefinesProp1("p1")
                }
            }
        }

        val actual = Examples_ModelAsString.Examples_asString(model)
        val expected = """
            Examples 'ExamplesFactoryRam0.Test'
              content = List [
                PropType 'ExamplesFactoryRam0.p1'
                SingleRefRedefDiffNameSameTypeAttributeRam 'ExamplesFactoryRam0.obj'
                  prop1 PropType 'ExamplesFactoryRam0.p1'
              ]
        """.trimIndent()

        assertEquals(expected, actual)
    }
}


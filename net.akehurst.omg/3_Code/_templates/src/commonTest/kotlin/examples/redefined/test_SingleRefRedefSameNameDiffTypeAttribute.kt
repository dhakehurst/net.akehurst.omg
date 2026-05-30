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

package net.akehurst.omg.templates.examples.redefined

import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreExt.resolve
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.kotlinx.utils.cast
import net.akehurst.omg.templates.examples.Example
import net.akehurst.omg.templates.examples.Examples_ModelAsString
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import kotlin.test.*

class test_SingleRefRedefSameNameDiffTypeAttribute {

    @Test
    fun factory() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.SingleRefRedefSameNameDiffTypeAttribute_construct("obj")
        assertNotNull(obj)
        try {
            obj.prop1
            fail("Exception expected")
        } catch (e: IllegalStateException) {
            assertEquals("prop1 not resolved", e.message)
        }
        assertNull(obj.prop2)
    }

    @Test
    fun property_by_value() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val model = Example(factory, "Test") {
            content {
                PropTypeB("p1")
                PropTypeB("p2")
                SingleRefRedefSameNameDiffTypeAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }
        val obj = model.contentList[2].cast<SingleRefRedefSameNameDiffTypeAttribute>()
        assertEquals("p1", obj.prop1._identity)
        assertEquals("p2", obj.prop2?._identity)
    }

    @Test
    fun property_by_reference_and_builder() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.SingleRefRedefSameNameDiffTypeAttribute_construct("obj")
        val p1 = factory.common.PropTypeB_construct("p1")

        obj.prop1Reference.mutable.reference = "p1"
        factory.resolve(obj.prop1Reference)
        assertEquals(p1, obj.prop1)

        val actual = Example(factory, "Test") {
            content {
                PropTypeB("b1")
                SingleRefRedefSameNameDiffTypeAttribute("obj") {
                    prop1("b1")
                }
            }
        }
        val inst = actual.contentList[1].cast<SingleRefRedefSameNameDiffTypeAttribute>()
        assertEquals("b1", inst.prop1._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val a = factory.redefined.SingleRefRedefSameNameDiffTypeAttribute_construct("a")
        val b = factory.redefined.SingleRefRedefSameNameDiffTypeAttribute_construct("b")
        // identities are non-null by contract; assert they differ
        assertTrue(a._identity != b._identity)
    }

    @Test
    fun asString() {
        val factory =examples_ModelFactoryRam("TestModelFactory")
        val model = Example(factory, "Test") {
            content {
                PropTypeB("p1")
                PropTypeB("p2")
                SingleRefRedefSameNameDiffTypeAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        val actual = Examples_ModelAsString.Example_asString(model)
        val expected = """
            Examples 'TestModelFactory.common.Test'
              content = List [
                PropTypeB 'TestModelFactory.common.p1'
                PropTypeB 'TestModelFactory.common.p2'
                SingleRefRedefSameNameDiffTypeAttribute 'TestModelFactory.redefined.obj'
                  prop1 PropTypeB 'TestModelFactory.common.p1'
                  prop2 PropTypeB 'TestModelFactory.common.p2'
              ]
        """.trimIndent()

        assertEquals(expected, actual)
    }
}


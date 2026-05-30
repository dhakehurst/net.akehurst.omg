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

package net.akehurst.omg.templates.examples.simple

import net.akehurst.kotlinx.utils.ValueExt.mutable
import net.akehurst.kotlinx.utils.cast
import net.akehurst.omg.templates.examples.Example
import net.akehurst.omg.templates.examples.Examples_ModelAsString
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import net.akehurst.omg.templates.examples.simple.SingleCmpAttribute
import kotlin.test.*

class test_SingleCmpAttribute {

    @Test
    fun factory() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.SingleCmpAttribute_construct("obj")
        assertNotNull(obj)
        assertNotNull(obj.prop1)
        assertNull(obj.prop2)
    }

    @Test
    fun property() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.SingleCmpAttribute_construct("obj")
        assertNotNull(obj.prop1)
        assertNull(obj.prop2)

        val propValue = factory.common.PropType_construct("p1")

        obj.prop1Value.mutable.set(propValue)
        assertEquals(propValue, obj.prop1)

        obj.prop2Value.mutable.set(propValue)
        assertEquals(propValue, obj.prop2)
    }

    @Test
    fun builder() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val actual = Example(factory, "Test") {
            content {
                SingleCmpAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        assertEquals("obj", actual.contentList[0]._identity)
        assertTrue( actual.contentList[0] is SingleCmpAttribute)
        assertEquals("p1", actual.contentList[0].cast<SingleCmpAttribute>().prop1._identity)
        assertEquals("p2", actual.contentList[0].cast<SingleCmpAttribute>().prop2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val a = factory.simple.SingleCmpAttribute_construct("a")
        val b = factory.simple.SingleCmpAttribute_construct("b")

        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity, "Factory should create distinct identities for separate instances")
        // stable for object lifetime
        assertEquals(a._identity, a._identity)
        assertEquals(b._identity, b._identity)
    }

    @Test
    fun value_holder_updates_are_reflected() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.SingleCmpAttribute_construct("obj")
        val v1 = factory.common.PropType_construct("v1")
        val v2 = factory.common.PropType_construct("v2")

        // set required composite
        obj.prop1Value.mutable.set(v1)
        assertEquals(v1, obj.prop1)

        // update to a different value
        obj.prop1Value.mutable.set(v2)
        assertEquals(v2, obj.prop1)
    }

    @Test
    fun builder_with_only_required_leaves_optional_null() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val actual = Example(factory, "TestOnlyRequired") {
            content {
                SingleCmpAttribute("objOnly") {
                    prop1("p1Only")
                    // prop2 omitted - should be null
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<SingleCmpAttribute>()
        assertEquals("p1Only", inst.prop1._identity)
        assertNull(inst.prop2)
    }

    @Test
    fun asString() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val model = Example(factory, "Test") {
            content {
                SingleCmpAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        val actual = Examples_ModelAsString.Example_asString(model)
        val expected = """
            Examples 'TestModelFactory.common.Test'
              content = List [
                SingleCmpAttribute 'simple.obj'
                  prop1 PropType 'TestModelFactory.common.p1'
                  prop2 PropType 'TestModelFactory.common.p2'
              ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
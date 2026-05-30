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

import net.akehurst.kotlinx.utils.ValueExt.mutable
import net.akehurst.kotlinx.utils.cast
import net.akehurst.omg.templates.examples.Example
import net.akehurst.omg.templates.examples.Examples_ModelAsString
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import net.akehurst.omg.templates.examples.redefined.SingleCmpRedefDiffNameSameTypeAttribute
import kotlin.test.*

class test_SingleCmpRedefDiffNameSameTypeAttribute {

    @Test
    fun factory() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.SingleCmpRedefDiffNameSameTypeAttribute_construct("obj")
        assertNotNull(obj)
        assertNotNull(obj.redefinesProp1)
        assertNull(obj.redefinesProp2)
    }

    @Test
    fun property() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.SingleCmpRedefDiffNameSameTypeAttribute_construct("obj")

        val pv = factory.common.PropType_construct("p1")
        obj.redefinesProp1Value.mutable.set(pv)
        assertEquals(pv, obj.redefinesProp1)

        val pv2 = factory.common.PropType_construct("p2")
        obj.redefinesProp2Value.mutable.set(pv2)
        assertEquals(pv2, obj.redefinesProp2)
    }

    @Test
    fun builder() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val actual = Example(factory, "Test") {
            content {
                SingleCmpRedefDiffNameSameTypeAttribute("obj") {
                    redefinesProp1("p1")
                    redefinesProp2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<SingleCmpRedefDiffNameSameTypeAttribute>()
        assertEquals("p1", inst.redefinesProp1._identity)
        assertEquals("p2", inst.redefinesProp2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val a = factory.redefined.SingleCmpRedefDiffNameSameTypeAttribute_construct("a")
        val b = factory.redefined.SingleCmpRedefDiffNameSameTypeAttribute_construct("b")
        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val model = Example(factory, "Test") {
            content {
                SingleCmpRedefDiffNameSameTypeAttribute("obj") {
                    redefinesProp1("p1") {}
                    redefinesProp2("pb2") {}
                }
            }
        }

        val actual = Examples_ModelAsString.Example_asString(model)
        val expected = """
            Examples 'ExamplesFactoryRam0.Test'
              content = List [
                SingleCmpRedefDiffNameSameTypeAttribute 'ExamplesFactoryRam0.obj'
                  prop1 PropType 'ExamplesFactoryRam0.p1'
                  prop2 PropType 'ExamplesFactoryRam0.pb2'
              ]
        """.trimIndent()

        assertEquals(expected, actual)
    }
}


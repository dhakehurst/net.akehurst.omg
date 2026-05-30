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

import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.collections.orderedSetOf
import net.akehurst.kotlinx.utils.cast
import net.akehurst.omg.templates.examples.Example
import net.akehurst.omg.templates.examples.Examples_ModelAsString
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class test_CollectionCmpRedefSameNameDiffTypeAttribute {

    @Test
    fun factory() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_construct("obj")
        assertNotNull(obj)
        assertNotNull(obj.prop1OrderedSet)
        assertTrue(obj.prop1OrderedSet.isEmpty())
    }

    @Test
    fun property() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_construct("obj")

        val propValue = factory.common.PropTypeB_construct("p1")

        obj.prop1OrderedSet.mutable.add(propValue)
        assertEquals(orderedSetOf(propValue), obj.prop1OrderedSet)

        obj.prop2List.mutable.add(propValue)
        assertEquals(listOf(propValue), obj.prop2List)
    }

    @Test
    fun builder() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val actual = Example(factory, "Test") {
            content {
                CollectionCmpRedefSameNameDiffTypeAttribute("obj") {
                    prop1 {
                        PropTypeB("p1.1")
                        PropTypeB("p1.2")
                    }
                    prop2 {
                        PropTypeB("p2.1")
                        PropTypeB("p2.2")
                    }
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        assertEquals("obj", actual.contentList[0]._identity)
        assertTrue(actual.contentList[0] is CollectionCmpRedefSameNameDiffTypeAttribute)
        assertEquals(2, actual.contentList[0].cast<CollectionCmpRedefSameNameDiffTypeAttribute>().prop1OrderedSet.size)
        assertEquals(2, actual.contentList[0].cast<CollectionCmpRedefSameNameDiffTypeAttribute>().prop2List.size)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val a = factory.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_construct("a")
        val b = factory.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_construct("b")

        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val model = Example(factory, "Test") {
            content {
                CollectionCmpRedefSameNameDiffTypeAttribute("obj") {
                    prop1 {
                        PropTypeB("pb1")
                    }
                    prop2 {
                        PropTypeB("pb2")
                    }
                }
            }
        }

        val actual = Examples_ModelAsString.Example_asString(model)
        val expected = """
            Examples 'TestModelFactory.common.Test'
              content = List [
                CollectionCmpRedefSameNameDiffTypeAttribute 'TestModelFactory.redefined.obj'
                  prop1 = OrderedSet [
                    PropTypeB 'TestModelFactory.common.pb1'
                  ]
                  prop2 = List [
                    PropTypeB 'TestModelFactory.common.pb2'
                  ]
                  prop3 = Set []
                  prop4 = Collection []
              ]
        """.trimIndent()

        assertEquals(expected, actual)
    }
}
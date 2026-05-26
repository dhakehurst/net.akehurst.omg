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

import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.collections.orderedSetOf
import net.akehurst.kotlinx.utils.cast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class test_CollectionCmpAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.CollectionCmpAttribute_construct("obj")
        assertNotNull(obj)
        assertNotNull(obj.prop1OrderedSet)
        assertTrue(obj.prop1OrderedSet.isEmpty())
    }

    @Test
    fun property() {
        val factory = ExamplesFactoryRam()
        val obj = factory.CollectionCmpAttribute_construct("obj")

        val propValue = factory.PropType_construct("p1")

        obj.prop1OrderedSet.mutable.add(propValue)
        assertEquals(orderedSetOf(propValue), obj.prop1OrderedSet)

        obj.prop2List.mutable.add(propValue)
        assertEquals(listOf(propValue), obj.prop2List)
    }

    @Test
    fun builder() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "Test") {
            content {
                CollectionCompositeAttribute("obj") {
                    prop1 {
                        PropType("p1.1")
                        PropType("p1.2")
                    }
                    prop2 {
                        PropType("p2.1")
                        PropType("p2.2")
                        PropType("p2.3")
                    }
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        assertEquals("obj", actual.contentList[0]._identity)
        assertTrue(actual.contentList[0] is CollectionCmpAttribute)
        assertEquals(2, actual.contentList[0].cast<CollectionCmpAttribute>().prop1OrderedSet.size)
        assertEquals(3, actual.contentList[0].cast<CollectionCmpAttribute>().prop2List.size)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = ExamplesFactoryRam()
        val a = factory.CollectionCmpAttribute_construct("a")
        val b = factory.CollectionCmpAttribute_construct("b")

        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity, "Factory should create distinct identities for separate instances")
        // stable for object lifetime
        assertEquals(a._identity, a._identity)
        assertEquals(b._identity, b._identity)
    }

    @Test
    fun collection_holder_add_remove_reflected() {
        val factory = ExamplesFactoryRam()
        val obj = factory.CollectionCmpAttribute_construct("obj")
        val p1 = factory.PropType_construct("c1")
        val p2 = factory.PropType_construct("c2")

        // add to ordered set
        obj.prop1OrderedSet.mutable.add(p1)
        obj.prop1OrderedSet.mutable.add(p2)
        assertEquals(2, obj.prop1OrderedSet.size)
        // remove one
        obj.prop1OrderedSet.mutable.remove(p1)
        assertEquals(1, obj.prop1OrderedSet.size)

        // add to list
        obj.prop2List.mutable.add(p1)
        obj.prop2List.mutable.add(p2)
        assertEquals(2, obj.prop2List.size)
        // remove one
        obj.prop2List.mutable.remove(p2)
        assertEquals(1, obj.prop2List.size)
    }

    @Test
    fun builder_with_only_prop1_leaves_prop2_empty() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "TestOnlyProp1") {
            content {
                CollectionCompositeAttribute("objOnly") {
                    prop1 {
                        PropType("p1.1")
                    }
                    // prop2 omitted
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<CollectionCmpAttribute>()
        assertEquals(1, inst.prop1OrderedSet.size)
        assertEquals(0, inst.prop2List.size)
    }

    @Test
    fun asString() {
        val factory = ExamplesFactoryRam()
        val model = Examples(factory, "Test") {
            content {
                CollectionCompositeAttribute("obj") {
                    prop1 {
                        PropType("p1.1")
                        PropType("p1.2")
                    }
                    prop2 {
                        PropType("p2.1")
                        PropType("p2.2")
                        PropType("p2.3")
                    }
                }
            }
        }

        val actual = Examples_ModelAsString.Examples_asString(model)
        val expected = """
            Examples 'ExamplesFactoryRam0.Test'
              content = List [
                CollectionCmpAttribute 'ExamplesFactoryRam0.obj'
                  prop1 = OrderedSet [
                    PropType 'ExamplesFactoryRam0.p1.1'
                    PropType 'ExamplesFactoryRam0.p1.2'
                  ]
                  prop2 = List [
                    PropType 'ExamplesFactoryRam0.p2.1'
                    PropType 'ExamplesFactoryRam0.p2.2'
                    PropType 'ExamplesFactoryRam0.p2.3'
                  ]
                  prop3 = Set []
                  prop4 = Collection []
              ]
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
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
import kotlin.test.*

class test_CollectionCmpRedefSameNameDiffTypeAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createCollectionCmpRedefSameNameDiffTypeAttribute()
        assertNotNull(obj)
        assertNotNull(obj.prop1OrderedSet)
        assertTrue(obj.prop1OrderedSet.isEmpty())
    }

    @Test
    fun property() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createCollectionCmpRedefSameNameDiffTypeAttribute()

        val propValue = factory.createPropTypeB()

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
        val factory = ExamplesFactoryRam()
        val a = factory.createCollectionCmpRedefSameNameDiffTypeAttribute()
        val b = factory.createCollectionCmpRedefSameNameDiffTypeAttribute()

        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString_contains_elements() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createCollectionCmpRedefSameNameDiffTypeAttribute()
        val p1 = factory.createPropTypeB("as1")
        val p2 = factory.createPropTypeB("as2")

        obj.prop1OrderedSet.mutable.add(p1)
        obj.prop2List.mutable.add(p2)

        val s = Examples_ModelAsString.asStringCollectionCmpRedefSameNameDiffTypeAttribute(obj)
        assertTrue(s.contains(p1._identity.toString()))
        assertTrue(s.contains(p2._identity.toString()))
    }
}


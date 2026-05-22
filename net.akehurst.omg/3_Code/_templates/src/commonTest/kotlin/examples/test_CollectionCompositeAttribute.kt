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

import net.akehurst.kotlinx.collections.mutableList
import net.akehurst.kotlinx.collections.mutableOrderedSet
import net.akehurst.kotlinx.collections.orderedSetOf
import net.akehurst.kotlinx.utils.cast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class test_CollectionCompositeAttribute {

    @Test
    fun factory() {
        val obj = ExamplesFactoryRam.createCollectionCompositeAttribute()
        assertNotNull(obj)
        assertNotNull(obj.prop1OrderedSet)
        assertTrue(obj.prop1OrderedSet.isEmpty())
    }

    @Test
    fun property() {
        val obj = ExamplesFactoryRam.createCollectionCompositeAttribute()

        val propValue = ExamplesFactoryRam.createPropType()

        obj.prop1OrderedSet.mutableOrderedSet.add(propValue)
        assertEquals(orderedSetOf(propValue), obj.prop1OrderedSet)

        obj.prop2.mutableList.add(propValue)
        assertEquals(listOf(propValue), obj.prop2)
    }

    @Test
    fun builder() {
        val actual = Examples(ExamplesFactoryRam, "Test") {
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
        assertEquals(1, actual.content.size)
        assertEquals("obj", actual.content[0].identifier_)
        assertTrue(actual.content[0] is CollectionCmpAttribute)
        assertEquals(2, actual.content[0].cast<CollectionCmpAttribute>().prop1OrderedSet.size)
        assertEquals(3, actual.content[0].cast<CollectionCmpAttribute>().prop2.size)
    }
}
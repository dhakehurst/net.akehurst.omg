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

import net.akehurst.kotlinx.utils.cast
import net.akehurst.kotlinx.utils.mutableReference
import net.akehurst.kotlinx.utils.resolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class test_SingleReferenceAttribute {

    @Test
    fun factory() {
        val obj = ExamplesFactoryRam.createSingleReferenceAttribute()
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

        val obj = ExamplesFactoryRam.createSingleReferenceAttribute()

        val propValue = ExamplesFactoryRam.createPropType()

        obj.prop1_set(propValue)
        assertEquals(propValue, obj.prop1)

        obj.prop2_set(propValue)
        assertEquals(propValue, obj.prop2)

    }

    @Test
    fun property_by_reference() {

        val obj = ExamplesFactoryRam.createSingleReferenceAttribute()

        val propValue1 = ExamplesFactoryRam.createPropType("p1")
        val propValue2 = ExamplesFactoryRam.createPropType("p2")

        obj.prop1Reference.mutableReference.reference = "p1"
        ExamplesFactoryRam.resolve(obj.prop1Reference)
        assertEquals(propValue1, obj.prop1)

        obj.prop2Reference.mutableReference.reference = "p2"
        ExamplesFactoryRam.resolve(obj.prop2Reference)
        assertEquals(propValue2, obj.prop2)

    }

    @Test
    fun builder() {
        val actual = Examples(ExamplesFactoryRam, "Test") {
            content {
                PropType("p1")
                PropType("p2")
                SingleReferenceAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.content.size)
        assertEquals("obj", actual.content[0].identifier_)
        assertTrue( actual.content[0] is SingleReferenceAttribute)
        assertEquals("p1", actual.content[0].cast<SingleReferenceAttribute>().prop1.identifier_)
        assertEquals("p2", actual.content[0].cast<SingleReferenceAttribute>().prop2?.identifier_)
    }
}
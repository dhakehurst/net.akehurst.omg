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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class test_SingleCompositeAttribute {

    @Test
    fun factory() {
        val obj = ExamplesFactoryRam.createSingleCompositeAttribute()
        assertNotNull(obj)
        assertNotNull(obj.prop1)
        assertNull(obj.prop2)
    }

    @Test
    fun property() {

        val obj = ExamplesFactoryRam.createSingleCompositeAttribute()
        assertNotNull(obj.prop1)
        assertNull(obj.prop2)

        val propValue = ExamplesFactoryRam.createPropType()

        obj.set_prop1(propValue)
        assertEquals(propValue, obj.prop1)

        obj.set_prop2(propValue)
        assertEquals(propValue, obj.prop2)
    }

    @Test
    fun builder() {
        val actual = Examples(ExamplesFactoryRam, "Test") {
            content {
                SingleCompositeAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.content.size)
        assertEquals("obj", actual.content[0].identifier_)
        assertTrue( actual.content[0] is SingleCompositeAttribute)
        assertEquals("p1", actual.content[0].cast<SingleCompositeAttribute>().prop1.identifier_)
        assertEquals("p2", actual.content[0].cast<SingleCompositeAttribute>().prop2?.identifier_)
    }
}
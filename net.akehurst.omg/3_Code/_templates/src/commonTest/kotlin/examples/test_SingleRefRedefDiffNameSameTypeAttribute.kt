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
        val obj = factory.createSingleRefRedefDiffNameSameTypeAttribute()
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
        val obj = factory.createSingleRefRedefDiffNameSameTypeAttribute()

        val p1 = factory.createPropType("p1")
        val p2 = factory.createPropType("p2")

        obj.redefinesProp1Reference.mutable.set(p1._identity, p1)
        assertEquals(p1, obj.redefinesProp1)

        obj.redefinesProp2Reference.mutable.set(p2._identity, p2)
        assertEquals(p2, obj.redefinesProp2)

        // by reference id and resolve
        val obj2 = factory.createSingleRefRedefDiffNameSameTypeAttribute()
        val pv1 = factory.createPropType("rv1")
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
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<SingleRefRedefDiffNameSameTypeAttribute>()
        assertEquals("p1", inst.redefinesProp1._identity)
        assertEquals("p2", inst.redefinesProp2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = ExamplesFactoryRam()
        val a = factory.createSingleRefRedefDiffNameSameTypeAttribute()
        val b = factory.createSingleRefRedefDiffNameSameTypeAttribute()
        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString_contains_properties() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleRefRedefDiffNameSameTypeAttribute()
        val p = factory.createPropType("as1")
        obj.redefinesProp1Reference.mutable.set(p._identity, p)

        val s = Examples_ModelAsString.asStringSingleRefRedefDiffNameSameTypeAttribute(obj)
        assertTrue(s.contains("redefinesProp1"))
        assertTrue(s.contains(p._identity.toString()))
    }
}


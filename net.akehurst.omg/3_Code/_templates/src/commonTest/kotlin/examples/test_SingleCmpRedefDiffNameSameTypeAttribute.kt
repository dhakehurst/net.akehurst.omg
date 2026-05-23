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

import net.akehurst.kotlinx.utils.ValueExt.mutable
import net.akehurst.kotlinx.utils.cast
import kotlin.test.*

class test_SingleCmpRedefDiffNameSameTypeAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleCmpRedefDiffNameSameTypeAttribute()
        assertNotNull(obj)
        assertNotNull(obj.redefinesProp1)
        assertNull(obj.redefinesProp2)
    }

    @Test
    fun property() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleCmpRedefDiffNameSameTypeAttribute()

        val pv = factory.createPropType()
        obj.redefinesProp1Value.mutable.set(pv)
        assertEquals(pv, obj.redefinesProp1)

        val pv2 = factory.createPropType()
        obj.redefinesProp2Value.mutable.set(pv2)
        assertEquals(pv2, obj.redefinesProp2)
    }

    @Test
    fun builder() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "Test") {
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
        val factory = ExamplesFactoryRam()
        val a = factory.createSingleCmpRedefDiffNameSameTypeAttribute()
        val b = factory.createSingleCmpRedefDiffNameSameTypeAttribute()
        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString_contains_properties() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleCmpRedefDiffNameSameTypeAttribute()
        val p = factory.createPropType("as1")
        obj.redefinesProp1Value.mutable.set(p)

        val s = Examples_ModelAsString.SingleCmpRedefDiffNameSameTypeAttribute_asString(obj)
        assertTrue(s.contains("redefinesProp1"))
        assertTrue(s.contains(p._identity.toString()))
    }
}


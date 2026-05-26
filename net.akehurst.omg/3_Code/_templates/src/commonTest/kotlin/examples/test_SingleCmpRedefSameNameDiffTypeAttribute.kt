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

class test_SingleCmpRedefSameNameDiffTypeAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.SingleCmpRedefSameNameDiffTypeAttribute_construct()
        assertNotNull(obj)
        assertNotNull(obj.prop1)
        assertNull(obj.prop2)
    }

    @Test
    fun property() {
        val factory = ExamplesFactoryRam()
        val obj = factory.SingleCmpRedefSameNameDiffTypeAttribute_construct()

        val v1 = factory.PropTypeB_construct()
        obj.prop1Value.mutable.set(v1)
        assertEquals(v1, obj.prop1)

        val v2 = factory.PropTypeB_construct()
        obj.prop2Value.mutable.set(v2)
        assertEquals(v2, obj.prop2)
    }

    @Test
    fun builder() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "Test") {
            content {
                SingleCmpRedefSameNameDiffTypeAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<SingleCmpRedefSameNameDiffTypeAttribute>()
        assertEquals("p1", inst.prop1._identity)
        assertEquals("p2", inst.prop2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = ExamplesFactoryRam()
        val a = factory.SingleCmpRedefSameNameDiffTypeAttribute_construct()
        val b = factory.SingleCmpRedefSameNameDiffTypeAttribute_construct()
        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity)
    }

    @Test
    fun asString() {
        val factory = ExamplesFactoryRam()
        val obj = factory.SingleCmpRedefSameNameDiffTypeAttribute_construct()
        val p = factory.PropTypeB_construct("as1")
        obj.prop1Value.mutable.set(p)

        val s = Examples_ModelAsString.SingleCmpRedefSameNameDiffTypeAttribute_asString(obj)
        assertTrue(s.contains("prop1"))
        assertTrue(s.contains(p._identity.toString()))
    }
}


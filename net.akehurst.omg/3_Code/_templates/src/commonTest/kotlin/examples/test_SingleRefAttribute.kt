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

class test_SingleRefAttribute {

    @Test
    fun factory() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleReferenceAttribute()
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
        val factory = ExamplesFactoryRam()

        val obj = factory.createSingleReferenceAttribute()

        val propValue = factory.createPropType()

        obj.prop1Reference.mutable.set(propValue._identity, propValue)
        assertEquals(propValue, obj.prop1)

        obj.prop2Reference.mutable.set(propValue._identity, propValue)
        assertEquals(propValue, obj.prop2)

    }

    @Test
    fun property_by_reference() {
        val factory = ExamplesFactoryRam()

        val obj = factory.createSingleReferenceAttribute()

        val propValue1 = factory.createPropType("p1")
        val propValue2 = factory.createPropType("p2")

        obj.prop1Reference.mutable.reference = "p1"
        factory.resolve(obj.prop1Reference)
        assertEquals(propValue1, obj.prop1)

        obj.prop2Reference.mutable.reference = "p2"
        factory.resolve(obj.prop2Reference)
        assertEquals(propValue2, obj.prop2)

    }

    @Test
    fun builder() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "Test") {
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
        assertEquals(1, actual.contentList.size)
        assertEquals("obj", actual.contentList[0]._identity)
        assertTrue(actual.contentList[0] is SingleRefAttribute)
        assertEquals("p1", actual.contentList[0].cast<SingleRefAttribute>().prop1._identity)
        assertEquals("p2", actual.contentList[0].cast<SingleRefAttribute>().prop2?._identity)
    }

    @Test
    fun identity_stability_and_uniqueness() {
        val factory = ExamplesFactoryRam()
        val a = factory.createSingleReferenceAttribute()
        val b = factory.createSingleReferenceAttribute()

        assertNotNull(a._identity)
        assertNotNull(b._identity)
        assertNotEquals(a._identity, b._identity, "Factory should create distinct identities for separate instances")
        // stable for object lifetime
        assertEquals(a._identity, a._identity)
        assertEquals(b._identity, b._identity)
    }

    @Test
    fun reference_holder_updates_are_reflected() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleReferenceAttribute()
        val pv1 = factory.createPropType("rv1")
        val pv2 = factory.createPropType("rv2")

        // set required reference by value
        obj.prop1Reference.mutable.set(pv1._identity, pv1)
        assertEquals(pv1, obj.prop1)

        // update to a different value
        obj.prop1Reference.mutable.set(pv2._identity, pv2)
        assertEquals(pv2, obj.prop1)
    }

    @Test
    fun builder_with_only_required_leaves_optional_null() {
        val factory = ExamplesFactoryRam()
        val actual = Examples(factory, "TestOnlyRequiredRef") {
            content {
                PropType("p1OnlyRef")
                SingleReferenceAttribute("objOnlyRef") {
                    prop1("p1OnlyRef")
                    // prop2 omitted
                }
            }
        }

        assertNotNull(actual)
        assertEquals(1, actual.contentList.size)
        val inst = actual.contentList[0].cast<SingleRefAttribute>()
        assertEquals("p1OnlyRef", inst.prop1._identity)
        assertNull(inst.prop2)
    }

    @Test
    fun resolver_idempotence_and_re_resolve() {
        val factory = ExamplesFactoryRam()
        val obj = factory.createSingleReferenceAttribute()
        val p1 = factory.createPropType("r1")
        val p2 = factory.createPropType("r2")

        // resolve by reference id r1
        obj.prop1Reference.mutable.reference = "r1"
        factory.resolve(obj.prop1Reference)
        assertEquals(p1, obj.prop1)

        // idempotent: resolve again should be safe and leave value unchanged
        factory.resolve(obj.prop1Reference)
        assertEquals(p1, obj.prop1)

        // change reference and re-resolve
        obj.prop1Reference.mutable.reference = "r2"
        factory.resolve(obj.prop1Reference)
        assertEquals(p2, obj.prop1)
    }

    @Test
    fun asString_contains_properties() {
        val factory = ExamplesFactoryRam()
        val model = Examples(factory, "Test") {
            content {
                PropType("p1")
                PropType("p2")
                SingleReferenceAttribute("obj") {
                    prop1("p1")
                    prop2("p2")
                }
            }
        }

        val actual = Examples_ModelAsString.Examples_asString(model)
        val expected = """
            Examples('ExamplesFactoryRam0','Test')
              contentList
                SingleRefAttribute('ExamplesFactoryRam0','obj')
                  prop1 PropType('ExamplesFactoryRam0','p1')
                  prop2 PropType('ExamplesFactoryRam0','p2')
        """.trimIndent()

        assertEquals(expected, actual)
    }
}
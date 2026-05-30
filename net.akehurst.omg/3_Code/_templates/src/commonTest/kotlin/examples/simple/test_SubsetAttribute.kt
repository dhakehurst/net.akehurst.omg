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

package net.akehurst.omg.templates.examples.simple

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam

class test_SubsetAttribute {

    @Test
    fun addingToP_increasesQMultiplicity() {
        val factory = examples_ModelFactoryRam("Test") // or use your ExamplesFactoryRam() helper
        val simpleFactory = factory.simple
        val ram = SubsetAttributeRam(simpleFactory, "s1")

        val p1 = factory.common.PropType_construct("pt1")

        // add twice to p
        ram.pList.mutable.add(p1)
        ram.pList.mutable.add(p1)

        // p should have 2 occurrences and q should have 2 occurrences
        assertEquals(2, ram.pList.count { it == p1 })
        assertEquals(2, ram.qList.count { it == p1 })
    }

    @Test
    fun cannotRemoveFromQ_belowPMultiplicity() {
        val factory = examples_ModelFactoryRam("Test")
        val simpleFactory = factory.simple
        val ram = SubsetAttributeRam(simpleFactory, "s2")

        val p1 = factory.common.PropType_construct("pt2")
        ram.pList.mutable.add(p1)
        ram.pList.mutable.add(p1) // p has 2, q has 2

        // remove once from q => still allowed (q becomes 1 < p 2) -> should fail
        assertFailsWith<IllegalStateException> {
            ram.qList.mutable.remove(p1)
        }
    }

    @Test
    fun removingFromP_doesNotRemoveFromQ() {
        val factory = examples_ModelFactoryRam("Test")
        val simpleFactory = factory.simple
        val ram = SubsetAttributeRam(simpleFactory, "s3")

        val p1 = factory.common.PropType_construct("pt3")
        ram.pList.mutable.add(p1)
        ram.pList.mutable.add(p1) // q has 2

        // remove one from p only
        ram.pList.mutable.remove(p1)

        // now p has 1, q should still have 2
        assertEquals(1, ram.pList.count { it == p1 })
        assertEquals(2, ram.qList.count { it == p1 })
    }

    @Test
    fun subset_enforcement() {
        val factory =examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.SubsetAttribute_construct("obj")

        val p = factory.common.PropType_construct("s-p1")
        // add via the provided helper to ensure subset is maintained
        obj.pList.mutable.add(p)
        // p must be in q as well
        assertEquals(1, obj.qList.size)
        assertEquals(1, obj.pList.size)

        // removing from q while p contains element should fail
        assertFailsWith<IllegalStateException> {
            obj.qList.mutable.remove(p)
        }

        // remove from p then q can be removed
        obj.pList.mutable.remove(p)
        obj.qList.mutable.remove(p)
        assertEquals(0, obj.qList.size)
        assertEquals(0, obj.pList.size)
    }
}

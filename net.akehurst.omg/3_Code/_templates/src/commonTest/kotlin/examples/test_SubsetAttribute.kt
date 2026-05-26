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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class test_SubsetAttribute {

    @Test
    fun subset_enforcement() {
        val factory = ExamplesFactoryRam()
        val ram = SubsetAttributeRam(factory, "s1")

        val p = factory.PropType_construct("s-p1")
        // add via the provided helper to ensure subset is maintained
        ram.addToP(p)
        // p must be in q as well
        assertEquals(1, ram.qList.size)
        assertEquals(1, ram.pList.size)

        // removing from q while p contains element should fail
        assertFailsWith<IllegalStateException> {
            ram.removeFromQ(p)
        }

        // remove from p then q can be removed
        ram.removeFromP(p)
        ram.removeFromQ(p)
        assertEquals(0, ram.qList.size)
        assertEquals(0, ram.pList.size)
    }
}

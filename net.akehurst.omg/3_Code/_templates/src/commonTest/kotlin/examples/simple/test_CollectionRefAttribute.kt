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

import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.collections.orderedSetOf
import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreExt.resolve
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.omg.templates.examples.common.PropType
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class test_CollectionRefAttribute {

    @Test
    fun factory_and_add_by_value() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.CollectionRefAttribute_construct( "cr1")
        assertNotNull(obj)
        assertTrue(obj.prop1OrderedSet.isEmpty())

        val pv = factory.common.PropType_construct("p1")

        val mref = ManagedReference<Any, PropType>(null, "test.mref", PropType::class)
        mref.mutable.set(pv._identity, pv)

        obj.prop1OrderedSetReference.mutable.add(mref)

        assertEquals(orderedSetOf(pv), obj.prop1OrderedSet)
    }

    @Test
    fun add_by_reference_and_resolve() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.CollectionRefAttribute_construct( "cr2")

        val pv = factory.common.PropType_construct("pr1")
        val mref = ManagedReference<Any, PropType>(null, "test2.mref", PropType::class)
        mref.mutable.reference = pv._identity
        obj.prop2ListReference.mutable.add(mref)

        // unresolved until factory.resolve is called
        try {
            obj.prop2List
            // should throw if unresolved elements exist
        } catch (e: IllegalStateException) {
            // expected
        }

        factory.resolve(mref)
        assertEquals(listOf(pv), obj.prop2List)
    }
}

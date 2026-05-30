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
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.omg.templates.examples.common.PropType
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class test_DerivedUnionRefAttribute {

    @Test
    fun derived_reference_computation() {
        val factory = examples_ModelFactoryRam("TestModelFactory")
        val obj = factory.simple.DerivedUnionRefAttribute_construct( "dr1")
        assertNotNull(obj)

        // initially null
        assertNull(obj.derivedRef)

        val p1 = factory.common.PropType_construct("r1")
        val mref = ManagedReference<Any, PropType>(null, "dr.refs", PropType::class)
        mref.mutable.set(p1._identity, p1)
        obj.refsReference.mutable.add(mref)

        assertEquals(p1, obj.derivedRef)

        // override derivedRef via its backing reference
        val p2 = factory.common.PropType_construct("r2")
        obj.derivedRefReference.mutable.set(p2._identity, p2)
        assertEquals(p2, obj.derivedRef)
    }
}

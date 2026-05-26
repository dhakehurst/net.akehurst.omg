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

import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.collections.orderedSetOf
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class test_CollectionRefRedefSameNameDiffTypeAttribute {

	@Test
	fun redefine_collection_refs() {
		val factory = ExamplesFactoryRam()
		val obj = CollectionRefRedefSameNameDiffTypeAttributeRam(factory, "crr1")
		assertNotNull(obj)

		val pv = factory.PropTypeB_construct("pb1")
		val mref = ManagedReference<Any, PropTypeB>(null, "crr.mref", PropTypeB::class)
		mref.mutable.set(pv._identity, pv)

		obj.prop1OrderedSetReference.mutable.add(mref)
		assertEquals(orderedSetOf(pv), obj.prop1OrderedSet)
	}
}



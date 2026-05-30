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
import net.akehurst.omg.templates.examples.examples_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class test_DerivedUnionCmpAttribute {

	@Test
	fun derived_composite_computation() {
		val factory = examples_ModelFactoryRam("TestModelFactory")
		val obj = factory.simple.DerivedUnionCmpAttribute_construct( "d1")
		assertNotNull(obj)

		val p1 = factory.common.PropType_construct("c1")
		obj.children.mutable.add(p1)
		assertEquals(p1, obj.derived)
	}
}



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

package net.akehurst.omg.kerml.v1_0.agl.types

import net.akehurst.language.types.api.DataType
import net.akehurst.language.types.api.InterfaceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_KerMLTypesDomain {

    private val types = KerMLTypesDomain().types
    private val defs = types.namespace.flatMap { it.ownedTypes }.filter { it is DataType || it is InterfaceType }

    @Test
    fun all_properties_resolve_across_namespaces() {
        assertTrue(defs.isNotEmpty())
        for (d in defs) {
            // resolves supertypes and property types, which are in other namespaces
            d.type().allResolvedProperty.values.forEach { it.typeInstance.resolvedDefinition }
        }
        val feature = defs.first { it.name.value == "Feature" }
        val names = feature.type().allResolvedProperty.keys.map { it.value }
        // declaredName is owned by KerML Element
        assertTrue("declaredName" in names, "$names")
        assertTrue("featuringType" in names, "$names")
    }

    @Test
    fun construct_and_set_by_name() {
        val feature = defs.first { it.name.value == "Feature" } as DataType
        val ctor = feature.constructors.first { it.parameters.size == 1 }
        val obj = ctor.execution!!.invoke(listOf("_f1"))!!
        val declaredName = feature.type().allResolvedProperty.entries.first { it.key.value == "declaredName" }.value.original
        declaredName.mutator!!.invoke(obj, "mass")
        assertEquals("mass", declaredName.accessor!!.invoke(obj))
    }
}

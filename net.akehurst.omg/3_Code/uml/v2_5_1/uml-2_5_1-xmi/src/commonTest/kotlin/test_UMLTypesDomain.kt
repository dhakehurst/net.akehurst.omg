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

package net.akehurst.omg.uml.v2_5_1.xmi

import net.akehurst.language.types.api.DataType
import net.akehurst.language.types.api.InterfaceType
import net.akehurst.omg.uml.v2_5_1.agl.types.UMLTypesDomain
import kotlin.test.Test
import kotlin.test.assertTrue

class test_UMLTypesDomain {

    @Test
    fun all_properties_resolve_across_namespaces() {
        val types = UMLTypesDomain().types
        val defs = types.namespace.flatMap { it.ownedTypes }.filter { it is DataType || it is InterfaceType }
        assertTrue(defs.isNotEmpty())
        for (d in defs) {
            // resolves supertypes and property types, which are in other namespaces
            d.type().allResolvedProperty.values.forEach { it.typeInstance.resolvedDefinition }
        }
        val pkg = defs.first { it.name.value == "Package" }
        val names = pkg.type().allResolvedProperty.keys.map { it.value }
        assertTrue("name" in names, "Package inherits 'name' from NamedElement (UML.CommonStructure): $names")
        assertTrue("packagedElement" in names)
    }
}

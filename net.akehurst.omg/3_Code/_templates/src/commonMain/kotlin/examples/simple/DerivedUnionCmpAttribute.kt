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

import net.akehurst.kotlinx.collections.ManagedList
import net.akehurst.omg.templates.examples.common.Element
import net.akehurst.omg.templates.examples.common.PropType

/**
 * Example of a derived-union composite attribute. The attribute `derived` is a derived-union and therefore
 * appears as a read-only computed getter in the API. Realisation must provide the computation logic.
 */
interface DerivedUnionCmpAttribute : Element {
    /**
     * children: PropType [0..*] {composite}
     */
    val children: List<PropType>

    /**
     * derived: PropType [0..1] {composite isDerivedUnion=true}
     * This is a derived-union: API exposes it as a read-only computed getter (no `{name}Value` holder)
     */
    val derived: PropType?
}

data class DerivedUnionCmpAttributeRam(val _factory: simple_PackageFactory, override val _identity: Any) : DerivedUnionCmpAttribute {
    override val children = ManagedList<PropType>("DerivedUnionCompositeAttribute.children", PropType::class)

    // computed getter: here we provide a simple illustrative computation (first child or null)
    override val derived: PropType? get() = children.firstOrNull()

    override fun toString(): String = "DerivedUnionCompositeAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity'"
}


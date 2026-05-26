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

import net.akehurst.kotlinx.collections.ManagedList
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.Reference

/**
 * Example of a derived-union reference attribute. The attribute `derivedRef` is a derived-union and therefore
 * appears as a read-only computed getter in the API. Realisation must provide the computation logic.
 */
interface DerivedUnionRefAttribute : Element {
    /**
     * refs: PropType [0..*] {reference}
     */
    val refsReference: List<Reference<Any, PropType>>
    val refs: List<PropType>

    /**
     * derivedRef: PropType [0..1] {reference isDerivedUnion=true}
     */
    val derivedRefReference: Reference<Any, PropType>
    val derivedRef: PropType?
}

data class DerivedUnionRefAttributeRam(val _factory: Examples_Factory, override val _identity: Any) : DerivedUnionRefAttribute {
    private val _refs = ManagedList<ManagedReference<Any, PropType>>("DerivedUnionRefAttribute.refs", ManagedReference::class)
    override val refsReference: List<Reference<Any, PropType>> get() = _refs
    override val refs: List<PropType> get() = _refs.map { it.resolved ?: throw IllegalStateException("unresolved reference in refs") }

    // derivedRef is a computed read-only reference. For illustration we compute it from the first resolved ref if present.
    private val _derivedRefBacking = ManagedReference<Any, PropType>(null, "DerivedUnionRefAttribute.derivedRef", PropType::class)
    override val derivedRefReference: Reference<Any, PropType> get() = _derivedRefBacking
    override val derivedRef: PropType? get() = _derivedRefBacking.resolved ?: refs.firstOrNull()

    override fun toString(): String = "DerivedUnionRefAttribute '${_factory._identity}.$_identity'"
}


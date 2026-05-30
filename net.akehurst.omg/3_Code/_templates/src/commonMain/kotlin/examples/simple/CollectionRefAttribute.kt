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
import net.akehurst.kotlinx.collections.ManagedOrderedSet
import net.akehurst.kotlinx.collections.ManagedSet
import net.akehurst.kotlinx.collections.OrderedSet
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.Reference
import net.akehurst.omg.templates.examples.common.Element
import net.akehurst.omg.templates.examples.common.PropType


interface CollectionRefAttribute : Element {
    /**
     * prop1: PropType [0..*] {reference unique ordered}
     */
    val prop1OrderedSet: OrderedSet<PropType>
    val prop1OrderedSetReference: OrderedSet<Reference<Any, PropType>>

    /**
     * prop2: PropType [0..*] {reference nonunique ordered}
     */
    val prop2List: List<PropType>
    val prop2ListReference: List<Reference<Any, PropType>>

    /**
     * prop3: PropType [0..*] {reference unique unordered}
     */
    val prop3Set: Set<PropType>
    val prop3SetReference: Set<Reference<Any, PropType>>

    /**
     * prop4: PropType [0..*] {reference nonunique unordered}
     */
    val prop4Collection: Collection<PropType>
    val prop4CollectionReference: Collection<Reference<Any, PropType>>
}


data class CollectionRefAttributeRam(val _factory: simple_PackageFactory, override val _identity: Any) : CollectionRefAttribute {

    // backing stores are managed collections of ManagedReference so callbacks can be supported by RAM realisation
    private val _prop1 = ManagedOrderedSet<ManagedReference<Any, PropType>>("CollectionRefAttribute.prop1", ManagedReference::class)
    override val prop1OrderedSetReference: OrderedSet<Reference<Any, PropType>> get() = _prop1
    override val prop1OrderedSet: OrderedSet<PropType>
        get() = run {
            val res = ManagedOrderedSet<PropType>("CollectionRefAttribute.prop1.res", PropType::class)
            for (r in _prop1) {
                val resolved = r.resolved ?: throw IllegalStateException("unresolved reference in prop1OrderedSet")
                res.add(resolved)
            }
            res
        }

    private val _prop2 = ManagedList<ManagedReference<Any, PropType>>("CollectionRefAttribute.prop2", ManagedReference::class)
    override val prop2ListReference: List<Reference<Any, PropType>> get() = _prop2
    override val prop2List: List<PropType>
        get() = _prop2.map { it.resolved ?: throw IllegalStateException("unresolved reference in prop2List") }

    private val _prop3 = ManagedSet<ManagedReference<Any, PropType>>("CollectionRefAttribute.prop3", ManagedReference::class)
    override val prop3SetReference: Set<Reference<Any, PropType>> get() = _prop3
    override val prop3Set: Set<PropType>
        get() = run {
            val res = ManagedSet<PropType>("CollectionRefAttribute.prop3.res", PropType::class)
            for (r in _prop3) {
                val resolved = r.resolved ?: throw IllegalStateException("unresolved reference in prop3Set")
                res.add(resolved)
            }
            res
        }

    private val _prop4 = ManagedList<ManagedReference<Any, PropType>>("CollectionRefAttribute.prop4", ManagedReference::class)
    override val prop4CollectionReference: Collection<Reference<Any, PropType>> get() = _prop4
    override val prop4Collection: Collection<PropType>
        get() = _prop4.map { it.resolved ?: throw IllegalStateException("unresolved reference in prop4Collection") }

    override fun toString(): String = "CollectionRefAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity'"
}


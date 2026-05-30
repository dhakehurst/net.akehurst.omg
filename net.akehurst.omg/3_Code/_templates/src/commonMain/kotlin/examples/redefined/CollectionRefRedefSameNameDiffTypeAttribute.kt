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

package net.akehurst.omg.templates.examples.redefined

import net.akehurst.kotlinx.collections.ManagedList
import net.akehurst.kotlinx.collections.ManagedOrderedSet
import net.akehurst.kotlinx.collections.ManagedSet
import net.akehurst.kotlinx.collections.OrderedSet
import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.Reference
import net.akehurst.omg.templates.examples.common.PropTypeB
import net.akehurst.omg.templates.examples.simple.CollectionRefAttribute


interface CollectionRefRedefSameNameDiffTypeAttribute : CollectionRefAttribute {
    /**
     * prop1: PropTypeB [0..*] {reference unique ordered} redefines CollectionRefAttribute.prop1
     */
    override val prop1OrderedSet: OrderedSet<PropTypeB>
    override val prop1OrderedSetReference: OrderedSet<Reference<Any, PropTypeB>>

    /**
     * prop2: PropTypeB [0..*] {reference nonunique ordered} redefines CollectionRefAttribute.prop2
     */
    override val prop2List: List<PropTypeB>
    override val prop2ListReference: List<Reference<Any, PropTypeB>>

    /**
     * prop3: PropTypeB [0..*] {reference unique unordered} redefines CollectionRefAttribute.prop3
     */
    override val prop3Set: Set<PropTypeB>
    override val prop3SetReference: Set<Reference<Any, PropTypeB>>

    /**
     * prop4: PropTypeB [0..*] {reference nonunique unordered} redefines CollectionRefAttribute.prop4
     */
    override val prop4Collection: Collection<PropTypeB>
    override val prop4CollectionReference: Collection<Reference<Any, PropTypeB>>
}


data class CollectionRefRedefSameNameDiffTypeAttributeRam(val _factory: redefined_PackageFactory, override val _identity: Any) : CollectionRefRedefSameNameDiffTypeAttribute {

    private val _prop1 = ManagedOrderedSet<ManagedReference<Any, PropTypeB>>("CollectionRefRedefSameNameDiffTypeAttribute.prop1", ManagedReference::class)
    override val prop1OrderedSetReference: OrderedSet<Reference<Any, PropTypeB>> get() = _prop1
    override val prop1OrderedSet: OrderedSet<PropTypeB>
        get() = run {
            val res = ManagedOrderedSet<PropTypeB>("CollectionRefRedefSameNameDiffTypeAttribute.prop1.res", PropTypeB::class)
            for (r in _prop1) {
                val resolved = r.resolved ?: throw IllegalStateException("unresolved reference in prop1OrderedSet")
                res.add(resolved)
            }
            res
        }

    private val _prop2 = ManagedList<ManagedReference<Any, PropTypeB>>("CollectionRefRedefSameNameDiffTypeAttribute.prop2", ManagedReference::class)
    override val prop2ListReference: List<Reference<Any, PropTypeB>> get() = _prop2
    override val prop2List: List<PropTypeB>
        get() = _prop2.map { it.resolved ?: throw IllegalStateException("unresolved reference in prop2List") }

    private val _prop3 = ManagedSet<ManagedReference<Any, PropTypeB>>("CollectionRefRedefSameNameDiffTypeAttribute.prop3", ManagedReference::class)
    override val prop3SetReference: Set<Reference<Any, PropTypeB>> get() = _prop3
    override val prop3Set: Set<PropTypeB>
        get() = _prop3.mapTo(LinkedHashSet()) { it.resolved ?: throw IllegalStateException("unresolved reference in prop3Set") }

    private val _prop4 = ManagedList<ManagedReference<Any, PropTypeB>>("CollectionRefRedefSameNameDiffTypeAttribute.prop4", ManagedReference::class)
    override val prop4CollectionReference: Collection<Reference<Any, PropTypeB>> get() = _prop4
    override val prop4Collection: Collection<PropTypeB>
        get() = _prop4.map { it.resolved ?: throw IllegalStateException("unresolved reference in prop4Collection") }

    override fun toString(): String = "CollectionRefRedefSameNameDiffTypeAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity'"
}


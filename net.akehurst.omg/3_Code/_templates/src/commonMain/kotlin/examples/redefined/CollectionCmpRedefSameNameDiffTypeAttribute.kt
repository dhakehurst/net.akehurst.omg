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
import net.akehurst.omg.templates.examples.common.PropTypeB
import net.akehurst.omg.templates.examples.simple.CollectionCmpAttribute


interface CollectionCmpRedefSameNameDiffTypeAttribute : CollectionCmpAttribute {
    /**
     * prop1: PropType [0..*] { composite unique ordered redefines CollectionCompositeAttribute.prop1 }
     */
    override val prop1OrderedSet: OrderedSet<PropTypeB>

    /**
     * prop2: PropType [0..*] { composite nonunique ordered redefines CollectionCompositeAttribute.prop2 }
     */
    override val prop2List: List<PropTypeB>

    /**
     * prop3: PropType [0..*] {composite unique unordered redefines CollectionCompositeAttribute.prop3 }
     */
    override val prop3Set: Set<PropTypeB>

    /**
     * prop4: PropType [0..*] {composite nonunique unordered redefines CollectionCompositeAttribute.prop4 }
     */
    override val prop4Collection: Collection<PropTypeB>
}

data class CollectionCmpRedefSameNameDiffTypeAttributeRam(val _factory: redefined_PackageFactory, override val _identity: Any): CollectionCmpRedefSameNameDiffTypeAttribute {

    // --- CollectionCompositeAttribute ---
    //override var prop1Collection: PropType  REDEFINED
    //override val prop2List: PropType REDEFINED
    //override val prop3Set: PropType REDEFINED
    //override val prop4Collection: PropType REDEFINED

    // --- CollectionCompositeRedefinedSameNameDiffTypeAttribute ---
    override val prop1OrderedSet: OrderedSet<PropTypeB> = ManagedOrderedSet("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop1",PropTypeB::class)
    override val prop2List: List<PropTypeB> = ManagedList("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop2", PropTypeB::class)
    override val prop3Set: Set<PropTypeB> = ManagedSet("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop3", PropTypeB::class)
    override val prop4Collection: Collection<PropTypeB> = ManagedList("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop4", PropTypeB::class)

    override fun toString(): String = "CollectionCmpRedefSameNameDiffTypeAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity'"
}
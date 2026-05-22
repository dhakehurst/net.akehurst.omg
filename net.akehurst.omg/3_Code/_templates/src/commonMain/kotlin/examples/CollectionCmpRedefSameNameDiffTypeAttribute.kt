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
import net.akehurst.kotlinx.collections.ManagedOrderedSet
import net.akehurst.kotlinx.collections.OrderedSet


interface CollectionCmpRedefSameNameDiffTypeAttribute : CollectionCmpAttribute {
    /**
     * prop1: PropType [1] { composite unique ordered redefines CollectionCompositeAttribute.prop1 }
     */
    override val prop1OrderedSet: OrderedSet<PropTypeB>

    /**
     * prop2: PropType [0..1] { composite nonunique ordered redefines CollectionCompositeAttribute.prop2 }
     */
    override val prop2: List<PropTypeB>
}

data class CollectionCmpRedefSameNameDiffTypeAttributeRam(override val identifier_: Any): CollectionCmpRedefSameNameDiffTypeAttribute {

    // --- CollectionCompositeAttribute ---
    //override var prop1Collection  REDEFINED
    //override val prop2: PropType? REDEFINED

    // --- CollectionCompositeRedefinedSameNameDiffTypeAttribute ---
    override val prop1OrderedSet = ManagedOrderedSet<PropTypeB>("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop1",CollectionCmpRedefSameNameDiffTypeAttribute::class)
    override val prop2: List<PropTypeB> = ManagedList<PropTypeB>("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop2", CollectionCmpRedefSameNameDiffTypeAttribute::class)
    override val prop3: Set<PropType>  get() = TODO("not implemented")
    override val prop4: Collection<PropType> = ManagedList<PropTypeB>("CollectionCompositeRedefinedSameNameDiffTypeAttribute.prop4",CollectionCmpRedefSameNameDiffTypeAttribute::class)
}
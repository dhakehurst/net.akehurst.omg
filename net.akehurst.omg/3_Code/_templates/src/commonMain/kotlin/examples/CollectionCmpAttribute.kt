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
import net.akehurst.kotlinx.collections.ManagedSet
import net.akehurst.kotlinx.collections.MutableOrderedSet
import net.akehurst.kotlinx.collections.OrderedSet


interface CollectionCmpAttribute : Element {
    /**
     * prop1: PropType [0..*] {composite unique ordered}
     */
    val prop1OrderedSet: OrderedSet<PropType>

    /**
     * prop2: PropType [0..*] {composite nonunique ordered}
     */
    val prop2: List<PropType>

    /**
     * prop3: PropType [0..*] {composite unique unordered}
     */
    val prop3: Set<PropType>

    /**
     * prop4: PropType [0..*] {composite nonunique unordered}
     */
    val prop4: Collection<PropType>
}
fun CollectionCmpAttribute.prop1OrderedSet_mutable(): MutableOrderedSet<PropType> = this.prop1OrderedSet as MutableOrderedSet

data class CollectionCmpAttributeRam(override val identifier_: Any) : CollectionCmpAttribute {
    override var prop1OrderedSet = ManagedOrderedSet<PropType>("CollectionCompositeAttribute.prop1",PropType::class)
    override val prop2 = ManagedList<PropType>("CollectionCompositeAttribute.prop2",PropType::class)
    override val prop3 = ManagedSet<PropType>("CollectionCompositeAttribute.prop3",PropType::class)
    override val prop4 = ManagedList<PropType>("CollectionCompositeAttribute.prop4",PropType::class)
}
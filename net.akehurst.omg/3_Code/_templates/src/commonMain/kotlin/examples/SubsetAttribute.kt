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

/**
 * Example showing subsetting: p is a subset of q. Realisations must enforce p ⊆ q at runtime.
 */
interface SubsetAttribute : Element {
    /**
     * q: PropType [0..*] {composite}
     */
    val qList: List<PropType>

    /**
     * p: PropType [0..*] {composite subset q}
     */
    val pList: List<PropType>
}

data class SubsetAttributeRam(val _factory: Examples_Factory, override val _identity: Any) : SubsetAttribute {
    // backing stores
    private val _q = ManagedList<PropType>("SubsetAttribute.q", PropType::class)
    override val qList: List<PropType> get() = _q

    private val _p = ManagedList<PropType>("SubsetAttribute.p", PropType::class)
    override val pList: List<PropType> get() = _p

    // helper mutators that enforce p ⊆ q semantics (illustrative - real enforcement approach may vary)
    fun addToQ(item: PropType) {
        if (!_q.contains(item)) _q.add(item)
    }

    fun addToP(item: PropType) {
        // ensure membership in q
        if (!_q.contains(item)) _q.add(item)
        if (!_p.contains(item)) _p.add(item)
    }

    fun removeFromQ(item: PropType) {
        // cannot remove from q if that would violate p ⊆ q
        if (_p.contains(item)) throw IllegalStateException("cannot remove from q: element present in subset p")
        _q.remove(item)
    }

    fun removeFromP(item: PropType) {
        _p.remove(item)
    }

    override fun toString(): String = "SubsetAttribute '${_factory._identity}.$_identity'"
}


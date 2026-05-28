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

import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.kotlinx.collections.ManagedList
import net.akehurst.omg.templates.examples.common.Element
import net.akehurst.omg.templates.examples.common.PropType

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

data class SubsetAttributeRam(val _factory: simple_PackageFactory, override val _identity: Any) : SubsetAttribute {
    // q is the superset
    override val qList: List<PropType> = ManagedList<PropType>(
        "SubsetAttribute.q",
        PropType::class,
        onAdded = { element -> },
        onRemoved = { element ->
            // For lists we must consider multiplicity: after removal the count of element in q
            // must still be >= the count in p, otherwise p ⊆ q would be violated.
            val pCount = pList.count { it == element }
            val qCountAfter = qList.mutable.count { it == element }
            if (qCountAfter < pCount) error("Removed element '$element' from qList, but subset pList still contains $pCount occurrences")
        }
    )

    // p is the subset. It must be stored (not recreated on each access) so containment checks work.
    // Adding to p must ensure the element is also present in q. Removing from p does not need to
    // remove from q (p ⊆ q still holds), so we leave q unchanged on p removal.
    override val pList: List<PropType> = ManagedList<PropType>(
        "SubsetAttribute.p",
        PropType::class,
        onAdded = { element ->
            // For lists we preserve multiplicity: every time an element is added to p,
            // add one occurrence to q so that p ⊆ q (counts are maintained).
            qList.mutable.add(element)
        },
        onRemoved = { element ->
            // no-op: removing one occurrence from the subset does not force removal from the superset
        }
    )

    override fun toString(): String = "SubsetAttribute '${_factory.identity}.$_identity'"
}


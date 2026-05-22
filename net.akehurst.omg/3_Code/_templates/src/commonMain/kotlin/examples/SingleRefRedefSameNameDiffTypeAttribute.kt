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

import net.akehurst.kotlinx.utils.MutableReferenceDefault
import net.akehurst.kotlinx.utils.Reference

interface SingleRefRedefSameNameDiffTypeAttribute : SingleRefAttribute {
    /**
     * prop1: PropType [1] { redefines SingleReferenceAttribute.prop1 }
     */
    override val prop1: PropTypeB
    override val prop1Reference: Reference<Any, PropTypeB>
    fun prop1_set(value: PropTypeB)

    /**
     * prop2: PropType [0..1] { redefines SingleReferenceAttribute.prop1 }
     */
    override val prop2: PropTypeB?
    override val prop2Reference: Reference<Any, PropTypeB>
    fun prop2_set(value: PropTypeB?)
}

data class SingleRefRedefSameNameDiffTypeAttributeRam(override val identifier_: Any): SingleRefRedefSameNameDiffTypeAttribute {

    // --- SingleCompositeAttribute ---
    //override val prop1: PropTypeB REDEFINED
    override fun prop1_set(value: PropType) = this.prop1_set(value as PropTypeB)

    //override val prop2: PropTypeB? REDEFINED
    override fun prop2_set(value: PropType?) = this.prop2_set(value as PropTypeB?)

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override val prop1: PropTypeB get() = prop1Reference.resolved ?: error("prop1 not resolved")
    override val prop1Reference = MutableReferenceDefault<Any, PropTypeB>(null)
    override fun prop1_set(value: PropTypeB) {
        this.prop1Reference.clear()
        this.prop1Reference.set(value.identifier_, value)
    }

    override val prop2: PropTypeB? get() = prop2Reference.resolved
    override val prop2Reference = MutableReferenceDefault<Any, PropTypeB>(null)
    override fun prop2_set(value: PropTypeB?) {
        this.prop2Reference.clear()
        value?.let { this.prop2Reference.set(value.identifier_, value) }
    }
}
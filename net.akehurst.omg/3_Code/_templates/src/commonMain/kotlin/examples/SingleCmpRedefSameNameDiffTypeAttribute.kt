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



interface SingleCmpRedefSameNameDiffTypeAttribute : SingleCmpAttribute {
    /**
     * prop1: PropType [1] { redefines SingleCompositeAttribute.prop1 }
     */
    override val prop1: PropTypeB
    fun prop1_set(value: PropTypeB)

    /**
     * prop2: PropType [0..1] { redefines SingleCompositeAttribute.prop2 }
     */
    override val prop2: PropTypeB?
    fun prop2_set(value: PropTypeB?)
}

data class SingleCmpRedefSameNameDiffTypeAttributeRam(override val identifier_: Any): SingleCmpRedefSameNameDiffTypeAttribute {

    // --- SingleCompositeAttribute ---
    //override val prop1: PropType REDEFINED
    override fun prop1_set(value: PropType) = this.prop1_set(value as PropTypeB)

    //override val prop2: PropType? REDEFINED
    override fun prop2_set(value: PropType?) = this.prop2_set(value as PropTypeB?)

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override var prop1: PropTypeB = PropTypeBRam()
    override fun prop1_set(value: PropTypeB) {
        this.prop1 = value
    }

    override var prop2: PropTypeB? = null
    override fun prop2_set(value: PropTypeB?) {
        this.prop2 = value
    }
}
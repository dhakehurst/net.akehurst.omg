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



interface SingleCompositeRedefinedSameNameDiffTypeAttribute : SingleCompositeAttribute {
    /**
     * prop1: PropType [1] { redefines SingleCompositeAttribute.prop1 }
     */
    override val prop1: PropTypeB
    fun set_prop1(value: PropTypeB)

    /**
     * prop2: PropType [0..1] { redefines SingleCompositeAttribute.prop2 }
     */
    override val prop2: PropTypeB?
    fun set_prop2(value: PropTypeB?)
}

data class SingleCompositeRedefinedSameNameDiffTypeAttributeRam(override val identifier_: Any): SingleCompositeRedefinedSameNameDiffTypeAttribute {

    // --- SingleCompositeAttribute ---
    //override val prop1: PropType REDEFINED
    override fun set_prop1(value: PropType) = this.set_prop1(value as PropTypeB)

    //override val prop2: PropType? REDEFINED
    override fun set_prop2(value: PropType?) = this.set_prop2(value as PropTypeB?)

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override var prop1: PropTypeB = PropTypeBRam()
    override fun set_prop1(value: PropTypeB) {
        this.prop1 = value
    }

    override var prop2: PropTypeB? = null
    override fun set_prop2(value: PropTypeB?) {
        this.prop2 = value
    }
}
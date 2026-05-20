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

interface SingleCompositeRedefinedDiffNameSameTypeAttribute : SingleCompositeAttribute {
    /**
     * prop1: PropType [1] { redefines SingleCompositeAttribute.prop1 }
     */
    val redefinesProp1: PropType
    fun set_redefinesProp1(value: PropType)

    /**
     * prop2: PropType [0..1] { redefines SingleCompositeAttribute.prop2 }
     */
    val redefinesProp2: PropType?
    fun set_redefinesProp2(value: PropType?)
}

data class SingleCompositeRedefinedDiffNameSameTypeAttributeRam(override val identifier_: Any): SingleCompositeRedefinedDiffNameSameTypeAttribute {

    // --- SingleCompositeAttribute ---
    override val prop1: PropType get() = redefinesProp1
    override fun set_prop1(value: PropType) = this.set_redefinesProp1(value)

    override val prop2: PropType? get() = redefinesProp2
    override fun set_prop2(value: PropType?) = this.set_redefinesProp2(value)

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override var redefinesProp1: PropType = PropTypeRam()
    override fun set_redefinesProp1(value: PropType) {
        this.redefinesProp1 = value
    }

    override var redefinesProp2: PropType? = null

    override fun set_redefinesProp2(value: PropType?) {
        this.redefinesProp2 = value
    }
}
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


interface SingleReferenceRedefinedDiffNameSameTypeAttribute : SingleReferenceAttribute {
    /**
     * prop1: PropType [1] { redefines SingleReferenceAttribute.prop1 }
     */
    val redefinesProp1: PropType
    val redefinesProp1Reference: MutableReferenceDefault<Any, PropType>
    fun redefinesProp1_set(value: PropType?)

    /**
     * prop2: PropType [0..1] { redefines SingleReferenceAttribute.prop2 }
     */
    val redefinesProp2: PropType?
    val redefinesProp2Reference: MutableReferenceDefault<Any, PropType>
    fun redefinesProp2_set(value: PropType?)
}

data class SingleReferenceRedefinedDiffNameSameTypeAttributeRam(override val identifier_: Any) : SingleReferenceRedefinedDiffNameSameTypeAttribute {

    // --- SingleCompositeAttribute ---
    override val prop1: PropType get() = redefinesProp1
    override val prop1Reference: MutableReferenceDefault<Any, PropType> get() = redefinesProp1Reference
    override fun prop1_set(value: PropType) {
        TODO("not implemented")
    }

    override val prop2: PropType? get() = redefinesProp2
    override val prop2Reference: MutableReferenceDefault<Any, PropType> get() = redefinesProp2Reference
    override fun prop2_set(value: PropType?) {
        TODO("not implemented")
    }

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override val redefinesProp1: PropType get() = redefinesProp1Reference.resolved ?: error("prop1 not resolved")
    override val redefinesProp1Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun redefinesProp1_set(value: PropType?) {
        TODO("not implemented")
    }

    override val redefinesProp2: PropType? get() = redefinesProp2Reference.resolved
    override val redefinesProp2Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun redefinesProp2_set(value: PropType?) {
        TODO("not implemented")
    }

}
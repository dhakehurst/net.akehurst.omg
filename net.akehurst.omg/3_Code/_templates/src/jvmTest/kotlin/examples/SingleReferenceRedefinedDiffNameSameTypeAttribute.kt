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
     * prop1: PropType [1] {redefines SingleCompositeAttribute.prop1}
     */
    val redefinesProp1: PropType
    fun set_redefinesProp1(value: PropType)
    fun set_redefinesProp1Reference(ref: Any)

    /**
     * prop2: PropType [0..1]
     */
    val redefinesProp2: PropType?
    fun set_redefinesProp2(value: PropType?)
    fun set_redefinesProp2Reference(ref: Any?)
}

data class SingleReferenceRedefinedDiffNameSameTypeAttributeRam(val identifier_: Any): SingleReferenceRedefinedDiffNameSameTypeAttribute {

    // --- SingleCompositeAttribute ---
    override val prop1: PropType get() = redefinesProp1
    override fun set_prop1(value: PropType) = this.set_redefinesProp1(value)
    override fun set_prop1Reference(ref: Any) = this.set_redefinesProp1Reference(ref)

    override val prop2: PropType? get() = redefinesProp2
    override fun set_prop2(value: PropType?) = this.set_redefinesProp2(value)
    override fun set_prop2Reference(ref: Any?) = this.set_redefinesProp2Reference(ref)

    // --- SingleCompositeRedefinedDiffNameSameTypeAttribute ---
    override val redefinesProp1: PropType get() = _redefinesProp1Reference.resolved ?: error("prop1 not resolved")
    private var _redefinesProp1Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun set_redefinesProp1(value: PropType) {
        this._redefinesProp1Reference.clear()
        this._redefinesProp1Reference.set(value.identifier_, value)
    }
    override fun set_redefinesProp1Reference(ref: Any) {
        this._redefinesProp1Reference.clear()
        this._redefinesProp1Reference.reference = ref
    }

    override val redefinesProp2: PropType? get() = _redefinesProp2Reference.resolved
    private var _redefinesProp2Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun set_redefinesProp2(value: PropType?) {
        this._redefinesProp2Reference.clear()
        value?.let { this._redefinesProp2Reference.set(value.identifier_, value) }
    }
    override fun set_redefinesProp2Reference(ref: Any?) {
        this._redefinesProp2Reference.clear()
        this._redefinesProp2Reference.reference = ref
    }
}
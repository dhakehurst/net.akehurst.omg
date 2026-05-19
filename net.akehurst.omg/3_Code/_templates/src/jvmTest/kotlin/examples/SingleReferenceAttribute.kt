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

interface SingleReferenceAttribute {
    /**
     * prop1: PropType [1] {reference}
     */
    val prop1: PropType
    fun set_prop1(value: PropType)
    fun set_prop1Reference(ref: Any)

    /**
     * prop2: PropType [0..1] {reference}
     */
    val prop2: PropType?
    fun set_prop2(value: PropType?)
    fun set_prop2Reference(ref: Any?)
}

data class ReferenceSingleAttributeRam(val identifier_: Any) : SingleReferenceAttribute {

    override val prop1: PropType get() = _prop1Reference.resolved ?: error("prop1 not resolved")
    private var _prop1Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun set_prop1(value: PropType) {
        this._prop1Reference.clear()
        this._prop1Reference.set(value.identifier_, value)
    }
    override fun set_prop1Reference(ref: Any) {
        this._prop1Reference.clear()
        this._prop1Reference.reference = ref
    }

    override val prop2: PropType? get() = _prop2Reference.resolved
    private var _prop2Reference = MutableReferenceDefault<Any, PropType>(null)
    override fun set_prop2(value: PropType?) {
        this._prop1Reference.clear()
        value?.let { this._prop1Reference.set(value.identifier_, value) }
    }
    override fun set_prop2Reference(ref: Any?) {
        this._prop2Reference.clear()
        this._prop2Reference.reference = ref
    }
}
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


interface SingleCompositeAttribute: Element {
    /**
     * prop1: PropType [1] {composite}
     */
    val prop1: PropType
    fun prop1_set(value: PropType)

    /**
     * prop2: PropType [0..1] {composite}
     */
    val prop2: PropType?
    fun prop2_set(value: PropType?)
}

data class SingleCompositeAttributeRam(override val identifier_: Any): SingleCompositeAttribute {
    override var prop1: PropType = PropTypeRam()
    override fun prop1_set(value: PropType) {
        this.prop1 = value
    }

    override var prop2: PropType? = null
    override fun prop2_set(value: PropType?) {
        this.prop2 = value
    }
}
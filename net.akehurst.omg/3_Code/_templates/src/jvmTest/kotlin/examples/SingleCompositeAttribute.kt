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


interface PropType {
    val identifier_:Any
}

interface SingleCompositeAttribute {
    /**
     * prop1: PropType [1] {composite}
     */
    val prop1: PropType
    fun set_prop1(value: PropType)

    /**
     * prop2: PropType [0..1] {composite}
     */
    val prop2: PropType?
    fun set_prop2(value: PropType?)
}

data class SingleCompositeAttributeRam(val identifier_: Any): SingleCompositeAttribute {
    override lateinit var prop1: PropType
    override fun set_prop1(value: PropType) {
        this.prop1 = value
    }

    override var prop2: PropType? = null
    override fun set_prop2(value: PropType?) {
        this.prop2 = value
    }
}
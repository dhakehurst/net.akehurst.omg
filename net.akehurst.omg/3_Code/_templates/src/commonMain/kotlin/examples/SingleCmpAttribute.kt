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

import net.akehurst.kotlinx.utils.ManagedValue
import net.akehurst.kotlinx.utils.Value


interface SingleCmpAttribute : Element {
    /**
     * prop1: PropType [1] {composite}
     */
    val prop1: PropType
    val prop1Value: Value<PropType>

    /**
     * prop2: PropType [0..1] {composite}
     */
    val prop2: PropType?
    val prop2Value: Value<PropType?>
}



data class SingleCmpAttributeRam(val _factory: Examples_Factory, override val _identity: Any) : SingleCmpAttribute {
    override val prop1Value: Value<PropType> = ManagedValue(_factory.PropType_construct(), "SingleCmpAttributeRam.prop1", PropType::class)
    override val prop1: PropType get() = prop1Value.get()

    override val prop2Value: Value<PropType?> = ManagedValue(null,"SingleCmpAttributeRam.prop2", PropType::class)
    override val prop2: PropType? get() = prop2Value.get()

    override fun toString(): String = "SingleCmpAttribute '${_factory._identity}.${_identity}'"
}


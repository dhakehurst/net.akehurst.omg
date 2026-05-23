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


interface SingleCmpRedefSameNameDiffTypeAttribute : SingleCmpAttribute {
    /**
     * prop1: PropTypeB [1] { redefines SingleCompositeAttribute.prop1 }
     */
    override val prop1: PropTypeB
    override val prop1Value: Value<PropTypeB>

    /**
     * prop2: PropTypeB [0..1] { redefines SingleCompositeAttribute.prop2 }
     */
    override val prop2: PropTypeB?
    override val prop2Value: Value<PropTypeB?>
}

data class SingleCmpRedefSameNameDiffTypeAttributeRam(val _factory: Examples_Factory, override val _identity: Any) : SingleCmpRedefSameNameDiffTypeAttribute {
    override val prop1Value: Value<PropTypeB> = ManagedValue(_factory.createPropTypeB(), "SingleCmpRedefSameNameDiffTypeAttribute.prop1",PropTypeB::class)
    override val prop1: PropTypeB get() = prop1Value.get()

    override val prop2Value: Value<PropTypeB?> = ManagedValue<PropTypeB?>(null, "SingleCmpRedefSameNameDiffTypeAttribute.prop2", PropTypeB::class)
    override val prop2: PropTypeB? get() = prop2Value.get()

    override fun toString(): String = "SingleCmpRedefSameNameDiffTypeAttribute('${_factory._identity}','$_identity')"
}

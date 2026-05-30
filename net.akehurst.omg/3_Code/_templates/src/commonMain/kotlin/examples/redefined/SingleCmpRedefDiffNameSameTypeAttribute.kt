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

package net.akehurst.omg.templates.examples.redefined

import net.akehurst.kotlinx.utils.ManagedValue
import net.akehurst.kotlinx.utils.Value
import net.akehurst.omg.templates.examples.common.PropType
import net.akehurst.omg.templates.examples.examples_ModelFactory
import net.akehurst.omg.templates.examples.simple.SingleCmpAttribute

interface SingleCmpRedefDiffNameSameTypeAttribute : SingleCmpAttribute {
    /**
     * prop1: PropType [1] { redefines SingleCompositeAttribute.prop1 }
     */
    val redefinesProp1: PropType
    val redefinesProp1Value: Value<PropType>

    /**
     * prop2: PropType [0..1] { redefines SingleCompositeAttribute.prop2 }
     */
    val redefinesProp2: PropType?
    val redefinesProp2Value: Value<PropType?>
}

data class SingleCmpRedefDiffNameSameTypeAttributeRam(val _factory: redefined_PackageFactory, override val _identity: Any) : SingleCmpRedefDiffNameSameTypeAttribute {
    override val redefinesProp1Value: Value<PropType> = ManagedValue(_factory.rootFactory.common.PropType_construct("${_identity}.redefinesProp1ValueDefaultValue"),"SingleCmpRedefDiffNameSameTypeAttributeRam.redefinesProp1", PropType::class)
    override val redefinesProp1: PropType get() = redefinesProp1Value.get()
    override val prop1Value: Value<PropType> get() = redefinesProp1Value
    override val prop1: PropType get() = prop1Value.get()

    override val redefinesProp2Value: Value<PropType?> = ManagedValue(null,"SingleCmpRedefDiffNameSameTypeAttributeRam.redefinesProp2", PropType::class)
    override val redefinesProp2: PropType? get() = redefinesProp2Value.get()
    override val prop2Value: Value<PropType?> get() = redefinesProp2Value
    override val prop2: PropType? get() = prop2Value.get()

    override fun toString(): String = "SingleCmpRedefDiffNameSameTypeAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity'"
}

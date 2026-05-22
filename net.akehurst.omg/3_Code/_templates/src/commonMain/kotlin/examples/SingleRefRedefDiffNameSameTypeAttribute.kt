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


import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.Reference


interface SingleRefRedefDiffNameSameTypeAttribute : SingleRefAttribute {
    /**
     * prop1: PropType [1] { redefines SingleReferenceAttribute.prop1 }
     */
    val redefinesProp1: PropType
    val redefinesProp1Reference: Reference<Any, PropType>

    /**
     * prop2: PropType [0..1] { redefines SingleReferenceAttribute.prop2 }
     */
    val redefinesProp2: PropType?
    val redefinesProp2Reference: Reference<Any, PropType>
}

data class SingleRefRedefDiffNameSameTypeAttributeRam(val _factory: Examples_Factory, override val _identity: Any) : SingleRefRedefDiffNameSameTypeAttribute {

    override val prop1: PropType get() = redefinesProp1
    override val prop1Reference: Reference<Any, PropType> get() = redefinesProp1Reference

    override val prop2: PropType? get() = redefinesProp2
    override val prop2Reference: Reference<Any, PropType> get() = redefinesProp2Reference

    override val redefinesProp1: PropType get() = redefinesProp1Reference.resolved ?: error("prop1 not resolved")
    override val redefinesProp1Reference = ManagedReference<Any, PropType>(null)

    override val redefinesProp2: PropType? get() = redefinesProp2Reference.resolved
    override val redefinesProp2Reference = ManagedReference<Any, PropType>(null)

}
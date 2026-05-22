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

interface SingleRefRedefSameNameDiffTypeAttribute : SingleRefAttribute {
    /**
     * prop1: PropType [1] { redefines SingleReferenceAttribute.prop1 }
     */
    override val prop1: PropTypeB
    override val prop1Reference: Reference<Any, PropTypeB>

    /**
     * prop2: PropType [0..1] { redefines SingleReferenceAttribute.prop1 }
     */
    override val prop2: PropTypeB?
    override val prop2Reference: Reference<Any, PropTypeB>
}

data class SingleRefRedefSameNameDiffTypeAttributeRam(val _factory: Examples_Factory, override val _identity: Any): SingleRefRedefSameNameDiffTypeAttribute {
    override val prop1: PropTypeB get() = prop1Reference.resolved ?: error("prop1 not resolved")
    override val prop1Reference = ManagedReference<Any, PropTypeB>(null)

    override val prop2: PropTypeB? get() = prop2Reference.resolved
    override val prop2Reference = ManagedReference<Any, PropTypeB>(null)
}
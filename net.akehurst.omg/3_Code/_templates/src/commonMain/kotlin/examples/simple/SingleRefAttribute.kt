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
package net.akehurst.omg.templates.examples.simple

import net.akehurst.kotlinx.utils.ManagedReference
import net.akehurst.kotlinx.utils.Reference
import net.akehurst.omg.templates.examples.common.Element
import net.akehurst.omg.templates.examples.common.PropType
import net.akehurst.omg.templates.examples.examples_ModelFactory

interface SingleRefAttribute: Element {
    /**
     * prop1: PropType [1] {reference}
     */
    val prop1: PropType
    val prop1Reference: Reference<Any, PropType>

    /**
     * prop2: PropType [0..1] {reference}
     */
    val prop2: PropType?
    val prop2Reference: Reference<Any, PropType>
}

data class SingleRefAttributeRam(val _factory: simple_PackageFactory, override val _identity: Any) : SingleRefAttribute {

    override val prop1: PropType get() = prop1Reference.resolved ?: error("prop1 not resolved")
    override val prop1Reference = ManagedReference<Any, PropType>(null, "SingleRefAttributeRam.prop1", PropType::class)

    override val prop2: PropType? get() = prop2Reference.resolved
    override val prop2Reference = ManagedReference<Any, PropType>(null, "SingleRefAttributeRam.prop2", PropType::class)

    override fun toString(): String = "SingleRefAttribute '${_factory.qualifiedIdentity.joinToString(".")}.$_identity' "
}
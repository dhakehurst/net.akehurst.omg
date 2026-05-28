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

package net.akehurst.omg.templates.examples.common

import net.akehurst.kotlinx.utils.HierarchicalFactory
import net.akehurst.kotlinx.utils.HierarchicalReferenceStore
import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreByHashMap
import net.akehurst.omg.templates.examples.examples_ModelFactory

interface common_PackageFactory : HierarchicalFactory, HierarchicalReferenceStore<Any> {

    fun Example_construct(_identity: Any): Example
    fun PropType_construct(_identity: Any): PropType
    fun PropTypeB_construct(_identity: Any): PropTypeB

}

class common_PackageFactoryRam(
    override val parentFactory: examples_ModelFactory,
    override val identity: Any,
) : common_PackageFactory, HierarchicalReferenceStore<Any> by HierarchicalReferenceStoreByHashMap(parentFactory, identity) {

    override fun Example_construct(_identity: Any): Example = ExampleRam(rootFactory.common, _identity).also { this[Example::class, _identity] = it }
    override fun PropType_construct(_identity: Any): PropType = PropTypeRam(rootFactory.common, _identity).also { this[PropType::class, _identity] = it }
    override fun PropTypeB_construct(_identity: Any): PropTypeB = PropTypeBRam(rootFactory.common, _identity).also { this[PropTypeB::class, _identity] = it }

    // --- HierarchicalFactory ---
    override val qualifiedIdentity: List<Any> = parentFactory.qualifiedIdentity + identity
    override val rootFactory get() = rootReferenceStore as examples_ModelFactory

    // --- Any ---
    override fun toString(): String = "common_PackageFactoryRam '${identity}'"
}
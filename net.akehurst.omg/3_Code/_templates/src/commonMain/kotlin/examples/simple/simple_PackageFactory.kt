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

import net.akehurst.kotlinx.utils.HierarchicalFactory
import net.akehurst.kotlinx.utils.HierarchicalReferenceStore
import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreByHashMap
import net.akehurst.omg.templates.examples.examples_ModelFactory
import kotlin.collections.plus

interface simple_PackageFactory : HierarchicalFactory, HierarchicalReferenceStore<Any> {

    fun SingleCmpAttribute_construct(_identity: Any): SingleCmpAttribute
    fun SingleRefAttribute_construct(_identity: Any): SingleRefAttribute
    fun CollectionCmpAttribute_construct(_identity: Any): CollectionCmpAttribute
    fun IsIDAttributeRam_construct(id: String): IsIDAttribute

}

class simple_PackageFactoryRam(
    override val parentFactory: examples_ModelFactory,
    override val identity: Any,
) : simple_PackageFactory, HierarchicalReferenceStore<Any> by HierarchicalReferenceStoreByHashMap(parentFactory, identity) {


    override fun SingleCmpAttribute_construct(_identity: Any): SingleCmpAttribute = SingleCmpAttributeRam(this, _identity).also { this[SingleCmpAttribute::class, _identity] = it }
    override fun SingleRefAttribute_construct(_identity: Any): SingleRefAttribute = SingleRefAttributeRam(this, _identity).also { this[SingleRefAttribute::class, _identity] = it }
    override fun CollectionCmpAttribute_construct(_identity: Any): CollectionCmpAttribute = CollectionCmpAttributeRam(this, _identity).also { this[CollectionCmpAttribute::class, _identity] = it }
    override fun IsIDAttributeRam_construct(id: String): IsIDAttribute = IsIDAttributeRam(this, id).also { this[IsIDAttribute::class, identity] = it }

    // --- HierarchicalFactory ---
    override val qualifiedIdentity: List<Any> = parentFactory.qualifiedIdentity + identity
    override val rootFactory get() = rootReferenceStore as examples_ModelFactory

    // --- Any ---
    override fun toString(): String = "simple_PackageFactoryRam '${identity}'"
}
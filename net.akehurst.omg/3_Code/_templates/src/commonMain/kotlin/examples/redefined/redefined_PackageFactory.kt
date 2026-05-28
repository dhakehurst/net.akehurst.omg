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

import net.akehurst.kotlinx.utils.HierarchicalFactory
import net.akehurst.kotlinx.utils.HierarchicalReferenceStore
import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreByHashMap
import net.akehurst.omg.templates.examples.examples_ModelFactory

interface redefined_PackageFactory : HierarchicalReferenceStore<Any>, HierarchicalFactory {
    fun SingleCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleCmpRedefSameNameDiffTypeAttribute
    fun SingleCmpRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleCmpRedefDiffNameSameTypeAttribute
    fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): CollectionCmpRedefSameNameDiffTypeAttribute
    fun SingleRefRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleRefRedefSameNameDiffTypeAttribute
    fun SingleRefRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleRefRedefDiffNameSameTypeAttribute
}

data class redefined_PackageFactoryRam(
    override val parentFactory: examples_ModelFactory,
    override val identity: Any,
) : redefined_PackageFactory, HierarchicalReferenceStore<Any> by HierarchicalReferenceStoreByHashMap(parentFactory, identity) {

    override fun SingleCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleCmpRedefSameNameDiffTypeAttribute =
        SingleCmpRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[SingleCmpRedefSameNameDiffTypeAttribute::class, _identity] = it }

    override fun SingleCmpRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleCmpRedefDiffNameSameTypeAttribute =
        SingleCmpRedefDiffNameSameTypeAttributeRam(this, _identity).also { this[SingleCmpRedefDiffNameSameTypeAttribute::class, _identity] = it }

    override fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): CollectionCmpRedefSameNameDiffTypeAttribute =
        CollectionCmpRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[CollectionCmpRedefSameNameDiffTypeAttribute::class, _identity] = it }

    override fun SingleRefRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleRefRedefSameNameDiffTypeAttribute =
        SingleRefRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[SingleRefRedefSameNameDiffTypeAttribute::class, _identity] = it }

    override fun SingleRefRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleRefRedefDiffNameSameTypeAttribute =
        SingleRefRedefDiffNameSameTypeAttributeRam(this, _identity).also { this[SingleRefRedefDiffNameSameTypeAttribute::class, _identity] = it }

    // --- HierarchicalFactory ---
    override val qualifiedIdentity: List<Any> = parentFactory.qualifiedIdentity + identity
    override val rootFactory get() = rootReferenceStore as examples_ModelFactory

    // --- Any ---
    override fun toString(): String = "redefined_PackageFactoryRam '${identity}'"
}
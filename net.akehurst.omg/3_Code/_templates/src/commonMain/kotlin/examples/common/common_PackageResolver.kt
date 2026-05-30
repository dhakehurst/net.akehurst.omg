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

import net.akehurst.kotlinx.utils.HierarchicalResolver
import net.akehurst.omg.templates.examples.examples_ModelResolver
import net.akehurst.omg.templates.examples.simple.*


class common_PackageResolver(
    override val parentResolver: examples_ModelResolver,
    override val identity: Any,
    val store: common_PackageFactory
) : HierarchicalResolver {

    override val rootResolver = parentResolver.rootResolver
    override val qualifiedIdentity: List<Any> get() = parentResolver.qualifiedIdentity + identity

    fun Example_resolve(obj: Example) {
        obj.contentList.forEach { resolveElement(it) }
    }

    fun resolveElement(obj: Element) = when (obj) {
        is SingleCmpAttribute -> rootResolver.simple.SingleCmpAttribute_resolve(obj)
        is SingleRefAttribute -> rootResolver.simple.SingleRefAttribute_resolve(obj)
        is PropType -> PropType_resolve(obj)
        is CollectionCmpAttribute -> rootResolver.simple.CollectionCmpAttribute_resolve(obj)
        is CollectionRefAttribute -> rootResolver.simple.CollectionRefAttribute_resolve(obj)
        is DerivedUnionCmpAttribute -> rootResolver.simple.DerivedUnionCmpAttribute_resolve(obj)
        is DerivedUnionRefAttribute -> rootResolver.simple.DerivedUnionRefAttribute_resolve(obj)
        is SubsetAttribute -> rootResolver.simple.SubsetAttribute_resolve(obj)
        is IsIDAttribute -> rootResolver.simple.IsIDAttribute_resolve(obj)
        is Example -> Example_resolve(obj)
        else -> error("Subtype '${obj::class.simpleName}' of Element not handled.")
    }

    fun PropType_resolve(obj: PropType) = when(obj) {
        is PropTypeB -> PropTypeB_resolve(obj)
        else -> { // resolve PropType
            // nothing to resolve
        }
    }

    fun PropTypeB_resolve(obj: PropTypeB)  {
    }

    // --- Any ---
    override fun toString(): String = "common_PackageResolverRam '${identity}'"
}
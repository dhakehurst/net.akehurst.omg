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

import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreExt.resolve
import net.akehurst.kotlinx.utils.HierarchicalResolver
import net.akehurst.omg.templates.examples.examples_ModelResolver
import net.akehurst.omg.templates.examples.redefined.*

class simple_PackageResolver(
    override val parentResolver: examples_ModelResolver,
    override val identity: Any,
    val store: simple_PackageFactory
) : HierarchicalResolver {

    override val rootResolver get() = parentResolver.rootResolver
    override val qualifiedIdentity: List<Any> get() = parentResolver.qualifiedIdentity + identity

    fun SingleCmpAttribute_resolve(obj: SingleCmpAttribute) = when (obj) {
        is SingleCmpRedefDiffNameSameTypeAttribute -> rootResolver.redefined.SingleCmpRedefDiffNameSameTypeAttribute_resolve(obj)
        is SingleCmpRedefSameNameDiffTypeAttribute -> rootResolver.redefined.SingleCmpRedefSameNameDiffTypeAttribute_resolve(obj)
        else -> { // is SingleCmpAttribute
            rootResolver.common.PropType_resolve(obj.prop1)
            obj.prop2?.let { rootResolver.common.PropType_resolve(it) }
        }
    }

    fun SingleRefAttribute_resolve(obj: SingleRefAttribute) = when(obj) {
        is SingleRefRedefDiffNameSameTypeAttribute -> rootResolver.redefined.SingleRefRedefDiffNameSameTypeAttribute_resolve(obj)
        is SingleRefRedefSameNameDiffTypeAttribute -> rootResolver.redefined.SingleRefRedefSameNameDiffTypeAttribute_resolve(obj)
        else -> { // is SingleRefAttribute
            store.rootReferenceStore.resolve(obj.prop1Reference)
            store.rootReferenceStore.resolve(obj.prop2Reference)
        }
    }

    fun CollectionCmpAttribute_resolve(obj: CollectionCmpAttribute) = when (obj) {
        is CollectionCmpRedefSameNameDiffTypeAttribute -> rootResolver.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_resolve(obj)
        else -> { // is CollectionCmpAttribute
            obj.prop1OrderedSet.forEach { rootResolver.common.PropType_resolve(it) }
            obj.prop2List.forEach { rootResolver.common.PropType_resolve(it) }
            obj.prop3Set.forEach { rootResolver.common.PropType_resolve(it) }
            obj.prop4Collection.forEach { rootResolver.common.PropType_resolve(it) }
        }
    }

    fun CollectionRefAttribute_resolve(obj: CollectionRefAttribute) {
        obj.prop1OrderedSet.forEach { rootResolver.common.PropType_resolve(it) }
        obj.prop2List.forEach { rootResolver.common.PropType_resolve(it) }
        obj.prop3Set.forEach { rootResolver.common.PropType_resolve(it) }
        obj.prop4Collection.forEach { rootResolver.common.PropType_resolve(it) }
    }

    fun DerivedUnionCmpAttribute_resolve(obj: DerivedUnionCmpAttribute) {
        obj.children.forEach { rootResolver.common.PropType_resolve(it) }
        // obj.derived is derived, no need to resolve
    }

    fun DerivedUnionRefAttribute_resolve(obj: DerivedUnionRefAttribute) {
        obj.refsReference.forEach { store.resolve(it) }
        // obj.derivedRef is derived, no need to resolve
    }

    fun SubsetAttribute_resolve(obj: SubsetAttribute) {
        obj.qList.forEach { rootResolver.common.PropType_resolve(it) }
        // obj.pList is a subset, resolved by superset
    }

    fun IsIDAttribute_resolve(obj: IsIDAttribute) {

    }

    // --- Any ---
    override fun toString(): String = "simple_PackageResolver '${identity}'"
}
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

import net.akehurst.kotlinx.utils.ReferenceStore
import net.akehurst.kotlinx.utils.resolve

class Examples_ModelResolver(val store: ReferenceStore<Any>) {

    fun Examples_resolve(obj: Examples) {
        obj.contentList.forEach { resolveElement(it) }
    }

    fun resolveElement(obj: Element) = when (obj) {
        is PropTypeB -> PropTypeB_resolve(obj)
        is PropType -> PropType_resolve(obj)
        is SingleCmpAttribute -> SingleCmpAttribute_resolve(obj)
        is SingleRefRedefSameNameDiffTypeAttribute -> SingleRefRedefSameNameDiffTypeAttribute_resolve(obj)
        is SingleRefAttribute -> SingleRefAttribute_resolve(obj)
        is CollectionCmpAttribute -> CollectionCmpAttribute_resolve(obj)
 //       is CollectionRefAttribute -> CollectionRefAttribute_resolve(obj)
        else -> error("Subtype '${obj::class.simpleName}' of Element not handled.")
    }

    fun PropType_resolve(obj: PropType) {
    }

    fun PropTypeB_resolve(obj: PropTypeB)  {
    }

    fun SingleCmpAttribute_resolve(obj: SingleCmpAttribute) {
        PropType_resolve(obj.prop1)
        obj.prop2?.let { PropType_resolve(it) }
    }

    fun SingleRefAttribute_resolve(obj: SingleRefAttribute) {
        store.resolve(obj.prop1Reference)
        store.resolve(obj.prop2Reference)
    }

    fun CollectionCmpAttribute_resolve(obj: CollectionCmpAttribute) {
        obj.prop1OrderedSet.forEach { PropType_resolve(it) }
        obj.prop2List.forEach { PropType_resolve(it) }
        obj.prop3Set.forEach { PropType_resolve(it) }
        obj.prop4Collection.forEach { PropType_resolve(it) }
    }

    fun SingleRefRedefSameNameDiffTypeAttribute_resolve(obj: SingleRefRedefSameNameDiffTypeAttribute) {
        store.resolve(obj.prop1Reference)
        store.resolve(obj.prop2Reference)
    }
}
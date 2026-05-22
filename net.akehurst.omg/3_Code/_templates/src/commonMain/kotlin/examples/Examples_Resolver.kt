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

class Examples_Resolver(val store: ReferenceStore<Any>) {

    fun resolveExamples(obj: Examples) {
        obj.contentList.forEach { resolveElement(it) }
    }

    fun resolveElement(obj: Element) = when (obj) {
        is SingleCmpAttribute -> resolveSingleCompositeAttribute(obj)
        is SingleRefAttribute -> resolveSingleReferenceAttribute(obj)
        is CollectionCmpAttribute -> resolveCollectionCompositeAttribute(obj)
        else -> error("Subtype of Element not handled.")
    }

    fun resolvePropType(obj: PropType) {
    }

    fun resolvePropTypeB(obj: PropTypeB)  {
    }

    fun resolveSingleCompositeAttribute(obj: SingleCmpAttribute) {
        resolvePropType(obj.prop1)
        obj.prop2?.let { resolvePropType(it) }
    }

    fun resolveSingleReferenceAttribute(obj: SingleRefAttribute) {
        store.resolve(obj.prop1Reference)
        store.resolve(obj.prop2Reference)
    }

    fun resolveCollectionCompositeAttribute(obj: CollectionCmpAttribute) {
        obj.prop1OrderedSet.forEach { resolvePropType(it) }
        obj.prop2List.forEach { resolvePropType(it) }
        obj.prop3Set.forEach { resolvePropType(it) }
        obj.prop4Collection.forEach { resolvePropType(it) }
    }
}
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

import net.akehurst.kotlinx.utils.HierarchicalResolver
import net.akehurst.kotlinx.utils.resolve
import net.akehurst.omg.templates.examples.common.Element
import net.akehurst.omg.templates.examples.common.Example
import net.akehurst.omg.templates.examples.common.PropType
import net.akehurst.omg.templates.examples.common.PropTypeB
import net.akehurst.omg.templates.examples.common.common_PackageFactory
import net.akehurst.omg.templates.examples.examples_ModelResolver
import net.akehurst.omg.templates.examples.simple.*


class redefined_PackageResolver(
    override val parentResolver: examples_ModelResolver,
    override val identity: Any,
    val store: redefined_PackageFactory
) : HierarchicalResolver {

    override val rootResolver = parentResolver.rootResolver
    override val qualifiedIdentity: List<Any> get() = parentResolver.qualifiedIdentity + identity

    fun CollectionCmpRedefSameNameDiffTypeAttribute_resolve(obj:CollectionCmpRedefSameNameDiffTypeAttribute) {

    }

    fun CollectionRefRedefSameNameDiffTypeAttribute_resolve(obj:CollectionRefRedefSameNameDiffTypeAttribute) {

    }

    fun SingleCmpRedefDiffNameSameTypeAttribute_resolve(obj:SingleCmpRedefDiffNameSameTypeAttribute) {

    }

    fun SingleCmpRedefSameNameDiffTypeAttribute_resolve(obj:SingleCmpRedefSameNameDiffTypeAttribute) {

    }

    fun SingleRefRedefSameNameDiffTypeAttribute_resolve(obj: SingleRefRedefSameNameDiffTypeAttribute) {
        store.resolve(obj.prop1Reference)
        store.resolve(obj.prop2Reference)
    }

    fun SingleRefRedefDiffNameSameTypeAttribute_resolve(obj: SingleRefRedefDiffNameSameTypeAttribute) {
        store.resolve(obj.prop1Reference)
        store.resolve(obj.prop2Reference)
    }

    // --- Any ---
    override fun toString(): String = "redefined_PackageResolverRam '${identity}'"
}
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

import net.akehurst.kotlinx.utils.HierarchicalResolver
import net.akehurst.omg.templates.examples.common.common_PackageResolver
import net.akehurst.omg.templates.examples.redefined.redefined_PackageResolver
import net.akehurst.omg.templates.examples.simple.simple_PackageResolver

class examples_ModelResolver(
    override val identity: Any,
    val store: examples_ModelFactory
) : HierarchicalResolver {

    override val parentResolver: HierarchicalResolver? = null
    override val rootResolver: examples_ModelResolver = this
    override val qualifiedIdentity: List<Any> get() = listOf(identity)

    val common = common_PackageResolver(this, "common", store.common)
    val simple = simple_PackageResolver(this, "simple", store.simple)
    val redefined = redefined_PackageResolver(this, "redefined", store.redefined)

}
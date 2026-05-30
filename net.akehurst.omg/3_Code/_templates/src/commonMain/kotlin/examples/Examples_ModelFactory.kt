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

import net.akehurst.kotlinx.utils.Factory
import net.akehurst.kotlinx.utils.HierarchicalFactory
import net.akehurst.kotlinx.utils.HierarchicalReferenceStore
import net.akehurst.kotlinx.utils.HierarchicalReferenceStoreByHashMap
import net.akehurst.omg.templates.examples.common.common_PackageFactory
import net.akehurst.omg.templates.examples.common.common_PackageFactoryRam
import net.akehurst.omg.templates.examples.redefined.redefined_PackageFactory
import net.akehurst.omg.templates.examples.redefined.redefined_PackageFactoryRam
import net.akehurst.omg.templates.examples.simple.simple_PackageFactory
import net.akehurst.omg.templates.examples.simple.simple_PackageFactoryRam

interface examples_ModelFactory : HierarchicalFactory, HierarchicalReferenceStore<Any> {

    val common: common_PackageFactory
    val simple: simple_PackageFactory
    val redefined: redefined_PackageFactory

}

data class examples_ModelFactoryRam(
    override val identity: Any,
) : examples_ModelFactory, HierarchicalReferenceStore<Any> by HierarchicalReferenceStoreByHashMap(null, identity) {

    // --- HierarchicalFactory ---
    override val parentFactory: HierarchicalFactory? = null
    override val rootFactory: HierarchicalFactory = this
    override val qualifiedIdentity: List<Any> = listOf(identity)

    // --- examples_ModelFactory ---
    override val common = common_PackageFactoryRam(this, "common")
    override val simple = simple_PackageFactoryRam(this, "simple")
    override val redefined = redefined_PackageFactoryRam(this, "redefined")

    // --- Any ---
    override fun toString(): String = "examples_ModelFactoryRam '${identity}'"
}
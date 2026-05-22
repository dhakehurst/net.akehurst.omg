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

import net.akehurst.kotlinx.collections.MutableMapNotNull
import net.akehurst.kotlinx.collections.lazyMutableMapNotNull
import net.akehurst.kotlinx.utils.ReferenceStore
import net.akehurst.kotlinx.utils.UniqueIdentityGenerator
import kotlin.reflect.KClass

interface Examples_Factory : ReferenceStore<Any> {
    fun createExamples(_identity: Any = UniqueIdentityGenerator.generate("Examples")): Examples

    fun createPropType(_identity: Any = UniqueIdentityGenerator.generate("PropType")): PropType
    fun createPropTypeB(_identity: Any = UniqueIdentityGenerator.generate("PropTypeB")): PropTypeB

    fun createSingleCompositeAttribute(_identity: Any = UniqueIdentityGenerator.generate("SingleCompositeAttribute")): SingleCmpAttribute
    fun createSingleReferenceAttribute(_identity: Any = UniqueIdentityGenerator.generate("SingleReferenceAttribute")): SingleRefAttribute
    fun createCollectionCompositeAttribute(_identity: Any = UniqueIdentityGenerator.generate("CollectionCompositeAttribute")): CollectionCmpAttribute
}

object ExamplesFactoryRam : Examples_Factory {
    val references: MutableMapNotNull<KClass<*>, MutableMap<Any, Any?>> by lazyMutableMapNotNull { mutableMapOf() }
    override fun <T : Any> get(clazz: KClass<T>, reference: Any): T? = references[clazz][reference] as? T
    override fun <T : Any> set(clazz: KClass<T>, reference: Any, value: T?) {
        references[clazz][reference] = value
    }

    override fun createExamples(_identity: Any): Examples = ExamplesRam(this, _identity).also { this[Examples::class, _identity] = it }
    override fun createPropType(_identity: Any): PropType = PropTypeRam(this, _identity).also { this[PropType::class, _identity] = it }
    override fun createPropTypeB(_identity: Any): PropTypeB = PropTypeBRam(this, _identity).also { this[PropTypeB::class, _identity] = it }

    override fun createSingleCompositeAttribute(_identity: Any): SingleCmpAttribute = SingleCmpAttributeRam(this, _identity).also { this[SingleCmpAttribute::class, _identity] = it }
    override fun createSingleReferenceAttribute(_identity: Any): SingleRefAttribute = SingleRefAttributeRam(this, _identity).also { this[SingleRefAttribute::class, _identity] = it }
    override fun createCollectionCompositeAttribute(_identity: Any): CollectionCmpAttribute =
        CollectionCmpAttributeRam(this, _identity).also { this[CollectionCmpAttribute::class, _identity] = it }

}
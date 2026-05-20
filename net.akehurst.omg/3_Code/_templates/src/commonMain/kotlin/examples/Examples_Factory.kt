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
    fun createExamples(identifier_: Any = UniqueIdentityGenerator.generate("Examples")): Examples

    fun createPropType(identifier_: Any = UniqueIdentityGenerator.generate("PropType")): PropType
    fun createPropTypeB(identifier_: Any = UniqueIdentityGenerator.generate("PropTypeB")): PropTypeB

    fun createSingleCompositeAttribute(identifier_: Any = UniqueIdentityGenerator.generate("SingleCompositeAttribute")): SingleCompositeAttribute
    fun createSingleReferenceAttribute(identifier_: Any = UniqueIdentityGenerator.generate("SingleReferenceAttribute")): SingleReferenceAttribute
    fun createCollectionCompositeAttribute(identifier_: Any = UniqueIdentityGenerator.generate("CollectionCompositeAttribute")): CollectionCompositeAttribute
}

object ExamplesFactoryRam : Examples_Factory {
    val references: MutableMapNotNull<KClass<*>, MutableMap<Any, Any?>> by lazyMutableMapNotNull { mutableMapOf() }
    override fun <TO : Any> get(clazz: KClass<TO>, reference: Any): TO? = references[clazz][reference] as? TO
    override fun <TO : Any> set(clazz: KClass<TO>, reference: Any, value: TO?) {
        references[clazz][reference] = value
    }

    override fun createExamples(identifier_: Any): Examples = ExamplesRam(identifier_).also { this[Examples::class, identifier_] = it }
    override fun createPropType(identifier_: Any): PropType = PropTypeRam(identifier_).also { this[PropType::class, identifier_] = it }
    override fun createPropTypeB(identifier_: Any): PropTypeB = PropTypeBRam(identifier_).also { this[PropTypeB::class, identifier_] = it }

    override fun createSingleCompositeAttribute(identifier_: Any): SingleCompositeAttribute = SingleCompositeAttributeRam(identifier_).also { this[SingleCompositeAttribute::class, identifier_] = it }
    override fun createSingleReferenceAttribute(identifier_: Any): SingleReferenceAttribute = SingleReferenceAttributeRam(identifier_).also { this[SingleReferenceAttribute::class, identifier_] = it }
    override fun createCollectionCompositeAttribute(identifier_: Any): CollectionCompositeAttribute =
        CollectionCompositeAttributeRam(identifier_).also { this[CollectionCompositeAttribute::class, identifier_] = it }

}
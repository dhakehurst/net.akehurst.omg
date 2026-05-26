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
    val _identity: Any

//    fun Examples_construct(): Examples
    fun Examples_construct(_identity: Any): Examples

//    fun PropType_construct(): PropType
    fun PropType_construct(_identity: Any): PropType
//    fun PropTypeB_construct(): PropTypeB
    fun PropTypeB_construct(_identity: Any): PropTypeB

    fun IsIDAttributeRam_construct(id: String): IsIDAttribute
//    fun SingleCmpAttribute_construct(): SingleCmpAttribute
    fun SingleCmpAttribute_construct(_identity: Any): SingleCmpAttribute
//    fun SingleRefAttribute_construct(): SingleRefAttribute
    fun SingleRefAttribute_construct(_identity: Any): SingleRefAttribute
//    fun CollectionCmpAttribute_construct(): CollectionCmpAttribute
    fun CollectionCmpAttribute_construct(_identity: Any): CollectionCmpAttribute
//    fun SingleCmpRedefSameNameDiffTypeAttribute_construct(): SingleCmpRedefSameNameDiffTypeAttribute
    fun SingleCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleCmpRedefSameNameDiffTypeAttribute
//    fun SingleCmpRedefDiffNameSameTypeAttribute_construct(): SingleCmpRedefDiffNameSameTypeAttribute
    fun SingleCmpRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleCmpRedefDiffNameSameTypeAttribute
//    fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(): CollectionCmpRedefSameNameDiffTypeAttribute
    fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): CollectionCmpRedefSameNameDiffTypeAttribute
//    fun SingleRefRedefSameNameDiffTypeAttribute_construct(): SingleRefRedefSameNameDiffTypeAttribute
    fun SingleRefRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleRefRedefSameNameDiffTypeAttribute
//    fun SingleRefRedefDiffNameSameTypeAttribute_construct(): SingleRefRedefDiffNameSameTypeAttribute
    fun SingleRefRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleRefRedefDiffNameSameTypeAttribute
}

class ExamplesFactoryRam(
    _identity: Any? = null,
) : Examples_Factory {

    private val _idGen = UniqueIdentityGenerator()
    override val _identity = _identity ?: _idGen.generate("ExamplesFactoryRam")

    val references: MutableMapNotNull<KClass<*>, MutableMap<Any, Any?>> by lazyMutableMapNotNull { mutableMapOf() }
    override fun <T : Any> get(clazz: KClass<T>, reference: Any): T? = references[clazz][reference] as? T
    override fun <T : Any> set(clazz: KClass<T>, reference: Any, value: T?) {
        references[clazz][reference] = value
    }

//    override fun Examples_construct(): Examples = Examples_construct(_idGen.generate("Examples"))
    override fun Examples_construct(_identity: Any): Examples = ExamplesRam(this, _identity).also { this[Examples::class, _identity] = it }



//    override fun PropType_construct(): PropType = PropType_construct(_idGen.generate("PropType"))
    override fun PropType_construct(_identity: Any): PropType = PropTypeRam(this, _identity).also { this[PropType::class, _identity] = it }

//    override fun PropTypeB_construct(): PropTypeB = PropTypeB_construct(_idGen.generate("PropTypeB"))
    override fun PropTypeB_construct(_identity: Any): PropTypeB = PropTypeBRam(this, _identity).also { this[PropTypeB::class, _identity] = it }

    override fun IsIDAttributeRam_construct(id: String): IsIDAttribute = IsIDAttributeRam(this, id).also { this[IsIDAttribute::class, _identity] = it }

//    override fun SingleCmpAttribute_construct(): SingleCmpAttribute = SingleCmpAttribute_construct(_idGen.generate("SingleCmpAttribute"))
    override fun SingleCmpAttribute_construct(_identity: Any): SingleCmpAttribute = SingleCmpAttributeRam(this, _identity).also { this[SingleCmpAttribute::class, _identity] = it }

//    override fun SingleRefAttribute_construct(): SingleRefAttribute = SingleRefAttribute_construct(_idGen.generate("SingleRefAttribute"))
    override fun SingleRefAttribute_construct(_identity: Any): SingleRefAttribute = SingleRefAttributeRam(this, _identity).also { this[SingleRefAttribute::class, _identity] = it }

//    override fun CollectionCmpAttribute_construct(): CollectionCmpAttribute = CollectionCmpAttribute_construct(_idGen.generate("CollectionCmpAttribute"))
    override fun CollectionCmpAttribute_construct(_identity: Any): CollectionCmpAttribute = CollectionCmpAttributeRam(this, _identity).also { this[CollectionCmpAttribute::class, _identity] = it }

//    override fun SingleCmpRedefSameNameDiffTypeAttribute_construct(): SingleCmpRedefSameNameDiffTypeAttribute = SingleCmpRedefSameNameDiffTypeAttribute_construct(_idGen.generate("SingleCmpRedefSameNameDiffTypeAttribute"))
    override fun SingleCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleCmpRedefSameNameDiffTypeAttribute = SingleCmpRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[SingleCmpRedefSameNameDiffTypeAttribute::class, _identity] = it }

//    override fun SingleCmpRedefDiffNameSameTypeAttribute_construct(): SingleCmpRedefDiffNameSameTypeAttribute = SingleCmpRedefDiffNameSameTypeAttribute_construct(_idGen.generate("SingleCmpRedefDiffNameSameTypeAttribute"))
    override fun SingleCmpRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleCmpRedefDiffNameSameTypeAttribute = SingleCmpRedefDiffNameSameTypeAttributeRam(this, _identity).also { this[SingleCmpRedefDiffNameSameTypeAttribute::class, _identity] = it }

//    override fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(): CollectionCmpRedefSameNameDiffTypeAttribute = CollectionCmpRedefSameNameDiffTypeAttribute_construct(_idGen.generate("CollectionCmpRedefSameNameDiffTypeAttribute"))
    override fun CollectionCmpRedefSameNameDiffTypeAttribute_construct(_identity: Any): CollectionCmpRedefSameNameDiffTypeAttribute = CollectionCmpRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[CollectionCmpRedefSameNameDiffTypeAttribute::class, _identity] = it }

//    override fun SingleRefRedefSameNameDiffTypeAttribute_construct(): SingleRefRedefSameNameDiffTypeAttribute = SingleRefRedefSameNameDiffTypeAttribute_construct(_idGen.generate("SingleRefRedefSameNameDiffTypeAttribute"))
    override fun SingleRefRedefSameNameDiffTypeAttribute_construct(_identity: Any): SingleRefRedefSameNameDiffTypeAttribute = SingleRefRedefSameNameDiffTypeAttributeRam(this, _identity).also { this[SingleRefRedefSameNameDiffTypeAttribute::class, _identity] = it }

//    override fun SingleRefRedefDiffNameSameTypeAttribute_construct(): SingleRefRedefDiffNameSameTypeAttribute = SingleRefRedefDiffNameSameTypeAttribute_construct(_idGen.generate("SingleRefRedefDiffNameSameTypeAttribute"))
    override fun SingleRefRedefDiffNameSameTypeAttribute_construct(_identity: Any): SingleRefRedefDiffNameSameTypeAttribute = SingleRefRedefDiffNameSameTypeAttributeRam(this, _identity).also { this[SingleRefRedefDiffNameSameTypeAttribute::class, _identity] = it }

    override fun toString(): String = "ExamplesFactoryRam '${_identity}'"
}
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

import net.akehurst.kotlinx.collections.mutableList
import net.akehurst.kotlinx.collections.mutableOrderedSet
import net.akehurst.kotlinx.utils.mutableReference

@DslMarker
annotation class ExamplesDslMarker

fun Examples(factory: Examples_Factory, id: Any, init: Examples_Builder.() -> Unit): Examples {
    val b = Examples_Builder(factory, id)
    b.init()
    return b.build().also {
        Examples_Resolver(factory).resolveExamples(it)
    }
}

@ExamplesDslMarker
class Examples_Builder(
    val factory: Examples_Factory,
    val id: Any
) {
    private var _content: Collection<Element>? = null

    fun content(init: Element_ContentBuilder.() -> Unit): Collection<Any> {
        val b = Element_ContentBuilder(factory)
        b.init()
        val obj = b.build()
        _content = obj
        return obj
    }

    fun build(): Examples = factory.createExamples(id).also { self ->
        _content?.let { self.content.addAll(it) }
    }
}

@ExamplesDslMarker
class Element_ContentBuilder(
    val factory: Examples_Factory
) {
    private val _content = mutableListOf<Element>()

    fun PropType(id: Any): PropType = factory.createPropType(id)

    fun SingleCompositeAttribute(id: Any, init: SingleCompositeAttribute_Builder.() -> Unit = {}): SingleCompositeAttribute {
        val b = SingleCompositeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleReferenceAttribute(id: Any, init: SingleReferenceAttribute_Builder.() -> Unit = {}): SingleReferenceAttribute {
        val b = SingleReferenceAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun CollectionCompositeAttribute(id: Any, init: CollectionCompositeAttribute_Builder.() -> Unit = {}): CollectionCompositeAttribute {
        val b = CollectionCompositeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun build(): Collection<Element> = _content
}

@ExamplesDslMarker
class PropType_ContentBuilder(
    val factory: Examples_Factory
) {
    private val _content = mutableListOf<PropType>()

    fun PropType(id: Any): PropType = factory.createPropType(id).also { _content.add(it) }

    fun build(): Collection<PropType> = _content
}

@ExamplesDslMarker
class PropType_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    fun build(): PropType = factory.createPropType(_id)
}

@ExamplesDslMarker
class SingleCompositeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: PropType? = null
    fun prop1(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType {
        val b = PropType_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _prop1 = obj
        return obj
    }

    private var _prop2: PropType? = null
    fun prop2(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType {
        val b = PropType_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _prop2 = obj
        return obj
    }

    fun build(): SingleCompositeAttribute = factory.createSingleCompositeAttribute(_id).also { self ->
        _prop1?.let { self.prop1_set(it) }
        _prop2?.let { self.prop2_set(it) }
    }
}

@ExamplesDslMarker
class SingleReferenceAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: Any? = null
    fun prop1(reference_: Any) {
        _prop1 = reference_
    }

    private var _prop2: Any? = null
    fun prop2(reference_: Any) {
        _prop2 = reference_
    }

    fun build(): SingleReferenceAttribute = factory.createSingleReferenceAttribute(_id).also { self ->
        _prop1?.let { self.prop1Reference.mutableReference.reference = it }
        _prop2?.let { self.prop2Reference.mutableReference.reference = it }
    }
}

@ExamplesDslMarker
class CollectionCompositeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: Collection<PropType>? = null
    fun prop1(init: PropType_ContentBuilder.() -> Unit = {}) {
        val b = PropType_ContentBuilder(factory)
        b.init()
        val obj = b.build()
        _prop1 = obj
    }

    private var _prop2: Collection<PropType>? = null
    fun prop2(init: PropType_ContentBuilder.() -> Unit = {}) {
        val b = PropType_ContentBuilder(factory)
        b.init()
        val obj = b.build()
        _prop2 = obj
    }

    fun build(): CollectionCompositeAttribute = factory.createCollectionCompositeAttribute(_id).also { self ->
        _prop1?.let { self.prop1OrderedSet.mutableOrderedSet.addAll(it) }
        _prop2?.let { self.prop2.mutableList.addAll(it) }
    }
}
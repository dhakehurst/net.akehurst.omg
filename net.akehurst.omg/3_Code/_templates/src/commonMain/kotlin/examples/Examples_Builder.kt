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

import net.akehurst.kotlinx.collections.ListExt.mutable
import net.akehurst.kotlinx.collections.OrderedSetExt.mutable
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.kotlinx.utils.UniqueIdentityGenerator
import net.akehurst.kotlinx.utils.ValueExt.mutable

@DslMarker
annotation class ExamplesDslMarker

fun Examples(
    factory: Examples_Factory,
    id: Any = UniqueIdentityGenerator.generate("Examples"),
    init: Examples_Builder.() -> Unit
): Examples {
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

    fun content(init: Element_ContentBuilder.() -> Unit): Collection<Element> {
        val b = Element_ContentBuilder(factory)
        b.init()
        val obj = b.build()
        _content = obj
        return obj
    }

    fun build(): Examples = factory.createExamples(id).also { self ->
        _content?.let { self.contentList.mutable.addAll(it) }
    }
}

@ExamplesDslMarker
class Element_ContentBuilder(
    val factory: Examples_Factory
) {
    private val _content = mutableListOf<Element>()

    fun PropType(id: Any): PropType = factory.createPropType(id)
    fun PropTypeB(id: Any): PropTypeB = factory.createPropTypeB(id)

    fun SingleCompositeAttribute(id: Any, init: SingleCompositeAttribute_Builder.() -> Unit = {}): SingleCmpAttribute {
        val b = SingleCompositeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleReferenceAttribute(id: Any, init: SingleReferenceAttribute_Builder.() -> Unit = {}): SingleRefAttribute {
        val b = SingleReferenceAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun CollectionCompositeAttribute(id: Any, init: CollectionCompositeAttribute_Builder.() -> Unit = {}): CollectionCmpAttribute {
        val b = CollectionCompositeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleCmpRedefSameNameDiffTypeAttribute(id: Any, init: SingleCmpRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): SingleCmpRedefSameNameDiffTypeAttribute {
        val b = SingleCmpRedefSameNameDiffTypeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleCmpRedefDiffNameSameTypeAttribute(id: Any, init: SingleCmpRedefDiffNameSameTypeAttribute_Builder.() -> Unit = {}): SingleCmpRedefDiffNameSameTypeAttribute {
        val b = SingleCmpRedefDiffNameSameTypeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun CollectionCmpRedefSameNameDiffTypeAttribute(id: Any, init: CollectionCmpRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): CollectionCmpRedefSameNameDiffTypeAttribute {
        val b = CollectionCmpRedefSameNameDiffTypeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleRefRedefSameNameDiffTypeAttribute(id: Any, init: SingleRefRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): SingleRefRedefSameNameDiffTypeAttribute {
        val b = SingleRefRedefSameNameDiffTypeAttribute_Builder(factory, id)
        b.init()
        val obj = b.build()
        _content.add(obj)
        return obj
    }

    fun SingleRefRedefDiffNameSameTypeAttribute(id: Any, init: SingleRefRedefDiffNameSameTypeAttribute_Builder.() -> Unit = {}): SingleRefRedefDiffNameSameTypeAttribute {
        val b = SingleRefRedefDiffNameSameTypeAttribute_Builder(factory, id)
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
    fun PropTypeB(id: Any): PropTypeB = factory.createPropTypeB(id).also { _content.add(it) }

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
class PropTypeB_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    fun build(): PropTypeB = factory.createPropTypeB(_id)
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

    fun build(): SingleCmpAttribute = factory.createSingleCompositeAttribute(_id).also { self ->
        _prop1?.let { self.prop1Value.mutable.set(it) }
        _prop2?.let { self.prop2Value.mutable.set(it) }
    }
}

@ExamplesDslMarker
class SingleCmpRedefSameNameDiffTypeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: PropTypeB? = null
    fun prop1(identity_: Any, init: PropTypeB_Builder.() -> Unit = {}): PropTypeB {
        val b = PropTypeB_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _prop1 = obj
        return obj
    }

    private var _prop2: PropTypeB? = null
    fun prop2(identity_: Any, init: PropTypeB_Builder.() -> Unit = {}): PropTypeB {
        val b = PropTypeB_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _prop2 = obj
        return obj
    }

    fun build(): SingleCmpRedefSameNameDiffTypeAttribute = factory.createSingleCmpRedefSameNameDiffTypeAttribute(_id).also { self ->
        _prop1?.let { self.prop1Value.mutable.set(it) }
        _prop2?.let { self.prop2Value.mutable.set(it) }
    }
}

@ExamplesDslMarker
class SingleCmpRedefDiffNameSameTypeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _redefinesProp1: PropType? = null
    fun redefinesProp1(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType {
        val b = PropType_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _redefinesProp1 = obj
        return obj
    }

    private var _redefinesProp2: PropType? = null
    fun redefinesProp2(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType {
        val b = PropType_Builder(factory, identity_)
        b.init()
        val obj = b.build()
        _redefinesProp2 = obj
        return obj
    }

    fun build(): SingleCmpRedefDiffNameSameTypeAttribute = factory.createSingleCmpRedefDiffNameSameTypeAttribute(_id).also { self ->
        _redefinesProp1?.let { self.redefinesProp1Value.mutable.set(it) }
        _redefinesProp2?.let { self.redefinesProp2Value.mutable.set(it) }
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

    fun build(): SingleRefAttribute = factory.createSingleReferenceAttribute(_id).also { self ->
        _prop1?.let { self.prop1Reference.mutable.reference = it }
        _prop2?.let { self.prop2Reference.mutable.reference = it }
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

    fun build(): CollectionCmpAttribute = factory.createCollectionCompositeAttribute(_id).also { self ->
        _prop1?.let { self.prop1OrderedSet.mutable.addAll(it) }
        _prop2?.let { self.prop2List.mutable.addAll(it) }
    }
}

@ExamplesDslMarker
class CollectionCmpRedefSameNameDiffTypeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: Collection<PropTypeB>? = null
    fun prop1(init: PropType_ContentBuilder.() -> Unit = {}) {
        val b = PropType_ContentBuilder(factory)
        b.init()
        val obj = b.build().filterIsInstance<PropTypeB>()
        _prop1 = obj
    }

    private var _prop2: Collection<PropTypeB>? = null
    fun prop2(init: PropType_ContentBuilder.() -> Unit = {}) {
        val b = PropType_ContentBuilder(factory)
        b.init()
        val obj = b.build().filterIsInstance<PropTypeB>()
        _prop2 = obj
    }

    fun build(): CollectionCmpRedefSameNameDiffTypeAttribute = factory.createCollectionCmpRedefSameNameDiffTypeAttribute(_id).also { self ->
        _prop1?.let { self.prop1OrderedSet.mutable.addAll(it) }
        _prop2?.let { self.prop2List.mutable.addAll(it) }
    }
}

@ExamplesDslMarker
class SingleRefRedefSameNameDiffTypeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _prop1: Any? = null
    fun prop1(reference_: Any) { _prop1 = reference_ }

    private var _prop2: Any? = null
    fun prop2(reference_: Any) { _prop2 = reference_ }

    fun build(): SingleRefRedefSameNameDiffTypeAttribute = factory.createSingleRefRedefSameNameDiffTypeAttribute(_id).also { self ->
        _prop1?.let { self.prop1Reference.mutable.reference = it }
        _prop2?.let { self.prop2Reference.mutable.reference = it }
    }
}

@ExamplesDslMarker
class SingleRefRedefDiffNameSameTypeAttribute_Builder(
    val factory: Examples_Factory,
    private val _id: Any
) {
    private var _redefinesProp1: Any? = null
    fun redefinesProp1(reference_: Any) { _redefinesProp1 = reference_ }

    private var _redefinesProp2: Any? = null
    fun redefinesProp2(reference_: Any) { _redefinesProp2 = reference_ }

    fun build(): SingleRefRedefDiffNameSameTypeAttribute = factory.createSingleRefRedefDiffNameSameTypeAttribute(_id).also { self ->
        _redefinesProp1?.let { self.redefinesProp1Reference.mutable.reference = it }
        _redefinesProp2?.let { self.redefinesProp2Reference.mutable.reference = it }
    }
}

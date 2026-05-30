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
import net.akehurst.kotlinx.utils.Builder
import net.akehurst.kotlinx.utils.ReferenceExt.mutable
import net.akehurst.kotlinx.utils.ValueExt.mutable
import net.akehurst.omg.templates.examples.common.*
import net.akehurst.omg.templates.examples.simple.*
import net.akehurst.omg.templates.examples.redefined.*

@DslMarker
annotation class ExamplesDslMarker

fun Example(
    factory: examples_ModelFactory,
    id: Any,
    init: Examples_Builder.() -> Unit
): Example = buildObject(factory, id, ::Examples_Builder, init) {
    val rootResolver = examples_ModelResolver("examplesModel", factory)
    rootResolver.common.Example_resolve(it)
}

@ExamplesDslMarker
class Examples_Builder(
    val factory: examples_ModelFactory,
    val id: Any
) : Builder<Example> {

    private var _content: Collection<Element>? = null

    fun content(init: Element_ContentBuilder.() -> Unit): Collection<Element> =
        buildContent(factory, ::Element_ContentBuilder, init) { _content = it }

    override fun build(): Example =
        factory.common.Example_construct(id).also { self ->
            _content?.let { self.contentList.mutable.addAll(it) }
        }
}

private inline fun <B : Builder<T>, T : Element> buildObject(
    factory: examples_ModelFactory,
    id: Any,
    noinline ctor: (examples_ModelFactory, Any) -> B,
    init: B.() -> Unit = {},
    action: (T) -> Unit
): T = Builder.buildAction({ ctor.invoke(factory, id) }, init, action)

private inline fun <B : Builder<T>, T : Any> buildContent(
    factory: examples_ModelFactory,
    noinline ctor: (examples_ModelFactory) -> B,
    init: B.() -> Unit = {},
    action: (T) -> Unit
): T = Builder.buildAction({ ctor.invoke(factory) }, init, action)

@ExamplesDslMarker
class Element_ContentBuilder(
    val factory: examples_ModelFactory
) : Builder<Collection<Element>> {

    private val _content = mutableListOf<Element>()

    fun PropType(id: Any, init: PropType_Builder.() -> Unit = {}): PropType = buildObject(factory, id, ::PropType_Builder, init) { _content.add(it) }

    //fun PropType(id: Any): PropType = factory.createPropType(id)
    fun PropTypeB(id: Any, init: PropTypeB_Builder.() -> Unit = {}): PropTypeB = buildObject(factory, id, ::PropTypeB_Builder, init) { _content.add(it) }

    fun SingleCmpAttribute(id: Any, init: SingleCmpAttribute_Builder.() -> Unit = {}): SingleCmpAttribute = buildObject(factory, id, ::SingleCmpAttribute_Builder, init) { _content.add(it) }

    fun SingleReferenceAttribute(id: Any, init: SingleRefAttribute_Builder.() -> Unit = {}): SingleRefAttribute = buildObject(factory, id, ::SingleRefAttribute_Builder, init) { _content.add(it) }

    fun CollectionCompositeAttribute(id: Any, init: CollectionCmpAttribute_Builder.() -> Unit = {}): CollectionCmpAttribute =
        buildObject(factory, id, ::CollectionCmpAttribute_Builder, init) { _content.add(it) }

    fun SingleCmpRedefSameNameDiffTypeAttribute(id: Any, init: SingleCmpRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): SingleCmpRedefSameNameDiffTypeAttribute =
        buildObject(factory, id, ::SingleCmpRedefSameNameDiffTypeAttribute_Builder, init) { _content.add(it) }

    fun SingleCmpRedefDiffNameSameTypeAttribute(id: Any, init: SingleCmpRedefDiffNameSameTypeAttribute_Builder.() -> Unit = {}): SingleCmpRedefDiffNameSameTypeAttribute =
        buildObject(factory, id, ::SingleCmpRedefDiffNameSameTypeAttribute_Builder, init) { _content.add(it) }

    fun CollectionCmpRedefSameNameDiffTypeAttribute(id: Any, init: CollectionCmpRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): CollectionCmpRedefSameNameDiffTypeAttribute =
        buildObject(factory, id, ::CollectionCmpRedefSameNameDiffTypeAttribute_Builder, init) { _content.add(it) }

    fun SingleRefRedefSameNameDiffTypeAttribute(id: Any, init: SingleRefRedefSameNameDiffTypeAttribute_Builder.() -> Unit = {}): SingleRefRedefSameNameDiffTypeAttribute =
        buildObject(factory, id, ::SingleRefRedefSameNameDiffTypeAttribute_Builder, init) { _content.add(it) }

    fun SingleRefRedefDiffNameSameTypeAttribute(id: Any, init: SingleRefRedefDiffNameSameTypeAttribute_Builder.() -> Unit = {}): SingleRefRedefDiffNameSameTypeAttribute =
        buildObject(factory, id, ::SingleRefRedefDiffNameSameTypeAttribute_Builder, init) { _content.add(it) }

    override fun build(): Collection<Element> = _content
}

@ExamplesDslMarker
class PropType_ContentBuilder(
    val factory: examples_ModelFactory
) : Builder<Collection<PropType>> {
    private val _content = mutableListOf<PropType>()

    fun PropType(id: Any): PropType = buildObject(factory, id, ::PropType_Builder, {}) { _content.add(it) }
    fun PropTypeB(id: Any): PropTypeB = buildObject(factory, id, ::PropTypeB_Builder, {}) { _content.add(it) }

    override fun build(): Collection<PropType> = _content
}

@ExamplesDslMarker
class PropType_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<PropType> {
    override fun build(): PropType = factory.common.PropType_construct(_id)
}

@ExamplesDslMarker
class PropTypeB_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<PropTypeB> {
    override fun build(): PropTypeB = factory.common.PropTypeB_construct(_id)
}

@ExamplesDslMarker
class SingleCmpAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleCmpAttribute> {
    private var _prop1: PropType? = null
    fun prop1(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType = buildObject(factory, identity_, ::PropType_Builder, {}) { _prop1 = it }

    private var _prop2: PropType? = null
    fun prop2(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType = buildObject(factory, identity_, ::PropType_Builder, {}) { _prop2 = it }

    override fun build(): SingleCmpAttribute = factory.simple.SingleCmpAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1Value.mutable.set(it) }
        _prop2?.let { self.prop2Value.mutable.set(it) }
    }
}

@ExamplesDslMarker
class SingleCmpRedefSameNameDiffTypeAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleCmpRedefSameNameDiffTypeAttribute> {

    private var _prop1: PropTypeB? = null
    fun prop1(identity_: Any, init: PropTypeB_Builder.() -> Unit = {}): PropTypeB = buildObject(factory, identity_, ::PropTypeB_Builder, {}) { _prop1 = it }

    private var _prop2: PropTypeB? = null
    fun prop2(identity_: Any, init: PropTypeB_Builder.() -> Unit = {}): PropTypeB = buildObject(factory, identity_, ::PropTypeB_Builder, {}) { _prop2 = it }

    override fun build(): SingleCmpRedefSameNameDiffTypeAttribute = factory.redefined.SingleCmpRedefSameNameDiffTypeAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1Value.mutable.set(it) }
        _prop2?.let { self.prop2Value.mutable.set(it) }
    }
}

@ExamplesDslMarker
class SingleCmpRedefDiffNameSameTypeAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleCmpRedefDiffNameSameTypeAttribute> {
    private var _redefinesProp1: PropType? = null
    fun redefinesProp1(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType = buildObject(factory, identity_, ::PropType_Builder, {}) { _redefinesProp1 = it }

    private var _redefinesProp2: PropType? = null
    fun redefinesProp2(identity_: Any, init: PropType_Builder.() -> Unit = {}): PropType = buildObject(factory, identity_, ::PropType_Builder, {}) { _redefinesProp2 = it }

    override fun build(): SingleCmpRedefDiffNameSameTypeAttribute = factory.redefined.SingleCmpRedefDiffNameSameTypeAttribute_construct(_id).also { self ->
        _redefinesProp1?.let { self.redefinesProp1Value.mutable.set(it) }
        _redefinesProp2?.let { self.redefinesProp2Value.mutable.set(it) }
    }
}

@ExamplesDslMarker
class SingleRefAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleRefAttribute> {
    private var _prop1: Any? = null
    fun prop1(reference_: Any) = run { _prop1 = reference_ }

    private var _prop2: Any? = null
    fun prop2(reference_: Any) = run { _prop2 = reference_ }

    override fun build(): SingleRefAttribute = factory.simple.SingleRefAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1Reference.mutable.reference = it }
        _prop2?.let { self.prop2Reference.mutable.reference = it }
    }
}

@ExamplesDslMarker
class CollectionCmpAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<CollectionCmpAttribute> {
    private var _prop1: Collection<PropType>? = null
    fun prop1(init: PropType_ContentBuilder.() -> Unit = {}) = buildContent(factory, ::PropType_ContentBuilder, init) { _prop1 = it }

    private var _prop2: Collection<PropType>? = null
    fun prop2(init: PropType_ContentBuilder.() -> Unit = {}) = buildContent(factory, ::PropType_ContentBuilder, init) { _prop2 = it }

    override fun build(): CollectionCmpAttribute = factory.simple.CollectionCmpAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1OrderedSet.mutable.addAll(it) }
        _prop2?.let { self.prop2List.mutable.addAll(it) }
    }
}

@ExamplesDslMarker
class CollectionCmpRedefSameNameDiffTypeAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<CollectionCmpRedefSameNameDiffTypeAttribute> {
    private var _prop1: Collection<PropTypeB>? = null
    fun prop1(init: PropType_ContentBuilder.() -> Unit = {}) = buildContent(factory, ::PropType_ContentBuilder, init) {
        //TODO: should we record an issue or throw exception if wrong type of object has been built !
        _prop1 = it.filterIsInstance<PropTypeB>()
    }

    private var _prop2: Collection<PropTypeB>? = null
    fun prop2(init: PropType_ContentBuilder.() -> Unit = {}) = buildContent(factory, ::PropType_ContentBuilder, init) { _prop2 = it.filterIsInstance<PropTypeB>() }

    override fun build(): CollectionCmpRedefSameNameDiffTypeAttribute = factory.redefined.CollectionCmpRedefSameNameDiffTypeAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1OrderedSet.mutable.addAll(it) }
        _prop2?.let { self.prop2List.mutable.addAll(it) }
    }
}

@ExamplesDslMarker
class SingleRefRedefSameNameDiffTypeAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleRefRedefSameNameDiffTypeAttribute> {
    private var _prop1: Any? = null
    fun prop1(reference_: Any) = run { _prop1 = reference_ }

    private var _prop2: Any? = null
    fun prop2(reference_: Any) = run { _prop2 = reference_ }

    override fun build(): SingleRefRedefSameNameDiffTypeAttribute = factory.redefined.SingleRefRedefSameNameDiffTypeAttribute_construct(_id).also { self ->
        _prop1?.let { self.prop1Reference.mutable.reference = it }
        _prop2?.let { self.prop2Reference.mutable.reference = it }
    }
}

@ExamplesDslMarker
class SingleRefRedefDiffNameSameTypeAttribute_Builder(
    val factory: examples_ModelFactory,
    private val _id: Any
) : Builder<SingleRefRedefDiffNameSameTypeAttribute> {
    private var _redefinesProp1: Any? = null
    fun redefinesProp1(reference_: Any) = run { _redefinesProp1 = reference_ }

    private var _redefinesProp2: Any? = null
    fun redefinesProp2(reference_: Any) = run { _redefinesProp2 = reference_ }

    override fun build(): SingleRefRedefDiffNameSameTypeAttribute = factory.redefined.SingleRefRedefDiffNameSameTypeAttribute_construct(_id).also { self ->
        _redefinesProp1?.let { self.redefinesProp1Reference.mutable.reference = it }
        _redefinesProp2?.let { self.redefinesProp2Reference.mutable.reference = it }
    }
}

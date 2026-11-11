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

package net.akehurst.omg.uml.v2_5_1.xmi

import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.Element
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelFactory
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam

/** A metaclass as generated from the metamodel XMI by the xmi template (see _templates/xmi). */
class XmiGenClass(
    val name: String,
    val isAbstract: Boolean,
    val superclassNames: List<String>,
    val construct: ((UML_ModelFactory, Any) -> Any)?,
    val ownedFeatures: List<XmiGenFeature>,
)

/** A property as generated from the metamodel XMI by the xmi template (see _templates/xmi). */
class XmiGenFeature(
    override val name: String,
    override val kind: XmiFeatureKind,
    override val isMany: Boolean,
    override val typeName: String,
    override val isDerived: Boolean,
    private val converter: ((String) -> Any?)?,
    private val adder: ((Any, Any) -> Unit)?,
    private val referenceAdder: ((Any, Any, Any?) -> Unit)?,
) : XmiMetaFeature {
    companion object {
        fun derived(name: String, typeName: String, isMany: Boolean) =
            XmiGenFeature(name, XmiFeatureKind.DATA, isMany, typeName, true, null, null, null)

        fun data(name: String, typeName: String, isMany: Boolean, convert: (String) -> Any?, add: (Any, Any) -> Unit) =
            XmiGenFeature(name, XmiFeatureKind.DATA, isMany, typeName, false, convert, add, null)

        fun composite(name: String, typeName: String, isMany: Boolean, add: (Any, Any) -> Unit) =
            XmiGenFeature(name, XmiFeatureKind.COMPOSITE, isMany, typeName, false, null, add, null)

        fun reference(name: String, typeName: String, isMany: Boolean, add: (Any, Any, Any?) -> Unit) =
            XmiGenFeature(name, XmiFeatureKind.REFERENCE, isMany, typeName, false, null, null, add)
    }

    override fun convert(text: String): Any? = converter?.invoke(text)
    override fun addValue(obj: Any, value: Any) {
        (adder ?: error("'$name' is derived or a reference")).invoke(obj, value)
    }

    override fun addReference(obj: Any, identity: Any, resolved: Any?) {
        (referenceAdder ?: error("'$name' is not a reference")).invoke(obj, identity, resolved)
    }

    override fun toString(): String = "$name: $typeName${if (isMany) "[*]" else ""} ($kind${if (isDerived) ", derived" else ""})"
}

/**
 * The UML 2.5.1 metamodel for the XMI reader: creates objects with [factory].
 * Features are looked up by their XMI name in the metaclass, then in its superclasses (nearest first),
 * so a redefining property is found before the property it redefines.
 */
class UmlXmiMetamodel(
    val factory: UML_ModelFactory = UML_ModelFactoryRam("UML"),
) : XmiMetamodel, XmiIdentityProvider {

    private val genClasses: Map<String, XmiGenClass> by lazy { UML_XmiMetaclasses.all.associateBy { it.name } }
    private val metaclasses = mutableMapOf<String, Metaclass?>()
    private val metaclassByObjectClass = mutableMapOf<kotlin.reflect.KClass<*>, Metaclass>()

    override fun metaclass(name: String): XmiMetaclass? = metaclassInternal(name)

    private fun metaclassInternal(name: String): Metaclass? = metaclasses.getOrPut(name) {
        genClasses[name]?.let { Metaclass(it) }
    }

    override fun metaclassOf(obj: Any): XmiMetaclass? = metaclassByObjectClass[obj::class]

    override fun identityOf(obj: Any): Any? = (obj as? Element)?._identity

    /** all metaclass names, concrete and abstract */
    val metaclassNames: Set<String> get() = genClasses.keys

    inner class Metaclass(private val gen: XmiGenClass) : XmiMetaclass {
        override val name: String get() = gen.name
        override val isAbstract: Boolean get() = gen.isAbstract
        override val superclassNames: List<String> get() = gen.superclassNames

        /** this metaclass and all its superclasses, nearest first */
        val linearisation: List<Metaclass> by lazy {
            val result = mutableListOf<Metaclass>()
            val queue = ArrayDeque<Metaclass>()
            queue.add(this)
            while (queue.isNotEmpty()) {
                val c = queue.removeFirst()
                if (c !in result) {
                    result.add(c)
                    c.superclassNames.mapNotNullTo(queue) { metaclassInternal(it) }
                }
            }
            result
        }

        private val features: Map<String, XmiGenFeature> by lazy {
            val m = mutableMapOf<String, XmiGenFeature>()
            for (c in linearisation) for (f in c.gen.ownedFeatures) {
                // the nearest declaration wins, so a redefining property is found before the one it redefines
                if (f.name !in m) m[f.name] = f
            }
            m
        }

        override fun feature(name: String): XmiMetaFeature? = features[name]

        /** all features, owned and inherited, by XMI name */
        val allFeatures: Map<String, XmiMetaFeature> get() = features

        fun isSubclassOf(superclassName: String): Boolean = linearisation.any { it.name == superclassName }

        override fun construct(identity: Any): Any {
            val obj = (gen.construct ?: error("Metaclass '$name' is abstract")).invoke(factory, identity)
            metaclassByObjectClass.getOrPut(obj::class) { this }
            return obj
        }

        override fun toString(): String = "uml:$name"
    }
}

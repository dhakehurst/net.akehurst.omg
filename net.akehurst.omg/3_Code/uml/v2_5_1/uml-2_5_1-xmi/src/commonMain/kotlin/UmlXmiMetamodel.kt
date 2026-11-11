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

import net.akehurst.language.types.api.CollectionType
import net.akehurst.language.types.api.DataType
import net.akehurst.language.types.api.EnumType
import net.akehurst.language.types.api.InterfaceType
import net.akehurst.language.types.api.PrimitiveType
import net.akehurst.language.types.api.PropertyDeclaration
import net.akehurst.language.types.api.TypeDefinition
import net.akehurst.language.types.api.TypesDomain
import net.akehurst.omg.uml.v2_5_1.agl.types.UMLTypesDomain
import net.akehurst.omg.uml.v2_5_1.agl.types.UMLTypesMutation
import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.Element
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelFactory
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam

/**
 * The UML 2.5.1 metamodel for the XMI reader, from the AGL types of UML ([UMLTypesDomain]):
 * objects are created by the types' constructors (with [factory]) and set by the properties' mutators.
 * Features are looked up by their metamodel name in the metaclass, then in its supertypes (nearest first),
 * so a redefining property is found before the property it redefines.
 */
class UmlXmiMetamodel(
    val factory: UML_ModelFactory = UML_ModelFactoryRam("UML"),
) : XmiMetamodel, XmiIdentityProvider {

    val typesDomain: TypesDomain = UMLTypesDomain(factory).types

    private val typesByName: Map<String, TypeDefinition> by lazy {
        typesDomain.namespace.flatMap { ns -> ns.ownedTypes }
            .filter { it is DataType || it is InterfaceType }
            .associateBy { it.name.value }
    }
    private val metaclasses = mutableMapOf<String, Metaclass?>()
    private val metaclassByObjectClass = mutableMapOf<kotlin.reflect.KClass<*>, Metaclass>()

    override fun metaclass(name: String): XmiMetaclass? = metaclassInternal(name)

    private fun metaclassInternal(name: String): Metaclass? = metaclasses.getOrPut(name) {
        typesByName[name]?.let { Metaclass(it) }
    }

    override fun metaclassOf(obj: Any): XmiMetaclass? = metaclassByObjectClass[obj::class]

    override fun identityOf(obj: Any): Any? = (obj as? Element)?._identity

    /** all metaclass names, concrete and abstract */
    val metaclassNames: Set<String> get() = typesByName.keys

    inner class Metaclass(val type: TypeDefinition) : XmiMetaclass {
        override val name: String get() = type.name.value
        override val isAbstract: Boolean get() = type !is DataType
        override val superclassNames: List<String> get() = type.supertypes.map { it.typeName.value }

        /** this metaclass and all its superclasses, nearest first */
        val linearisation: List<Metaclass> by lazy {
            val result = mutableListOf<Metaclass>()
            val queue = ArrayDeque<Metaclass>()
            queue.add(this)
            while (queue.isNotEmpty()) {
                val c = queue.removeFirst()
                if (result.none { it.type === c.type }) {
                    result.add(c)
                    c.superclassNames.mapNotNullTo(queue) { metaclassInternal(it) }
                }
            }
            result
        }

        private val features: Map<String, Feature> by lazy {
            val m = mutableMapOf<String, Feature>()
            for (c in linearisation) for (p in c.type.property) {
                // the nearest declaration wins, so a redefining property is found before the one it redefines
                if (p.name.value !in m) m[p.name.value] = Feature(p)
            }
            m
        }

        override fun feature(name: String): XmiMetaFeature? = features[name]

        /** all features, owned and inherited, by metamodel name */
        val allFeatures: Map<String, XmiMetaFeature> get() = features

        fun isSubclassOf(superclassName: String): Boolean = linearisation.any { it.name == superclassName }

        override fun construct(identity: Any): Any {
            val ctor = (type as? DataType)?.constructors?.firstOrNull { it.parameters.size == 1 }
                ?: error("Metaclass '$name' has no constructor")
            val obj = (ctor.execution ?: error("Constructor of '$name' has no execution")).invoke(listOf(identity))
                ?: error("Constructor of '$name' returned null")
            metaclassByObjectClass.getOrPut(obj::class) { this }
            return obj
        }

        override fun toString(): String = "uml:$name"
    }

    /** a property of an AGL type, as the XMI reader uses it */
    class Feature(val property: PropertyDeclaration) : XmiMetaFeature {
        override val name: String get() = property.name.value
        override val isMany: Boolean = property.typeInstance.resolvedDefinitionOrNull is CollectionType
        private val elementType = if (isMany) property.typeInstance.typeArguments.firstOrNull()?.type else property.typeInstance
        private val elementDef = elementType?.resolvedDefinitionOrNull
        override val typeName: String = when (elementDef) {
            is EnumType -> elementDef.qualifiedName.value
            null -> elementType?.typeName?.value ?: "Any"
            else -> elementDef.name.value
        }
        override val kind: XmiFeatureKind = when {
            elementDef is PrimitiveType || elementDef is EnumType -> XmiFeatureKind.DATA
            property.isComposite -> XmiFeatureKind.COMPOSITE
            else -> XmiFeatureKind.REFERENCE
        }
        override val isDerived: Boolean get() = property.isDerived

        override fun convert(text: String): Any? = when (elementDef) {
            is EnumType -> UMLTypesMutation.enumValue(typeName, text.trim())
            else -> when (typeName) {
                // UML Integer and UnlimitedNatural are both Integer ('*' is -1)
                "Integer" -> XmiDataConversion.convertKotlin("Long", text)
                else -> XmiDataConversion.convertPrimitive(typeName, text)
            }
        }

        override fun get(obj: Any): Any? = (property.accessor ?: error("'$name' has no accessor")).invoke(obj)

        override fun set(obj: Any, value: Any?) {
            (property.mutator ?: error("'$name' is derived")).invoke(obj, value)
        }

        override fun toString(): String = "$name: $typeName${if (isMany) "[*]" else ""} ($kind${if (isDerived) ", derived" else ""})"
    }
}

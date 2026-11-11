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

/**
 * What the XMI reader needs to know about a metamodel, and how it creates and sets objects of it.
 * The reader itself is independent of the metamodel; [UmlXmiMetamodel] is the one for UML 2.5.1.
 */
interface XmiMetamodel {
    /** the metaclass with this name (the part after 'uml:' in an xmi:type), or null */
    fun metaclass(name: String): XmiMetaclass?

    /** the metaclass of an object created by this metamodel, or null */
    fun metaclassOf(obj: Any): XmiMetaclass?
}

interface XmiMetaclass {
    val name: String
    val isAbstract: Boolean

    /** names of the direct superclasses */
    val superclassNames: List<String>

    /** creates an instance with the given identity; not called for an abstract metaclass */
    fun construct(identity: Any): Any

    /** the feature with this name, owned or inherited, or null */
    fun feature(name: String): XmiMetaFeature?
}

enum class XmiFeatureKind {
    /** a value of a primitive type or enumeration */
    DATA,

    /** a contained object */
    COMPOSITE,

    /** a reference to an object owned elsewhere */
    REFERENCE
}

interface XmiMetaFeature {
    val name: String
    val kind: XmiFeatureKind
    val isMany: Boolean

    /** name of the type: a metaclass, enumeration or primitive type (String, Integer, Boolean, Real, UnlimitedNatural) */
    val typeName: String

    /** derived features are computed, so they are not set when read; their values are skipped */
    val isDerived: Boolean

    /** converts the text of a DATA value to the type of this feature, or returns null if it is not valid */
    fun convert(text: String): Any?

    /** sets a single value (DATA or COMPOSITE), or adds it for a many valued feature */
    fun addValue(obj: Any, value: Any)

    /** sets a single reference, or adds it for a many valued feature; [resolved] is null for an [XmiProxy] */
    fun addReference(obj: Any, identity: Any, resolved: Any?)
}

/**
 * The identity of an unresolved reference, kept in place of the referenced object.
 * [href] is the href or xmi:idref as written; [uri] the document part resolved against the referring document.
 * Access it through the `...Reference` holder of the property, e.g. `property.typeReference.reference as XmiProxy`.
 */
data class XmiProxy(
    val href: String,
    val uri: String?,
    val fragment: String?,
) {
    override fun toString(): String = "XmiProxy($href)"
}

/** Converts XMI text to the Kotlin types used for UML primitive types. */
object XmiDataConversion {
    fun convertPrimitive(typeName: String, text: String): Any? = when (typeName) {
        "String" -> text
        "Boolean" -> when (text.trim()) {
            "true" -> true
            "false" -> false
            else -> null
        }

        "Integer" -> text.trim().toLongOrNull()
        "UnlimitedNatural" -> text.trim().let { if (it == "*") -1L else it.toLongOrNull() }
        "Real" -> text.trim().let {
            when (it) {
                "INF" -> Double.POSITIVE_INFINITY
                "-INF" -> Double.NEGATIVE_INFINITY
                "NaN" -> Double.NaN
                else -> it.toDoubleOrNull()
            }
        }

        else -> null
    }

    /** converts to a Kotlin type: String, Long (also for UnlimitedNatural, where '*' is -1), Double or Boolean */
    fun convertKotlin(kotlinTypeName: String, text: String): Any? = when (kotlinTypeName) {
        "String" -> text
        "Long" -> text.trim().let { if (it == "*") -1L else it.toLongOrNull() }
        "Int" -> text.trim().toIntOrNull()
        "Double" -> convertPrimitive("Real", text)
        "Boolean" -> convertPrimitive("Boolean", text)
        else -> null
    }

    fun isPrimitive(typeName: String) = typeName in setOf("String", "Boolean", "Integer", "UnlimitedNatural", "Real")
}

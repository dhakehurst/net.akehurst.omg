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

package net.akehurst.omg._simple_mof_for_xmi

import net.akehurst.language.collections.transitiveClosure
import net.akehurst.omg._simple_mof_for_xmi.MofClass.Companion.isSubtypeOf
import kotlin.collections.plus
import kotlin.collections.toSet

// Simplified MOF model representation

fun kotlinValidName(name: String): String {
    val keywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "interface", "is", "null", "object", "package", "return",
        "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
        "var", "when", "while"
    )
    val problematicKotlinTypes = setOf("Annotation", "Function")
    return when {
        keywords.contains(name) -> name + '_'
        problematicKotlinTypes.contains(name) -> name + '_'
        else -> name
    }
}

// A top-level container for the parsed model
class MofModel(
    val name: String,
    val instanceRootNames: List<String>,
    val refHandler: XmiReferenceHandler
) {
    fun UnknownType(xmiId: String) = ExternalReferenceClass(this, "UNKOWN", "Any /*TODO: <Unknown type for xmiId = $xmiId>*/")

    var generateParameters: Map<String, Any> = mutableMapOf()

    val subPackages: MutableList<MofPackage> = mutableListOf()
    val genSubPackages get() = subPackages.filterNot { pkg -> (this.generateParameters["EXCLUDE_PACKAGE"] as? List<String>)?.let { it.contains(pkg.qualifiedName) } ?: false }

    val packageList = mutableListOf<MofPackage>()
    val enumList = mutableListOf<MofEnum>()
    val interfaceList = mutableListOf<MofInterface>()
    val classList = mutableListOf<MofClass>()
    val associationList = mutableListOf<MofAssociation>()
    val allClasses get() = packageList.flatMap { it.classes }.toSet()

    val validName: String by lazy { kotlinValidName(name) }
    val instanceRootList: List<MofClass> by lazy {
        instanceRootNames.map { n -> allClasses.first { n == it.name } }
    }

    fun findTypeById(id: String): MofType? = when {
        id.startsWith("http://www.omg.org") || id.startsWith("https://www.omg.org") -> {
            refHandler.getRef(null, id) as? MofType
                ?: let {
                    val ref = id.substringAfter("#")
                    refHandler.getRef(null, ref) as? MofType
                }
        }

        else -> refHandler.getRef(null, id) as? MofType
    }

    fun findPropertyById(id: String) = when {
        id.startsWith("http://www.omg.org") || id.startsWith("https://www.omg.org") -> {
            val ref = id.substringAfter("#")
            refHandler.getRef(null, ref) as? MofProperty
        }

        else -> refHandler.getRef(null, id) as? MofProperty
    }

    fun findPackageById(id: String): MofPackage? = refHandler.getRef(null, id) as? MofPackage
    fun getElementById(id: String): Any? = refHandler.getRef(null, id)

    fun getFullPackagePath(pkg: MofPackage?): String {
        if (pkg == null) return ""
        val pathParts = mutableListOf<String>()
        var current: MofPackage? = pkg
        while (current != null) {
            pathParts.add(current.name.lowercase().replace("-", "_"))
            current = current.parentPackage
        }
        return pathParts.reversed().joinToString(".")
    }

    fun isPrimitive(type: MofType): Boolean {
        val primitives = setOf("String", "Boolean", "Real", "Integer", "UnlimitedNatural")
        return type is MofEnum || primitives.contains(type.name)
    }

    fun addPackage(file: String, parent: MofPackage?, obj: MofPackage) {
        packageList.add(obj)
        refHandler.setRef(file, obj.xmiId, obj)
        parent?.subPackages?.add(obj)
        obj.parentPackage = parent
    }

    override fun hashCode(): Int = name.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofModel -> false
        name != other.name -> false
        else -> true
    }

    override fun toString(): String = name
}

data class MofPackage(
    val model: MofModel,
    val name: String,
    val xmiId: String,
    var parentPackage: MofPackage? = null // For establishing hierarchy
) {
    var comment: String? = null

    val enums: MutableList<MofEnum> = mutableListOf()
    val interfaces: MutableList<MofInterface> = mutableListOf()
    val classes: MutableList<MofClass> = mutableListOf()
    val subPackages: MutableList<MofPackage> = mutableListOf()
    val associations: MutableList<MofAssociation> = mutableListOf()

    // this is actually useless because the xmi does not correctl include all needed
    val packageImportRefs: MutableList<String> = mutableListOf()

    val validName: String by lazy { kotlinValidName(name) }
    val qualifiedName: String get() = parentPackage?.let { it.qualifiedName + "." + name } ?: name
    val qualifiedPath: String get() = parentPackage?.let { it.qualifiedPath + "/" + name } ?: name

    val genSubPackages get() = subPackages.filterNot { pkg -> (model.generateParameters["EXCLUDE_PACKAGE"] as? List<String>)?.let { it.contains(pkg.qualifiedName) } ?: false }

    val packageImport: Set<MofPackage> by lazy {
        val frmInterfaceSupers = interfaces.flatMap { intf -> intf.superTypes.mapNotNull { it.parentPackage } }.toSet()
        val frmInterfaceAttrs = interfaces.flatMap { intf -> intf.allAttributes.mapNotNull { attr -> attr.type.parentPackage } }.toSet()
        val frmClsSupers = classes.flatMap { intf -> intf.superTypes.mapNotNull { it.parentPackage } }.toSet()
        val frmClsAttrs = classes.flatMap { intf -> intf.allAttributes.mapNotNull { attr -> attr.type.parentPackage } }.toSet()
        val imports = (frmInterfaceSupers + frmInterfaceAttrs + frmClsSupers + frmClsAttrs) - this
        imports.filterNot { pkg -> (model.generateParameters["EXCLUDE_PACKAGE"] as? List<String>)?.let { it.contains(pkg.qualifiedName) } ?: false }.toSet()
    }

    val packageImportWithSubtypes: Set<MofPackage> by lazy {
        val frmInterfaceSubs = interfaces.flatMap { intf -> intf.allSubTypes.mapNotNull { it.parentPackage } }.toSet()
        val frmClsSubs = classes.flatMap { intf -> intf.allSubTypes.mapNotNull { it.parentPackage } }.toSet()
        val imports = (packageImport + frmInterfaceSubs + frmClsSubs) - this
        imports.filterNot { pkg -> (model.generateParameters["EXCLUDE_PACKAGE"] as? List<String>)?.let { it.contains(pkg.qualifiedName) } ?: false }.toSet()
    }

    val allImport: List<MofPackage>
        get() = when (parentPackage) {
            null -> packageImport.toSet().sortedBy { it.qualifiedName }
            else -> (parentPackage!!.allImport + packageImport).toSet().sortedBy { it.qualifiedName }
        }

    val allImportWithSubtypes: List<MofPackage>
        get() = when (parentPackage) {
            null -> packageImportWithSubtypes.toSet().sortedBy { it.qualifiedName }
            else -> (parentPackage!!.allImportWithSubtypes + packageImportWithSubtypes).toSet().sortedBy { it.qualifiedName }
        }

    // TODO: this just imports all packages, would be better to calulates those needed as above tried to do
//    val allImport: List<MofPackage> by lazy {
//        model.packageList.filterNot { pkg -> (model.generateParameters["EXCLUDE_PACKAGE"] as? List<String>)?.let { it.contains(pkg.qualifiedName) } ?: false }
//    }

    val isEmpty: Boolean get() = enums.isEmpty() && classes.isEmpty()

    fun addEnum(file: String, obj: MofEnum) {
        model.enumList.add(obj)
        model.refHandler.setRef(file, obj.xmiId, obj)
        this.enums.add(obj)
        obj.parentPackage = this
    }

    fun addClass(file: String, obj: MofClass) {
        model.classList.add(obj)
        model.refHandler.setRef(file, obj.xmiId, obj)
        this.classes.add(obj)
        obj.parentPackage = this
    }

    fun addInterface(file: String, obj: MofInterface) {
        model.interfaceList.add(obj)
        model.refHandler.setRef(file, obj.xmiId, obj)
        this.interfaces.add(obj)
        obj.parentPackage = this
    }

    override fun hashCode(): Int = arrayOf(xmiId).contentHashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofClass -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentPackage?.qualifiedName}.$name"
}

interface MofType {
    val model: MofModel
    val name: String
    val xmiId: String
    var parentPackage: MofPackage?

    val ownedAttribute: List<MofProperty>

    // -- derived --
    val validName: String
    val isAbstract: Boolean
    val isPrimitive: Boolean
    val hasSubtypes: Boolean

    val allAttributes: Set<MofProperty>
    val ownedRedefiningAttribute: List<MofProperty>

    val superTypes: Set<MofType>
    val allSuperTypes: Set<MofType>
    val subTypes: Set<MofType>
    val allSubTypes: Set<MofType>
    val concreteSubclasses: Set<MofClass>

    /**
     * Returns all properties inherited from superclasses plus owned properties, with duplicate resolution.
     * When the same property appears multiple times (e.g., from diamond inheritance), resolves conflicts using
     * type covariance and multiplicity narrowing. Excludes properties that have been redefined.
     * Results are grouped by their defining parent class.
     */
    val allNormalisedAttribute: List<MofClassAttributeImplInfo>
    val allNormalisedAttribute2: Map<MofClass, List<MofProperty>>
}

class MofEnum(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
) : MofType {
    override var parentPackage: MofPackage? = null
    var comment: String? = null
    override val ownedAttribute: List<MofProperty> = emptyList()

    // --- derived ---
    override val validName: String by lazy { kotlinValidName(name) }
    override val isAbstract: Boolean = false
    override val isPrimitive: Boolean = true
    override val hasSubtypes: Boolean = false
    override val allAttributes: Set<MofProperty> = emptySet()
    override val ownedRedefiningAttribute: List<MofProperty> = emptyList()
    override val superTypes: Set<MofType> = emptySet()
    override val allSuperTypes: Set<MofType> = emptySet()
    override val subTypes: Set<MofType> = emptySet()
    override val allSubTypes: Set<MofType> = emptySet()
    override val concreteSubclasses: Set<MofClass> = emptySet()

    override val allNormalisedAttribute: List<MofClassAttributeImplInfo> = emptyList()
    override val allNormalisedAttribute2: Map<MofClass, List<MofProperty>> = emptyMap()

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofEnum -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentPackage?.qualifiedName}.$name"
}

abstract class MofAbstractType() : MofType {

    override var parentPackage: MofPackage? = null
    var comment: String? = null
    val generalizations: MutableList<String> = mutableListOf() // Stores xmi:idref of general classes
    override val ownedAttribute: MutableList<MofProperty> = mutableListOf()
    val operations: MutableList<MofOperation> = mutableListOf()

    // --- derived ---
    override val validName: String by lazy { kotlinValidName(name) }
    override val isPrimitive: Boolean by lazy { model.isPrimitive(this) }
    val qualifiedName: String get() = parentPackage?.let { it.qualifiedName + "." + name } ?: name

    override val superTypes: Set<MofType>
        get() = generalizations.map {
            model.findTypeById(it) ?: error("Type '${it}' not found")
        }.toSet()
    override val allSuperTypes: Set<MofType> by lazy { superTypes.transitiveClosure { it.superTypes }.toSet() }
    override val subTypes: Set<MofType> by lazy {
        model.allClasses.filter { it.superTypes.contains(this) }.toSet()
    }

    override val allSubTypes: Set<MofType> by lazy {
        model.allClasses.filter { it.allSuperTypes.contains(this) }.toSet()
    }

    override val hasSubtypes: Boolean get() = subTypes.isNotEmpty()

    override val allAttributes get() = (allSuperTypes.flatMap { it.ownedAttribute } + ownedAttribute).toSet()
    override val ownedRedefiningAttribute get() = ownedAttribute.filter { it.isRedefining }
    val allRedefiningAttribute by lazy { (allSuperTypes.flatMap { it.ownedRedefiningAttribute } + ownedRedefiningAttribute).toSet() }

    override val allNormalisedAttribute: List<MofClassAttributeImplInfo>
            by lazy {
                // 2. Recursively get the normalized properties from all superclasses
                val inheritedProperties = superTypes.flatMap { st -> st.allNormalisedAttribute.flatMap { c -> c.attributes.map { r -> r.attribute } } }.toSet()
                val allCandidateProperties = ownedAttribute + inheritedProperties
                // 4. Get ALL redefined targets from EVERY candidate property
                val redefinedTargets = allCandidateProperties.flatMap { it.redefinedProperty }.toSet()
                // 5. Filter out any property that was a target of redefinition
                val survivingProperties = allCandidateProperties.filter { it !in redefinedTargets }.toSet()

                val deduplicatedProperties = linkedMapOf<String, MofProperty>()
                for (prop in survivingProperties) {
                    val key = prop.validName
                    val existing = deduplicatedProperties[key]
                    if (existing == null) {
                        deduplicatedProperties[key] = prop
                    } else {
                        // --- COLLISION RESOLUTION ---
                        // 1. Identity: They are literally the exact same property inherited from a common ancestor.
                        if (existing == prop) {
                            continue
                        }

                        // 2. Type Narrowing (Covariance):
                        // If they have different types, we MUST pick the subtype.
                        // e.g., If existing is 'Element' and prop is 'Activity', we must keep 'Activity'.
                        if (isSubtypeOf(prop.parentClass!!, existing.parentClass!!)) {
                            deduplicatedProperties[key] = prop
                        } else if (isSubtypeOf(existing.parentClass!!, prop.parentClass!!)) {
                            // Keep the 'existing' one, it is already the narrower type
                            continue
                        }
                        // 3. Multiplicity Narrowing:
                        // If types are identical, but one restricts the multiplicity (e.g., [0..*] vs [0..1]),
                        // pick the scalar [0..1] version, as a scalar can conceptually satisfy a collection
                        // contract in your generated getters/setters.
                        else if (prop.upperBound == 1 && existing.upperBound != 1) {
                            deduplicatedProperties[key] = prop
                        } else if (existing.upperBound == 1 && prop.upperBound != 1) {
                            continue
                        }
                        // 4. Irresolvable Conflict (Fallback)
                        // They have the same name, but completely incompatible types (e.g., String vs Int).
                        // This means the source UML is fundamentally broken/ill-formed.
                        else {
                            throw IllegalStateException(
                                "Irresolvable property collision in flat implementation. " +
                                        "Property '${prop.name}' has conflicting types: '${existing.parentClass!!.name}' and '${prop.parentClass!!.name}'."
                            )
                        }
                    }
                }
                val normalized = deduplicatedProperties.values
                val grps = normalized.groupBy { it.parentClass!! }
                grps.map { (cls, props) ->
                    val attrs = props.map { p ->
                        val brdgs = p.requiredBridgingProperty(normalized)
                        MofRedefinedAttributeImplInfo(p, brdgs)
                    }
                    MofClassAttributeImplInfo(cls, attrs)
                }
            }

    override val allNormalisedAttribute2: Map<MofClass, List<MofProperty>>
            by lazy {
                // 2. Recursively get the normalized properties from all superclasses
                val inheritedProperties = superTypes.flatMap { it.allNormalisedAttribute2.values.flatten() }.toSet()
                val allCandidateProperties = ownedAttribute + inheritedProperties
                // 4. Get ALL redefined targets from EVERY candidate property
                val redefinedTargets = allCandidateProperties + allCandidateProperties.flatMap { it.redefinedProperty }.toSet()
                // 5. Filter out any property that redefines something unless its type is redefined also
                val survivingProperties = redefinedTargets.filter { it.isRedefining.not() || (it.isTypeRedefined && it.isComposite) }.toSet()

                val deduplicatedProperties = linkedMapOf<String, MofProperty>()
                for (prop in survivingProperties) {
                    val key = prop.validName
                    val existing = deduplicatedProperties[key]
                    if (existing == null) {
                        deduplicatedProperties[key] = prop
                    } else {
                        // --- COLLISION RESOLUTION ---
                        // 1. Identity: They are literally the exact same property inherited from a common ancestor.
                        if (existing == prop) {
                            continue
                        }

                        // 2. Type Narrowing (Covariance):
                        // If they have different types, we MUST pick the subtype.
                        // e.g., If existing is 'Element' and prop is 'Activity', we must keep 'Activity'.
                        if (isSubtypeOf(prop.parentClass!!, existing.parentClass!!)) {
                            deduplicatedProperties[key] = prop
                        } else if (isSubtypeOf(existing.parentClass!!, prop.parentClass!!)) {
                            // Keep the 'existing' one, it is already the narrower type
                            continue
                        }
                        // 3. Multiplicity Narrowing:
                        // If types are identical, but one restricts the multiplicity (e.g., [0..*] vs [0..1]),
                        // pick the scalar [0..1] version, as a scalar can conceptually satisfy a collection
                        // contract in your generated getters/setters.
                        else if (prop.upperBound == 1 && existing.upperBound != 1) {
                            deduplicatedProperties[key] = prop
                        } else if (existing.upperBound == 1 && prop.upperBound != 1) {
                            continue
                        }
                        // 4. Irresolvable Conflict (Fallback)
                        // They have the same name, but completely incompatible types (e.g., String vs Int).
                        // This means the source UML is fundamentally broken/ill-formed.
                        else {
                            throw IllegalStateException(
                                "Irresolvable property collision in flat implementation. " +
                                        "Property '${prop.name}' has conflicting types: '${existing.parentClass!!.name}' and '${prop.parentClass!!.name}'."
                            )
                        }
                    }
                }
                val normalized = deduplicatedProperties.values
                normalized.groupBy { it.parentClass!! }
            }

    val allNormalisedAttributeFlat: Set<MofProperty> by lazy {
        allNormalisedAttribute.flatMap { c -> c.attributes.map { r -> r.attribute } }.toSet()
    }
}

class MofInterface(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
) : MofAbstractType() {

    override val isAbstract: Boolean = true
    override val isPrimitive: Boolean by lazy { model.isPrimitive(this) }

    override val concreteSubclasses: Set<MofClass> by lazy { model.classList.filter { !it.isAbstract && it.allSuperTypes.contains(this) }.toSet() }

}

open class MofClass(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
) : MofAbstractType() {

    companion object {
        /**
         * Checks if 'potentialSubtype' inherits from 'superType' in the UML metamodel.
         */
        fun isSubtypeOf(potentialSubtype: MofType, superType: MofType): Boolean {
            if (potentialSubtype == superType) return true

            // Recursively check if any of the superclasses match
            return potentialSubtype.superTypes.any { isSubtypeOf(it, superType) }
        }
    }

    override var isAbstract: Boolean = false
    val allCompositeAttribute: List<MofProperty> by lazy { allNormalisedAttributeFlat.filter { it.isComposite } }
    val allReferenceAttribute: List<MofProperty> by lazy { allNormalisedAttributeFlat.filter { it.isReference } }
    override val concreteSubclasses: Set<MofClass> by lazy {
        ((if (isAbstract) mutableListOf() else mutableListOf(this)) +
                model.classList.filter { !it.isAbstract && it.allSuperTypes.contains(this) }).toSet()
    }

    fun addAttribute(file: String, attr: MofProperty) {
        model.refHandler.setRef(file, attr.xmiId, attr)
        this.ownedAttribute.add(attr)
        attr.parentClass = this
    }

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofClass -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentPackage?.qualifiedName}.$name"
}

class ExternalReferenceClass(
    model: MofModel,
    href: String,
    name: String
) : MofClass(model, name, href) {

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is ExternalReferenceClass -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "ExternalRef($xmiId)"
}

class MofProperty(
    val model: MofModel,
    val name: String,
    val xmiId: String,
) {
    var parentClass: MofClass? = null
    var comment: String? = null
    var typeXmiId: String? = null // For internal references like _MOF-Reflection-Element
    var typeHref: String? = null // For external references like PrimitiveTypes.xmi#String
    var lowerBound: Int = 0
    var upperBound: Int = 1 // -1 for unbounded (*)
    var isDerived: Boolean = false
    var isDerivedUnion: Boolean = false
    var isReadOnly: Boolean = false
    var isOrdered: Boolean = false
    var isUnique: Boolean = true
    var aggregation: MofAggregationKind = MofAggregationKind.NONE
    var associationXmiId: String? = null // To link with MofAssociation
    val redefinedPropertyRef: Set<String> = mutableSetOf()
    val subsettedPropertyRef: Set<String> = mutableSetOf()
    var opposite: MofProperty? = null

    // --- derived ---
    val attrName by lazy {
        when {
            (1 == upperBound) -> name
            else -> when {
                (false == isUnique) and (false == isOrdered) -> name + "Collection"
                (true == isUnique) and (false == isOrdered) -> name + "Set"
                (false == isUnique) and (true == isOrdered) -> name + "List"
                (true == isUnique) and (true == isOrdered) -> name + "OrderedSet"
                else -> "ERROR(normalising property name of '$qualifiedName')"
            }
        }
    }
    val validName by lazy { kotlinValidName(attrName) }
    val qualifiedName: String get() = parentClass?.let { it.qualifiedName + "." + name } ?: name
    val type: MofType by lazy {
        typeXmiId?.let { model.findTypeById(it) }
            ?: typeHref?.let { model.findTypeById(it) }
            ?: model.UnknownType(xmiId)
    }
    val colTypeName by lazy {
        when {
            isSingle -> "ERROR: '$qualifiedName' is not a collection type"

            else -> when {
                (false == isUnique) and (false == isOrdered) -> "Collection"
                (true == isUnique) and (false == isOrdered) -> "Set"
                (false == isUnique) and (true == isOrdered) -> "List"
                (true == isUnique) and (true == isOrdered) -> "OrderedSet"
                else -> "ERROR: getting collection type name for '$qualifiedName'"
            }
        }
    }
    val colTypeNameLower by lazy {
        when {
            isSingle -> "ERROR: '$qualifiedName' is not a collection type"

            else -> when {
                (false == isUnique) and (false == isOrdered) -> "collection"
                (true == isUnique) and (false == isOrdered) -> "set"
                (false == isUnique) and (true == isOrdered) -> "list"
                (true == isUnique) and (true == isOrdered) -> "orderedSet"
                else -> "ERROR: getting collection type name for '$qualifiedName'"
            }
        }
    }
    val validTypeName by lazy {
        when {
            isSingle -> when {
                isOptional -> type.validName + "?"
                else -> type.validName
            }

            else -> "$colTypeName<${type.validName}>"
        }
    }
    val validTypeNameNotNullable by lazy {
        when {
            isSingle -> type.validName
            else -> "$colTypeName<${type.validName}>"
        }
    }

    val isOptional: Boolean get() = 0 == lowerBound && isSingle // possibly empty collections are not treated as optional, just maybe empty
    val isSingle: Boolean get() = 1 == upperBound
    val isCollection: Boolean get() = -1 == upperBound || 1 < upperBound
    val isComposite get() = aggregation == MofAggregationKind.composite || type.isPrimitive
    val isReference get() = isComposite.not() && type !is MofEnum
    val isOverride
        get() = parentClass?.allSuperTypes?.any { sc ->
            sc.allAttributes.any { it.validName == this.validName }
        } ?: false

    val isNameRedefined get() = allRedefinedProperty.any { it.name != this.name }
    val isTypeRedefined get() = allRedefinedProperty.any { it.type != this.type }
    val isValidNameRedefined get() = allRedefinedProperty.any { it.validName != this.validName }
    val isGenTypeRedefined get() = isTypeRedefined || isValidNameRedefined.not()


    //val isRedefined by lazy { parentClass!!.allRedefiningAttribute.contains(this) }
    val isRedefining get() = redefinedPropertyRef.isNotEmpty()
    val redefinedProperty
        get() = redefinedPropertyRef.map {
            model.findPropertyById(it) ?: error("Cannot find redefinedPropertyRef '$it' in '$qualifiedName'")
        }

    val allRedefinedProperty: Set<MofProperty>
        get() {
            val byNormName = linkedMapOf<String, MofProperty>()
            val visited = mutableSetOf<String>()
            val queue = redefinedProperty.toMutableList()
            var index = 0

            while (index < queue.size) {
                val prop = queue[index++]
                if (!visited.add(prop.xmiId)) continue

                byNormName.putIfAbsent(prop.validName, prop)
                queue.addAll(prop.redefinedProperty)
            }

            return byNormName.values.toSet()
        }

    // due to codgen names of properties, and redefining changing names
    // a redefined property may need to implement bridging properties
    fun requiredBridgingProperty(normalized: Collection<MofProperty>): List<MofProperty> {
        return allRedefinedProperty
            .filter {
                // bridge redefined things with a different name
                it.validName != this.validName &&
                        // if not in the set of implemented/normalized properties
                        normalized.contains(it).not()
            }
    }

    val isSubsetting get() = subsettedPropertyRef.isNotEmpty()
    val subsettedProperty
        get() = subsettedPropertyRef.map {
            model.findPropertyById(it) ?: error("Cannot find subsettedPropertyRef '$it' in '$qualifiedName'")
        }

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofProperty -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentClass?.parentPackage?.qualifiedName}.${parentClass?.name}.$name"
}

data class MofClassAttributeImplInfo(
    val defClass: MofClass,
    val attributes: List<MofRedefinedAttributeImplInfo>
)

data class MofRedefinedAttributeImplInfo(
    val attribute: MofProperty,
    val bridges: List<MofProperty>,
)

class MofOperation(
    val model: MofModel,
    val name: String,
    val xmiId: String,
    val visibility: String = "public",
    val isQuery: Boolean = false,
    val parameters: MutableList<MofParameter> = mutableListOf(),
    val returnParameter: MofParameter? = null
) {
    // Helper to get the parent class
    var parentClass: MofClass? = null

    override fun hashCode(): Int = arrayOf(parentClass, name).contentHashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofOperation -> false
        name != other.name -> false
        parentClass != other.parentClass -> false
        else -> true
    }

    override fun toString(): String = "${parentClass?.parentPackage?.qualifiedName}.${parentClass?.name}.$name"

}

data class MofParameter(
    val model: MofModel,
    val name: String,
    val xmiId: String,
    val typeXmiId: String?,
    val typeHref: String?,
    val direction: String = "in" // "in", "out", "inout", "return"
)

data class MofAssociation(
    val model: MofModel,
    val name: String?,
    val xmiId: String,
    var memberEnds: List<MofProperty> // Association ends are properties
) {
    var parentPackage: MofPackage? = null
}

enum class MofAggregationKind {
    NONE, reference, composite
}


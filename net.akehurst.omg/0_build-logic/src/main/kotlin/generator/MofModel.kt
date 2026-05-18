package generator

import net.akehurst.language.collections.transitiveClosure

// Simplified MOF model representation

// A top-level container for the parsed model
class MofModel(
    val name: String,
    val instanceRootNames: List<String>,
    val refHandler: XmiReferenceHandler
) {
    fun UnknownType(xmiId:String) = ExternalReferenceClass(this, "UNKOWN","Any /*TODO: <Unknown type for xmiId = $xmiId>*/")

    val packageList = mutableListOf<MofPackage>()
    val enumList = mutableListOf<MofEnum>()
    val interfaceList = mutableListOf<MofInterface>()
    val classList = mutableListOf<MofClass>()
    val associationList = mutableListOf<MofAssociation>()
    val allClasses get() = packageList.flatMap { it.classes }.toSet()

    val instanceRootList: List<MofClass> by lazy {
        instanceRootNames.map { n -> allClasses.first { n == it.name } }
    }

    fun findTypeById(id: String): MofType? = when {
        id.startsWith("http://www.omg.org") -> {
            //val ref = id.substringAfter("#")
            refHandler.getRef(null, id) as? MofType
        }

        else -> refHandler.getRef(null, id) as? MofType
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
    val packageImport: MutableList<String> = mutableListOf()

    val qualifiedName: String = parentPackage?.let { it.qualifiedName + "." + name } ?: name
    val allImport: List<String>
        get() = when (parentPackage) {
            null -> packageImport.toSet().sorted()
            else -> (parentPackage!!.allImport + packageImport).toSet().sorted()
        }

    val isEmpty: Boolean get() = enums.isEmpty() && classes.isEmpty()

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
}

class MofEnum(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
    override var parentPackage: MofPackage? = null
) : MofType {
    var comment: String? = null

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofEnum -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentPackage?.qualifiedName}.$name"
}

class MofInterface(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
) : MofType {
    override var parentPackage: MofPackage? = null
    val generalizations: MutableList<String> = mutableListOf() // Stores xmi:idref of general classes
    val ownedAttribute: MutableList<MofProperty> = mutableListOf()
    val operations: MutableList<MofOperation> = mutableListOf()
    var comment: String? = null

    val superClasses: List<MofClass> get() = generalizations.map { model.findTypeById(it) as MofClass }
}

open class MofClass(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
    var isAbstract: Boolean = false,
    val generalizations: MutableList<String> = mutableListOf(), // Stores xmi:idref of general classes
    val ownedAttribute: MutableList<MofProperty> = mutableListOf(),
    val operations: MutableList<MofOperation> = mutableListOf(),
    override var parentPackage: MofPackage? = null
) : MofType {

    companion object {
        /**
         * Checks if 'potentialSubtype' inherits from 'superType' in the UML metamodel.
         */
        fun isSubtypeOf(potentialSubtype: MofClass, superType: MofClass): Boolean {
            if (potentialSubtype == superType) return true

            // Recursively check if any of the superclasses match
            return potentialSubtype.superClasses.any { isSubtypeOf(it, superType) }
        }
    }

    var comment: String? = null

    val qualifiedName: String = parentPackage?.let { it.qualifiedName + "." + name } ?: name
    val superClasses: List<MofClass> get() = generalizations.map { model.findTypeById(it) as MofClass }
    val allSuperClasses: Set<MofClass>
        get() {
            return superClasses.transitiveClosure { it.superClasses }.toSet()
        }

    val allAttributes get() = (allSuperClasses.flatMap { it.ownedAttribute } + ownedAttribute).toSet()

    val ownedRedefiningAttribute get() = ownedAttribute.filter { it.isRedefining }
    val allRedefiningAttribute by lazy { (allSuperClasses.flatMap { it.ownedRedefiningAttribute } + ownedRedefiningAttribute).toSet() }

    /**
     * Returns all properties inherited from superclasses plus owned properties, with duplicate resolution.
     * When the same property appears multiple times (e.g., from diamond inheritance), resolves conflicts using
     * type covariance and multiplicity narrowing. Excludes properties that have been redefined.
     * Results are grouped by their defining parent class.
     */
    val allNormalisedAttribute: Map<MofClass, List<MofProperty>>
            by lazy {
                // 2. Recursively get the normalized properties from all superclasses
                val inheritedProperties = superClasses.flatMap { it.allNormalisedAttribute.values.flatten() }.toSet()
                val allCandidateProperties = ownedAttribute + inheritedProperties
                // 4. Get ALL redefined targets from EVERY candidate property
                val redefinedTargets = allCandidateProperties.flatMap { it.redefinedProperty }.toSet()
                // 5. Filter out any property that was a target of redefinition
                val survivingProperties = allCandidateProperties.filter { it !in redefinedTargets }.toSet()

                val deduplicatedProperties = linkedMapOf<String, MofProperty>()
                for (prop in survivingProperties) {
                    val key = prop.genName
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

    val allNormalisedAttribute2: Map<MofClass, List<MofProperty>>
            by lazy {
                // 2. Recursively get the normalized properties from all superclasses
                val inheritedProperties = superClasses.flatMap { it.allNormalisedAttribute.values.flatten() }.toSet()
                val allCandidateProperties = ownedAttribute + inheritedProperties
                // 4. Get ALL redefined targets from EVERY candidate property
                val redefinedTargets = allCandidateProperties + allCandidateProperties.flatMap { it.redefinedProperty }.toSet()
                // 5. Filter out any property that redefines something unless its type is redefined also
                val survivingProperties = redefinedTargets.filter { it.isRedefining.not() || (it.isTypeRedefined && it.isComposite)  }.toSet()

                val deduplicatedProperties = linkedMapOf<String, MofProperty>()
                for (prop in survivingProperties) {
                    val key = prop.genName
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

    val compositeAttribute: List<MofProperty>
        get() {
            return allNormalisedAttribute.values.flatten().filter { it.isComposite  }
        }

    val concreteSubclasses: List<MofClass> by lazy {
        (if (isAbstract) mutableListOf() else mutableListOf(this)) +
        model.classList.filter { !it.isAbstract  && it.allSuperClasses.contains(this) }
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
    // Helper to get the parent class, assuming this property is an attribute of a class
    @Transient
    var parentClass: MofClass? = null

    var comment: String? = null
    var typeXmiId: String? = null // For internal references like _MOF-Reflection-Element
    var typeHref: String? = null // For external references like PrimitiveTypes.xmi#String
    var lowerBound: Int = 0
    var upperBound: Int = 1 // -1 for unbounded (*)
    var isComposite: Boolean = false
    var isDerived: Boolean = false
    var isReadOnly: Boolean = false
    var isOrdered: Boolean = false
    var isUnique: Boolean = true
    var aggregation: MofAggregationKind = MofAggregationKind.NONE
    var associationXmiId: String? = null // To link with MofAssociation
    val redefinedPropertyRef: Set<String> = mutableSetOf()
    val subsettedPropertyRef: Set<String> = mutableSetOf()
    var opposite: MofProperty? = null

    val qualifiedName: String = parentClass?.let { it.qualifiedName + "." + name } ?: name

    val type: MofType by lazy {
        typeXmiId?.let { model.findTypeById(it) }
            ?: typeHref?.let { model.findTypeById(it) }
            ?: model.UnknownType(xmiId)
    }

    val typeName get() = type.name

    val isReference get() = isComposite.not() && type !is MofEnum
    val isOverride
        get() = parentClass?.allSuperClasses?.any { sc ->
            sc.allAttributes.any { it.genName == this.genName }
        } ?: false

    val isNameRedefined get() = allRedefinedProperty.any { it.name != this.name }
    val isTypeRedefined get() = allRedefinedProperty.any { it.type != this.type }

    val isGenNameRedefined get() = allRedefinedProperty.any {it.genName != this.genName}

    val isGenTypeRedefined get() = isTypeRedefined || isGenNameRedefined.not()


    //val isRedefined by lazy { parentClass!!.allRedefiningAttribute.contains(this) }
    val isRedefining get() = redefinedPropertyRef.isNotEmpty()
    val redefinedProperty
        get() = redefinedPropertyRef.map {
            model.refHandler.getRef(null, it)!! as MofProperty
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

                byNormName.putIfAbsent(prop.genName, prop)
                queue.addAll(prop.redefinedProperty)
            }

            return byNormName.values.toSet()
        }



    val genName
        get() = when {
            (1 == upperBound) -> name
            else -> when {
                (false == isUnique) and (false == isOrdered) -> name + "Collection"
                (true == isUnique) and (false == isOrdered) -> name + "Set"
                (false == isUnique) and (true == isOrdered) -> name + "List"
                (true == isUnique) and (true == isOrdered) -> name + "OrderedSet"
                else -> "ERROR(normalising property name of '$qualifiedName')"
            }
        }

    val isSubsetting get() = subsettedPropertyRef.isNotEmpty()
    val subsettedProperty get() = subsettedPropertyRef.map { model.refHandler.getRef(null, it)!! as MofProperty }

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofProperty -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentClass?.parentPackage?.qualifiedName}.${parentClass?.name}.$name"
}

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
    @Transient
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
    @Transient
    var parentPackage: MofPackage? = null
}

enum class MofAggregationKind {
    NONE, SHARED, COMPOSITE
}


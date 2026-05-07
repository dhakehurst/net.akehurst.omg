package net.akehurst.omg.mof.gen

import net.akehurst.language.collections.transitiveClosure
import net.akehurst.language.types.builder.typesDomain

// Simplified MOF model representation

// A top-level container for the parsed model
data class MofModel(
    val packages: MutableMap<String, MofPackage> = mutableMapOf(),
    val enums: MutableMap<String, MofEnum> = mutableMapOf(),
    val classes: MutableMap<String, MofClass> = mutableMapOf(),
    val associations: MutableMap<String, MofAssociation> = mutableMapOf(),
    val idToElementMap: MutableMap<String, Any> = mutableMapOf(), // Generic map for resolving IDs
    val primitiveTypes: Map<String, String> = mapOf(
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String" to "String",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Integer" to "Int",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real" to "Double",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#UnlimitedNatural" to "Long" // Or custom type
        // Add more as needed
    )
) {
    fun findTypeById(id: String): MofType? = idToElementMap[id] as? MofType
    fun findPackageById(id: String): MofPackage? = idToElementMap[id] as? MofPackage
    fun getElementById(id: String): Any? = idToElementMap[id]

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
}

data class MofPackage(
    val model: MofModel,
    val name: String,
    val xmiId: String,
    val enums: MutableList<MofEnum> = mutableListOf(),
    val classes: MutableList<MofClass> = mutableListOf(),
    val subPackages: MutableList<MofPackage> = mutableListOf(),
    val associations: MutableList<MofAssociation> = mutableListOf(),
    val packageImport: MutableList<String> = mutableListOf(),
    var parentPackage: MofPackage? = null // For establishing hierarchy
) {
    var comment: String? = null

    val qualifiedName: String = parentPackage?.let { it.qualifiedName + "." + name } ?: name
    val allImport: List<String>
        get() = when (parentPackage) {
            null -> packageImport.toSet().sorted()
            else -> (parentPackage!!.allImport + packageImport).toSet().sorted()
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
}

data class MofEnum(
    override val model: MofModel,
    override val name: String,
    override val xmiId: String,
    override var parentPackage: MofPackage? = null
) : MofType {
    var comment: String? = null
}

class MofClass(
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

            // If it's a primitive or outside the metamodel hierarchy, handle accordingly
            if (potentialSubtype !is MofClass || superType !is MofClass) return false

            // Recursively check if any of the superclasses match
            return potentialSubtype.superClasses.any { isSubtypeOf(it, superType) }
        }
    }

    var comment: String? = null

    val superClasses: List<MofClass> get() = generalizations.map { model.findTypeById(it) as MofClass }
    val allSuperClasses: Set<MofClass>
        get() {
            return superClasses.transitiveClosure { it.superClasses }.toSet()
        }

    val allAttributes get() = (allSuperClasses.flatMap { it.ownedAttribute } + ownedAttribute).toSet()

    val redefiningProperty get() = ownedAttribute.filter { it.isRedefining }

    val allNormalisedAttribute: Map<MofClass, List<MofProperty>>
        by lazy {
            // 1. Get properties explicitly owned by this class
            val ownedProperties = ownedAttribute
            // 2. Recursively get the normalized properties from all superclasses
            val inheritedProperties = superClasses.flatMap { it.allNormalisedAttribute.values.flatten() }.toSet()
            val allCandidateProperties = ownedProperties + inheritedProperties
            // 4. THE FIX: Get ALL redefined targets from EVERY candidate property
            val redefinedTargets = allCandidateProperties.flatMap { it.redefinedProperty }.toSet()
            // 5. Filter out any property that was a target of redefinition
            val survivingProperties = allCandidateProperties.filter { it !in redefinedTargets }.toSet()

            val deduplicatedProperties = mutableMapOf<String, MofProperty>()
            for (prop in survivingProperties) {
                val existing = deduplicatedProperties[prop.name]
                if (existing == null) {
                    deduplicatedProperties[prop.name] = prop
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
                        deduplicatedProperties[prop.name] = prop
                    } else if (isSubtypeOf(existing.parentClass!!, prop.parentClass!!)) {
                        // Keep the 'existing' one, it is already the narrower type
                        continue
                    }
                    // 3. Multiplicity Narrowing:
                    // If types are identical, but one restricts the multiplicity (e.g., [0..*] vs [0..1]),
                    // pick the scalar [0..1] version, as a scalar can conceptually satisfy a collection
                    // contract in your generated getters/setters.
                    else if (prop.upperBound == 1 && existing.upperBound != 1) {
                        deduplicatedProperties[prop.name] = prop
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

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofClass -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentPackage?.qualifiedName}.$name"
}


class MofProperty(
    val model: MofModel,
    val name: String,
    val xmiId: String,
) {
    // Helper to get the parent class, assuming this property is an attribute of a class
    @Transient
    var parentClass: MofClass? = null

    val typeName: String by lazy {
        typeXmiId?.let { model.findTypeById(it)?.name }
            ?: typeHref?.let { model.primitiveTypes[it] }
            ?: "<Unknown type for xmiId = $xmiId>"
    }

    var comment: String? = null
    var typeXmiId: String? = null // For internal references like _MOF-Reflection-Element
    var typeHref: String? = null // For external references like PrimitiveTypes.xmi#String
    var lowerBound: Int = 0
    var upperBound: Int = 1 // -1 for unbounded (*)
    var isDerived: Boolean = false
    var isReadOnly: Boolean = false
    var isOrdered: Boolean = false
    var isUnique: Boolean = true
    var aggregation: MofAggregationKind = MofAggregationKind.NONE
    var associationXmiId: String? = null // To link with MofAssociation
    val redefinedPropertyRef: Set<String> = mutableSetOf()
    val subsettedPropertyRef: Set<String> = mutableSetOf()
    var opposite: MofProperty? = null

    val isOverride
        get() = parentClass?.allSuperClasses?.any { sc ->
            sc.allAttributes.any { it.name == this.name }
        } ?: false

    val isRedefining get() = redefinedPropertyRef.isNotEmpty()
    val redefinedProperty get() = redefinedPropertyRef.map { model.idToElementMap[it]!! as MofProperty }

    val redefinedName
        get() = when {
            isRedefining -> {}
            else -> name
        }

    val isSubsetting get() = subsettedPropertyRef.isNotEmpty()
    val subsettedProperty get() = subsettedPropertyRef.map { model.idToElementMap[it]!! as MofProperty }

    override fun hashCode(): Int = xmiId.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is MofProperty -> false
        xmiId != other.xmiId -> false
        else -> true
    }

    override fun toString(): String = "${parentClass?.parentPackage?.qualifiedName}.${parentClass?.name}.$name"
}

data class MofOperation(
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

object SimpleMof {

    val types = typesDomain("SimpleMof", true) {
        namespace("net.akehurst.omg.mof.gen.simple") {
            data("MofModel", implementation = MofModel::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "packages", "Map", propertyExecution = MofModel::packages) {
                        typeArgument("String")
                        typeArgument("MofPackage")
                    }
                    parameter(setOf(REF, VAL), "classes", "Map", propertyExecution = MofModel::classes) {
                        typeArgument("String")
                        typeArgument("MofClass")
                    }
                    parameter(setOf(REF, VAL), "associations", "Map", propertyExecution = MofModel::associations) {
                        typeArgument("String")
                        typeArgument("MofAssociation")
                    }
                    parameter(setOf(REF, VAL), "idToElementMap", "Map", propertyExecution = MofModel::idToElementMap) {
                        typeArgument("String")
                        typeArgument("Any")
                    }
                    parameter(setOf(CMP, VAL), "primitiveTypes", "Map", propertyExecution = MofModel::primitiveTypes) {
                        typeArgument("String")
                        typeArgument("String")
                    }
                }
                methodPrimitive("findTypeById", "MofType", true) {
                    execution { self, args ->
                        (args[0] as? String)?.let {
                            (self as MofModel).findTypeById(it)
                        }
                    }
                    parameter("id", "String")
                }
            }
            data("MofPackage", implementation = MofPackage::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofPackage::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofPackage::xmiId)
                    parameter(setOf(CMP, VAL), "classes", "List", propertyExecution = MofPackage::classes) {
                        typeArgument("MofClass")
                    }
                    parameter(setOf(CMP, VAL), "subPackages", "List", propertyExecution = MofPackage::subPackages) {
                        typeArgument("MofPackage")
                    }
                    parameter(setOf(CMP, VAL), "associations", "List", propertyExecution = MofPackage::associations) {
                        typeArgument("MofAssociation")
                    }
                    parameter(setOf(CMP, VAL), "packageImport", "List", propertyExecution = MofPackage::packageImport) {
                        typeArgument("String")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, propertyExecution = MofPackage::parentPackage)
                }
                propertyOf(setOf(DER), "qualifiedName", "String")
                propertyOf(setOf(DER), "allImport", "List") { typeArgument("String") }
            }
            interface_("MofType", implementation = MofType::class)
            data("MofEnum", implementation = MofEnum::class) {
                supertype("MofType")
            }
            data("MofClass", implementation = MofClass::class) {
                supertype("MofType")
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofClass::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofClass::xmiId)
                    parameter(setOf(CMP, VAL), "isAbstract", "Boolean", propertyExecution = MofClass::isAbstract)
                    parameter(setOf(CMP, VAL), "generalizations", "List", propertyExecution = MofClass::generalizations) {
                        typeArgument("String")
                    }
                    parameter(setOf(CMP, VAL), "attributes", "List", propertyExecution = MofClass::ownedAttribute) {
                        typeArgument("MofProperty")
                    }
                    parameter(setOf(CMP, VAL), "operations", "List", propertyExecution = MofClass::operations) {
                        typeArgument("MofOperation")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, propertyExecution = MofClass::parentPackage) {}
                }

                propertyOf(setOf(DER), "allNormalisedAttribute", "Map", execution = MofClass::allNormalisedAttribute) {
                    typeArgument("MofClass")
                    typeArgument("List") {
                        typeArgument("MofProperty")
                    }
                }
            }
            data("MofProperty", implementation = MofProperty::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofProperty::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, propertyExecution = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, propertyExecution = MofProperty::typeHref)
                    parameter(setOf(CMP, VAL), "lowerBound", "Integer", propertyExecution = MofProperty::lowerBound)
                    parameter(setOf(CMP, VAL), "upperBound", "Integer", propertyExecution = MofProperty::upperBound)
                    parameter(setOf(CMP, VAL), "isDerived", "Boolean", propertyExecution = MofProperty::isDerived)
                    parameter(setOf(CMP, VAL), "isReadOnly", "Boolean", propertyExecution = MofProperty::isReadOnly)
                    parameter(setOf(CMP, VAL), "isUnique", "Boolean", propertyExecution = MofProperty::isUnique)
                    parameter(setOf(CMP, VAL), "isOrdered", "Boolean", propertyExecution = MofProperty::isOrdered)
                    parameter(setOf(CMP, VAL), "aggregation", "MofAggregationKind", propertyExecution = MofProperty::aggregation)
                    parameter(setOf(CMP, VAL), "associationXmiId", "String", true, propertyExecution = MofProperty::associationXmiId)
                }
                propertyOf(setOf(DER), "isOverride", "Boolean", execution = MofProperty::isOverride)
            }
            data("MofOperation", implementation = MofOperation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofOperation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofOperation::xmiId)
                    parameter(setOf(CMP, VAL), "visibility", "String", propertyExecution = MofOperation::visibility)
                    parameter(setOf(CMP, VAL), "isQuery", "Boolean", propertyExecution = MofOperation::isQuery)
                    parameter(setOf(CMP, VAL), "parameters", "List", propertyExecution = MofOperation::parameters) { typeArgument("MofParameter") }
                    parameter(setOf(CMP, VAL), "returnParameter", "MofParameter", propertyExecution = MofOperation::returnParameter)
                }
            }
            data("MofParameter", implementation = MofParameter::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofParameter::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofParameter::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, propertyExecution = MofParameter::typeXmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, propertyExecution = MofParameter::typeHref)
                    parameter(setOf(CMP, VAL), "direction", "String", propertyExecution = MofParameter::direction)
                }
            }
            data("MofAssociation", implementation = MofAssociation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofAssociation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofAssociation::xmiId)
                    parameter(setOf(REF, VAL), "memberEnds", "List", propertyExecution = MofAssociation::memberEnds) { typeArgument("MofProperty") }
                }
            }
            enum("MofAggregationKind", listOf("NONE", "SHARED", "COMPOSITE"), implementation = MofAggregationKind::class)
        }
    }

}
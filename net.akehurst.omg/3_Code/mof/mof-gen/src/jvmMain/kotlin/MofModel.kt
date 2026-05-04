package net.akehurst.omg.mof.gen

// Simplified MOF model representation
data class MofPackage(
    val name: String,
    val xmiId: String,
    val classes: MutableList<MofClass> = mutableListOf(),
    val subPackages: MutableList<MofPackage> = mutableListOf(),
    val associations: MutableList<MofAssociation> = mutableListOf(),
    var parentPackage: MofPackage? = null // For establishing hierarchy
)

data class MofClass(
    val name: String,
    val xmiId: String,
    var isAbstract: Boolean = false,
    val generalizations: MutableList<String> = mutableListOf(), // Stores xmi:idref of general classes
    val attributes: MutableList<MofProperty> = mutableListOf(),
    val operations: MutableList<MofOperation> = mutableListOf(),
    var parentPackage: MofPackage? = null
)

data class MofProperty(
    val name: String,
    val xmiId: String,
    val typeXmiId: String?, // For internal references like _MOF-Reflection-Element
    val typeHref: String?,  // For external references like PrimitiveTypes.xmi#String
    val lowerBound: Int = 0,
    val upperBound: Int = 1, // -1 for unbounded (*)
    val isDerived: Boolean = false,
    val isReadOnly: Boolean = false,
    val aggregation: MofAggregationKind = MofAggregationKind.NONE,
    val associationXmiId: String? = null // To link with MofAssociation
) {
    // Helper to get the parent class, assuming this property is an attribute of a class
    @Transient var parentClass: MofClass? = null
}


enum class MofAggregationKind {
    NONE, SHARED, COMPOSITE
}

data class MofOperation(
    val name: String,
    val xmiId: String,
    val visibility: String = "public",
    val isQuery: Boolean = false,
    val parameters: MutableList<MofParameter> = mutableListOf(),
    val returnParameter: MofParameter? = null
) {
    // Helper to get the parent class
    @Transient var parentClass: MofClass? = null
}

data class MofParameter(
    val name: String,
    val xmiId: String,
    val typeXmiId: String?,
    val typeHref: String?,
    val direction: String = "in" // "in", "out", "inout", "return"
)

data class MofAssociation(
    val name: String?,
    val xmiId: String,
    var memberEnds: List<MofProperty> // Association ends are properties
) {
    @Transient var parentPackage: MofPackage? = null
}

// A top-level container for the parsed model
data class MofModel(
    val packages: MutableMap<String, MofPackage> = mutableMapOf(),
    val classes: MutableMap<String, MofClass> = mutableMapOf(),
    val associations: MutableMap<String, MofAssociation> = mutableMapOf(),
    val idToElementMap: MutableMap<String, Any> = mutableMapOf(), // Generic map for resolving IDs
    val primitiveTypes: Map<String, String> = mapOf(
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String" to "String",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Integer" to "Int",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Boolean" to "Boolean",
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#UnlimitedNatural" to "Long" // Or custom type
        // Add more as needed
    )
) {
    fun findClassById(id: String): MofClass? = idToElementMap[id] as? MofClass
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
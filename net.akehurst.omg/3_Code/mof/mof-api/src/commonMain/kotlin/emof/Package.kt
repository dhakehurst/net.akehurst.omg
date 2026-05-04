package net.akehurst.omg.mof.api.emof

interface PackageImport : DirectedRelationship {
    var importedPackage: String?
}

interface Package : PackageableElement {

    /** +URI :String [0..] */
    var URI : String

    var packageImport : List<PackageImport>

    /**
     * +packagedElement [0..*]
     * opposite: PackageableElement.owningPackage
     */
    val packagedElement: Set<PackageableElement>

    /**
     * +/ownedType [0..*]
     * opposite: Type.package
     */
    val ownedType: Set<Type>

    /**
     * +nestingPackage [0..1]
     * opposite: Package. nestedPackage
     */
    val nestingPackage: Package?

    /**
     *  +/nestedPackage [0..*]
     *  opposite: Package.nestingPackage
     */
    val nestedPackage: Set<Package>

}
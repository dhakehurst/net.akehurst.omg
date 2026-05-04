package net.akehurst.omg.mof.api.emof

import net.akehurst.omg.mof.ram.emof.PackageableElementRam

class PackageRam : Package, PackageableElementRam() {
    override var URI: String = ""
    override var packageImport: List<PackageImport> = mutableListOf()
    override val packagedElement: MutableSet<PackageableElement> = mutableSetOf()
    override val ownedType: MutableSet<Type> = mutableSetOf()
    override var nestingPackage: Package? = null
    override val nestedPackage: MutableSet<Package> = mutableSetOf()

}


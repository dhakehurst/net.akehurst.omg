package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.NamedElement
import net.akehurst.omg.mof.api.emof.PackageImport
import net.akehurst.omg.mof.api.emof.PackageableElement

abstract class NamedElementRam : NamedElement, ElementRam() {
    override var name: String
        get() = TODO("not implemented")
        set(value) {}
}

abstract class PackageableElementRam : PackageableElement, NamedElementRam() {

}

class PackageImportRam : PackageImport, DirectedRelationshipRam() {
    override var importedPackage: String? = null
}
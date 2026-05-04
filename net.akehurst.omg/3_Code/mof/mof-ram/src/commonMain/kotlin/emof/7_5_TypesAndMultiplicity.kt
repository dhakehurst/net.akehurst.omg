package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.MultiplicityElement
import net.akehurst.omg.mof.api.emof.Type
import net.akehurst.omg.mof.api.emof.TypedElement

abstract class TypedElementRam : TypedElement, NamedElementRam() {
    override var type: Type?
        get() = TODO("not implemented")
        set(value) {}
}

abstract class TypeRam : Type, PackageableElementRam() {
    override var typedElement: Set<TypedElement>
        get() = TODO("not implemented")
        set(value) {}
}

abstract class MultiplicityElementRam : MultiplicityElement {
    override var isOrdered: Boolean = false

    override var isUnique: Boolean = true

    override var lower: Int = 1

    override var upper: Int = 1
}
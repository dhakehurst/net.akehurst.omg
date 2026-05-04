package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.Constraint
import net.akehurst.omg.mof.api.emof.Element
import net.akehurst.omg.mof.api.emof.OpaqueExpression

class ConstraintRam : Constraint, PackageableElementRam() {
    //override context:Namespace

    override var constrainedElement: Element
        get() = TODO("not implemented")
        set(value) {}

    override var specification: OpaqueExpression
        get() = TODO("not implemented")
        set(value) {}
}


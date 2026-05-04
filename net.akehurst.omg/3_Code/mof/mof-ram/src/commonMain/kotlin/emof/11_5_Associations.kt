package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.Association
import net.akehurst.omg.mof.api.emof.Property

class AssociationRam : Association, ClassifierRam() {
    override var ownedEnd: List<Property>
        get() = TODO("not implemented")
        set(value) {}
    override var memberEnd: List<Property>
        get() = TODO("not implemented")
        set(value) {}
}


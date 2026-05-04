package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.Class
import net.akehurst.omg.mof.api.emof.Operation
import net.akehurst.omg.mof.api.emof.Property

class ClassRam : Class, EncapsulatedClassifierRam() {

    override var ownedAttribute: List<Property> = mutableListOf()

    override var class_: Set<Class> = mutableSetOf()

    override var superClass: Set<Class> = mutableSetOf()

    override var ownedOperation: List<Operation> = mutableListOf()
}

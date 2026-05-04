package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*

class OperationRam : Operation, StructuredClassifierRam() {

    override val isOrdered: Boolean = false
    override val isUnique: Boolean = true
    override val lower: Int = 1
    override val upper: Int = 1

    override var class_: Class? = null
    override var ownedParameter: List<Parameter> = mutableListOf()
    override var raisedException: Set<Type> = mutableSetOf()
    override var bodyCondition: String = ""
    override var isQuery: Boolean = false
    override var redefinedOperation: Operation? = null
    override var ownedRule: Set<Constraint> = mutableSetOf()
    override var preCondition: Constraint? = null
    override var visibility: VisibilityKind = VisibilityKind.public_
    override var isAbstract: Boolean = false


}
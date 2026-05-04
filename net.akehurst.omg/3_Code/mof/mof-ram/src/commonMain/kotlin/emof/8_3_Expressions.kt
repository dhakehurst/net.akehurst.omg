package net.akehurst.omg.mof.api.emof

import net.akehurst.omg.mof.ram.emof.PackageableElementRam

abstract class ValueSpecificationRam : ValueSpecification, PackageableElementRam() {
    override var expression: Expression? = null
    // --- TypedExpression ---
    override var type: Type? = null

}

class ExpressionRam : Expression, ValueSpecificationRam() {
    override var symbol: String? = null
    override var expression: Expression? = null
    override var operand: Set<ValueSpecification> = mutableSetOf()
}

class OpaqueExpressionRam : OpaqueExpression, ValueSpecificationRam() {
    override var body: List<String> = mutableListOf()
    override var language: List<String> = mutableListOf()
    override var behavior: Behavior? = null
    override var result: Parameter? = null
}
package net.akehurst.omg.mof.api.emof


interface ValueSpecification : TypedElement, PackageableElement {
    var expression: Expression?
}

interface Expression : ValueSpecification {
    var symbol:String?
    var operand : Set<ValueSpecification>
}

interface StringExpression : Expression { //, TemplateableElement
    var owningExpression: StringExpression?
    var subExpression:Set<Expression>
}

interface OpaqueExpression : ValueSpecification {
    var body:List<String>
    var language:List<String> //OrderedSet !
    var behavior:Behavior?
    var result: Parameter?
}

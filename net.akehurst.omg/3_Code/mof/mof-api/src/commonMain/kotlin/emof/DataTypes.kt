package net.akehurst.omg.mof.api.emof

interface DataType : Classifier {

}

interface PrimitiveType : DataType {

}

interface Enumeration : DataType {
    /**
     * +ownedLiteral [0..*]
     * opposite: EnumerationLiteral.enumeration
     */
    var ownedLiteral: Set<EnumerationLiteral>
}

interface InstanceSpecification : PackageableElement {

}

interface EnumerationLiteral : InstanceSpecification {
    /**
     * +enumeration
     * opposite: Enumeration.ownedLiteral
     */
    var enumeration: Enumeration
}
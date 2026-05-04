package net.akehurst.omg.mof.api.emof

interface Generalization {
    /**
     *  +general [1]
     *  opposite: Classifier.generalization
     */
    var general: Classifier?

    /**
     *  +specific [1]
     *  opposite: Classifier.generalization
     */
    var specific: Classifier?
}

interface Class : Classifier {
    /**
     * +ownedAttribute [0..*] {ordered}
     * opposite: Property.class
     */
    var ownedAttribute : List<Property>

    /**
     * +class [0..*]
     * opposite: Class.superClass
     */
    var class_: Set<Class>

    /**
     * +/superClass [0..*]
     * opposite: Class.class
     */
    var superClass: Set<Class>

    /**
     * +ownedOperation [0..*] {ordered}
     * opposite: Operation.class
     */
    var ownedOperation: List<Operation>
}

interface Property : StructuralFeature {
    /** +aggregation : AggregationKind = none */
    var aggregation: AggregationKind

    /** +/default: String [0..] */
    var default : String?

    /** +isComposite : Boolean = false */
    var isComposite:Boolean

    /** +/isDerived : Boolean = false */
    var isDerived : Boolean

    /** +isId : Boolean = false */
    var isId : Boolean

    /**
     * +/class [0..]
     * opposite: Class.ownedAttribute
     */
    var class_: Class?

    /**
     * +association [0..]
     * opposite: Association.memberEnd
     */
    var association: Association?

    /**
     * +owningAssociation: Association [0..]
     * opposite: Association.ownedEnd
     */
    var owningAssociation: Association?

    /**
     * +property: Property [0..]
     * opposite: Property.opposite
     */
    var property: Property?

    /**
     *  +/opposite [0..]
     *  opposite: Property.property
     */
    var opposite: Property?

    var subsettedProperty: Property?
    var redefinedProperty: Property?
    var defaultValue: String?

    var isDerivedUnion : Boolean
}

interface Association : Classifier {
    /**
     * +memberEnd [2..*] {ordered}
     * opposite: Property.association
     */
    var memberEnd: List<Property>

    /**
     * +ownedEnd [0..*] {ordered}
     * opposite: Property.owningAssociation
     */
    var ownedEnd: List<Property>

}

interface Operation {
    /** +/isOrdered : Boolean = false {readOnly} */
    val isOrdered: Boolean

    /** +/isUnique : Boolean = true {readOnly} */
    val isUnique: Boolean

    /** +/lower : Integer [0..] = 1 {readOnly} */
    val lower: Int get() = 1

    /** +/upper : UnlimitedNatural [0..] = 1 {readOnly} */
    val upper: Int get() = 1

    /**
     * +class [0..]
     * opposite: Class.ownedOperation
     */
    var class_: Class?

    /**
     * +ownedParameter [0..*] {ordered}
     * opposite: Parameter.operation
     */
    var ownedParameter: List<Parameter>

    /**
     * +raisedException [0..*]
     * opposite: Type.operation
     */
    var raisedException : Set<Type>

    var bodyCondition : String
    var isQuery: Boolean

    var redefinedOperation : Operation?

    var ownedRule : Set<Constraint>

    var preCondition : Constraint?

    var visibility : VisibilityKind

    var isAbstract : Boolean
}

interface Parameter : MultiplicityElement, TypedElement {
    /** +direction: ParameterDirectionKind = in */
    var direction: ParameterDirectionKind

    /**
     * +operation [0..]
     * opposite: Operation.ownedParameter
     */
    var operation: Operation?

    var visibilityKind : VisibilityKind
}


package net.akehurst.omg.mof.api.emof

typealias LiteralBoolean = Boolean
typealias LiteralInteger = Int
typealias LiteralNull = Unit
typealias LiteralString = String
typealias LiteralReal = Double
typealias LiteralUnlimitedNatural = Int

typealias Bag<E> = Collection<E>
//Set
typealias Sequence<E> = List<E>
//OrderedSet

interface Element {
    /**
     * +ownedComment [0..*]
     * opposite: Comment.owningElement
     */
    var ownedComment: Set<Comment>

    /**
     * +comment [0..*]
     * opposite: Comment.annotatedElement
     */
    var comment: Set<Comment>
}

interface NamedElement : Element {
    var name: String
}

interface PackageableElement : NamedElement


interface TypedElement : NamedElement {
    /**
     * +type [0..1]
     * opposite: Type.typedElement
     */
    var type: Type?
}

interface Type : PackageableElement {

    /**
     * +typedElement [*]
     * opposite: TypedElement.type
     */
    var typedElement : Set<TypedElement>
}

interface Classifier : Type {
    /** isAbstract [1] = false */
    var isAbstract: Boolean

    /**
     *  +generalization [0..*]
     *  opposite: Generalization.generl & Generalization.specific
     */
    var generalization: Set<Generalization>
}

interface Feature

interface MultiplicityElement {
    /** +isOrdered : Boolean = false m*/
    var isOrdered: Boolean

    /** +isUnique : Boolean = true */
    var isUnique: Boolean

    /** +/lower : Integer [0..] = 1 */
    var lower: Int

    /** +/upper : UnlimitedNatural [0..] = 1 */
    var upper: Int
}

interface StructuralFeature : Feature, TypedElement, MultiplicityElement {
    /** +isReadOnly : Boolean = false */
    var isReadOnly: Boolean
}

interface Constraint : NamedElement {
    var constrainedElement: Element
    var specification: OpaqueExpression
}

interface InstanceValue {
    var instance: InstanceSpecification
    var type: Type
}

enum class VisibilityKind {
    private_, public_, protected_
}

enum class AggregationKind {
    none, composite
}

enum class ParameterDirectionKind {
    in_, out_
}

interface Relationship : Element
interface DirectedRelationship : Relationship

interface Behavior
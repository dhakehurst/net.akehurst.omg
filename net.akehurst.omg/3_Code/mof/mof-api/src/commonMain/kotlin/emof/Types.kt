package net.akehurst.omg.mof.api.emof

interface Comment : Element {
    /** +body : String [0..] */
    var body: String?

    /**
     * +owningElement [0..1]
     * opposite: Element.ownedComment
     */
    var owningElement: Element?

    /**
     * +annotatedElement [0..*]
     * opposite: Element.comment
     */
    var annotatedElement: Set<Element>

}
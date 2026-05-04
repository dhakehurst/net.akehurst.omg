package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*

abstract class ElementRam : Element {
    override var ownedComment: Set<Comment> = mutableSetOf()

    override var comment: Set<Comment> = mutableSetOf()
}

class CommentRam : Comment, ElementRam() {

    override var body: String? = null

    override var owningElement: Element? = null

    override var annotatedElement: Set<Element> = mutableSetOf()
}


abstract class RelationshipRam(): Relationship, ElementRam() {

}

abstract class DirectedRelationshipRam() : DirectedRelationship, RelationshipRam() {

}
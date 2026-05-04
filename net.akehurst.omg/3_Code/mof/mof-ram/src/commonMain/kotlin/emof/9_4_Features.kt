package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.Comment
import net.akehurst.omg.mof.api.emof.Element
import net.akehurst.omg.mof.api.emof.Operation
import net.akehurst.omg.mof.api.emof.Parameter
import net.akehurst.omg.mof.api.emof.ParameterDirectionKind
import net.akehurst.omg.mof.api.emof.StructuralFeature
import net.akehurst.omg.mof.api.emof.Type
import net.akehurst.omg.mof.api.emof.VisibilityKind

abstract class BehavioralFeatureRam {

}

abstract class StructuralFeatureRam : StructuralFeature, TypedElementRam() {
    override var isReadOnly: Boolean = false
    override var type: Type? = null
    // --- MultiplicityElement ---
    override var isOrdered: Boolean = false
    override var isUnique: Boolean = true
    override var lower: Int = 1
    override var upper: Int = 1
}

class ParameterRam : Parameter, MultiplicityElementRam() {
    // --- Parameter ---
    override var direction: ParameterDirectionKind = ParameterDirectionKind.in_
    override var operation: Operation? = null
    override var visibilityKind: VisibilityKind = VisibilityKind.public_
    // --- TypedElement ---
    override var type: Type? = null
    // --- NamedElement ---
    override var name: String = "<unset>"
    // --- Element ---
    override var ownedComment: Set<Comment> = mutableSetOf()
    override var comment: Set<Comment> = mutableSetOf()
}

package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*

abstract class ClassifierRam : Classifier, TypeRam() {

    override var isAbstract: Boolean = false

    override var generalization: Set<Generalization> = mutableSetOf()

}

abstract class StructuredClassifierRam() : ClassifierRam() {

}

abstract class EncapsulatedClassifierRam() : StructuredClassifierRam() {

}
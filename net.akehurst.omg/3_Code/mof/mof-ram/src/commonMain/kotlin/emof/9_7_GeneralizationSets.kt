package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.Classifier
import net.akehurst.omg.mof.api.emof.Generalization


class GeneralizationRam  : Generalization {
    override var general: Classifier? = null
    override var specific: Classifier? = null
}
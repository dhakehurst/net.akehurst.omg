package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.AggregationKind
import net.akehurst.omg.mof.api.emof.Association
import net.akehurst.omg.mof.api.emof.Class
import net.akehurst.omg.mof.api.emof.Property

class PropertyRam : Property, StructuralFeatureRam() {
    override var aggregation: AggregationKind = AggregationKind.none
    override var isComposite: Boolean = false
    override var isDerived: Boolean = false
    override var isDerivedUnion: Boolean = false
    override var isId: Boolean = false
    override var association: Association? = null
    override var owningAssociation: Association? = null
    override var property: Property? = null
    override var opposite: Property? = null
    override var subsettedProperty: Property? = null
    override var redefinedProperty: Property? = null
    override var defaultValue: String? = null

    override var default: String?
        get() = defaultValue
        set(value) {
            defaultValue = value
        }

    override var class_: Class?
        get() = TODO("not implemented")
        set(value) {}
}

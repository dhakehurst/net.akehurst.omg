package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*
import kotlin.test.Test
import kotlin.test.assertTrue

class test_EmofRamFactory {

    @Test
    fun createByClass() {
        assertTrue(EmofRamFactory.createByClass(Association::class, emptyMap()) is AssociationRam)
        assertTrue(EmofRamFactory.createByClass(Comment::class, emptyMap()) is CommentRam)
        assertTrue(EmofRamFactory.createByClass(Constraint::class, emptyMap()) is ConstraintRam)
        assertTrue(EmofRamFactory.createByClass(Class::class, emptyMap()) is ClassRam)
        assertTrue(EmofRamFactory.createByClass(Generalization::class,
            mapOf("general" to EmofRamFactory.createClass(), "specific" to EmofRamFactory.createClass())
        )  is GeneralizationRam )
        assertTrue(EmofRamFactory.createByClass(Operation::class, emptyMap()) is OperationRam)
        assertTrue(EmofRamFactory.createByClass(Parameter::class, emptyMap()) is ParameterRam)
        assertTrue(EmofRamFactory.createByClass(Property::class, emptyMap()) is PropertyRam)

    }
}
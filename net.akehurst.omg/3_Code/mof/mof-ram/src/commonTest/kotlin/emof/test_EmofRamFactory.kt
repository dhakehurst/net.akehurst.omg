package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*
import kotlin.test.Test
import kotlin.test.assertTrue

class test_EmofRamFactory {

    @Test
    fun createByClass() {
        assertTrue(EmofRamFactory.createByClass(Association::class) is AssociationRam)
        assertTrue(EmofRamFactory.createByClass(Comment::class) is CommentRam)
        assertTrue(EmofRamFactory.createByClass(Constraint::class) is ConstraintRam)
        assertTrue(EmofRamFactory.createByClass(Class::class) is ClassRam)
        assertTrue(EmofRamFactory.createByClass(Generalization::class,
            EmofRamFactory.createClass(), EmofRamFactory.createClass()
        )  is GeneralizationRam )
        assertTrue(EmofRamFactory.createByClass(Operation::class) is OperationRam)
        assertTrue(EmofRamFactory.createByClass(Parameter::class) is ParameterRam)
        assertTrue(EmofRamFactory.createByClass(Property::class) is PropertyRam)

    }
}
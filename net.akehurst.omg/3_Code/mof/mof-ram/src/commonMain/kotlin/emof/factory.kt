package net.akehurst.omg.mof.ram.emof

import net.akehurst.omg.mof.api.emof.*
import kotlin.reflect.KClass

object EmofRamFactory {

    fun <T : Any> createByName(name: String, args: Map<String, Any>) : T = when(name) {
        "Association" -> createByClass(Association::class, args) as T
        "Comment" -> createByClass(Comment::class, args) as T
        "Constraint" -> createByClass(Constraint::class, args) as T
        "Class" -> createByClass(Class::class, args) as T
        "Generalization" -> createByClass(Generalization::class, args) as T
        "OpaqueExpression" -> createByClass(OpaqueExpression::class, args) as T
        "Operation" -> createByClass(Operation::class, args) as T
        "Package" -> createByClass(Package::class, args) as T
        "PackageImport" -> createByClass(PackageImport::class, args) as T
        "Parameter" -> createByClass(Parameter::class, args) as T
        "Property" -> createByClass(Property::class, args) as T
        else -> error("Unknown emof $name")
    }

    fun <T : Any> createByClass(klass: KClass<T>, args: Map<String, Any>) : T = when(klass) {
        Association::class -> createAssociation() as T
        Comment::class -> createComment() as T
        Constraint::class -> createConstraint() as T
        Class::class -> createClass() as T
        Generalization::class -> createGeneralization(args["general"] as Classifier, args["specific"] as Classifier) as T
        OpaqueExpression::class -> createOpaqueExpression() as T
        Operation::class -> createOperation() as T
        Package::class -> createPackage() as T
        PackageImport::class -> createPackageImport() as T
        Parameter::class -> createParameter() as T
        Property::class -> createProperty() as T
        else -> error("Unknown emof $klass")
    }

    fun createAssociation() : Association = AssociationRam()
    fun createComment() : Comment = CommentRam()
    fun createConstraint() : Constraint = ConstraintRam()
    fun createClass() : Class = ClassRam()
    fun createGeneralization(general: Classifier, specific: Classifier) : Generalization = GeneralizationRam()
    fun createOpaqueExpression() : OpaqueExpression = OpaqueExpressionRam()
    fun createOperation() : Operation = OperationRam()
    fun createPackage() : Package = PackageRam()
    fun createPackageImport() : PackageImport = PackageImportRam()
    fun createParameter() : Parameter = ParameterRam()
    fun createProperty() : Property = PropertyRam()

}

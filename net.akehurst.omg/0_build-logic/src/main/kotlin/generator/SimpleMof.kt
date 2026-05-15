package generator

import net.akehurst.language.types.builder.typesDomain

object SimpleMof {

    val types = typesDomain("SimpleMof", true) {
        namespace("net.akehurst.omg.mof.gen.simple") {
            data("MofModel", implementation = MofModel::class) {
                constructor_ {
                    parameter(setOf(REF, VAL), "name", "String", propertyExecution = MofModel::name)
                }
                propertyOf(setOf(CMP,VAL), "packageList", "List", execution = MofModel::packageList) { typeArgument("MofPackage") }
                propertyOf(setOf(CMP,VAL), "classeList", "List", execution = MofModel::classList) { typeArgument("MofEnum") }
                propertyOf(setOf(CMP,VAL), "enumList", "List", execution = MofModel::enumList) { typeArgument("MofClass") }
                propertyOf(setOf(CMP,VAL), "associationList", "List", execution = MofModel::associationList) { typeArgument("MofAssociation") }
                propertyOf(setOf(DER), "allClasses", "List", execution = MofModel::allClasses) { typeArgument("MofClass") }
                methodPrimitive("findTypeById", "MofType", true) {
                    execution { self, args ->
                        (args[0] as? String)?.let {
                            (self as MofModel).findTypeById(it)
                        }
                    }
                    parameter("id", "String")
                }
            }
            data("MofPackage", implementation = MofPackage::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofPackage::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofPackage::xmiId)
                    parameter(setOf(CMP, VAL), "classes", "List", propertyExecution = MofPackage::classes) {
                        typeArgument("MofClass")
                    }
                    parameter(setOf(CMP, VAL), "subPackages", "List", propertyExecution = MofPackage::subPackages) {
                        typeArgument("MofPackage")
                    }
                    parameter(setOf(CMP, VAL), "associations", "List", propertyExecution = MofPackage::associations) {
                        typeArgument("MofAssociation")
                    }
                    parameter(setOf(CMP, VAL), "packageImport", "List", propertyExecution = MofPackage::packageImport) {
                        typeArgument("String")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, propertyExecution = MofPackage::parentPackage)
                }
                propertyOf(setOf(DER), "qualifiedName", "String")
                propertyOf(setOf(DER), "allImport", "List") { typeArgument("String") }
            }
            interface_("MofType", implementation = MofType::class)
            data("MofEnum", implementation = MofEnum::class) {
                supertype("MofType")
            }
            data("MofInterface", implementation = MofInterface::class) {

            }
            data("MofClass", implementation = MofClass::class) {
                supertype("MofType")
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofClass::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofClass::xmiId)
                    parameter(setOf(CMP, VAL), "isAbstract", "Boolean", propertyExecution = MofClass::isAbstract)
                    parameter(setOf(CMP, VAL), "generalizations", "List", propertyExecution = MofClass::generalizations) {
                        typeArgument("String")
                    }
                    parameter(setOf(CMP, VAL), "attributes", "List", propertyExecution = MofClass::ownedAttribute) {
                        typeArgument("MofProperty")
                    }
                    parameter(setOf(CMP, VAL), "operations", "List", propertyExecution = MofClass::operations) {
                        typeArgument("MofOperation")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, propertyExecution = MofClass::parentPackage) {}
                }

                propertyOf(setOf(DER), "allNormalisedAttribute", "Map", execution = MofClass::allNormalisedAttribute) {
                    typeArgument("MofClass")
                    typeArgument("List") {
                        typeArgument("MofProperty")
                    }
                }
            }
            data("MofProperty", implementation = MofProperty::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofProperty::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, propertyExecution = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, propertyExecution = MofProperty::typeHref)
                    parameter(setOf(CMP, VAL), "lowerBound", "Integer", propertyExecution = MofProperty::lowerBound)
                    parameter(setOf(CMP, VAL), "upperBound", "Integer", propertyExecution = MofProperty::upperBound)
                    parameter(setOf(CMP, VAL), "isDerived", "Boolean", propertyExecution = MofProperty::isDerived)
                    parameter(setOf(CMP, VAL), "isReadOnly", "Boolean", propertyExecution = MofProperty::isReadOnly)
                    parameter(setOf(CMP, VAL), "isUnique", "Boolean", propertyExecution = MofProperty::isUnique)
                    parameter(setOf(CMP, VAL), "isOrdered", "Boolean", propertyExecution = MofProperty::isOrdered)
                    parameter(setOf(CMP, VAL), "aggregation", "MofAggregationKind", propertyExecution = MofProperty::aggregation)
                    parameter(setOf(CMP, VAL), "associationXmiId", "String", true, propertyExecution = MofProperty::associationXmiId)
                }
                propertyOf(setOf(DER), "isOverride", "Boolean", execution = MofProperty::isOverride)
                propertyOf(setOf(DER),"allRedefinedProperty","Set", execution = MofProperty::allRedefinedProperty) {typeArgument("MofProperty")}
            }
            data("MofOperation", implementation = MofOperation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofOperation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofOperation::xmiId)
                    parameter(setOf(CMP, VAL), "visibility", "String", propertyExecution = MofOperation::visibility)
                    parameter(setOf(CMP, VAL), "isQuery", "Boolean", propertyExecution = MofOperation::isQuery)
                    parameter(setOf(CMP, VAL), "parameters", "List", propertyExecution = MofOperation::parameters) { typeArgument("MofParameter") }
                    parameter(setOf(CMP, VAL), "returnParameter", "MofParameter", propertyExecution = MofOperation::returnParameter)
                }
            }
            data("MofParameter", implementation = MofParameter::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofParameter::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofParameter::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, propertyExecution = MofParameter::typeXmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, propertyExecution = MofParameter::typeHref)
                    parameter(setOf(CMP, VAL), "direction", "String", propertyExecution = MofParameter::direction)
                }
            }
            data("MofAssociation", implementation = MofAssociation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", propertyExecution = MofAssociation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", propertyExecution = MofAssociation::xmiId)
                    parameter(setOf(REF, VAL), "memberEnds", "List", propertyExecution = MofAssociation::memberEnds) { typeArgument("MofProperty") }
                }
            }
            enum("MofAggregationKind", listOf("NONE", "SHARED", "COMPOSITE"), implementation = MofAggregationKind::class)
        }
    }

}
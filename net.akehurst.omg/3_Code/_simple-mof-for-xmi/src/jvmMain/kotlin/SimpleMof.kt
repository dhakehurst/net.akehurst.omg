/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.akehurst.omg._simple_mof_for_xmi

import net.akehurst.language.types.builder.typesDomain

object SimpleMof {

    val types = typesDomain("SimpleMof", true) {
        namespace("net.akehurst.omg.mof.gen.simple") {
            data("MofModel", implementation = MofModel::class) {
                constructor_ {
                    parameter(setOf(REF, VAL), "name", "String", propertyExecution = MofModel::name)
                }
                propertyOf(setOf(CMP, VAL), "subPackages", "List", execution = MofModel::subPackages) { typeArgument("MofPackage") }
                propertyOf(setOf(CMP, VAL), "packageList", "List", execution = MofModel::packageList) { typeArgument("MofPackage") }
                propertyOf(setOf(CMP, VAL), "classeList", "List", execution = MofModel::classList) { typeArgument("MofEnum") }
                propertyOf(setOf(CMP, VAL), "enumList", "List", execution = MofModel::enumList) { typeArgument("MofClass") }
                propertyOf(setOf(CMP, VAL), "associationList", "List", execution = MofModel::associationList) { typeArgument("MofAssociation") }
                propertyOf(setOf(DER), "allClasses", "List", execution = MofModel::allClasses) { typeArgument("MofClass") }
                propertyOf(setOf(DER), "genSubPackages", "List", execution = MofModel::genSubPackages) { typeArgument("MofPackage") }
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
                propertyOf(setOf(CMP, VAL), "subPackages", "List", execution = MofPackage::subPackages) { typeArgument("MofPackage") }
                propertyOf(setOf(DER), "genSubPackages", "List", execution = MofPackage::genSubPackages) { typeArgument("MofPackage") }
            }
            interface_("MofType", implementation = MofType::class) {
                propertyOf(setOf(VAL), "isAbstract", "Boolean", execution = MofType::isAbstract)
                propertyOf(setOf(VAL), "isPrimitive", "Boolean", execution = MofType::isPrimitive)
                propertyOf(setOf(VAL), "ownedAttribute", "List", execution = MofType::ownedAttribute) { typeArgument("MofProperty") }
                propertyOf(setOf(VAL), "allAttributes", "Set", execution = MofType::allAttributes) { typeArgument("MofProperty") }
                propertyOf(setOf(VAL), "ownedRedefiningAttribute", "List", execution = MofType::ownedRedefiningAttribute) { typeArgument("MofProperty") }
                propertyOf(setOf(VAL), "superTypes", "Set", execution = MofType::superTypes) { typeArgument("MofType") }
                propertyOf(setOf(VAL), "allSuperTypes", "Set", execution = MofType::allSuperTypes) { typeArgument("MofType") }
                propertyOf(setOf(VAL), "hasSubtypes", "Boolean", execution = MofType::hasSubtypes)
                propertyOf(setOf(VAL), "concreteSubclasses", "Set", execution = MofType::concreteSubclasses) { typeArgument("MofClass") }
            }
            data("MofEnum", implementation = MofEnum::class) {
                supertype("MofType")
            }
            data("MofInterface", implementation = MofInterface::class) {
                supertype("MofType")
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

                propertyOf(setOf(DER), "allNormalisedAttribute", "List", execution = MofClass::allNormalisedAttribute) {
                    typeArgument("MofClassAttributeImplInfo")
                }
                propertyOf(setOf(DER), "bridgingAttributes", "Set", execution = MofClass::bridgingAttributes) {
                    typeArgument("MofAttributeBridging")
                }
                propertyOf(setOf(DER), "allNormalisedAttribute2", "Map", execution = MofClass::allNormalisedAttribute2) {
                    typeArgument("MofClass")
                    typeArgument("List") {
                        typeArgument("MofProperty")
                    }
                }
                propertyOf(setOf(DER), "allCompositeAttribute", "List", execution = MofClass::allCompositeAttribute) { typeArgument("MofProperty") }
                propertyOf(setOf(DER), "allReferenceAttribute", "List", execution = MofClass::allReferenceAttribute) { typeArgument("MofProperty") }

            }
            data("MofClassAttributeImplInfo") {
                constructor_ {
                    parameter(setOf(CMP, VAL), "defClass", "MofClass", propertyExecution = MofClassAttributeImplInfo::defClass)
                    parameter(setOf(CMP, VAL), "attributes", "List", propertyExecution = MofClassAttributeImplInfo::attributes) { typeArgument("MofRedefinedAttributeImplInfo") }
                }
            }
            data("MofRedefinedAttributeImplInfo") {
                constructor_ {
                    parameter(setOf(CMP, VAL), "attribute", "MofProperty", propertyExecution = MofRedefinedAttributeImplInfo::attribute)
                    parameter(setOf(CMP, VAL), "bridges", "List", propertyExecution = MofRedefinedAttributeImplInfo::bridges) {
                        typeArgument("Pair") {
                            typeArgument("MofProperty")
                            typeArgument("MofProperty")
                        }
                    }
                }
            }
            data("MofAttributeBridging") {
                propertyOf(setOf(VAL, REF), "original", "MofProperty")
                propertyOf(setOf(VAL, REF), "redefinedBy", "List") { typeArgument("MofProperty") }
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
                propertyOf(setOf(DER), "isComposite", "Boolean", execution = MofProperty::isComposite)
                propertyOf(setOf(DER), "isReference", "Boolean", execution = MofProperty::isReference)
                propertyOf(setOf(DER), "allRedefinedProperty", "Set", execution = MofProperty::allRedefinedProperty) { typeArgument("MofProperty") }
                propertyOf(setOf(DER), "subsettingProperty", "Set", execution = MofProperty::subsettingProperty) { typeArgument("MofProperty") }
                propertyOf(setOf(DER), "subsettedProperty", "Set", execution = MofProperty::subsettedProperty) { typeArgument("MofProperty") }
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
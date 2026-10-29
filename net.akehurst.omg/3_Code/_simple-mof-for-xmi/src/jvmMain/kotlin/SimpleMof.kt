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

import net.akehurst.kotlinx.utils.cast
import net.akehurst.language.types.builder.typesDomain

object SimpleMof {

    val types = typesDomain("SimpleMof", true) {
        namespace("net.akehurst.omg.mof.gen.simple") {
            data("MofModel", implementation = MofModel::class) {
                constructor_ {
                    parameter(setOf(REF, VAL), "name", "String", accessor = MofModel::name)
                }
                propertyOfWithBinding(setOf(CMP, VAL), "subPackages", "List", accessor = MofModel::subPackages) { typeArgument("MofPackage") }
                propertyOfWithBinding(setOf(CMP, VAL), "packageList", "List", accessor = MofModel::packageList) { typeArgument("MofPackage") }
                propertyOfWithBinding(setOf(CMP, VAL), "classeList", "List", accessor = MofModel::classList) { typeArgument("MofEnum") }
                propertyOfWithBinding(setOf(CMP, VAL), "enumList", "List", accessor = MofModel::enumList) { typeArgument("MofClass") }
                propertyOfWithBinding(setOf(CMP, VAL), "associationList", "List", accessor = MofModel::associationList) { typeArgument("MofAssociation") }
                propertyOfWithBinding(setOf(DER), "allClasses", "List", accessor = MofModel::allClasses) { typeArgument("MofClass") }
                propertyOfWithBinding(setOf(DER), "genSubPackages", "List", accessor = MofModel::genSubPackages) { typeArgument("MofPackage") }
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
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofPackage::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofPackage::xmiId)
                    parameter(setOf(CMP, VAL), "classes", "List", accessor = MofPackage::classes) {
                        typeArgument("MofClass")
                    }
                    parameter(setOf(CMP, VAL), "associations", "List", accessor = MofPackage::associations) {
                        typeArgument("MofAssociation")
                    }
                    parameter(setOf(CMP, VAL), "packageImport", "List", accessor = MofPackage::packageImport) {
                        typeArgument("String")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, accessor = MofPackage::parentPackage)
                }
                propertyOf(setOf(DER), "qualifiedName", "String")
                propertyOf(setOf(DER), "allImport", "List") { typeArgument("String") }
                propertyOfWithBinding(setOf(CMP, VAL), "subPackages", "List", accessor = MofPackage::subPackages) { typeArgument("MofPackage") }
                propertyOfWithBinding(setOf(DER), "genSubPackages", "List", accessor = MofPackage::genSubPackages) { typeArgument("MofPackage") }
            }
            interface_("MofType", implementation = MofType::class) {
                propertyOfWithBinding(setOf(VAL), "isAbstract", "Boolean", accessor = MofType::isAbstract)
                propertyOfWithBinding(setOf(VAL), "isPrimitive", "Boolean", accessor = MofType::isPrimitive)
                propertyOfWithBinding(setOf(VAL), "ownedAttribute", "List", accessor = MofType::ownedAttribute) { typeArgument("MofProperty") }
                //propertyOfWithBinding(setOf(VAL), "allAttributes", "Set", accessor = MofType::allOwnedAttributes) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(VAL), "ownedRedefiningAttribute", "List", accessor = MofType::ownedRedefiningAttribute) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(VAL), "superTypes", "Set", accessor = MofType::superTypes) { typeArgument("MofType") }
                propertyOfWithBinding(setOf(VAL), "allSuperTypes", "Set", accessor = MofType::allSuperTypes) { typeArgument("MofType") }
                propertyOfWithBinding(setOf(VAL), "hasSubtypes", "Boolean", accessor = MofType::hasSubtypes)
                propertyOfWithBinding(setOf(VAL), "concreteSubclasses", "Set", accessor = MofType::concreteSubclasses) { typeArgument("MofClass") }
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
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofClass::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofClass::xmiId)
                    parameter(setOf(CMP, VAL), "isAbstract", "Boolean", accessor = MofClass::isAbstract)
                    parameter(setOf(CMP, VAL), "generalizations", "List", accessor = MofClass::generalizations) {
                        typeArgument("String")
                    }
                    parameter(setOf(CMP, VAL), "attributes", "List", accessor = MofClass::ownedAttribute) {
                        typeArgument("MofProperty")
                    }
                    parameter(setOf(CMP, VAL), "operations", "List", accessor = MofClass::operations) {
                        typeArgument("MofOperation")
                    }
                    parameter(setOf(REF, VAL), "parentPackage", "MofPackage", true, accessor = MofClass::parentPackage) {}
                }

                propertyOfWithBinding(setOf(DER), "allNormalisedOwnedAttribute", "List", accessor = MofClass::allNormalisedOwnedAttribute) {
                    typeArgument("MofClassAttributeImplInfo")
                }
                propertyOfWithBinding(setOf(DER), "bridgingOwnedAttributes", "Set", accessor = MofClass::bridgingOwnedAttributes) {
                    typeArgument("MofAttributeBridging")
                }
                propertyOfWithBinding(setOf(DER), "allNormalisedAssociationOwnedAttribute", "List", accessor = MofClass::allNormalisedAssociationOwnedAttribute) {
                    typeArgument("MofClassAttributeImplInfo")
                }
                propertyOfWithBinding(setOf(DER), "bridgingAssociationOwnedAttributes", "Set", accessor = MofClass::bridgingAssociationOwnedAttributes) {
                    typeArgument("MofAttributeBridging")
                }
                propertyOfWithBinding(setOf(DER), "allCompositeAttribute", "List", accessor = MofClass::allCompositeAttribute) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(DER), "allReferenceAttribute", "List", accessor = MofClass::allReferenceAttribute) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(DER), "allAssociationOwnedAttribute", "Set", accessor = MofClass::allAssociationOwnedAttribute) { typeArgument("MofProperty") }

                propertyOfWithBinding(setOf(DER), "allNormalisedAttribute", "List", accessor = MofClass::allNormalisedAttribute) { typeArgument("MofClassAttributeImplInfo") }
                propertyOfWithBinding(setOf(DER), "bridgingAttributes", "Set", accessor = MofClass::bridgingAttributes) { typeArgument("MofAttributeBridging") }

            }
            data("MofClassAttributeImplInfo") {
                constructor_ {
                    parameter(setOf(CMP, VAL), "defClass", "MofClass", accessor = MofClassAttributeImplInfo::defClass)
                    parameter(setOf(CMP, VAL), "attributes", "List", accessor = MofClassAttributeImplInfo::attributes) { typeArgument("MofRedefinedAttributeImplInfo") }
                }
            }
            data("MofRedefinedAttributeImplInfo") {
                constructor_ {
                    parameter(setOf(CMP, VAL), "attribute", "MofProperty", accessor = MofRedefinedAttributeImplInfo::attribute)
                    parameter(setOf(CMP, VAL), "bridges", "List", accessor = MofRedefinedAttributeImplInfo::bridges) {
                        typeArgument("Pair") {
                            typeArgument("MofProperty")
                            typeArgument("MofProperty")
                        }
                    }
                }
            }
            data("MofUnionImpl") {
                propertyOf(setOf(VAL, REF), "union", "MofProperty")
                propertyOf(setOf(VAL, REF), "subsetters", "List") { typeArgument("MofProperty") }
            }
            data("MofAttributeBridging") {
                propertyOf(setOf(VAL, REF), "original", "MofProperty")
                propertyOf(setOf(VAL, REF), "redefinedBy", "List") { typeArgument("MofProperty") }
            }
            data("MofProperty", implementation = MofProperty::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofProperty::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, accessor = MofProperty::xmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, accessor = MofProperty::typeHref)
                    parameter(setOf(CMP, VAL), "lowerBound", "Integer", accessor = MofProperty::lowerBound)
                    parameter(setOf(CMP, VAL), "upperBound", "Integer", accessor = MofProperty::upperBound)
                    parameter(setOf(CMP, VAL), "isDerived", "Boolean", accessor = MofProperty::isDerived)
                    parameter(setOf(CMP, VAL), "isReadOnly", "Boolean", accessor = MofProperty::isReadOnly)
                    parameter(setOf(CMP, VAL), "isUnique", "Boolean", accessor = MofProperty::isUnique)
                    parameter(setOf(CMP, VAL), "isOrdered", "Boolean", accessor = MofProperty::isOrdered)
                    parameter(setOf(CMP, VAL), "aggregation", "MofAggregationKind", accessor = MofProperty::aggregation)
                    parameter(setOf(CMP, VAL), "associationXmiId", "String", true, accessor = MofProperty::associationXmiId)
                }
                propertyOfWithBinding(setOf(DER), "isOverride", "Boolean", accessor = MofProperty::isOverride)
                propertyOfWithBinding(setOf(DER), "isComposite", "Boolean", accessor = MofProperty::isComposite)
                propertyOfWithBinding(setOf(DER), "isReference", "Boolean", accessor = MofProperty::isReference)
                propertyOfWithBinding(setOf(DER), "allRedefinedProperty", "Set", accessor = MofProperty::allRedefinedProperty) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(DER), "subsettingProperty", "Set", accessor = MofProperty::subsettingProperty) { typeArgument("MofProperty") }
                propertyOfWithBinding(setOf(DER), "subsettedProperty", "Set", accessor = MofProperty::subsettedProperty) { typeArgument("MofProperty") }
                methodPrimitive("validSubset", "Boolean", false) {
                    parameter("other", "MofProperty")
                    execution { self, args ->  self.cast<MofProperty>().validSubset(args[0]!!.cast<MofProperty>())}
                }
                methodPrimitive("invalidSubset", "Boolean", false) {
                    parameter("other", "MofProperty")
                    execution { self, args ->  self.cast<MofProperty>().invalidSubset(args[0]!!.cast<MofProperty>())}
                }
            }
            data("MofOperation", implementation = MofOperation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofOperation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofOperation::xmiId)
                    parameter(setOf(CMP, VAL), "visibility", "String", accessor = MofOperation::visibility)
                    parameter(setOf(CMP, VAL), "isQuery", "Boolean", accessor = MofOperation::isQuery)
                    parameter(setOf(CMP, VAL), "parameters", "List", accessor = MofOperation::parameters) { typeArgument("MofParameter") }
                    parameter(setOf(CMP, VAL), "returnParameter", "MofParameter", accessor = MofOperation::returnParameter)
                }
            }
            data("MofParameter", implementation = MofParameter::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofParameter::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofParameter::xmiId)
                    parameter(setOf(CMP, VAL), "typeXmiId", "String", true, accessor = MofParameter::typeXmiId)
                    parameter(setOf(CMP, VAL), "typeHref", "String", true, accessor = MofParameter::typeHref)
                    parameter(setOf(CMP, VAL), "direction", "String", accessor = MofParameter::direction)
                }
            }
            data("MofAssociation", implementation = MofAssociation::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL), "name", "String", accessor = MofAssociation::name)
                    parameter(setOf(CMP, VAL), "xmiId", "String", accessor = MofAssociation::xmiId)
                    parameter(setOf(REF, VAL), "memberEnds", "List", accessor = MofAssociation::memberEnds) { typeArgument("MofProperty") }
                }
            }
            enum("MofAggregationKind", listOf("NONE", "SHARED", "COMPOSITE"), implementation = MofAggregationKind::class)
        }
    }

}
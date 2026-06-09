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

package net.akehurst.omg._simple_mof_for_xmi.code

import net.akehurst.language.types.builder.typesDomain

object Code {
    val domain = typesDomain("Code", true) {
        namespace("net.akehurst.omg.code") {
            interface_("TypeDefinition", implementation = TypeDefinition::class) {
                propertyOf(setOf(CMP,VAL),"name", "String", execution = TypeDefinition::name)
            }
            data("TypeReference", implementation = TypeReference::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL),"name", "String", propertyExecution = TypeReference::name)
                    parameter(setOf(CMP, VAL),"argument", "List", propertyExecution = TypeReference::argument) { typeArgument("TypeReference")}
                }
            }
            data("Enumeration", implementation = Enumeration::class) {
                supertype("TypeDefinition")
                constructor_ {
                    parameter(setOf(CMP, VAL),"name", "String", propertyExecution = Enumeration::name)
                }
                propertyOf(setOf(CMP,VAL),"literals", "List", execution = Enumeration::literals) {
                    typeArgument("String")
                }
            }
            data("Extends", implementation = Extends::class) {
                constructor_ {
                    parameter(setOf(CMP, VAL),"name", "String", propertyExecution = Extends::name)
                }
            }
            data("Interface", implementation = Interface::class) {
                supertype("TypeDefinition")
                constructor_ {
                    parameter(setOf(CMP, VAL),"name", "String", propertyExecution = Interface::name)
                }
                propertyOf(setOf(CMP, VAR),"extends","List",execution = Interface::extends) { typeArgument("Extends") }
                propertyOf(setOf(CMP, VAR),"property","Map",execution = Interface::property) {
                    typeArgument("String")
                    typeArgument("Property")
                }
            }
            data("ConcreteClass", implementation = ConcreteClass::class) {
                supertype("TypeDefinition")
                constructor_ {
                    parameter(setOf(CMP, VAL),"name", "String", propertyExecution = ConcreteClass::name)
                }
                propertyOf(setOf(CMP, VAR),"property","Map",execution = ConcreteClass::property) {
                    typeArgument("String")
                    typeArgument("Property")
                }
                propertyOf(setOf(CMP, VAR),"property","Property",execution = ConcreteClass::identityProperty)
            }

            interface_("Property", implementation = Property::class) {
                propertyOf(setOf(CMP, VAL),"name","String", execution = Property::name)
                propertyOf(setOf(CMP, VAL),"type","TypeReference", execution = Property::type)
            }
            data("PropertyStored", implementation = PropertyStored::class) {
                supertype("Property")
            }
            data("PropertyDerived", implementation = PropertyDerived::class) {
                supertype("Property")
            }
            data("PropertySubset", implementation = PropertySubset::class) {
                supertype("Property")
            }
            data("PropertyDerivedUnion", implementation = PropertyDerivedUnion::class) {
                supertype("Property")
            }
        }
    }
}

interface TypeDefinition {
    val name: String
}

data class TypeReference(
    val name: String,
    val argument: List<TypeReference>
)

data class Enumeration(
    override val name: String
) : TypeDefinition {
    val literals = mutableListOf<String>()
}

data class Extends(
    val name: String
)

data class Interface(
    override val name: String
) : TypeDefinition {
    val extends = mutableListOf<Extends>()
    val property = mutableMapOf<String, Property>()
}

data class ConcreteClass(
    override val name: String
) : TypeDefinition {
    val extends = mutableListOf<Extends>()
    val property = mutableMapOf<String, Property>()
    var identityProperty: Property? = null
}

interface Property {
    val name: String
    val type: TypeReference
}

data class PropertyStored(
    override val name: String,
) : Property {
    override lateinit var type: TypeReference
}

data class PropertyDerived(
    override val name: String,
) : Property {
    override lateinit var type: TypeReference
    var expression: String? = null
}

data class PropertySubset(
    override val name: String,
) : Property {
    override lateinit var type: TypeReference
}

data class PropertyDerivedUnion(
    override val name: String,
) : Property {
    override lateinit var type: TypeReference
    val unionOf = mutableMapOf<String, Property>()
}
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

package net.akehurst.omg.uml.v2_5_1

import net.akehurst.omg.uml.v2_5_1.api.ReflectiveFeatureKind
import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.Generalization
import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.InstanceValue
import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.Property
import net.akehurst.omg.uml.v2_5_1.api.UML.CommonStructure.VisibilityKind
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Model
import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.DataType
import net.akehurst.omg.uml.v2_5_1.api.UML.SimpleClassifiers.Enumeration
import net.akehurst.omg.uml.v2_5_1.api.UML.Classification.AggregationKind
import net.akehurst.omg.uml.v2_5_1.api.UML.StructuredClassifiers.Class
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralInteger
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralReal
import net.akehurst.omg.uml.v2_5_1.api.UML.Values.LiteralUnlimitedNatural
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelAsString
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelBuilder
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelReflective
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelReflectiveDefault
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelResolver
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class test_UML_ModelReflective {

    private fun create(r: UML_ModelReflective, metaclass: String, id: String, name: String? = null): Any {
        val obj = assertNotNull(r.construct(metaclass, id), "construct $metaclass")
        if (null != name) assertTrue(r.setValue(obj, "name", name), "name of $id")
        return obj
    }

    private fun contain(r: UML_ModelReflective, owner: Any, feature: String, child: Any) =
        assertTrue(r.addContained(owner, feature, child), "addContained $feature")

    /** Model > Package > Class 'Spacecraft' (with a property 'mass', typed by a DataType) built only through the reflective API */
    private fun buildSpacecraft(r: UML_ModelReflective): Any {
        val model = create(r, "Model", "m", "Testsat")
        val pkg = create(r, "Package", "p", "Structure")
        contain(r, model, "packagedElement", pkg)

        val kg = create(r, "DataType", "dt_kg", "Kilogram")
        contain(r, pkg, "packagedElement", kg)

        val mode = create(r, "Enumeration", "en_mode", "Mode")
        contain(r, pkg, "packagedElement", mode)
        contain(r, mode, "ownedLiteral", create(r, "EnumerationLiteral", "lit_safe", "SAFE"))
        contain(r, mode, "ownedLiteral", create(r, "EnumerationLiteral", "lit_nominal", "NOMINAL"))

        val vehicle = create(r, "Class", "c_vehicle", "Vehicle")
        assertTrue(r.setValue(vehicle, "isAbstract", "true"))
        contain(r, pkg, "packagedElement", vehicle)

        val spacecraft = create(r, "Class", "c_spacecraft", "Spacecraft")
        contain(r, pkg, "packagedElement", spacecraft)
        val comment = create(r, "Comment", "cmt")
        assertTrue(r.setValue(comment, "body", "a mock spacecraft"))
        contain(r, spacecraft, "ownedComment", comment)   // inherited from Element

        val gen = create(r, "Generalization", "g")
        contain(r, spacecraft, "generalization", gen)
        assertTrue(r.setReference(gen, "general", "c_vehicle"))

        val mass = create(r, "Property", "p_mass", "mass")
        contain(r, spacecraft, "ownedAttribute", mass)
        assertTrue(r.setReference(mass, "type", "dt_kg"))
        assertTrue(r.setValue(mass, "visibility", "private"))      // enumeration literal by name
        assertTrue(r.setValue(mass, "aggregation", AggregationKind.composite))  // or as the enum value
        val massDefault = create(r, "LiteralReal", "lr")
        assertTrue(r.setValue(massDefault, "value", "1200.5"))
        contain(r, mass, "defaultValue", massDefault)
        val lower = create(r, "LiteralInteger", "lo")
        assertTrue(r.setValue(lower, "value", 1))
        contain(r, mass, "lowerValue", lower)
        val upper = create(r, "LiteralUnlimitedNatural", "up")
        assertTrue(r.setValue(upper, "value", "*"))
        contain(r, mass, "upperValue", upper)

        val modeProp = create(r, "Property", "p_mode", "mode")
        contain(r, spacecraft, "ownedAttribute", modeProp)
        assertTrue(r.setReference(modeProp, "type", "en_mode"))
        val modeDefault = create(r, "InstanceValue", "iv")
        assertTrue(r.setReference(modeDefault, "instance", "lit_safe"))
        contain(r, modeProp, "defaultValue", modeDefault)

        return model
    }

    @Test
    fun build_model_then_resolve_and_check_with_typed_api() {
        val factory = UML_ModelFactoryRam("test")
        val r = UML_ModelReflectiveDefault(factory)
        val model = buildSpacecraft(r) as Model
        UML_ModelResolver("UMLModel", factory).UML.Packages.Model_resolve(model)

        assertEquals("Testsat", model.name)
        val pkg = model.packagedElementSet.single() as Package
        assertEquals("Structure", pkg.name)
        val kg = pkg.packagedElementSet.filterIsInstance<DataType>().single { it !is Enumeration }
        val mode = pkg.packagedElementSet.filterIsInstance<Enumeration>().single()
        val vehicle = pkg.packagedElementSet.filterIsInstance<Class>().single { it.name == "Vehicle" }
        val spacecraft = pkg.packagedElementSet.filterIsInstance<Class>().single { it.name == "Spacecraft" }

        assertEquals(true, vehicle.isAbstract)
        assertEquals(listOf("SAFE", "NOMINAL"), mode.ownedLiteralOrderedSet.map { it.name })
        assertEquals("a mock spacecraft", spacecraft.ownedCommentSet.single().body)

        // Generalization.general resolved, Classifier.general derived from it
        val gen = spacecraft.generalizationSet.single()
        assertSame(vehicle, gen.general)
        assertSame(spacecraft, gen.specific)
        assertTrue(spacecraft.generalSet.contains(vehicle))

        val mass = spacecraft.ownedAttributeOrderedSet.single { it.name == "mass" }
        assertSame(kg, mass.type)
        assertEquals(VisibilityKind.private, mass.visibility)
        assertEquals(AggregationKind.composite, mass.aggregation)
        assertEquals(1200.5, (mass.defaultValue as LiteralReal).value)
        assertEquals(1L, (mass.lowerValue as LiteralInteger).value)
        assertEquals(-1L, (mass.upperValue as LiteralUnlimitedNatural).value)

        val modeProp = spacecraft.ownedAttributeOrderedSet.single { it.name == "mode" }
        assertSame(mode, modeProp.type)
        assertSame(mode.ownedLiteralOrderedSet.first(), (modeProp.defaultValue as InstanceValue).instance)

        // subsets and derived unions are kept as when set through the typed api
        assertTrue(spacecraft.ownedMemberSet.contains(mass))
        assertTrue(spacecraft.ownedElementSet.contains(gen))
        assertSame(spacecraft, mass.owner)
        assertTrue(pkg.ownedTypeSet.contains(spacecraft))
    }

    @Test
    fun same_as_typed_builder() {
        val typed = UML_ModelBuilder.Model(UML_ModelFactoryRam("typed"), "m") {
            name("Testsat")
            packagedElementSet {
                Package("p") {
                    name("Structure")
                    packagedElementSet {
                        Class("c") {
                            name("Spacecraft")
                            ownedAttributeOrderedSet {
                                Property("a") { name("mass") }
                            }
                        }
                    }
                }
            }
        }

        val r = UML_ModelReflectiveDefault(UML_ModelFactoryRam("reflective"))
        val model = create(r, "Model", "m", "Testsat")
        val pkg = create(r, "Package", "p", "Structure")
        contain(r, model, "packagedElement", pkg)
        val cls = create(r, "Class", "c", "Spacecraft")
        contain(r, pkg, "packagedElement", cls)
        contain(r, cls, "ownedAttribute", create(r, "Property", "a", "mass"))

        assertEquals(UML_ModelAsString.Model_asString(typed), UML_ModelAsString.Model_asString(model as Model))
    }

    @Test
    fun featureKind_metaclassOf_superclasses() {
        val r = UML_ModelReflectiveDefault(UML_ModelFactoryRam("test"))

        val name = assertNotNull(r.featureKind("Class", "name"))
        assertEquals(ReflectiveFeatureKind.VALUE, name.kind)
        assertEquals("NamedElement", name.owner)
        assertEquals("String", name.typeName)
        assertFalse(name.isMany)
        assertTrue(name.isSettable)

        val comment = assertNotNull(r.featureKind("Class", "ownedComment"))
        assertEquals(ReflectiveFeatureKind.CONTAINED, comment.kind)
        assertTrue(comment.isMany)
        assertEquals("Comment", comment.typeName)

        val type = assertNotNull(r.featureKind("Property", "type"))
        assertEquals(ReflectiveFeatureKind.REFERENCE, type.kind)
        assertEquals("Type", type.typeName)

        assertEquals("UnlimitedNatural", r.featureKind("LiteralUnlimitedNatural", "value")?.typeName)
        assertEquals("VisibilityKind", r.featureKind("Property", "visibility")?.typeName)

        val general = assertNotNull(r.featureKind("Class", "general"))
        assertTrue(general.isDerived)
        assertFalse(general.isSettable)

        // abstract metaclasses too
        assertEquals(ReflectiveFeatureKind.VALUE, r.featureKind("NamedElement", "name")?.kind)

        val cls = r.construct("Class", "c")!!
        assertEquals("Class", r.metaclassOf(cls))
        assertEquals("Stereotype", r.metaclassOf(r.construct("Stereotype", "s")!!))
        assertNull(r.metaclassOf("not a model object"))

        val sup = r.superclasses("Class")
        assertTrue(sup.containsAll(listOf("EncapsulatedClassifier", "BehavioredClassifier", "Classifier", "Namespace", "NamedElement")))
        assertEquals("Element", sup.last())
        assertTrue(sup.indexOf("Classifier") < sup.indexOf("Namespace"))
        assertEquals(emptyList(), r.superclasses("Element"))
        assertEquals(emptyList(), r.superclasses("NoSuchMetaclass"))
    }

    @Test
    fun unknown_names_and_wrong_values_give_null_or_false() {
        val r = UML_ModelReflectiveDefault(UML_ModelFactoryRam("test"))
        assertNull(r.construct("NoSuchMetaclass", "x"))
        assertNull(r.construct("Classifier", "x"))   // abstract

        val cls = r.construct("Class", "c")!!
        val comment = r.construct("Comment", "cm")!!
        assertFalse(r.setValue(cls, "noSuchFeature", "x"))
        assertFalse(r.setValue(cls, "name", 42))                  // wrong type
        assertFalse(r.setValue(cls, "isAbstract", "maybe"))       // not a Boolean
        assertFalse(r.setValue(cls, "visibility", "nowhere"))     // no such literal
        assertFalse(r.setValue(cls, "ownedComment", "x"))         // not a VALUE feature
        assertFalse(r.addContained(cls, "ownedAttribute", comment))  // a Comment is not a Property
        assertFalse(r.addContained(cls, "noSuchFeature", comment))
        assertFalse(r.addContained(cls, "general", comment))      // derived
        assertFalse(r.setReference(cls, "noSuchFeature", "id"))
        assertFalse(r.setReference(cls, "name", "id"))            // not a REFERENCE feature
        assertFalse(r.addReference(cls, "general", "id"))         // derived
        assertFalse(r.setValue("not a model object", "name", "x"))
        assertNull(r.featureKind("Class", "noSuchFeature"))
        assertNull(r.featureKind("NoSuchMetaclass", "name"))
    }

    @Test
    fun unresolvable_reference_stays_unresolved() {
        val factory = UML_ModelFactoryRam("test")
        val r = UML_ModelReflectiveDefault(factory)
        val model = create(r, "Model", "m", "M") as Model
        val cls = create(r, "Class", "c", "C") as Class
        contain(r, model, "packagedElement", cls)
        val p = create(r, "Property", "p", "p") as Property
        contain(r, cls, "ownedAttribute", p)
        assertTrue(r.setReference(p, "type", "no_such_id"))
        assertTrue(r.addReference(cls, "useCase", "no_such_use_case"))

        UML_ModelResolver("UMLModel", factory).UML.Packages.Model_resolve(model)

        assertEquals("no_such_id", p.typeReference.reference)
        assertNull(p.typeReference.resolved)
        assertNull(p.type)
        val uc = cls.useCaseSetReference.single()
        assertEquals("no_such_use_case", uc.reference)
        assertNull(uc.resolved)
    }
}

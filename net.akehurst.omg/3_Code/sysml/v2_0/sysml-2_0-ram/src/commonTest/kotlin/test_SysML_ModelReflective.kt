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

import net.akehurst.omg.sysml.v2_0.api.KerML.Core.Classifiers.Subclassification
import net.akehurst.omg.sysml.v2_0.api.KerML.Kernel.Packages.Package
import net.akehurst.omg.sysml.v2_0.api.ReflectiveFeatureKind
import net.akehurst.omg.sysml.v2_0.api.SysML.Systems.Parts.PartDefinition
import net.akehurst.omg.sysml.v2_0.api.SysML_ModelReflectiveDefault
import net.akehurst.omg.sysml.v2_0.api.SysML_ModelResolver
import net.akehurst.omg.sysml.v2_0.ram.SysML_ModelFactoryRam
import kotlin.test.*

// the reflective (by-name) API on SysML v2; the UML tests are in uml-2_5_1-ram
class test_SysML_ModelReflective {

    @Test
    fun build_by_name() {
        val factory = SysML_ModelFactoryRam("test")
        val r = SysML_ModelReflectiveDefault(factory)
        fun owned(owner: Any, child: Any, id: String) {
            val m = r.construct("OwningMembership", id)!!
            assertTrue(r.addContained(owner, "ownedRelationship", m))
            assertTrue(r.addContained(m, "ownedRelatedElement", child))
        }

        val pkg = r.construct("Package", "pkg")!!
        assertTrue(r.setValue(pkg, "declaredName", "Testsat"))
        val vehicle = r.construct("PartDefinition", "pd_vehicle")!!
        assertTrue(r.setValue(vehicle, "declaredName", "Vehicle"))
        assertTrue(r.setValue(vehicle, "isAbstract", "true"))
        owned(pkg, vehicle, "m1")
        val spacecraft = r.construct("PartDefinition", "pd_spacecraft")!!
        assertTrue(r.setValue(spacecraft, "declaredName", "Spacecraft"))
        owned(pkg, spacecraft, "m2")
        val sub = r.construct("Subclassification", "sub")!!
        assertTrue(r.addContained(spacecraft, "ownedRelationship", sub))
        assertTrue(r.setReference(sub, "general", "pd_vehicle"))   // by the name of a redefined feature (superclassifier)
        assertTrue(r.setReference(sub, "subclassifier", "pd_spacecraft"))

        SysML_ModelResolver("test", factory).KerML.Kernel.Packages.Package_resolve(pkg as Package)

        assertEquals(listOf("Vehicle", "Spacecraft"), pkg.ownedMemberOrderedSet.map { it.name })
        val sc = spacecraft as PartDefinition
        assertEquals(true, (vehicle as PartDefinition).isAbstract)
        assertSame(vehicle, (sub as Subclassification).superclassifier)
        assertSame(sc, sub.subclassifier)

        assertEquals("PartDefinition", r.metaclassOf(sc))
        assertEquals("Element", r.superclasses("PartDefinition").last())
        assertEquals(ReflectiveFeatureKind.VALUE, r.featureKind("PartDefinition", "declaredName")?.kind)
        assertEquals(true, r.featureKind("PartDefinition", "name")?.isDerived)
        assertFalse(r.setValue(sc, "name", "x"))   // derived
        assertNull(r.construct("Relationship", "r"))  // abstract
    }
}

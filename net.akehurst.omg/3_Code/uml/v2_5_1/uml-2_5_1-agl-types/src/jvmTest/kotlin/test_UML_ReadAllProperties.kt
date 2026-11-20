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

package net.akehurst.omg.uml.v2_5_1.agl.types

import net.akehurst.language.types.api.DataType
import net.akehurst.language.types.api.InterfaceType
import net.akehurst.language.types.api.TypeDefinition
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelReflectiveDefault
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import net.akehurst.omg.uml.v2_5_1.xmi.UmlXmiReader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Reads every property (stored and derived) of every element of some sample models through the AGL types
 * (as AGL reflection does: the property's accessor), so derivations and operations that are not implemented,
 * or that fail on real models, show up. Collections are iterated, because derived ones compute on read.
 */
class test_UML_ReadAllProperties {

    private val types = UMLTypesDomain().types
    private val defs: Map<String, TypeDefinition> = types.namespace.flatMap { it.ownedTypes }
        .filter { it is DataType || it is InterfaceType }
        .associateBy { it.name.value }
    private val reflective = UML_ModelReflectiveDefault(UML_ModelFactoryRam("UML"))

    /** "Metaclass.property" -> the first failure found reading it */
    private fun readAll(elements: Collection<Any>): Map<String, String> {
        val failures = sortedMapOf<String, String>()
        for (o in elements) {
            val mc = reflective.metaclassOf(o) ?: continue
            val def = defs[mc] ?: fail("no AGL type for metaclass $mc")
            for (p in def.type().allResolvedProperty.values) {
                val key = "$mc.${p.name.value}"
                if (key in failures) continue
                // a resolved (inherited) property does not carry the accessor, its original does
                val accessor = p.accessor ?: p.original.accessor ?: fail("no accessor for $key")
                try {
                    when (val v = accessor.invoke(o)) {
                        is Iterable<*> -> v.forEach { it }
                        else -> v
                    }
                } catch (t: Throwable) {
                    failures[key] = "${t::class.simpleName}: ${t.message?.lineSequence()?.first()}"
                }
            }
        }
        return failures
    }

    private fun readXmi(uri: String, path: String): Collection<Any> {
        val r = UmlXmiReader().read(uri, File(path).readText())
        assertTrue(r.errors.isEmpty(), "errors reading $path: ${r.errors.take(5)}")
        return r.document.elementsById.values
    }

    /** one object of every concrete metaclass, with nothing set */
    private fun emptyObjects(): List<Any> = defs.keys
        .filter { reflective.isAbstract(it) == false }
        .mapNotNull { reflective.construct(it, "_empty_$it") }

    private fun report(name: String, failures: Map<String, String>) {
        println("== $name: ${failures.size} properties fail")
        failures.forEach { (k, v) -> println("$k -> $v") }
    }

    @Test
    fun empty_objects() {
        val objs = emptyObjects()
        assertTrue(objs.size > 100, "${objs.size} concrete metaclasses")
        val f = readAll(objs)
        report("empty objects (${objs.size})", f)
        assertTrue(f.isEmpty(), "${f.size} properties fail, see output")
    }

    @Test
    fun sysml_1_7_profile() {
        val f = readAll(readXmi("https://www.omg.org/spec/SysML/20240101/SysML.xmi", "../../../sysml/v1_7/specs/SysML_1-7_ptc-24-01-02.xmi"))
        report("SysML 1.7 profile", f)
        assertTrue(f.isEmpty(), "${f.size} properties fail, see output")
    }
}

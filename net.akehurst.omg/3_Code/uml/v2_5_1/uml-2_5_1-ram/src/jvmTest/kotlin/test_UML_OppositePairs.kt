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

import net.akehurst.omg.uml.v2_5_1.api.ReflectiveFeature
import net.akehurst.omg.uml.v2_5_1.api.ReflectiveFeatureKind
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelReflective
import net.akehurst.omg.uml.v2_5_1.api.UML_ModelReflectiveDefault
import net.akehurst.omg.uml.v2_5_1.ram.UML_ModelFactoryRam
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every pair of opposite navigable properties of the UML metamodel (two class-owned ends of one association),
 * for every pair of concrete metaclasses that have them, is set from both ends through the reflective API,
 * in both orders, as an XMI file written by a tool does (e.g. Lifeline.coveredBy and InteractionFragment.covered).
 * Each end must then hold the other object exactly once.
 */
class test_UML_OppositePairs {

    private class MetaProp(val owner: String, val name: String, val type: String, val association: String?)

    private class Metamodel(val classes: Map<String, Boolean>, val generals: Map<String, List<String>>, val props: List<MetaProp>) {
        private fun allSupers(c: String): Set<String> = generals[c].orEmpty().flatMap { allSupers(it) + it }.toSet()
        fun concreteSubclassesOf(c: String): List<String> = classes.filter { (n, abs) -> !abs && (n == c || c in allSupers(n)) }.keys.sorted()
    }

    private fun readMetamodel(): Metamodel {
        val f = File("../specs/UML_2-5-1_ASM_ptc-18-01-01.xmi")
        val doc = DocumentBuilderFactory.newInstance().also { it.isNamespaceAware = true }.newDocumentBuilder().parse(f)
        val xmiNs = "http://www.omg.org/spec/XMI/20131001"
        val classes = mutableMapOf<String, Boolean>()
        val generals = mutableMapOf<String, List<String>>()
        val props = mutableListOf<MetaProp>()
        val pes = doc.getElementsByTagName("packagedElement")
        for (i in 0 until pes.length) {
            val pe = pes.item(i) as Element
            if (pe.getAttributeNS(xmiNs, "type") != "uml:Class") continue
            val name = pe.getAttribute("name")
            classes[name] = pe.getAttribute("isAbstract") == "true"
            val children = pe.childNodes
            val gens = mutableListOf<String>()
            for (j in 0 until children.length) {
                val ch = children.item(j) as? Element ?: continue
                when (ch.tagName) {
                    "generalization" -> gens.add(ch.getAttribute("general"))
                    "ownedAttribute" -> props.add(MetaProp(name, ch.getAttribute("name"), ch.getAttribute("type"), ch.getAttribute("association").ifEmpty { null }))
                }
            }
            generals[name] = gens
        }
        return Metamodel(classes, generals, props)
    }

    /** the value(s) of [feature] of [obj], read through the generated Kotlin property */
    private fun valuesOf(obj: Any, feature: String): List<Any?>? {
        // a Kotlin keyword (e.g. 'class') is generated with a trailing '_'
        val names = listOf(feature + "_", feature).map { it.replaceFirstChar { c -> c.uppercase() } }
        val m = names.flatMap { n -> listOf("", "Set", "OrderedSet", "List", "Collection").map { s -> "get$n$s" } }
            .firstNotNullOfOrNull { g -> obj.javaClass.methods.firstOrNull { it.name == g && it.parameterCount == 0 } }
            ?: return null
        return when (val v = m.invoke(obj)) {
            null -> emptyList()
            is Collection<*> -> v.toList()
            else -> listOf(v)
        }
    }

    private fun set(r: UML_ModelReflective, obj: Any, f: ReflectiveFeature, id: String, value: Any): Boolean = when (f.kind) {
        ReflectiveFeatureKind.CONTAINED -> r.addContained(obj, f.name, value)
        ReflectiveFeatureKind.REFERENCE -> if (f.isMany) r.addReference(obj, f.name, id, value) else r.setReference(obj, f.name, id, value)
        ReflectiveFeatureKind.VALUE -> false
    }

    @Test
    fun opposite_pairs_set_from_both_ends() {
        val mm = readMetamodel()
        val pairs = mm.props.filter { null != it.association }.groupBy { it.association!! }.values.filter { it.size == 2 }
        val failures = mutableListOf<String>()
        var checked = 0
        val skipped = mutableMapOf<String, Int>()
        fun skip(reason: String) { skipped[reason] = (skipped[reason] ?: 0) + 1 }
        for ((pa, pb) in pairs) {
            for (ca in mm.concreteSubclassesOf(pa.owner)) {
                for (cb in mm.concreteSubclassesOf(pb.owner)) {
                    for (aFirst in listOf(true, false)) {
                        val r = UML_ModelReflectiveDefault(UML_ModelFactoryRam("UML"))
                        val fa = r.featureKind(ca, pa.name)
                        val fb = r.featureKind(cb, pb.name)
                        if (null == fa || null == fb) {
                            skip("no feature"); continue
                        }
                        if (!fa.isSettable || !fb.isSettable) {
                            skip("derived or read-only"); continue
                        }
                        // the type of a feature may be narrowed by redefinition in the concrete class
                        if (ca !in mm.concreteSubclassesOf(fb.typeName) || cb !in mm.concreteSubclassesOf(fa.typeName)) {
                            skip("type narrowed by redefinition"); continue
                        }
                        val a = r.construct(ca, "a")!!
                        val b = r.construct(cb, "b")!!
                        val what = "$ca.${pa.name} <-> $cb.${pb.name} (${if (aFirst) "a end first" else "b end first"})"
                        try {
                            val ok = if (aFirst) set(r, a, fa, "b", b) && set(r, b, fb, "a", a) else set(r, b, fb, "a", a) && set(r, a, fa, "b", b)
                            if (!ok) {
                                failures.add("$what: not set"); continue
                            }
                            val va = valuesOf(a, pa.name)
                            val vb = valuesOf(b, pb.name)
                            if (null == va || null == vb) {
                                failures.add("$what: no getter"); continue
                            }
                            checked++
                            val na = va.count { it === b }
                            val nb = vb.count { it === a }
                            if (na != 1 || nb != 1) failures.add("$what: ${pa.name} holds b $na times, ${pb.name} holds a $nb times")
                        } catch (e: Throwable) {
                            failures.add("$what: ${e::class.simpleName}: ${e.message}")
                        }
                    }
                }
            }
        }
        println("opposite pairs: ${pairs.size}, checked: $checked, skipped: $skipped, failures: ${failures.size}")
        failures.forEach { println("  $it") }
        assertTrue(checked > 0)
        assertEquals(emptyList(), failures)
    }
}

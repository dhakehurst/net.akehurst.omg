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


import net.akehurst.omg.kerml.v1_0.ram.KerML_ModelFactoryRam
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every class of the model is constructed and every property is read.
 * A redefined property is read through the property that redefines it, so a derivation that
 * uses the property it redefines would call itself without end. This test fails on any such cycle.
 * Other exceptions (e.g. a mandatory property that is not set) are expected on empty objects and ignored.
 */
class test_KerML_NoDerivationCycles {

    private fun constructors(factory: Any, visited: MutableSet<Any> = mutableSetOf()): List<Pair<Any, Method>> {
        if (!visited.add(factory)) return emptyList()
        val result = mutableListOf<Pair<Any, Method>>()
        for (m in factory.javaClass.methods) {
            if (m.parameterCount == 1 && m.name.endsWith("_construct")) result.add(Pair(factory, m))
            else if (m.parameterCount == 0 && m.name.startsWith("get") && m.returnType.simpleName.endsWith("PackageFactory")) {
                m.invoke(factory)?.let { result.addAll(constructors(it, visited)) }
            }
        }
        return result
    }

    private fun isGetter(m: Method) = m.parameterCount == 0 && !Modifier.isStatic(m.modifiers) &&
            (m.name.startsWith("get") || m.name.startsWith("is")) && m.name != "getClass"

    @Test
    fun no_derivation_calls_itself() {
        val factory = KerML_ModelFactoryRam("TestFactoryRam")
        val ctors = constructors(factory)
        assertTrue(ctors.size > 50, "found only ${ctors.size} classes")
        val cycles = mutableListOf<String>()
        var reads = 0
        ctors.forEachIndexed { i, (f, ctor) ->
            val obj = ctor.invoke(f, "obj$i")
            for (g in obj.javaClass.methods.filter { isGetter(it) }.sortedBy { it.name }) {
                reads++
                try {
                    val v = g.invoke(obj)
                    if (v is Iterable<*>) v.forEach { _ -> }
                } catch (e: Throwable) {
                    if (generateSequence(e) { it.cause }.any { it is StackOverflowError }) cycles.add("${ctor.name.removeSuffix("_construct")}.${g.name}")
                }
            }
        }
        assertTrue(reads > 1000, "read only $reads properties")
        assertEquals(emptyList(), cycles)
    }
}

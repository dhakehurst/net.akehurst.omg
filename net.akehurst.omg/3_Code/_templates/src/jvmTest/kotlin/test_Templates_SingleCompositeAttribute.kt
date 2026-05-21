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

package net.akehurst.omg.templates

import net.akehurst.omg._simple_mof_for_xmi.ExternalReferenceClass
import net.akehurst.omg._simple_mof_for_xmi.MofAggregationKind
import net.akehurst.omg._simple_mof_for_xmi.MofClass
import net.akehurst.omg._simple_mof_for_xmi.MofModel
import net.akehurst.omg._simple_mof_for_xmi.MofPackage
import net.akehurst.omg._simple_mof_for_xmi.MofProperty
import net.akehurst.omg.templates.test_Templates_SingleReferenceAttribute.Companion.model
import org.junit.jupiter.api.Test
import java.io.File

class test_Templates_SingleCompositeAttribute : test_TemplatesAbstract() {

    companion object {
        val xmiFile = "test"
        val model = MofModel("Test", listOf(), refHandler).also { mdl ->
            val rootPkg = MofPackage(mdl, "Root", "Root").also {
                mdl.addPackage(xmiFile, null, it)
            }
            val cls = MofClass(mdl, "TestClass", "TestClass").also {
                rootPkg.addClass(xmiFile, it)
                it.comment = "A test class"
            }
            mdl.refHandler.setRef("EXTERNAL", "ExtClass", ExternalReferenceClass(mdl, "ExtClass", "ExtClass"))
            val prop1 = MofProperty(mdl, "prop1", "prop1").also {
                it.typeXmiId = "ExtClass"
                it.lowerBound = 1
                it.upperBound = 1
                it.aggregation = MofAggregationKind.composite
                cls.addAttribute(xmiFile, it)
            }
            val prop2 = MofProperty(mdl, "prop2", "prop2").also {
                it.typeXmiId = "ExtClass"
                it.lowerBound = 0
                it.upperBound = 1
                it.aggregation = MofAggregationKind.composite
                cls.addAttribute(xmiFile, it)
            }
        }
    }

    @Test
    fun api_empty() {
        val template = File("api/EnumsAndInterfaces.agl-fmt").readText()
        val model = MofModel("Test", listOf(), refHandler)

        val expected = mapOf(
            "sentence" to ""
        )
        doTest(template, model, emptyMap(), expected)
    }

    @Test
    fun api_populated() {
        val xmiFile = "test"
        val template = File("api/EnumsAndInterfaces.agl-fmt").readText()
        val params = mapOf(
            "TARGET_PACKAGE" to "test"
        )
        val expected = mapOf(
            "api/Root.kt" to """
                // *** Generated code do NOT manually edit. ***
                package test.api
                
                import net.akehurst.kotlinx.collections.OrderedSet
                import net.akehurst.kotlinx.utils.Reference
                
                /**
                  * A test class
                  */
                interface TestClass {
                   val _identity: Any
                   /**
                    * prop1: ExtClass [1..1]{unique}
                    */
                   val prop1: ExtClass
                   fun set_prop1(value: ExtClass)
                   
                   /**
                    * prop2: ExtClass [0..1]{unique}
                    */
                   val prop1: ExtClass?
                   fun set_prop1(value: ExtClass?)
                }               
            """.trimIndent(),
            "sentence" to ""
        )
        doTest(template, model, params, expected)
    }

    @Test
    fun asString_populated() {
        val template = File("api/AsString.agl-fmt").readText()
        val expected = mapOf(
            "api/TestAsString.kt" to $$"""
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
                
                // *** Generated code do NOT manually edit. ***
                package .api
                
                import net.akehurst.kotlinx.utils.Indent
                
                /**
                 * Factory covering packages Root
                 */
                object TestAsString {
                  fun asStringTestClass(self: TestClass, indent:Indent = Indent()): String {
                    val sb = StringBuilder()
                    sb.append(self)
                    val indentInc = indent.inc
                    sb.append("${indentInc}prop1 ${this.asStringExtClass(self.prop1)}")
                    sb.append("${indentInc}prop2 ${this.asStringExtClass(self.prop2)}")
                    
                    return sb.toString()
                  }
                }
            """.trimIndent(),
            "sentence" to ""
        )
        doTest(template, model, emptyMap(), expected)
    }

    @Test
    fun builder_populated() {
        val template = File("api/Builder.agl-fmt").readText()
        val expected = mapOf(
            "api/Root_Builder.kt" to $$"""
                // *** Generated code do NOT manually edit. ***
                package .api
                
                import net.akehurst.kotlinx.collections.toOrderedSet
                
                @TestDslMarker
                class TestClass_ContentBuilder(
                   val factory: TestFactory,
                   val id: Any
                ) {
                    private val _content = mutableListOf<TestClass>()
                
                    fun TestClass(id:Any, init:TestClass_Builder.()->Unit): TestClass {
                val b = TestClass_Builder(factory,id)
                b.init()
                val obj = b.build()
                _content.add(obj)
                return obj
                    }
                
                    fun build():Collection<TestClass> = _content
                }
                
                @TestDslMarker
                class TestClass_Builder(
                   val factory: TestFactory,
                   val id: Any
                ) {
                
                    private var _prop1: ExtClass? = null
                    fun prop1(id:String, init:ExtClass_Builder.()->Unit): ExtClass {
                val b = ExtClass_Builder(factory, id)
                b.init()
                val obj = b.build()
                _prop1 = obj
                return obj
                    }
                    private var _prop2: ExtClass? = null
                    fun prop2(id:String, init:ExtClass_Builder.()->Unit): ExtClass {
                val b = ExtClass_Builder(factory, id)
                b.init()
                val obj = b.build()
                _prop2 = obj
                return obj
                    }
                
                    fun build(): TestClass = factory.createTestClass(id).apply {
                       _prop1?.let{ set_prop1(it) }
                       _prop2?.let{ set_prop2(it) }
                    }
                }
            """.trimIndent(),
            "api/Test_Builder.kt" to $$"""
                // *** Generated code do NOT manually edit. ***
                package .api
                
                import net.akehurst.kotlinx.utils.Indent
                
                /**
                 * Factory covering packages Root
                 */
                object TestAsString {
                  fun asStringTestClass(self: TestClass, indent:Indent = Indent()): String {
                    val sb = StringBuilder()
                    sb.append(self)
                    val indentInc = indent.inc
                    sb.append("${indentInc}prop1 ${this.asStringExtClass(self.prop1)}")
                    sb.append("${indentInc}prop2 ${this.asStringExtClass(self.prop2)}")
                    
                    return sb.toString()
                  }
                }
            """.trimIndent(),
            "sentence" to ""
        )
        doTest(template, model, emptyMap(), expected)
    }
}
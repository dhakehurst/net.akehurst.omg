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

import net.akehurst.omg._simple_mof_for_xmi.TestModelXmi.model
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.test.*

/**
 * Generates from the test model with the api and ram templates and checks the key parts of the output.
 * The testmodel-api and testmodel-ram modules compile and run the same output.
 */
class test_Templates {

    private val parameters = mapOf(
        "TARGET_PACKAGE" to "test",
        "COPYRIGHT" to "",
        "EXCLUDE_PACKAGE" to listOf("PrimitiveTypes")
    )

    private fun generate(template: String): Map<String, String> {
        val text = File("../_templates/$template.agl-fmt").readText()
        return GeneratorFromTemplate(LoggerFactory.getLogger(test_Templates::class.java), text, SimpleMof.types).generate(model, parameters)
    }

    private fun String.squash() = lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")

    private fun assertContains(text: String, vararg expected: String) {
        val t = text.squash()
        expected.forEach { e -> assertTrue(t.contains(e.squash()), "Expected to contain:\n$e\n--- in:\n$text") }
    }

    @Test
    fun api_interfaces() {
        val out = generate("api/EnumsAndInterfaces")
        val types = out["api/Test/Test_Types.kt"] ?: fail("no api/Test/Test_Types.kt in ${out.keys}")
        assertContains(
            types,
            "package test.api.Test",
            "enum class Kind { a, b, in_ }",
            "interface Element {",
            // settable, derived and collection properties
            "val declaredName: String?\nval declaredNameValue: Value<String?>",
            "val name: String?\nval nameDerived: Derived<String?>",
            "val ownedElementOrderedSet: OrderedSet<Element>",
            // operations are default methods
            "fun isNamed(aName: String): Boolean = (this.name == aName)",
            "fun describe(): String = TODO(\"Operation 'Test.Element.describe' is not implemented\")",
            "interface Relationship : Element {",
            "val relatedElementList: List<Element>",
            "interface Package : Element {",
            "override fun effectiveName(): String? = (if ((this.declaredName == null)) \"unnamed\" else super<Element>.effectiveName())",
        )
        // the association owned end is not in the api (it is named only in the KDoc of its opposite)
        assertFalse(types.contains("val A_target_targetOf_targetOf"))
    }

    @Test
    fun api_derivations() {
        val out = generate("api/Derivations")
        val d = out["api/Test/Test_Derivations.kt"] ?: fail("no api/Test/Test_Derivations.kt in ${out.keys}")
        assertContains(
            d,
            "object Test_Derivations {",
            "fun Element_name(self: Element): String? = self.effectiveName()",
            "fun Element_isA(self: Element): Boolean = (self.kind == Kind.a)",
            "fun Element_everythingSet(self: Element): Set<Element> = TODO(\"Derivation of 'Test.Element.everything' is not implemented\")",
        )
        // the OCL is kept in the KDoc
        assertContains(d, "* everything = Element.allInstances()", "* Not translated: unknown name 'Element'.")
    }

    @Test
    fun ram_classes() {
        val out = generate("ram/ClassesRam")
        assertNull(out["ram/Test/Element_Ram.kt"], "abstract classes have no implementation")
        val r = out["ram/Test/Relationship_Ram.kt"] ?: fail("no ram/Test/Relationship_Ram.kt in ${out.keys}")
        assertContains(
            r,
            "internal data class RelationshipRam(",
            // a derived property delegates to the derivation
            "override val nameDerived: Derived<String?> = ManagedDerived(\"Element.name\", String::class) { test.api.Test.Test_Derivations.Element_name(this) as String? }",
            // a derived union is the union of its stored subsets
            "override val relatedElementList: List<Element_NN> = DerivedList(\"Relationship.relatedElementList\", Element_NN::class) { (listOfNotNull(owningRelatedElement) + ownedRelatedElementOrderedSet + targetOrderedSet).map { it as Element_NN }.toList() }",
            // a composite end sets its opposite
            "item.owningRelationshipReference.mutable.set(this._identity, this)",
            // the association owned end is held in the ram
            "override val A_target_targetOf_targetOfSetReference: Set<Reference<Any, Relationship_NN>> = ManagedReferenceSet(",
        )
    }
}

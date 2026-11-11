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

import java.io.File

/** the test model, 3_Code/_testmodel/specs/testmodel.xmi, parsed as the testmodel modules parse it */
object TestModelXmi {
    val model: MofModel by lazy {
        val parser = MofXmiParser(
            "Test", listOf("Package"), mapOf(
                "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String" to "String",
                "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Integer" to "Long",
                "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean" to "Boolean",
                "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Real" to "Double",
                "https://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#UnlimitedNatural" to "Long"
            )
        )
        // tests run in the module directory
        parser.parse(File("../uml/v2_5_1/specs/UML_2-5-1_PT_ptc-18-01-02.xmi"))
        parser.parse(File("../_testmodel/specs/testmodel.xmi"))
    }

    fun cls(name: String): MofClass = model.allClasses.filterIsInstance<MofClass>().first { it.name == name }
    fun prop(cls: String, name: String): MofProperty = cls(cls).allOwnedAttributes.first { it.nameInXmi == name }
    fun op(cls: String, name: String): MofOperation = cls(cls).operations.first { it.name == name }
}

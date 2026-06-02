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

package net.akehurst.omg.templates.examples

import net.akehurst.kotlinx.utils.Indent
import net.akehurst.omg.templates.examples.common.*
import net.akehurst.omg.templates.examples.simple.*
import net.akehurst.omg.templates.examples.redefined.*

object Examples_ModelAsString {

    fun Element_Content_asString(self: Collection<Element>, indent: Indent): String = when {
        0 == self.size -> "[]"
        else -> {
            val sb = StringBuilder()
            sb.appendLine("[")
            val indentItem = indent.inc
            self.forEach { item -> sb.appendLine(this.Element_asString(item, indentItem)) }
            sb.append("${indent}]")
            sb.toString()
        }
    }

    fun PropType_Content_asString(self: Collection<PropType>, indent: Indent): String = when {
        0 == self.size -> "[]"
        else -> {
            val sb = StringBuilder()
            sb.appendLine("[")
            val indentItem = indent.inc
            self.forEach { item -> sb.appendLine(this.PropType_asString(item, indentItem)) }
            sb.append("${indent}]")
            sb.toString()
        }
    }


    fun Element_asString(self: Element, indent: Indent = Indent()): String = when(self) {
        is Example -> Example_asString(self, indent)
        is SingleCmpAttribute -> SingleCmpAttribute_asString(self, indent)
        is SingleCmpRedefSameNameDiffTypeAttribute -> SingleCmpRedefSameNameDiffTypeAttribute_asString(self, indent)
        is SingleCmpRedefDiffNameSameTypeAttribute -> SingleCmpRedefDiffNameSameTypeAttribute_asString(self, indent)
        is CollectionCmpAttribute -> CollectionCmpAttribute_asString(self, indent)
        is CollectionCmpRedefSameNameDiffTypeAttribute -> CollectionCmpRedefSameNameDiffTypeAttribute_asString(self, indent)
        is SingleRefAttribute -> SingleRefAttribute_asString(self, indent)
        is SingleRefRedefSameNameDiffTypeAttribute -> SingleRefRedefSameNameDiffTypeAttribute_asString(self, indent)
        is SingleRefRedefDiffNameSameTypeAttribute -> SingleRefRedefDiffNameSameTypeAttribute_asString(self, indent)
        is PropType -> PropType_asString(self, indent)
        else -> error("Element subtype '${self::class.simpleName}' not handled.")
    }

    fun Example_asString(self: Example, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append(self)
        val indentInc = indent.inc
        self.contentList.let {
            sb.appendLine()
            sb.append("${indentInc}content = List ")
            sb.append(Element_Content_asString(it, indentInc))
        }
        return sb.toString()
    }

    fun PropType_asString(self: PropType, indent: Indent = Indent()): String {
        return when(self) {
            is PropTypeB -> PropTypeB_asString(self, indent)
            else -> {
                val sb = StringBuilder()
                sb.append("$indent$self")
                sb.toString()
            }
        }
    }

    fun PropTypeB_asString(self: PropTypeB, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        return sb.toString()
    }

    fun SingleCmpAttribute_asString(self: SingleCmpAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        // prop1: PropType [1] {composite}
        self.prop1.let {
            sb.appendLine()
            sb.append("${indentInc}prop1 ${this.PropType_asString(self.prop1)}")
        }
        // prop2: PropType [0..1] {composite}
        self.prop2?.let {
            sb.appendLine()
            sb.append("${indentInc}prop2 ${this.PropType_asString(it)}")
        }
        return sb.toString()
    }

    fun SingleCmpRedefSameNameDiffTypeAttribute_asString(self: SingleCmpRedefSameNameDiffTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        // prop1: PropTypeB [1] { redefines SingleCompositeAttribute.prop1 }
        self.prop1.let {
            sb.appendLine()
            sb.append("${indentInc}prop1 ${this.PropTypeB_asString(it)}")
        }
        // prop2: PropTypeB [0..1] { redefines SingleCompositeAttribute.prop2 }
        self.prop2?.let {
            sb.appendLine()
            sb.append("${indentInc}prop2 ${this.PropTypeB_asString(it)}")
        }
        return sb.toString()
    }

    fun SingleCmpRedefDiffNameSameTypeAttribute_asString(self: SingleCmpRedefDiffNameSameTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        self.redefinesProp1.let {
            sb.appendLine()
            sb.append("${indentInc}redefinesProp1 ${this.PropType_asString(it)}")
        }
        self.redefinesProp2?.let {
            sb.appendLine()
            sb.append("${indentInc}redefinesProp2 ${this.PropType_asString(it)}")
        }
        return sb.toString()
    }

    fun CollectionCmpAttribute_asString(self: CollectionCmpAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        self.prop1OrderedSet.let {
            sb.appendLine()
            sb.append("${indentInc}prop1 = OrderedSet ")
            sb.append(PropType_Content_asString(it, indentInc))
        }
        self.prop2List.let {
            sb.appendLine()
            sb.append("${indentInc}prop2 = List ")
            sb.append(PropType_Content_asString(it, indentInc))
        }
        self.prop3Set.let {
            sb.appendLine()
            sb.append("${indentInc}prop3 = Set ")
            sb.append(PropType_Content_asString(it, indentInc))
        }
        self.prop4Collection.let { col ->
            if(col.isNotEmpty()) {
                sb.appendLine()
                sb.append("${indentInc}prop4 = Collection ")
                sb.append(PropType_Content_asString(col, indentInc))
            }
        }
        return sb.toString()
    }

    fun CollectionCmpRedefSameNameDiffTypeAttribute_asString(self: CollectionCmpRedefSameNameDiffTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        self.prop1OrderedSet.forEach { sb.appendLine(); sb.append("${indentInc}prop1 ${this.PropTypeB_asString(it)}") }
        self.prop2List.forEach { sb.appendLine(); sb.append("${indentInc}prop2 ${this.PropTypeB_asString(it)}") }
        // prop3 and prop4 are from the original type and may contain PropType
        self.prop3Set.forEach { sb.appendLine(); sb.append("${indentInc}prop3 ${this.PropType_asString(it)}") }
        self.prop4Collection.forEach { sb.appendLine(); sb.append("${indentInc}prop4 ${this.PropType_asString(it)}") }
        return sb.toString()
    }

    fun SingleRefAttribute_asString(self: SingleRefAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        // prop1: PropType [1] {reference}
        sb.appendLine()
        sb.append("${indentInc}prop1 ${this.PropType_asString(self.prop1)}")
        // prop2: PropType [0..1] {reference}
        self.prop2?.let { sb.appendLine(); sb.append("${indentInc}prop2 ${this.PropType_asString(it)}") }
        return sb.toString()
    }

    fun SingleRefRedefSameNameDiffTypeAttribute_asString(self: SingleRefRedefSameNameDiffTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        sb.appendLine()
        sb.append("${indentInc}prop1 ${this.PropTypeB_asString(self.prop1)}")
        self.prop2?.let { sb.appendLine(); sb.append("${indentInc}prop2 ${this.PropTypeB_asString(it)}") }
        return sb.toString()
    }

    fun SingleRefRedefDiffNameSameTypeAttribute_asString(self: SingleRefRedefDiffNameSameTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        sb.appendLine()
        sb.append("${indentInc}redefinesProp1 ${this.PropType_asString(self.redefinesProp1)}")
        self.redefinesProp2?.let { sb.appendLine(); sb.append("${indentInc}redefinesProp2 ${this.PropType_asString(it)}") }
        return sb.toString()
    }

}


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

object Examples_ModelAsString {

    fun Element_Content_asString(self: Collection<Element>, indent: Indent): String = when {
        0 == self.size -> "[]"
        else -> {
            val sb = StringBuilder()
            sb.appendLine("[")
            val indentItem = indent.inc
            self.forEach { item -> sb.append(this.Element_asString(item, indentItem)) }
            sb.appendLine()
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
        is Examples -> Examples_asString(self, indent)
        is SingleCmpAttribute -> SingleCmpAttribute_asString(self, indent)
        is SingleCmpRedefSameNameDiffTypeAttribute -> SingleCmpRedefSameNameDiffTypeAttribute_asString(self, indent)
        is SingleCmpRedefDiffNameSameTypeAttribute -> SingleCmpRedefDiffNameSameTypeAttribute_asString(self, indent)
        is CollectionCmpAttribute -> CollectionCmpAttribute_asString(self, indent)
        is CollectionCmpRedefSameNameDiffTypeAttribute -> asStringCollectionCmpRedefSameNameDiffTypeAttribute(self, indent)
        is SingleRefAttribute -> asStringSingleRefAttribute(self, indent)
        is SingleRefRedefSameNameDiffTypeAttribute -> asStringSingleRefRedefSameNameDiffTypeAttribute(self, indent)
        is SingleRefRedefDiffNameSameTypeAttribute -> asStringSingleRefRedefDiffNameSameTypeAttribute(self, indent)
        is PropType -> PropType_asString(self, indent)
        else -> error("Element subtype '${self::class.simpleName}' not handled.")
    }

    fun Examples_asString(self: Examples, indent: Indent = Indent()): String {
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
        sb.append(self)
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
        sb.appendLine()
        sb.append("${indentInc}prop1 ${this.PropTypeB_asString(self.prop1)}")
        // prop2: PropTypeB [0..1] { redefines SingleCompositeAttribute.prop2 }
        self.prop2?.let { sb.appendLine(); sb.append("${indentInc}prop2 ${this.PropTypeB_asString(it)}") }
        return sb.toString()
    }

    fun SingleCmpRedefDiffNameSameTypeAttribute_asString(self: SingleCmpRedefDiffNameSameTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        sb.appendLine()
        sb.append("${indentInc}redefinesProp1 ${this.PropType_asString(self.redefinesProp1)}")
        self.redefinesProp2?.let { sb.appendLine(); sb.append("${indentInc}redefinesProp2 ${this.PropType_asString(it)}") }
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
        self.prop4Collection.let {
            sb.appendLine()
            sb.append("${indentInc}prop4 = Collection ")
            sb.append(PropType_Content_asString(it, indentInc))
        }
        return sb.toString()
    }

    fun asStringCollectionCmpRedefSameNameDiffTypeAttribute(self: CollectionCmpRedefSameNameDiffTypeAttribute, indent: Indent = Indent()): String {
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

    fun asStringSingleRefAttribute(self: SingleRefAttribute, indent: Indent = Indent()): String {
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

    fun asStringSingleRefRedefSameNameDiffTypeAttribute(self: SingleRefRedefSameNameDiffTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        sb.appendLine()
        sb.append("${indentInc}prop1 ${this.PropTypeB_asString(self.prop1)}")
        self.prop2?.let { sb.appendLine(); sb.append("${indentInc}prop2 ${this.PropTypeB_asString(it)}") }
        return sb.toString()
    }

    fun asStringSingleRefRedefDiffNameSameTypeAttribute(self: SingleRefRedefDiffNameSameTypeAttribute, indent: Indent = Indent()): String {
        val sb = StringBuilder()
        sb.append("$indent$self")
        val indentInc = indent.inc
        sb.appendLine()
        sb.append("${indentInc}redefinesProp1 ${this.PropType_asString(self.redefinesProp1)}")
        self.redefinesProp2?.let { sb.appendLine(); sb.append("${indentInc}redefinesProp2 ${this.PropType_asString(it)}") }
        return sb.toString()
    }

}


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


package net.akehurst.omg.diagram

/**
 * A generic tree, e.g. a model browser. What it shows is decided by the transform that makes it.
 */
class Tree {
    var name: String = ""
    var kind: String = ""

    /** The top level items. */
    var item: List<TreeItem> = emptyList()

    /** All items, depth first. */
    val allItem: List<TreeItem> get() = item.flatMap { it.allItem }
}

/** An item of a [Tree]: drawn by its [shape] (e.g. an icon and the label), with nested [child] items. */
class TreeItem : DiagramElement() {
    var child: List<TreeItem> = emptyList()

    /** This item and all items nested in it, depth first. */
    val allItem: List<TreeItem> get() = listOf(this) + child.flatMap { it.allItem }
}

/**
 * A generic table: [column] headings and [row]s of texts. What it shows is decided by the transform that makes it.
 */
class Table {
    var name: String = ""
    var kind: String = ""

    /** The column headings, in order. */
    var column: List<String> = emptyList()
    var row: List<TableRow> = emptyList()
}

/** A row of a [Table]: one text per column. */
class TableRow : DiagramElement() {
    var cell: List<String> = emptyList()
}

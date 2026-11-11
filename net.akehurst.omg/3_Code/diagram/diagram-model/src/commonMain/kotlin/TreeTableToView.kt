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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.akehurst.kotlin.compose.components.table.TableState
import net.akehurst.kotlin.compose.components.table.TableView
import net.akehurst.kotlin.compose.components.tree.TreeViewNode

/** Draws the items of a [Tree] by their shape names. */
fun interface TreeShapeRegistry {
    fun itemContent(item: TreeItem): @Composable () -> Unit
}

/** Shows each item as its keywords and label on one line, e.g. “«Class» Car”. */
val PlainTreeShapes = TreeShapeRegistry { item ->
    {
        val text = (item.keyword.map { "«$it»" } + item.label).joinToString(" ")
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Converts this tree to the nodes of the components TreeView (put them in a TreeViewStateHolder with updateItems).
 * Each node's id is the path of item ids from the top, so items need only differ from their siblings;
 * an item with a blank or repeated id is given its shape and position instead. The [TreeItem] is in the node's data as "item".
 */
fun Tree.toTreeViewNodes(shapes: TreeShapeRegistry = PlainTreeShapes): List<TreeViewNode> {
    fun nodes(parentPath: String, items: List<TreeItem>): List<TreeViewNode> {
        val used = mutableSetOf<String>()
        return items.mapIndexed { i, item ->
            val local = item.id.takeIf { it.isNotBlank() && used.add(it) } ?: "${item.shape}[$i]"
            val path = if (parentPath.isEmpty()) local else "$parentPath/$local"
            TreeViewNode(path).also { n ->
                n.content = shapes.itemContent(item)
                n.hasChildren = item.child.isNotEmpty()
                n.data["item"] = item
                val children = nodes(path, item.child)
                n.children.update { children }
            }
        }
    }
    return nodes("", item)
}

/**
 * Shows this table with the components TableView: a header row of the column headings, then one row per [TableRow].
 * [cell] draws one cell; by default its text.
 */
@Composable
fun TableModelView(
    table: Table,
    modifier: Modifier = Modifier,
    cell: @Composable (row: TableRow, column: Int, text: String) -> Unit = { _, _, text -> Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
) {
    val state = remember(table) { TableState() }
    val border = Color(0xFFD8D8D8)
    val cellModifier = Modifier.border(1.dp, border).padding(horizontal = 6.dp, vertical = 2.dp)
    TableView(
        state,
        tableModifier = modifier.fillMaxWidth(),
        headerModifier = Modifier.fillMaxWidth(),
        headerContent = {
            tableHeader {
                table.column.forEachIndexed { i, heading ->
                    tableHeaderCell(i, boxModifier = cellModifier.weight(1f)) {
                        Text(heading, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    ) {
        table.row.forEach { r ->
            tableRow {
                table.column.indices.forEach { i ->
                    // the TableView gives body cells the width of their header cell
                    tableCell(i, boxModifier = cellModifier) {
                        cell(r, i, r.cell.getOrElse(i) { "" })
                    }
                }
            }
        }
    }
}

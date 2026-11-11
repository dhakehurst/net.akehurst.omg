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


package net.akehurst.omg.uml.gui.compose

import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent
import net.akehurst.omg.diagram.LadderShapeRegistry
import net.akehurst.omg.diagram.Operand
import net.akehurst.omg.diagram.Rail
import net.akehurst.omg.diagram.Step

/**
 * Draws ladder elements with the UML sequence diagram shapes, by [Step.shape] name:
 * 'SynchCall', 'AsynchCall', 'AsynchSignal', 'Reply', 'CreateMessage', 'DeleteMessage', and 'CombinedFragment'.
 */
object UmlLadderShapeRegistry : LadderShapeRegistry {

    override fun railHeader(rail: Rail): @Composable () -> Unit = { SequenceDiagram.LifelineHeaderView(rail.label) }

    override fun stepContent(step: Step): GraphLayoutEdgeContent = when (step.shape) {
        "SynchCall" -> SequenceDiagram.SynchCallMessage(step.label)
        "AsynchCall", "AsynchSignal" -> SequenceDiagram.AsynchMessage(step.label)
        "Reply" -> SequenceDiagram.ReplyMessage(step.label)
        "CreateMessage" -> SequenceDiagram.CreateMessage(step.label)
        "DeleteMessage" -> SequenceDiagram.DeleteMessage(step.label)
        else -> umlEdge(middleText = listOf("unknown shape '${step.shape}'", step.label))
    }

    override fun fragmentLabel(step: Step): @Composable () -> Unit = { SequenceDiagram.CombinedFragmentLabel(step.label) }

    override fun operandLabel(operand: Operand): (@Composable () -> Unit)? =
        operand.guard.takeIf { it.isNotEmpty() }?.let { g -> { SequenceDiagram.OperandGuard(g) } }
}

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

import androidx.compose.runtime.Composable
import net.akehurst.kotlin.compose.layout.graph.GraphLayoutEdgeContent
import net.akehurst.kotlin.compose.layout.ladder.LadderBarEnd
import net.akehurst.kotlin.compose.layout.ladder.LadderBarStart
import net.akehurst.kotlin.compose.layout.ladder.LadderEvent
import net.akehurst.kotlin.compose.layout.ladder.LadderGroup
import net.akehurst.kotlin.compose.layout.ladder.LadderGroupPart
import net.akehurst.kotlin.compose.layout.ladder.LadderLayoutState
import net.akehurst.kotlin.compose.layout.ladder.LadderModel
import net.akehurst.kotlin.compose.layout.ladder.LadderRail
import net.akehurst.kotlin.compose.layout.ladder.LadderRung

/**
 * Draws the elements of a [Ladder] by their shape names, like [ShapeRegistry] does for a [Diagram].
 */
interface LadderShapeRegistry {
    /** The header at the top of a rail. */
    fun railHeader(rail: Rail): @Composable () -> Unit

    /** How a message step is drawn. */
    fun stepContent(step: Step): GraphLayoutEdgeContent

    /** The label in the tab of a fragment step. */
    fun fragmentLabel(step: Step): @Composable () -> Unit

    /** The label of an operand of a fragment, or null for none. */
    fun operandLabel(operand: Operand): (@Composable () -> Unit)?
}

/** The id given to the [index]th operand of fragment [step] (operands have no id of their own). */
fun operandId(step: Step, index: Int) = "${step.id}#$index"

/**
 * Converts this ladder for the ladder view of layout-graph (LadderLayoutView).
 * A step with operands becomes a group; any other step a rung. A bar starts after the step [LadderBar.startId]
 * and ends after the step [LadderBar.finishId].
 */
fun Ladder.toLadderLayoutState(shapes: LadderShapeRegistry, stateId: String = name): LadderLayoutState {
    val barsStartingAfter = bar.groupBy { it.startId }
    val barsEndingAfter = bar.groupBy { it.finishId }

    fun events(steps: List<Step>): List<LadderEvent> = steps.flatMap { s ->
        val e: LadderEvent = when {
            s.operand.isNotEmpty() || s.covered.isNotEmpty() -> LadderGroup(
                id = s.id,
                rails = s.covered,
                parts = s.operand.mapIndexed { i, o -> LadderGroupPart(operandId(s, i), events(o.step)) }
            )

            else -> LadderRung(s.id, s.sourceId, s.targetId)
        }
        listOf(e) +
                barsEndingAfter[s.id].orEmpty().map { LadderBarEnd(it.id) } +
                barsStartingAfter[s.id].orEmpty().map { LadderBarStart(it.id, it.railId) }
    }

    val state = LadderLayoutState(stateId, LadderModel(rail.map { LadderRail(it.id) }, events(step)))
    rail.forEach { state.addRailHeader(it.id, shapes.railHeader(it)) }
    allStep.forEach { s ->
        when {
            s.operand.isNotEmpty() || s.covered.isNotEmpty() -> {
                state.addGroupLabel(s.id, shapes.fragmentLabel(s))
                s.operand.forEachIndexed { i, o -> shapes.operandLabel(o)?.let { state.addGroupPartLabel(operandId(s, i), it) } }
            }

            else -> state.addRungContent(s.id, shapes.stepContent(s))
        }
    }
    return state
}

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
 * A generic "ladder" diagram, e.g. a sequence diagram: [rail]s side by side (e.g. lifelines), and [step]s drawn from top to bottom
 * across them. Like [Diagram], each element names the shape that draws it.
 */
class Ladder {
    /** Shown as the diagram's title. */
    var name: String = ""

    /** The kind of diagram, e.g. 'SequenceDiagram'. */
    var kind: String = ""

    /** The rails (e.g. lifelines), in order from left to right. */
    var rail: List<Rail> = emptyList()

    /** The top level steps, in order from top to bottom. */
    var step: List<Step> = emptyList()

    /** Bars on rails, e.g. UML execution specifications. */
    var bar: List<LadderBar> = emptyList()

    /** All steps, including those nested in fragment operands, in order from top to bottom. */
    val allStep: List<Step> get() = step.flatMap { it.allStep }
}

/** A vertical line, with a header (drawn by its shape) at the top. */
class Rail : DiagramElement()

/**
 * One row of a ladder. It is either
 * - a message: an arrow from rail [sourceId] to rail [targetId] (the same for a self message); or
 * - a fragment: a box over the [covered] rails, split into [operand]s that hold their own steps.
 * The [shape] says which, e.g. 'SynchCall', 'Reply' or 'CombinedFragment'.
 */
class Step : DiagramElement() {
    /** [Rail.id] the message starts at. */
    var sourceId: String = ""

    /** [Rail.id] the message ends at. */
    var targetId: String = ""

    /** [Rail.id]s a fragment spans. */
    var covered: List<String> = emptyList()

    /** The parts of a fragment, in order from top to bottom. */
    var operand: List<Operand> = emptyList()

    /** This step and the steps nested in it, in order from top to bottom. */
    val allStep: List<Step> get() = listOf(this) + operand.flatMap { o -> o.step.flatMap { it.allStep } }
}

/** A part of a fragment: an optional guard, and the steps in it. */
class Operand {
    var guard: String = ""
    var step: List<Step> = emptyList()
}

/** A bar on rail [railId], from the row of step [startId] to the row of step [finishId]. */
class LadderBar : DiagramElement() {
    var railId: String = ""
    var startId: String = ""
    var finishId: String = ""
}

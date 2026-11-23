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


package net.akehurst.omg.uml.v2_5_1.demo

/**
 * The agl-m2m transform from a UML Interaction to a generic [net.akehurst.omg.diagram.Ladder] (a sequence diagram).
 *
 * Lifelines become rails. The interaction's fragments, in order, become the steps: each message becomes one step
 * (made from the fragment that sends it), each combined fragment a step with operands holding their own steps.
 * Execution specifications become bars from the step that starts them to the step that finishes them.
 * Steps are identified by the message (or fragment) name, so names must be unique within the interaction.
 *
 * See [UmlDiagramRules] for notes on the expression language.
 */
object UmlSequenceRules {

    val transform = """
        namespace net.akehurst.omg.uml.diagrams
        transform UmlToLadder(uml: UML, dgm: Diagram) {
            import-types UML.CommonStructure
            import-types UML.Interactions
            import-types net.akehurst.omg.diagram

            top relation InteractionToLadder {
                pivot n: String
                domain uml i: Interaction { name == n  lifeline == i_ls  fragment == i_fs }
                domain dgm l: Ladder { name == n  kind == 'SequenceDiagram'  rail == l_rails  step == l_steps  bar == l_bars }
                where {
                    relate all LifelineToRail { uml := i_ls.map({it -> it}) dgm := l_rails }
                    relate all FragmentToStep {
                        uml := i_fs.map({it -> it}).filter({it -> when {
                            it is CombinedFragment -> true
                            it is MessageOccurrenceSpecification -> it.message.sendEvent == it
                            else -> false
                        }})
                        dgm := l_steps
                    }
                    relate all ExecutionToBar {
                        uml := i_fs.map({it -> it}).filter({it -> it is ExecutionSpecification})
                        dgm := l_bars
                    }
                }
            }

            relation LifelineToRail {
                pivot ln: String
                domain uml ll: Lifeline { name == ln }
                domain dgm r: Rail {
                    id == ln
                    shape == 'Lifeline'
                    label == ln + (when { ll.represents.type is Type -> ' : ' + ll.represents.type.name  else -> '' })
                }
            }

            relation FragmentToStep {
                domain uml f: InteractionFragment { }
                domain dgm s: Step {
                    id == (when {
                        f is MessageOccurrenceSpecification -> f.message.name
                        else -> f.name
                    })
                    label == (when {
                        f is MessageOccurrenceSpecification -> f.message.name
                        f is CombinedFragment -> f.interactionOperator.name
                        else -> ''
                    })
                    shape == (when {
                        f is CombinedFragment -> 'CombinedFragment'
                        f is MessageOccurrenceSpecification -> (when {
                            f.message.messageSort.name == 'synchCall' -> 'SynchCall'
                            f.message.messageSort.name == 'asynchCall' -> 'AsynchCall'
                            f.message.messageSort.name == 'asynchSignal' -> 'AsynchSignal'
                            f.message.messageSort.name == 'reply' -> 'Reply'
                            f.message.messageSort.name == 'createMessage' -> 'CreateMessage'
                            f.message.messageSort.name == 'deleteMessage' -> 'DeleteMessage'
                            else -> 'SynchCall'
                        })
                        else -> ''
                    })
                    sourceId == (when {
                        f is MessageOccurrenceSpecification -> f.message.sendEvent.covered.name
                        else -> ''
                    })
                    targetId == (when {
                        f is MessageOccurrenceSpecification -> f.message.receiveEvent.covered.name
                        else -> ''
                    })
                    covered == (when {
                        f is CombinedFragment -> f.covered.map({it -> it.name})
                        else -> List()
                    })
                    operand == s_ops
                }
                where {
                    relate all OperandToOperand {
                        uml := (when { f is CombinedFragment -> f.operand.map({it -> it})  else -> List() })
                        dgm := s_ops
                    }
                }
            }

            relation OperandToOperand {
                domain uml o: InteractionOperand { }
                domain dgm op: Operand {
                    guard == (when { o.guard.specification is LiteralString -> o.guard.specification.value  else -> '' })
                    step == op_steps
                }
                where {
                    relate all FragmentToStep {
                        uml := o.fragment.map({it -> it}).filter({it -> when {
                            it is CombinedFragment -> true
                            it is MessageOccurrenceSpecification -> it.message.sendEvent == it
                            else -> false
                        }})
                        dgm := op_steps
                    }
                }
            }

            relation ExecutionToBar {
                pivot en: String
                domain uml e: ExecutionSpecification { name == en }
                domain dgm b: LadderBar {
                    id == en
                    shape == 'ExecutionSpecification'
                    railId == e.covered.map({it -> it.name}).first
                    startId == (when { e.start is MessageOccurrenceSpecification -> e.start.message.name  else -> '' })
                    finishId == (when { e.finish is MessageOccurrenceSpecification -> e.finish.message.name  else -> '' })
                }
            }
        }
    """.trimIndent()
}

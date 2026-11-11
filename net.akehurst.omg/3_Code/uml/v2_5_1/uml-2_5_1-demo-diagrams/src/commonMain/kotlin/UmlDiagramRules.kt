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
 * The agl-m2m transform from UML model elements to the generic diagram model.
 *
 * A diagram is made from a Package: its packaged elements become nodes, and the relationships found anywhere
 * below it become edges. The shape of each node and edge is chosen from the type of the UML element;
 * the shape names are those known by [net.akehurst.omg.uml.gui.compose.UmlShapeRegistry].
 *
 * Notes on the agl-m2m expression language as it is now:
 * - a lambda always binds its argument to `it`, whatever name it declares, so all lambdas here use `it`;
 * - transitiveClosure needs a List, so UML Sets are turned into Lists with `.map({it -> it})`;
 * - there is no list concatenation, so each list in the diagram is filled by one `relate all` call
 *   (where two lists are needed, transitiveClosure is used to reach the second list from the first);
 * - `and`/`or` evaluate both sides, so a property of a subtype is guarded with `when { x is T -> ... }`;
 * - an unset Boolean is nothing, so Boolean properties are compared with `== true`.
 */
object UmlDiagramRules {

    val transform = """
        namespace net.akehurst.omg.uml.diagrams
        transform UmlToDiagram(uml: UML, dgm: Diagram) {
            import-types UML.CommonStructure
            import-types UML.Packages
            import-types UML.Classification
            import-types UML.SimpleClassifiers
            import-types UML.StructuredClassifiers
            import-types net.akehurst.omg.diagram

            top relation PackageToDiagram {
                pivot n: String
                domain uml p: Package { name == n  packagedElement == p_els }
                domain dgm d: Diagram { name == n  kind == 'UML'  node == d_nodes  edge == d_edges }
                where {
                    // the packaged elements, and the metaclasses extended by stereotypes (found from the extensions)
                    relate all ElementToNode {
                        uml := p_els.map({it -> it})
                            .transitiveClosure({it -> when {
                                it is Extension -> it.memberEnd.filter({it -> (it is ExtensionEnd) == false}).map({it -> it.type})
                                else -> List()
                            }})
                            .filter({it -> (it is Relationship) == false})
                            .filter({it -> (when { it is EnumerationLiteral -> false  it is InstanceSpecification -> it.classifier.map({it -> it}).first is Association  else -> false }) == false})
                        dgm := d_nodes
                    }
                    relate all RelationshipToEdge {
                        uml := p.ownedElement.map({it -> it})
                            .transitiveClosure({it -> it.ownedElement.map({it -> it})})
                            .filter({it -> (it is Relationship) or (it is ActivityEdge) or (it is Connector) or (when { it is EnumerationLiteral -> false  it is InstanceSpecification -> it.classifier.map({it -> it}).first is Association  else -> false })})
                        dgm := d_edges
                    }
                }
            }

            relation ElementToNode {
                pivot en: String
                domain uml e: NamedElement { name == en }
                domain dgm n: Node {
                    id == en
                    label == (when {
                        e is Port -> e.name
                        e is Property -> e.name + (when { e.type is Type -> ' : ' + e.type.name  else -> '' })
                        e is InstanceSpecification -> e.name + ' : ' + e.classifier.map({it -> it.name}).first
                        e is Pin -> e.name
                        e is ControlNode -> ''
                        else -> e.name
                    })
                    shape == (when {
                        (e is Class) and (e.owner.name == 'UML') -> 'Metaclass'
                        e is Stereotype -> 'Stereotype'
                        e is Profile -> 'Profile'
                        e is Model -> 'Model'
                        e is Package -> 'Package'
                        e is Enumeration -> 'Enumeration'
                        e is PrimitiveType -> 'PrimitiveType'
                        e is DataType -> 'DataType'
                        e is Interface -> 'Interface'
                        e is Activity -> 'Activity'
                        e is Device -> 'Device'
                        e is ExecutionEnvironment -> 'ExecutionEnvironment'
                        e is UML.Deployments.Node -> 'Node'
                        e is DeploymentSpecification -> 'DeploymentSpecification'
                        e is Artifact -> 'Artifact'
                        e is Component -> 'Component'
                        e is Collaboration -> 'Collaboration'
                        e is InstanceSpecification -> 'InstanceSpecification'
                        e is Port -> 'Port'
                        e is Property -> 'Part'
                        e is InitialNode -> 'InitialNode'
                        e is ActivityFinalNode -> 'ActivityFinalNode'
                        e is FlowFinalNode -> 'FlowFinalNode'
                        e is DecisionNode -> 'DecisionNode'
                        e is MergeNode -> 'MergeNode'
                        e is ForkNode -> 'ForkNode'
                        e is JoinNode -> 'JoinNode'
                        e is CallBehaviorAction -> 'CallBehaviorAction'
                        e is SendSignalAction -> 'SendSignalAction'
                        e is AcceptEventAction -> 'AcceptEventAction'
                        e is Action -> 'Action'
                        e is Pin -> 'Pin'
                        e is ActivityParameterNode -> 'ActivityParameterNode'
                        e is DataStoreNode -> 'DataStoreNode'
                        e is CentralBufferNode -> 'CentralBufferNode'
                        e is ObjectNode -> 'ObjectNode'
                        e is ActivityPartition -> 'ActivityPartition'
                        else -> 'Class'
                    })
                    option == (when {
                        e is Classifier -> (when { e.isAbstract == true -> List('abstract')  else -> List() })
                        e is Port -> (when { e.isBehavior == true -> List('behavior')  else -> List() })
                        e is Property -> (when { e.isComposite == true -> List()  else -> List('reference') })
                        else -> List()
                    })
                    compartment == [
                        Compartment { name == 'attributes'  item == (when {
                            e is Stereotype -> e.ownedAttribute.filter({it -> (it.association is Extension) == false}).map({it -> it.name + (when { it.type is Type -> ' : ' + it.type.name  else -> '' })})
                            e is Class -> e.ownedAttribute.filter({it -> (it is Port) == false}).filter({it -> (it.isComposite == true) == false}).map({it -> it.name + (when { it.type is Type -> ' : ' + it.type.name  else -> '' })})
                            e is DataType -> e.ownedAttribute.map({it -> it.name + (when { it.type is Type -> ' : ' + it.type.name  else -> '' })})
                            e is Interface -> e.ownedAttribute.map({it -> it.name + (when { it.type is Type -> ' : ' + it.type.name  else -> '' })})
                            e is Artifact -> e.ownedAttribute.map({it -> it.name + (when { it.type is Type -> ' : ' + it.type.name  else -> '' })})
                            else -> List()
                        }) },
                        Compartment { name == 'operations'  item == (when {
                            e is Class -> e.ownedOperation.map({it -> it.name + '()'})
                            e is DataType -> e.ownedOperation.map({it -> it.name + '()'})
                            e is Interface -> e.ownedOperation.map({it -> it.name + '()'})
                            else -> List()
                        }) },
                        Compartment { name == 'literals'  item == (when {
                            e is Enumeration -> e.ownedLiteral.map({it -> it.name})
                            else -> List()
                        }) },
                        Compartment { name == 'slots'  item == (when {
                            e is InstanceSpecification -> e.slot.map({it -> it.definingFeature.name + ' = ' + it.value.map({it -> when {
                                it is LiteralString -> '"' + it.value + '"'
                                it is LiteralInteger -> it.value
                                it is LiteralBoolean -> it.value
                                it is LiteralReal -> it.value
                                it is InstanceValue -> it.instance.name
                                else -> '?'
                            }}).first})
                            else -> List()
                        }) }
                    ]
                    child == n_children
                    port == n_ports
                }
                where {
                    relate all ElementToNode {
                        uml := (when {
                            e is Package -> e.packagedElement.map({it -> it}).filter({it -> (it is Relationship) == false})
                            e is Activity -> (when {
                                e.partition.map({it -> it}).first is ActivityPartition -> e.partition.map({it -> it})
                                else -> e.node.map({it -> it})
                            })
                            e is ActivityPartition -> e.node.map({it -> it})
                            e is UML.Deployments.Node -> e.nestedNode.map({it -> it})
                            e is Component -> e.packagedElement.map({it -> it}).filter({it -> (it is Relationship) == false})
                            // internal structure is shown only for classes that have connectors
                            e is StructuredClassifier -> (when {
                                e.ownedConnector.map({it -> it}).first is Connector -> e.ownedAttribute.filter({it -> (it is Port) == false}).filter({it -> it.isComposite == true})
                                else -> List()
                            })
                            else -> List()
                        })
                        dgm := n_children
                    }
                    relate all ElementToNode {
                        uml := (when {
                            e is EncapsulatedClassifier -> e.ownedAttribute.filter({it -> it is Port})
                            // input pins then output pins: an input pin leads (via its action) to the output pins
                            e is Action -> List(e).transitiveClosure({it -> when {
                                it is Action -> (when { it.input.map({it -> it}).first is Pin -> it.input.map({it -> it})  else -> it.output.map({it -> it}) })
                                it is InputPin -> it.owner.output.map({it -> it})
                                else -> List()
                            }}).filter({it -> it is Pin})
                            else -> List()
                        })
                        dgm := n_ports
                    }
                }
            }

            relation RelationshipToEdge {
                domain uml r: Element { }
                domain dgm e: Edge {
                    label == (when {
                        r is NamedElement -> r.name
                        else -> ''
                    })
                    shape == (when {
                        r is Generalization -> 'Generalization'
                        r is Extension -> 'Extension'
                        r is CommunicationPath -> 'CommunicationPath'
                        r is Association -> 'Association'
                        r is InstanceSpecification -> 'Link'
                        r is InterfaceRealization -> 'InterfaceRealization'
                        r is ComponentRealization -> 'ComponentRealization'
                        r is Deployment -> 'Deployment'
                        r is Manifestation -> 'Manifestation'
                        r is Realization -> 'Realization'
                        r is Abstraction -> 'Abstraction'
                        r is Usage -> 'Usage'
                        r is Dependency -> 'Dependency'
                        r is PackageImport -> 'PackageImport'
                        r is ElementImport -> 'ElementImport'
                        r is PackageMerge -> 'PackageMerge'
                        r is ProfileApplication -> 'ProfileApplication'
                        r is ControlFlow -> 'ControlFlow'
                        r is ObjectFlow -> 'ObjectFlow'
                        r is Connector -> 'Connector'
                        else -> 'Dependency'
                    })
                    sourceId == (when {
                        r is Generalization -> r.specific.name
                        r is Extension -> r.ownedEnd.type.name
                        r is Association -> r.memberEnd.map({it -> it.type.name}).first
                        r is Deployment -> r.supplier.map({it -> it.name}).first
                        r is Dependency -> r.client.map({it -> it.name}).first
                        r is PackageImport -> r.importingNamespace.name
                        r is ElementImport -> r.importingNamespace.name
                        r is PackageMerge -> r.receivingPackage.name
                        r is ProfileApplication -> r.applyingPackage.name
                        r is ActivityEdge -> r.source.name
                        r is Connector -> r.end.map({it -> it.role.name}).first
                        r is InstanceSpecification -> r.slot.map({it -> it.value.map({it -> it.instance.name}).first}).first
                        else -> ''
                    })
                    targetId == (when {
                        r is Generalization -> r.general.name
                        r is Extension -> r.memberEnd.filter({it -> (it is ExtensionEnd) == false}).map({it -> it.type.name}).first
                        r is Association -> r.memberEnd.map({it -> it.type.name}).last
                        r is Deployment -> r.client.map({it -> it.name}).first
                        r is Dependency -> r.supplier.map({it -> it.name}).first
                        r is PackageImport -> r.importedPackage.name
                        r is ElementImport -> r.importedElement.name
                        r is PackageMerge -> r.mergedPackage.name
                        r is ProfileApplication -> r.appliedProfile.name
                        r is ActivityEdge -> r.target.name
                        r is Connector -> r.end.map({it -> it.role.name}).last
                        r is InstanceSpecification -> r.slot.map({it -> it.value.map({it -> it.instance.name}).first}).last
                        else -> ''
                    })
                    keyword == (when {
                        r is ActivityEdge -> (when { r.guard is LiteralString -> List(r.guard.value)  else -> List() })
                        else -> List()
                    })
                    option == (when {
                        r is Extension -> (when { r.isRequired == true -> List('required')  else -> List() })
                        r is ProfileApplication -> (when { r.isStrict == true -> List('strict')  else -> List() })
                        else -> List()
                    })
                    sourceEnd == EdgeEnd {
                        decoration == (when {
                            r is Extension -> ''
                            r is Association -> (when {
                                r.memberEnd.map({it -> it}).last.isComposite == true -> 'filledDiamond'
                                else -> ''
                            })
                            else -> ''
                        })
                        label == (when {
                            r is Association -> List(r.memberEnd.map({it -> it}).first.name)
                        r is InstanceSpecification -> List(r.slot.map({it -> it.definingFeature.name}).first)
                            else -> List()
                        })
                    }
                    targetEnd == EdgeEnd {
                        decoration == (when {
                            r is Extension -> ''
                            r is Association -> (when {
                                r.memberEnd.map({it -> it}).first.isComposite == true -> 'filledDiamond'
                                r.ownedEnd.map({it -> it}).contains(r.memberEnd.map({it -> it}).last) -> ''
                                else -> 'openArrow'
                            })
                            else -> ''
                        })
                        label == (when {
                            r is Association -> List(r.memberEnd.map({it -> it}).last.name)
                        r is InstanceSpecification -> List(r.slot.map({it -> it.definingFeature.name}).last)
                            else -> List()
                        })
                    }
                }
            }
        }
    """.trimIndent()
}

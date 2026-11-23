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
 * Example agl-m2m transforms from UML to the generic tree and table models.
 * A tree or table shows whatever its transform selects; these are two examples. See [UmlDiagramRules]
 * for notes on the expression language.
 */
object UmlTreeTableRules {

    /** the metaclass of a UML element, as a name (agl expressions have no way to ask an object for its type name) */
    private val metaclass = """(when {
                        e is Model -> 'Model'
                        e is Profile -> 'Profile'
                        e is Package -> 'Package'
                        e is Stereotype -> 'Stereotype'
                        e is Enumeration -> 'Enumeration'
                        e is PrimitiveType -> 'PrimitiveType'
                        e is DataType -> 'DataType'
                        e is Interface -> 'Interface'
                        e is Component -> 'Component'
                        e is Activity -> 'Activity'
                        e is Interaction -> 'Interaction'
                        e is Class -> 'Class'
                        e is Association -> 'Association'
                        e is Generalization -> 'Generalization'
                        e is InterfaceRealization -> 'InterfaceRealization'
                        e is Usage -> 'Usage'
                        e is Dependency -> 'Dependency'
                        e is Operation -> 'Operation'
                        e is Port -> 'Port'
                        e is Property -> 'Property'
                        e is EnumerationLiteral -> 'EnumerationLiteral'
                        e is InstanceSpecification -> 'InstanceSpecification'
                        else -> 'Element'
                    })"""

    /** The containment tree of a Package: every owned element, with its metaclass as a keyword. */
    val containmentTree = """
        namespace net.akehurst.omg.uml.trees
        transform UmlToTree(uml: UML, dgm: Diagram) {
            import-types UML.CommonStructure
            import-types UML.Packages
            import-types net.akehurst.omg.diagram

            top relation PackageToTree {
                pivot n: String
                domain uml p: Package { name == n  packagedElement == p_els }
                domain dgm t: Tree { name == n  kind == 'Containment'  item == t_items }
                where {
                    relate all ElementToItem { uml := p_els.map({it -> it}) dgm := t_items }
                }
            }

            relation ElementToItem {
                domain uml e: Element { }
                domain dgm i: TreeItem {
                    id == (when {
                        e is Generalization -> 'general ' + e.general.name
                        e is NamedElement -> (when { e.name is String -> e.name  else -> '' })
                        else -> ''
                    })
                    label == (when {
                        e is Generalization -> e.general.name
                        e is Dependency -> e.client.map({it -> it.name}).first + ' -> ' + e.supplier.map({it -> it.name}).first
                        e is Property -> e.name + (when { e.type is Type -> ' : ' + e.type.name  else -> '' })
                        e is Operation -> e.name + '()'
                        e is NamedElement -> e.name
                        else -> ''
                    })
                    shape == $metaclass
                    keyword == List($metaclass)
                    child == i_children
                }
                where {
                    relate all ElementToItem {
                        uml := e.ownedElement.map({it -> it})
                            .filter({it -> (it is NamedElement) or (it is Generalization)})
                            .filter({it -> (it is ValueSpecification) == false})
                        dgm := i_children
                    }
                }
            }
        }
    """.trimIndent()

    /** A table of the attributes of all classifiers in a Package (and the packages in it). */
    val attributeTable = """
        namespace net.akehurst.omg.uml.tables
        transform UmlToTable(uml: UML, dgm: Diagram) {
            import-types UML.CommonStructure
            import-types UML.Packages
            import-types UML.Classification
            import-types net.akehurst.omg.diagram

            top relation PackageToTable {
                pivot n: String
                domain uml p: Package { name == n }
                domain dgm t: Table {
                    name == n
                    kind == 'Attributes'
                    column == List('Classifier', 'Attribute', 'Type', 'Multiplicity', 'Aggregation')
                    row == t_rows
                }
                where {
                    relate all PropertyToRow {
                        uml := p.ownedElement.map({it -> it})
                            .transitiveClosure({it -> it.ownedElement.map({it -> it})})
                            .filter({it -> it is Property})
                            .filter({it -> (it.owner is Association) == false})
                        dgm := t_rows
                    }
                }
            }

            relation PropertyToRow {
                domain uml a: Property { }
                domain dgm r: TableRow {
                    id == a.owner.name + '::' + a.name
                    cell == List(
                        a.owner.name,
                        a.name,
                        (when { a.type is Type -> a.type.name  else -> '' }),
                        (('' + a.lower) + '..') + (when { a.upper == -1 -> '*'  else -> '' + a.upper }),
                        (when { a.isComposite == true -> 'composite'  else -> '' })
                    )
                }
            }
        }
    """.trimIndent()
}

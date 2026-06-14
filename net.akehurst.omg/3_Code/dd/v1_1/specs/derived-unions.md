# Derived-union report — DD (Diagram Interchange)

Source XMI files: `DD_20131001_DiagramInterchange.xmi`, `DD_20131001DiagramGraph.xmi`, `DD_20131001_DiagramCommon.xmi`

Summary
- Found several `isDerivedUnion="true"` attributes in `DD_20131001_DiagramInterchange.xmi`.
- No explicit `<subsettedProperty xmi:idref="..."/>` entries were present in the DD XMI that enumerate the sources of those derived unions (i.e., the XMI does not list the subsetted properties by idref).

Details

1) Class: `DiagramElement` (xmi:id="DiagramElement")
- Attribute: `xmi:id="DiagramElement-modelElement"`, name: `modelElement`
  - xmi attributes: `isReadOnly="true" isDerived="true" isDerivedUnion="true"`
  - type: `uml:Element` (via href to UML Element)
  - association: `A_modelElement_diagramElement`
  - subset sources: NONE explicit in the XMI (no `<subsettedProperty xmi:idref="DiagramElement-modelElement"/>` found).
  - complexity: HIGH — union sources are implicit in the DI spec (textual semantics and associations). Auto-generation would require domain knowledge and heuristics; recommend manual implementation.

- Attribute: `xmi:id="DiagramElement-owningElement"`, name: `owningElement`
  - xmi attributes: `isReadOnly="true" isDerived="true" isDerivedUnion="true"`
  - type: `DiagramElement`
  - association: `A_ownedElement_owningElement`
  - subset sources: NONE explicit in the XMI.
  - complexity: HIGH — manual required.

- Attribute: `xmi:id="DiagramElement-ownedElement"`, name: `ownedElement`
  - xmi attributes: `isOrdered="true" isReadOnly="true" isDerived="true" isDerivedUnion="true" aggregation="composite"`
  - type: `DiagramElement`
  - association: `A_ownedElement_owningElement`
  - subset sources: NONE explicit in the XMI.
  - complexity: HIGH — manual required.

2) Class: `Edge` (xmi:id="Edge")
- Attribute: `xmi:id="Edge-source"`, name: `source`
  - `isReadOnly="true" isDerived="true" isDerivedUnion="true"`, type `DiagramElement`, association `A_source_sourceEdge`
  - subset sources: NONE explicit in the XMI.
  - complexity: HIGH — manual required.

- Attribute: `xmi:id="Edge-target"`, name: `target`
  - `isReadOnly="true" isDerived="true" isDerivedUnion="true"`, type `DiagramElement`, association `A_target_targetEdge`
  - subset sources: NONE explicit in the XMI.
  - complexity: HIGH — manual required.

3) Associations (ownedEnds marked derived union)
- `A_target_targetEdge::ownedEnd` — `xmi:id="A_target_targetEdge-targetEdge"`, name: `targetEdge` (derivedUnion)
- `A_source_sourceEdge::ownedEnd` — `xmi:id="A_source_sourceEdge-sourceEdge"`, name: `sourceEdge` (derivedUnion)
- `A_modelElement_diagramElement::ownedEnd` — `xmi:id="A_modelElement_diagramElement-diagramElement"`, name: `diagramElement` (derivedUnion)
  - subset sources: NONE explicit in the XMI for these association-ownedEnds.
  - complexity: HIGH — manual implementation recommended (semantics are in textual spec).

Recommendation
- For the DD (Diagram Interchange) model, the XMI marks derived unions but does not enumerate subsettedProperty entries that define the union sources. Because the union sources are implicit and tied to the DI specification semantics, the safe approach is to require hand-written implementation for these getters. The generator can, however, emit a clearly-labelled TODO stub for each derived-union attribute and include a pointer to this report to help implementers.


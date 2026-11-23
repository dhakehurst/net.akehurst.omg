# Diagram Definition 1.1

Kotlin Multiplatform DD 1.1 (Diagram Definition), generated from the three DD XMI files in `specs`:

| XMI | Package | Contents |
|---|---|---|
| `DD_20131001_DiagramCommon.xmi` | `DC` | Diagram Common: data types (`Point`, `Dimension`, `Bounds`, `Color`) and `AlignmentKind` |
| `DD_20131001DiagramGraph.xmi` | `DG` | Diagram Graphics: graphical elements (`Canvas`, `Group`, `Rectangle`, `Path`, `Text`, ...), fills, markers and `Style` |
| `DD_20131001_DiagramInterchange.xmi` | `DI` | Diagram Interchange: the abstract `DiagramElement`, `Diagram`, `Shape` and `Edge`, which language specifications specialise |

The specification is `specs/DiagramDefinition_formal-15-06-01.pdf`.

## Modules

All modules have the group `net.akehurst.omg`. The Kotlin package is `net.akehurst.omg.dd.v1_1`; the generated names
start with `DiagramDefinition_` (e.g. `DiagramDefinition_ModelFactory`).

| Module | Contents |
|---|---|
| `dd-1_1-api` | Generated: one interface per metaclass, the factory, builder, resolver, `asString`, the reflective (by-name) API and the derivations of derived properties, as for [UML](../../uml/v2_5_1/README.md) |
| `dd-1_1-ram` | Generated: in-memory classes that implement the api, created by `DiagramDefinition_ModelFactoryRam` |
| `dd-1_1-agl-types` | Generated: `DiagramDefinitionTypesDomain`, the AGL types of DD bound to the ram classes |

All three modules are generated from the same three XMI files. A reference to `UML::Element` (e.g. the
`modelElement` of a `DI::DiagramElement`) is generated as `Any`.

DD is the OMG's model for interchanging diagrams. The diagrams in this repository do not use it; they use the generic
[diagram model](../../diagram/diagram-model/README.md).

## Notes

- The DI derived unions (`DiagramElement.modelElement`, `ownedElement`, `Edge.source`, ...) have no subsetting
  properties in the XMI; see [specs/derived-unions.md](specs/derived-unions.md). They are meant to be subset by the
  diagram interchange of a particular language.
- The association with `xmi:id` `A_style_canvas` has no name; the generator uses its `xmi:id` in place of the name
  when it names the association-owned end (`canvas`).

There are no tests for the DD modules yet.

# diagram-model

A generic diagram model: nodes (which may contain other nodes and have ports), compartments and edges. It knows
nothing about the modelling language. Each element names the shape that draws it and carries the texts and options
that shape needs.

The module has the group `net.akehurst.omg` and the Kotlin package `net.akehurst.omg.diagram`.

```kotlin
val diagram = Diagram().apply {
    name = "Vehicles"
    node = listOf(
        Node().apply { id = "Car"; shape = "Class"; label = "Car" },
        Node().apply { id = "Engine"; shape = "Class"; label = "Engine" },
    )
    edge = listOf(
        Edge().apply { id = "e1"; shape = "Association"; sourceId = "Car"; targetId = "Engine" }
    )
}
val state = diagram.toCompoundGraphState(UmlShapeRegistry)   // for layout-graph's CompoundGraphLayoutView
```

## Contents

| Type | Use |
|---|---|
| `Diagram` | `name`, `kind`, the top-level `node`s and all `edge`s |
| `Node` | A box-like shape. `child` nodes make it a container; `port` nodes sit on its border; `compartment`s are named lists of texts |
| `Edge` | A line from `sourceId` to `targetId` (node or port ids), with an `EdgeEnd` (`decoration`, `label`) at each end |
| `DiagramElement` | What nodes and edges share: `id`, `shape`, `label`, `keyword` (e.g. stereotypes), `option` (e.g. `abstract`), `modelElement` |
| `ShapeRegistry` | Turns a node's `shape` name into Compose content, and an edge's into a line style |
| `Diagram.toCompoundGraphState` | Converts a diagram for the `layout-graph` view: containers become nested graphs, ports become border nodes |
| `DiagramTypesDomain` | The AGL types of these classes, so that an agl-m2m transform can create and fill a diagram |

The classes are simple and mutable so that an agl-m2m transform can build them. The UML shapes are in
[uml-2_5_1-gui-compose](../../uml/v2_5_1/README.md), and the UML diagram demo shows a complete transform from a UML
model to a diagram.

It uses `layout-graph` from [net.akehurst.kotlin.compose](https://github.com/dhakehurst/net.akehurst.kotlin.compose)
and `agl-processor` from [net.akehurst.language](https://github.com/dhakehurst/net.akehurst.language).

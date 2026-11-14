# Systems Modeling API 1.0

Kotlin Multiplatform api of the OMG Systems Modeling API and Services 1.0 (the API used to read and write SysML v2
models in a repository: projects, commits, branches, queries, ...), generated from
`specs/SysModApi-1_0-ptc-25-02-29.xmi`. The specification is `specs/SystemsModelingAPI-1_0-formal-26-03-04.pdf`.

## Modules

| Module | Contents |
|---|---|
| `sysmod-api-1_0-api` | Generated api (model name `SysModApi`, package `net.akehurst.omg.sysmodapi.v1_0`). It depends on [`kerml-1_0-api`](../../kerml/v1_0/README.md) for the KerML types the XMI refers to |

## How the XMI is mapped

- The XMI's root is a `uml:Model` named "Systems Modeling API and Services PIM". The generator replaces characters
  that are not valid in a Kotlin name by `_` in package names, so the generated packages are
  `net.akehurst.omg.sysmodapi.v1_0.api.Systems_Modeling_API_and_Services_PIM` and `...Systems_Modeling_API_and_Services_PIM.API_Model`.
- References to KerML (by href to "KerML Abstract Syntax.xml") are mapped in `referencedTypeMapping` in the build file:
  `..._42199` to KerML `Element`, and `..._42192` (the result of `getRelationships`) to KerML `Relationship`.
- The interface `Data` is realized by KerML `Element` as well as by `ProjectUsage`, `ExternalRelationship` and
  `ExternalData`. `kerml-1_0-api` cannot implement an interface of this module, so `Data` is mapped to `Any` in
  `referencedTypeMapping`: properties, parameters and results typed `Data` (e.g. `DataVersion.payload`) are typed `Any`.
  A composite property with such a type is set as a value in the builder and is not resolved by the resolver.
- The `Operator` enumeration has the literals `=`, `<`, `<=`, `>`, `>=`, which are not Kotlin names. The generator
  spells them with the names of their symbols: `EQ`, `LT`, `LT_EQ`, `GT`, `GT_EQ`; the reflective api still reads
  them by their names in the XMI.
- The property `ExternalRelationship::externalDataEnd` is named `" externalDataEnd"` (with a leading space) in the XMI;
  the generator trims property names.
- The `uml:Realization` elements of the XMI are not read by the generator (it prints "Unknown element type").

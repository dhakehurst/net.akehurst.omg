# Systems Modeling API 1.0

Kotlin Multiplatform api of the OMG Systems Modeling API and Services 1.0 (the API used to read and write SysML v2
models in a repository: projects, commits, branches, queries, ...), to be generated from
`specs/SysModApi-1_0-ptc-25-02-29.xmi`. The specification is `specs/SystemsModelingAPI-1_0-formal-26-03-04.pdf`.

## Status

**This module does not build at the moment:** the `generate` task fails with the current generator. It has been set
aside while the UML, KerML and SysML modules are worked on.

## Modules

| Module | Contents |
|---|---|
| `sysmod-api-1_0-api` | Generated api (model name `SysModApi`). References to KerML `Element` in the XMI are mapped to `net.akehurst.omg.kerml.v1_0.api.Element`, so it depends on [`kerml-1_0-api`](../../kerml/v1_0/README.md) |

The generate task sets `TARGET_PACKAGE` to `net.akehurst.omg.sysmodapi.v2_0`, although the module is version 1.0.

# _templates

The [AGL format](https://github.com/dhakehurst/net.akehurst.language) templates that generate the Kotlin code of each
metamodel. A generated module runs every `*.agl-fmt` file in one of these folders; see
[the generator](../_simple-mof-for-xmi/README.md).

The templates use the parameters `TARGET_PACKAGE`, `COPYRIGHT` (file header, from `text/COPYRIGHT.txt`) and
`EXCLUDE_PACKAGE`. `<Model>` below is the task's `modelName` (e.g. `UML`), `<Package>` is a metamodel package and
`<path>` its qualified path.

## api

Package `<TARGET_PACKAGE>.api`.

| Template | Generates |
|---|---|
| `EnumsAndInterfaces.agl-fmt` | `api/<path>/<Package>_Types.kt`: an enum per enumeration and an interface per class, with the operations translated from OCL |
| `Derivations.agl-fmt` | `api/<path>/<Package>_Derivations.kt`: one function per derived (not union) property, translated from OCL |
| `Factory.agl-fmt` | `<Package>_PackageFactory` per package and `<Model>_ModelFactory` |
| `Builder.agl-fmt` | `<Package>_PackageBuilder.kt` per package and `<Model>_ModelBuilder`, a DSL for building models (stored properties only) |
| `Resolver.agl-fmt` | `<Package>_PackageResolver` per package and `<Model>_ModelResolver`, which resolve references |
| `AsString.agl-fmt` | `<Package>_PackageAsString` per package and `<Model>_ModelAsString`, which print a model as text |
| `Reflective.agl-fmt` | `<Model>_ModelReflective` (interface) and `<Model>_ModelReflectiveDefault`, a by-name API that builds models from metaclass and feature names as in the XMI, plus an internal `<Package>_PackageReflective` per package with the `when` tables. Written only against the api and `<Model>_ModelFactory`, so it works with any implementation |

## ram

Package `<TARGET_PACKAGE>.ram`. The in-memory realisation of the api.

| Template | Generates |
|---|---|
| `ClassesRam.agl-fmt` | `ram/<path>/<Class>_Ram.kt` per class. Stored values are created on first use; derived values and derived unions are computed when read; subsets, supersets and opposite ends are kept consistent. A subset, superset or opposite that a subclass computes by redefinition is left as it is (`addIfStored`/`removeIfStored` in the factory) |
| `FactoryRam.agl-fmt` | `<Package>_FactoryRam` per package and `<Model>_ModelFactoryRam` |
| `InterfaceNonNavigable.agl-fmt` | `ram/<path>/<Package>_TypesNonNavigable.kt`: internal interfaces with the association ends that are not navigable from the class |

## agl-types

| Template | Generates |
|---|---|
| `TypesDomain.agl-fmt` | `agl/<Model>TypesDomain.kt`: the AGL types of the metamodel (supertypes, properties named as in the XMI, with accessors and mutators bound to the ram classes) and `<Model>TypesMutation`, which says how a mutator sets a value. Packages in `EXCLUDE_PACKAGE` are left out |

The agl-types modules are `uml-2_5_1-agl-types`, `sysml-2_0-agl-types` and `dd-1_1-agl-types`.

## The rules

The requirements that the generated code meets (naming, references, subsets, derived properties, identity) are in
[Specification.md](../../1_Requirements/Specification.md). The `_testmodel` modules generate a small test metamodel
with these templates and test the result.

`src` holds hand-written examples of the generated code and tests of them. They are older than the current
templates and are not kept up to date with them.

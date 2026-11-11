# SysML 2.0

Kotlin Multiplatform SysML 2.0, generated from `specs/sysml_patched.xmi`, a patched copy of the OMG XMI
`specs/SysML_2-0_ptc-25-02-15.xmi`.

SysML 2.0 extends KerML, so the SysML modules are generated from three XMI files together: the UML primitive types,
the patched KerML XMI and the patched SysML XMI. The KerML metaclasses are generated again inside the SysML packages
(`net.akehurst.omg.sysml.v2_0.api.KerML...`); the SysML modules do not use the [KerML modules](../../kerml/v1_0/README.md).

## Modules

All modules have the group `net.akehurst.omg`. The Kotlin package is `net.akehurst.omg.sysml.v2_0`.

| Module | Contents |
|---|---|
| `sysml-2_0-api` | Generated: one interface per metaclass, in `api.KerML.<Package>` and `api.SysML.<Package>` (e.g. `api.SysML.Systems.Parts.PartDefinition`), `SysML_ModelFactory`, `SysML_ModelBuilder`, `SysML_ModelResolver`, `SysML_ModelAsString`, and the derivations of derived properties |
| `sysml-2_0-ram` | Generated: in-memory classes that implement the api, created by `SysML_ModelFactoryRam` |

The builder and `asString` have root functions for `Package` and `LibraryPackage`.

## Building a model in code

As in KerML, ownership is held by relationships and names are set as `declaredName`; `owner`, `ownedMember`, `name`
and the other derived properties are computed when they are read.

```kotlin
val factory = SysML_ModelFactoryRam("myModel")
val root = SysML_ModelBuilder.Package(factory, "RootPackage") {
    declaredName("Root Package")
    ownedRelationshipOrderedSet {
        OwningMembership("SubPackageMembership") {
            ownedRelatedElementOrderedSet {
                Package("SubPackage") { declaredName("Sub Package") }
            }
        }
    }
}
println(SysML_ModelAsString.Package_asString(root))
```

## The patched XMI

The OMG XMI does not set the aggregation of composite properties, and some of its OCL has errors or is missing. Every
change made in `sysml_patched.xmi` is listed in [specs/ocl-fixes.md](specs/ocl-fixes.md).
[spec-problems.md](../../spec-problems.md) summarises the problems.

## Tests

```sh
./gradlew --max-workers=1 :sysml-2_0-ram:jvmTest
```

The SysML modules are the largest; `--max-workers=1` keeps the Kotlin daemon from running out of memory.
`test_SysML_NoDerivationCycles` (JVM) reads every property of every class, and fails if a derivation calls itself without end (for example a derivation that uses the property it redefines).

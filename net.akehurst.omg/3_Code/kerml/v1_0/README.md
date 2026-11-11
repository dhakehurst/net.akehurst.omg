# KerML 1.0

Kotlin Multiplatform KerML 1.0 (Kernel Modeling Language), generated from `specs/kerml_patched.xmi`, a patched copy
of the OMG XMI `specs/KerML_1-0_ptc-25-04-04.xmi`.

## Modules

All modules have the group `net.akehurst.omg`. The Kotlin package is `net.akehurst.omg.kerml.v1_0`.

| Module | Contents |
|---|---|
| `kerml-1_0-api` | Generated: one interface per metaclass (package `api.KerML.<Package>`, e.g. `api.KerML.Kernel.Packages.Package`), `KerML_ModelFactory`, `KerML_ModelBuilder`, `KerML_ModelResolver`, `KerML_ModelAsString`, and the derivations of derived properties |
| `kerml-1_0-ram` | Generated: in-memory classes that implement the api, created by `KerML_ModelFactoryRam` |

The builder and `asString` have root functions for `Package` and `LibraryPackage`.

## Building a model in code

In KerML, ownership is held by relationships, and names are set as `declaredName`. `owner`, `ownedElement`,
`ownedMember` and `name` are derived from those, and are computed when they are read.

```kotlin
val factory = KerML_ModelFactoryRam("myModel")
val root = KerML_ModelBuilder.Package(factory, "RootPackage") {
    declaredName("Root Package")
    ownedRelationshipOrderedSet {
        OwningMembership("SubPackageMembership") {
            ownedRelatedElementOrderedSet {
                Package("SubPackage") { declaredName("Sub Package") }
            }
        }
    }
}
println(KerML_ModelAsString.Package_asString(root))
```

## The patched XMI

The OMG XMI does not set the aggregation of composite properties, and much of its OCL has errors or is missing. The
code is generated from `kerml_patched.xmi`, and every change is listed in [specs/ocl-fixes.md](specs/ocl-fixes.md).
[spec-problems.md](../../spec-problems.md) summarises the problems.

A derivation that the generator cannot translate from OCL is generated as a `TODO(...)`.

## Tests

```sh
./gradlew :kerml-1_0-ram:jvmTest
```

`test_KerML_NoDerivationCycles` (JVM) reads every property of every class, and fails if a derivation calls itself without end (for example a derivation that uses the property it redefines).

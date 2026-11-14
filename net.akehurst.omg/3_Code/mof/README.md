# MOF 2.5.1

Hand-written Kotlin Multiplatform EMOF (Essential MOF), from the MOF 2.5.1 specification
(`specs/2.5.1/MOF_2-5-1_formal-19-10-01.pdf`, XMI `specs/2.5.1/MOF_ptc_14-08-10.xmi`). Unlike the other metamodels,
this code is **not generated**, and it is not used by the generator, which has its own small MOF model (see
[_simple-mof-for-xmi](../_simple-mof-for-xmi/README.md)).

## Modules

These modules have the group `net.akehurst.omg` and the version `2.5.1` (not the project version).

| Module | Contents |
|---|---|
| `mof-api` | Interfaces of the EMOF metaclasses (package `net.akehurst.omg.mof.api.emof`: `Class`, `Property`, `Operation`, `Association`, `Package`, ...) and a first CMOF sketch (`net.akehurst.omg.mof.api.cmof`) |
| `mof-ram` | In-memory classes for the EMOF interfaces (`ClassRam`, `PropertyRam`, ...) and `EmofRamFactory`, which creates them by name or by `KClass` |
| `mof-gen` | Not part of the build (its build file is `build.gradle.kts_`): an early experiment that extracts metaclasses from an XMI file and prints Kotlin interfaces and an AGL types domain |

```kotlin
val cls: Class = EmofRamFactory.createByName("Class", emptyMap())
val gen = EmofRamFactory.createGeneralization(general = EmofRamFactory.createClass(), specific = cls)
```

## Tests

```sh
./gradlew :mof-ram:jvmTest
```

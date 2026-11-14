# _testmodel

A small test metamodel, generated with the real [templates](../_templates/README.md), to test the generated api and
ram code without the size of UML or SysML. Not published.

`specs/testmodel.xmi` has four classes, `Element`, `Relationship`, `PackageRelationship` and `Package`, modelled on
the KerML ownership pattern (ownership held by relationships), with OCL derivations and operations that cover what the
generator translates. It is read together with the UML primitive types (`uml/v2_5_1/specs/UML_2-5-1_PT_ptc-18-01-02.xmi`).

| Module | Contents |
|---|---|
| `testmodel-api` | Generated api, model name `Test`, package `net.akehurst.omg.testmodel` |
| `testmodel-ram` | Generated ram classes, and the tests in `src/commonTest/kotlin/test_TestModel.kt` |

The tests cover opposites, derived properties (including derived values following changes), closure, `selectByKind`,
enumeration literals, operations with parameters, a Kotlin body and redefinitions, derived unions,
association-owned opposites, subsets of a redefined type, and that untranslated OCL becomes `TODO(...)`.

```sh
./gradlew :testmodel-ram:jvmTest
```

The generator's own tests (parser, OCL translation, templates) are in
[_simple-mof-for-xmi](../_simple-mof-for-xmi/README.md).

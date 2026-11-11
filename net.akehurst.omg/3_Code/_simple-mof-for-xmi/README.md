# _simple-mof-for-xmi

The code generator (JVM only, not published). It is used by the `generate` task of the `code-generator` Gradle plugin
in `0_build-logic`; see [the top-level README](../../../README.md#how-a-module-is-generated) for how a module
configures it.

1. `MofXmiParser` reads one or more MOF XMI files into a small MOF model (`MofModel`, `MofPackage`, `MofClass`,
   `MofProperty`, ...). References between files and to external types (`referencedTypeMapping`) are resolved by
   `XmiReferenceHandler`.
2. `OclToKotlin` translates the OCL of derived properties and operation bodies into Kotlin over the generated api.
   It handles a subset of OCL (navigation, `let`, `if`, literals, boolean and comparison operators,
   `oclIsKindOf`/`oclAsType` and the common collection operations). Where it cannot translate, the generated code is a
   `TODO(...)` with the OCL in its documentation. A body whose language is `Kotlin` is copied as it is.
3. `GeneratorFromTemplate` runs an [AGL format](https://github.com/dhakehurst/net.akehurst.language) template over the
   model. `SimpleMof` is the AGL types domain of the MOF model, so the templates can navigate it. Each `#output` of
   the template gives the file to write; a file with no content is not written.

`src/m2m/Mof-to-Interface.agl-m2m` is an experiment with a model-to-model approach and is not used.

## Tests

```sh
./gradlew :_simple-mof-for-xmi:jvmTest
```

The tests cover the XMI parser, the OCL translation and the templates, using a small test metamodel (`TestModelXmi.kt`).

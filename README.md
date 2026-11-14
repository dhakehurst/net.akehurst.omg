![Kotlin Version](https://img.shields.io/badge/dynamic/regex?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdhakehurst%2Fnet.akehurst.omg%2Frefs%2Fheads%2Fmain%2Fnet.akehurst.omg%2Fgradle%2Flibs.versions.toml&search=kotlin%20%3D%20%22(%5B%5E%22%5D%2B)%22&replace=%241&label=Kotlin&color=green&link=https%3A%2F%2Fkotlinlang.org%2F)

# net.akehurst.omg

Kotlin Multiplatform implementations of OMG metamodels (UML, KerML, SysML and others), for JVM, JS and Wasm (JS).

The Kotlin code for each metamodel is generated from the OMG's own XMI file of that metamodel. A Gradle task reads
the XMI and runs a set of [AGL format](https://github.com/dhakehurst/net.akehurst.language) templates over it.
The templates produce:

- an **api**: one Kotlin interface per metaclass, with a factory, a builder DSL, a resolver, `asString` and the
  derived properties and operations translated from the OCL in the XMI;
- a **ram** realisation: in-memory classes that implement the api and keep subsets, opposites and derived unions
  consistent;
- **agl-types**: an AGL types domain for the metamodel, so that AGL expressions and agl-m2m transforms can read,
  create and set model objects.

## Metamodels

| Metamodel | Folder | Modules | Source XMI |
|---|---|---|---|
| UML 2.5.1 | [`uml/v2_5_1`](net.akehurst.omg/3_Code/uml/v2_5_1/README.md) | api, ram, agl-types, xmi (model reader), gui-compose, demo-diagrams | ptc-18-01-01 |
| KerML 1.0 | [`kerml/v1_0`](net.akehurst.omg/3_Code/kerml/v1_0/README.md) | api, ram | ptc-25-04-04, patched |
| SysML 2.0 | [`sysml/v2_0`](net.akehurst.omg/3_Code/sysml/v2_0/README.md) | api, ram (includes KerML), agl-types | ptc-25-02-15, patched |
| Systems Modeling API 1.0 | [`sysmod-api/v1_0`](net.akehurst.omg/3_Code/sysmod-api/v1_0/README.md) | api | ptc-25-02-29 |
| Diagram Definition 1.1 | [`dd/v1_1`](net.akehurst.omg/3_Code/dd/v1_1/README.md) | api, ram, agl-types | DD 2013-10-01 |
| MOF 2.5.1 | [`mof`](net.akehurst.omg/3_Code/mof/README.md) | api, ram (hand written, not generated) | ptc-14-08-10 |

The `generate` task of the Systems Modeling API module fails with the current generator, so that module does not
build at the moment.

Each metamodel folder has a `specs` folder with the XMI and specification PDF the code is generated from.

### Where the spec files come from

The repository does not record where the files in the `specs` folders were downloaded from. Their names follow OMG
document numbers (`ptc-18-01-01`, `formal-26-03-01`, ...), which suggests they are the machine-readable files and
specifications published by the OMG, but that is not confirmed. In particular, the SysML 2.0 and KerML metamodel XMI
is not from the SysML v2 release repository (Systems-Modeling/SysML-v2-Release), which has no metamodel XMI.

The KerML and SysML XMI from the OMG has errors in its OCL (and does not set aggregations), so the code is generated
from patched copies: `kerml_patched.xmi` and `sysml_patched.xmi`. Each change is listed in the `specs/ocl-fixes.md`
file next to it, and [spec-problems.md](net.akehurst.omg/3_Code/spec-problems.md) summarises the problems found in
the KerML, SysML and UML specs and XMI. The UML XMI is used as published.

`sysml/v1_7/specs` holds only the SysML 1.7 profile XMI. It is used to test reading SysML 1.x models with the UML XMI
reader; there is no generated SysML 1.7 code.

## Other modules

| Module | Contents |
|---|---|
| [`diagram/diagram-model`](net.akehurst.omg/3_Code/diagram/diagram-model/README.md) | A generic diagram model (nodes, compartments, ports, edges) that knows nothing about the modelling language, with its AGL types and a conversion to the `layout-graph` Compose view |
| [`_simple-mof-for-xmi`](net.akehurst.omg/3_Code/_simple-mof-for-xmi/README.md) | The generator: reads MOF XMI into a simple MOF model, translates OCL to Kotlin, runs the templates |
| [`_templates`](net.akehurst.omg/3_Code/_templates/README.md) | The AGL format templates for api, ram and agl-types |
| [`_testmodel`](net.akehurst.omg/3_Code/_testmodel/README.md) | A small test metamodel (`specs/testmodel.xmi`) generated with the real templates, with tests of the generated code |

These modules are not published: `_simple-mof-for-xmi`, `_templates`, `_testmodel` (`testmodel-api`, `testmodel-ram`).
`uml-2_5_1-demo-diagrams` is a demo. `mof/mof-gen` is not part of the build (its build file is named
`build.gradle.kts_`).

## Using

The modules are not yet published to Maven Central. Build them to your local Maven repository (see below) and use
the group `net.akehurst.omg`, the module name as the artifact, and the version in `gradle/libs.versions.toml`
(`project`, currently `2.4.0-SNAPSHOT`). The `mof` modules have version `2.5.1`.

```kotlin
// build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
}
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("net.akehurst.omg:uml-2_5_1-ram:2.4.0-SNAPSHOT")
            }
        }
    }
}
```

Building a small UML model with the generated builder:

```kotlin
val factory = UML_ModelFactoryRam("myModel")
val model = UML_ModelBuilder.Model(factory, "test") {
    packagedElementSet {
        Package("P1") {
            name("P1")
            packagedElementSet {
                Class("C1") { name("C1") }
            }
        }
    }
}
model.nestedPackageSet.single().ownedTypeSet.map { it.name }   // [C1], derived from packagedElement
println(UML_ModelAsString.Model_asString(model))
```

## Building

The Gradle project is in the `net.akehurst.omg/` folder, so run Gradle from there. The Gradle wrapper (Gradle 9.5.0,
`gradle/wrapper/gradle-wrapper.jar` included) is checked in, so `./gradlew` works in a fresh clone.

It depends on these other projects. When their versions in `gradle/libs.versions.toml` are `-SNAPSHOT`, build each one
with `publishToMavenLocal` first, in this order:

1. [net.akehurst.kotlinx](https://github.com/dhakehurst/net.akehurst.kotlinx) (`nak-kotlinx`); this needs
   [net.akehurst.kotlin.gradle.plugin](https://github.com/dhakehurst/net.akehurst.kotlin.gradle.plugin) first
2. [net.akehurst.language](https://github.com/dhakehurst/net.akehurst.language) (AGL, `nal-agl`); the generator in
   `0_build-logic` uses `agl-processor`, so this must be in Maven local before omg is configured
3. [net.akehurst.kotlin.compose](https://github.com/dhakehurst/net.akehurst.kotlin.compose) (`nak-compose`), for
   `layout-graph`, used by the diagram modules

```sh
cd net.akehurst.omg
./gradlew assemble
./gradlew jvmTest            # JVM tests (use jvmTest, not test)
./gradlew :uml-2_5_1-ram:generate   # only regenerate the code of one module
./gradlew publishToMavenLocal
```

While the Systems Modeling API module does not build, a whole-project `assemble` or `build` fails at its `generate`
task; build the other modules by name (e.g. `./gradlew :uml-2_5_1-ram:assemble`).

The generated code is written to `.gradle-build/<module>/generated/sources/generator/commonMain/kotlin` and is not
checked in. Compiling a generated module runs its `generate` task first.

The generated modules are large. If the Kotlin daemon runs out of memory, use `--max-workers=1` and give Gradle more
heap in `gradle.properties` (`org.gradle.jvmargs`).

### How a module is generated

A generated module applies the `code-generator` plugin from `0_build-logic` and configures its `generate` task:

```kotlin
plugins {
    id("project-conventions")
    id("code-generator")
}

tasks.named<generator.GenerateTask>("generate") {
    modelName = "UML"                       // prefix of the generated top-level names, e.g. UML_ModelFactory
    instanceRoots = listOf("Model")         // metaclasses that get a root function in the builder and asString
    sourceXmiPaths = listOf(layout.projectDirectory.file("../specs/UML_2-5-1_ASM_ptc-18-01-01.xmi"))
    generateDir.set(layout.projectDirectory.dir("../../../_templates/api"))   // api, ram or agl-types
    parameters = mapOf(
        "TARGET_PACKAGE" to "net.akehurst.omg.uml.v2_5_1",
        "COPYRIGHT" to "...",               // header of each generated file
        "EXCLUDE_PACKAGE" to emptyList<String>()   // metamodel packages to leave out
    )
    referencedTypeMapping = mapOf(          // types referenced from outside the XMI, by href
        "http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String" to "String",
    )
}
```

Every `*.agl-fmt` file in `generateDir` is run. Several XMI files can be given; the SysML modules read the UML
primitive types, the patched KerML and the patched SysML XMI together.

## Documentation

- [1_Requirements/Specification.md](net.akehurst.omg/1_Requirements/Specification.md): the requirements for the
  generated api and realisations (naming, references, subsets, derived properties, identity), numbered `REQ-x.y`.
- [3_Code/spec-problems.md](net.akehurst.omg/3_Code/spec-problems.md): problems found in the OMG specs and XMI.
- `specs/ocl-fixes.md` (KerML, SysML): every change made in the patched XMI.
- `specs/derived-unions.md` (each metamodel): notes on the derived unions in the XMI and the properties that subset them.
- The README in each module folder linked above.

## License

Apache License 2.0, see [LICENSE](LICENSE).

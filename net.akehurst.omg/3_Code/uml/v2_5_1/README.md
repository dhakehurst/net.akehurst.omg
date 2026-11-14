# UML 2.5.1

Kotlin Multiplatform UML 2.5.1, generated from the OMG UML XMI (`specs/UML_2-5-1_ASM_ptc-18-01-01.xmi`), with a reader
for UML model XMI (including stereotype applications) and Compose diagrams.

```kotlin
val result = readUmlXmi(xmiText, "model.xmi")
result.errors.forEach { println(it) }
val model = result.model!!
model.packagedElementSet.filterIsInstance<Class>().map { it.name }
```

## Modules

All modules have the group `net.akehurst.omg`. The Kotlin package is `net.akehurst.omg.uml.v2_5_1`.

| Module | Contents |
|---|---|
| `uml-2_5_1-api` | Generated: one interface per metaclass (package `api.UML.<Package>`, e.g. `api.UML.StructuredClassifiers.Class`), `UML_ModelFactory`, `UML_ModelBuilder`, `UML_ModelResolver`, `UML_ModelAsString`, `UML_ModelReflective` (by-name API), and the derivations of derived properties |
| `uml-2_5_1-ram` | Generated: in-memory classes that implement the api, created by `UML_ModelFactoryRam` |
| `uml-2_5_1-agl-types` | Generated: `UMLTypesDomain`, the AGL types of UML, bound to the ram classes, so that AGL expressions and agl-m2m transforms can read, create and set UML objects |
| `uml-2_5_1-xmi` | Reads UML 2.5.1 models (not metamodels) from XMI into the ram classes (through `UML_ModelReflective`), with stereotype applications |
| `uml-2_5_1-gui-compose` | Compose shapes for UML diagrams (`UmlShapeRegistry`), drawn from the generic [diagram model](../../diagram/diagram-model/README.md) |
| `uml-2_5_1-demo-diagrams` | Demo: one diagram per UML diagram type, made from example XMI by an agl-m2m transform. Not published |

The generated code is not checked in; see [the templates](../../_templates/README.md) for what is generated.

## Building a model in code

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
```

The argument of each builder function (`"P1"`, `"C1"`) is the object's `_identity`. Only stored properties can be
set; derived ones are computed when they are read, e.g. `model.nestedPackageSet` is derived from `packagedElement`.
Derivations that use OCL `allInstances()` look at every object created by the same factory, whether it is in the
model or not: e.g. `clientDependencySet` of a `NamedElement` is the Dependencies of that factory whose `client`
includes it. An object taken out of the model stays in the factory until it is removed from its package factory
(`(e._factory as HierarchicalReferenceStore<Any>).remove(<metaclass>::class, e._identity)`).

Names follow the XMI, with a suffix for collections: `Set` (unique), `OrderedSet` (unique, ordered), `List` (ordered)
or `Collection`. A stored reference `p` also has `pReference`, which holds the reference even when it is not resolved.
The rules are in [Specification.md](../../../1_Requirements/Specification.md).

The ram classes keep the model consistent: adding to a subset adds to its superset, removing from a superset removes
from its subsets, and opposite ends are updated. Collections are changed through the kotlinx `.mutable` extensions:

```kotlin
association.ownedEndOrderedSet.mutable.remove(p)   // also removed from navigableOwnedEnd
```

An opposite end that is a collection computed by redefinition is not updated from the other end. For example
`InteractionFragment.covered[*]` is redefined by `OccurrenceSpecification.covered[1]`, so on an occurrence
specification `coveredSet` is computed from `covered`: adding it to a lifeline's `coveredBySet` does not set
`covered`. Set the redefining property itself (XMI written by tools has both ends, so the reader gets both).

## Building a model by name

`UML_ModelReflective` builds a model from the names of metaclasses and features, as in the OMG XMI, for readers that
don't know the types at compile time (e.g. an XMI or JSON reader). It uses only the api and a `UML_ModelFactory`,
so it works with the ram classes or any other implementation, on JVM, JS and Wasm.

```kotlin
val factory = UML_ModelFactoryRam("myModel")
val r: UML_ModelReflective = UML_ModelReflectiveDefault(factory)

val model = r.construct("Model", "m")!!            // null for an unknown or abstract metaclass
val cls = r.construct("Class", "c1")!!
r.setValue(cls, "name", "Spacecraft")              // inherited features work too
r.addContained(model, "packagedElement", cls)
val p = r.construct("Property", "p1")!!
r.addContained(cls, "ownedAttribute", p)
r.setReference(p, "type", "dt_kg")                 // by identity, resolved later
r.setValue(p, "visibility", "private")             // an enumeration literal by name

UML_ModelResolver("myModel", factory).UML.Packages.Model_resolve(model as Model)
```

| Operation | For |
|---|---|
| `construct(metaclass, identity)` | a new object of a concrete metaclass |
| `setValue(obj, feature, value)` / `addValue` | VALUE features (primitive types, enumerations). Enumeration literals by name; Boolean, Integer, Real, UnlimitedNatural also as a String (`"*"` is -1). `setValue` on a many-valued feature sets the whole content |
| `addContained(obj, feature, child)` | CONTAINED (composite) features, single or many |
| `setReference(obj, feature, id, resolved?)` / `addReference` | REFERENCE features, by identity, and optionally the object itself if the reader has found it. `setReference` is for single-valued ones; `addReference` adds to a many-valued one (or sets a single one) |
| `featureKind(metaclass, feature)` | a `ReflectiveFeature`: kind (VALUE, CONTAINED, REFERENCE), many or single, type name, derived, read-only |
| `metaclassOf(obj)`, `superclasses(metaclass)`, `isAbstract(metaclass)` | the metaclass name of an object; all superclasses, most specific first; whether a metaclass is abstract (null if unknown) |
| `enumLiteral(enumeration, literal)` | the enum value of a literal by name, e.g. `enumLiteral("VisibilityKind", "private")`; null if there is none |

Nothing throws: an unknown name, a value of the wrong type, or a derived or read-only feature gives `null` or `false`,
so a reader can collect warnings. Setting a feature updates subsets, supersets and opposites as the typed api does.
A redefined feature can also be set by the name of the feature it redefines. An identity that never resolves stays as
an unresolved reference in the property's `...Reference`.

## Reading XMI

`uml-2_5_1-xmi` reads UML 2.5.1 model XMI (for example a SysML 1.x model exported from a modelling tool) in common
code, so it works on JVM, JS and Wasm. XML is parsed with [xmlutil](https://github.com/pdvrieze/xmlutil).

```kotlin
val reader = UmlXmiReader(XmiReadOptions(resolver = XmiResourceResolver.fromMap(mapOf("SysML.xmi" to sysmlProfileText))))
reader.load("http://www.omg.org/spec/SysML/20181001/SysML.xmi", sysmlProfileText)   // optional: load profiles first
val result = reader.read("model.xmi", modelText)

result.model                 // the uml:Model, if there is one
result.roots                 // all root objects
result.elementById("_123")   // by xmi:id
result.idOf(element)         // the xmi:id an element was read with (also from a profile or library); uuidOf for xmi:uuid
result.issues                // problems found; see XmiIssueKind
```

- Reading is lenient by default: problems are collected in `result.issues` (`errors`, `warnings`), not thrown.
  Set `XmiReadOptions(lenient = false)` to throw `XmiReadException` on the first error.
- Documents referenced by `href` (profiles, libraries, other model parts) are asked for from the `resolver`.
  The UML PrimitiveTypes library and the UML StandardProfile (ptc/18-01-03, a copy is in `specs`) are built in,
  so they need no resolver. A reference that cannot be resolved is kept as an `XmiProxy`
  in the property's `...Reference`, with its `href`.
- The xmi:id and xmi:uuid stay properties of the XMI, not of the UML elements: `idOf`/`uuidOf` (on the result, the
  `XmiResourceSet` or an `XmiDocument`) look the element up by identity, and `resourceSet.documentOf(element)` gives the
  document it was read from. Elements created after reading give null.
- A read element's `_identity` is its xmi:id (else xmi:uuid), unless an element of any document read by the same reader
  already has it (a duplicate xmi:id, the same xmi:id in another document); it is then generated. So distinct elements
  are never equal.
- `xmi:Extension` elements (tool specific data) are skipped, or kept with `keepExtensions = true`.
- One reader holds one set of documents. Use a new reader for an unrelated model.

### Stereotypes

There is no generated code for profiles. A profile is read as a plain UML model, and the stereotype applications in
the model XMI are matched to it at runtime, through `result.stereotypes`:

```kotlin
val st = result.stereotypes
st.hasStereotype(element, SYSML_URI, "Block")                    // also true for sub-stereotypes of Block
st.elementsWithStereotype(SYSML_URI, "Requirement")
st.tagValue(requirement, SYSML_URI, "Requirement", "text")?.value

val app = st.applicationsOf(element).single()
app.qualifiedName          // e.g. "MyProfile::SafetyRequirement"
app.tag("id")?.value
```

`SYSML_URI` is the profile's URI, the namespace of its stereotype applications in the XMI. A tag value is a
`String`, `Long`, `Double` or `Boolean` (for primitive types), an `EnumerationLiteral`, a model element (or an
`XmiProxy`), or a UML object such as a `ValueSpecification`. When the profile is not loaded, the application is kept
untyped and its tags are the strings as written.

## Diagram demo

`uml-2_5_1-demo-diagrams` shows a class, package, object, component, composite structure, deployment, profile,
activity and sequence diagram, plus a containment tree and an attribute table. Each one is made from an example UML
model in XMI (`DemoDiagrams`):

1. the XMI is read with `UmlXmiReader`;
2. an agl-m2m transform maps the UML element to a generic model (`UmlToDiagram`):
   - `UmlDiagramRules`: a package to a `Diagram`;
   - `UmlSequenceRules`: an interaction to a `Ladder`;
   - `UmlTreeTableRules.containmentTree`: a package to a `Tree` of everything it owns;
   - `UmlTreeTableRules.attributeTable`: a package to a `Table` of the attributes of its classifiers;
3. the result is shown from [net.akehurst.kotlin.compose](https://github.com/dhakehurst/net.akehurst.kotlin.compose):
   a diagram with `UmlShapeRegistry` and the `layout-graph` graph layout, a ladder with `UmlLadderShapeRegistry` and
   the ladder layout, a tree with the `components` `TreeView`, and a table with its `TableView`.

A tree or table can show anything a transform selects; containment and attributes are only examples.

The desktop entry point is `main()` in `src/jvmMain/kotlin/main.kt`. The module has no Gradle run task, so start it
from the IDE. The JVM test `render_DemoDiagrams` writes each view as a PNG to `<java.io.tmpdir>/demo-diagrams`.

## Tests

```sh
./gradlew :uml-2_5_1-ram:jvmTest :uml-2_5_1-xmi:jvmTest
```

`test_UML_OppositePairs` (JVM) sets every class-owned opposite pair of UML from both ends, for every pair of
concrete metaclasses, through `UML_ModelReflective`, and checks that each end holds the other once.

The XMI reader tests use a made-up spacecraft model ("Testsat", `TestsatModel.kt`) with a stand-in SysML profile,
and the SysML 1.7 profile from `3_Code/sysml/v1_7/specs`.

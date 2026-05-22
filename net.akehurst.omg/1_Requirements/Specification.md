# Requirement Specification

The purpose of this specification is to define how to generate Kotlin multiplatform/common code from OMG MOF XMI files.

## 1. Generator customisation
- [REQ-1.1] The generator SHALL have the following configurable parameters.
  - [REQ-1.1.1] `TARGET_PACKAGE` SHALL define the prefix for all Kotlin packages.
  - [REQ-1.1.2] `COPYRIGHT` SHALL define the copyright text for the top of each file.
  - [REQ-1.1.3] External types configuration SHALL be supported.
    - [REQ-1.1.3.1] External types configuration SHALL include a map from URL to type name.
    - [REQ-1.1.3.2] External types configuration SHALL support mapping for types referenced by URL from outside the XMI file.

## 2. MOF Model
- [REQ-2.1] The MOF model SHALL be defined by the main XMI file.
- [REQ-2.2] A MOF model SHALL map to Kotlin as follows.
  - [REQ-2.2.1] Kotlin API: A top-level builder function SHALL be generated.
  - [REQ-2.2.2] Kotlin API: A top-level Factory referencing sub-package factories (if any) SHALL be generated.
  - [REQ-2.2.3] Kotlin realisation: One or more implementation profiles MAY be generated and SHALL implement the generated Kotlin API contracts.

## 3. MOF Package
- [REQ-3.1] A MOF Package SHALL map to Kotlin as follows.
  - [REQ-3.1.1] Kotlin API: A Factory interface SHALL be generated.
  - [REQ-3.1.2] Kotlin API: A Builder enabling construction via a Kotlin builder DSL SHALL be generated.
  - [REQ-3.1.3] Kotlin API: A Resolver for resolving reference properties in a complete structure SHALL be generated.
  - [REQ-3.1.4] Kotlin API: An `AsString` object for creating a string representation of instances, covering each class in the package, SHALL be generated.
  - [REQ-3.1.5] Kotlin realisation: At least one Factory implementation profile SHALL be supported.
    - [REQ-3.1.5.1] Ram profile (optional): A `Ram` Factory implementation MAY be generated and, when generated, SHALL store references until it is garbage collected.

## 4. MOF Primitive Types
- [REQ-4.1] `UML.String` SHALL map to `kotlin.String`.
- [REQ-4.2] `UML.Integer` SHALL map to `kotlin.Long`.
- [REQ-4.3] `UML.Boolean` SHALL map to `kotlin.Boolean`.
- [REQ-4.4] `UML.Real` SHALL map to `kotlin.Double`.
- [REQ-4.5] `UML.UnlimitedNatural` SHALL map to `kotlin.Long`.
- [REQ-4.6] Enums SHALL be considered primitive types.

## 5. MOF Enums
- [REQ-5.1] Kotlin API: Enums SHALL be generated in the Kotlin API module.
- [REQ-5.2] Kotlin API: Enums SHALL be mapped directly to Kotlin enums.
- [REQ-5.3] Kotlin realisation: Implementations SHALL use the generated API enums directly (no alternate enum contract).

## 6. MOF Interfaces
- [REQ-6.1] Kotlin API: Interfaces SHALL be generated in the Kotlin API module.
- [REQ-6.2] Kotlin API: Interfaces SHALL be mapped directly to Kotlin interfaces.
- [REQ-6.3] Kotlin realisation: Implementations SHALL conform to generated API interfaces.

## 7. MOF Classes
- [REQ-7.1] Kotlin API: Each class SHALL be mapped to a Kotlin-valid interface name.
- [REQ-7.2] Kotlin realisation: Each class SHALL support one or more implementation profiles.
- [REQ-7.3] Kotlin realisation: Implementations that use flattened storage SHALL be flat and SHALL implement all final resolved attributes (owned and inherited) after applying `REQ-7.5`.
- [REQ-7.3.1] Ram profile: `Ram` implementation SHALL be flat and SHALL implement all final resolved attributes (owned and inherited) after applying `REQ-7.5`.
- [REQ-7.4] Kotlin API: Every generated class interface SHALL declare `val _identity: Any` if it is a root type (has no supertypes).
  - [REQ-7.4.1] Subtypes SHALL inherit `_identity` from supertypes.
  - [REQ-7.4.2] `_identity` SHALL be immutable and set at object construction time.
  - [REQ-7.4.3] `_identity` SHALL be non-null.
  - [REQ-7.4.4] `_identity` values SHALL be unique per Factory instance at runtime.
  - [REQ-7.4.5] `_identity` SHALL be stable for the lifetime of the object.
- [REQ-7.5] Generated attributes SHALL be resolved using the following ordered rules.
  - [REQ-7.5.1] Collect all attributes from `ownedAttribute` and inherited supertypes.
  - [REQ-7.5.2] For each redefining attribute `A` over `B` (`redefinedProperty`):
    - [REQ-7.5.2.1] If `A` and `B` have the same name, same type, and same collection status, then `A` SHALL override `B` in the interface (`override` getter).
    - [REQ-7.5.2.2] If `A` narrows type (covariance) or narrows multiplicity (for example collection to single), then `A` SHALL override `B`.
    - [REQ-7.5.2.3] If `A` has different name than `B` or different `genType` (collection suffix effect), then `A` SHALL be treated as a separate property and SHALL NOT override `B` in Kotlin.
  - [REQ-7.5.3] If `A` redefines `B`, then `B` SHALL be filtered out from the flattened list.
  - [REQ-7.5.4] For duplicate attribute names after filtering, collision resolution SHALL be applied (covariance, multiplicity narrowing; error if irresolvable).
  - [REQ-7.5.5] Final generated attributes SHALL be deduplicated by `validName` and grouped by originating parent class for documentation.

## 8. MOF Associations
- [REQ-8.1] Association-owned ends SHALL be non-navigable.
- [REQ-8.2] Kotlin API: Class-owned member ends SHALL be exposed as attributes.
- [REQ-8.3] Kotlin realisation: Class-owned member ends SHALL be implemented as attributes.
  - [REQ-8.3.1] Opposite-end setting SHALL be implemented.
  - [REQ-8.3.2] During mutable collection mutations (`add`/`remove`), opposite ends SHALL be set automatically.

## 9. MOF Attributes
- [REQ-9.1] `isID` SHALL not be supported.
- [REQ-9.2] All objects SHALL be identified by `_identity`.
- [REQ-9.3] Every object SHALL have `_identity` as a non-null, unique (per Factory instance), stable identifier for object lifetime.
- [REQ-9.4] `_identity` SHALL be set at construction time and SHALL be immutable.
- [REQ-9.5] `_identity` type SHALL be `Any` (not nullable).

- [REQ-9.6] `isReadOnly` SHALL not be supported in generated code.
- [REQ-9.7] `isReadOnly` values in source models SHALL be ignored by the generator.
- [REQ-9.8] The generator SHALL NOT emit warnings or diagnostics for ignored `isReadOnly` values.
- [REQ-9.9] Every generated class constructor SHALL accept only `_identity: Any`.
- [REQ-9.10] Generated API attribute properties SHALL be read-only Kotlin `val` properties.
  - [REQ-9.10.1] Implementation backing state for attributes SHALL be mutable in general.
  - [REQ-9.10.2] For collection attributes, the Kotlin property SHALL be `val` while the collection instance it exposes SHALL be mutable.
  - [REQ-9.10.3] Attributes with `isDerivedUnion=true` SHALL NOT be mutable.
- [REQ-9.11] Attribute values SHALL be set after construction via builder DSL or explicit `_set()` mutators.

- [REQ-9.12] `isDerived` (non-union) SHALL not be supported.
- [REQ-9.13] Attributes with `isDerived=true` and `isDerivedUnion=false` SHALL be ignored (no interface property, no realization field).

- [REQ-9.14] For `isDerivedUnion=true`:
  - [REQ-9.14.1] A read-only computed getter returning the union of all subsetting properties at query time SHALL be generated.
  - [REQ-9.14.2] No mutator SHALL be generated.

- [REQ-9.15] Subsetting invariants SHALL be enforced.
  - [REQ-9.15.1] If property `p` subsets `q`, runtime SHALL enforce `p ⊆ q`.
  - [REQ-9.15.2] On add/set into subset `p`, implementation SHALL ensure element exists in `q` (auto-add if missing).
  - [REQ-9.15.3] On remove/unset from superset `q`, implementation SHALL reject operation if it violates `p ⊆ q`.
  - [REQ-9.15.4] For single-valued subset/superset:
    - [REQ-9.15.4.1] Setting `p = x` SHALL set `q = x` if `q` is null.
    - [REQ-9.15.4.2] If `q != x`, the operation SHALL fail unless replacement is explicitly allowed.
  - [REQ-9.15.5] For collection-valued properties, enforcement SHALL occur on each mutation (`add`, `remove`, `clear`, bulk operations).
  - [REQ-9.15.6] Violations SHALL throw `IllegalStateException`.

- [REQ-9.16] A reference attribute of type `T` SHALL generate two properties.
  - [REQ-9.16.1] Kotlin API: A resolved-value accessor (`T`, `T?`, or collection of `T`) SHALL be generated.
  - [REQ-9.16.2] A reference-holder accessor named `${propertyName}Reference` SHALL be generated.
    - [REQ-9.16.2.1] Kotlin API (single-valued): interface type SHALL be `Reference<Any, T>`.
    - [REQ-9.16.2.2] Kotlin realisation (single-valued): implementation holder type SHALL be mutable and API-compatible.
    - [REQ-9.16.2.2.1] Ram profile (single-valued): holder type SHALL be `ManagedReference<Any, T>`.
    - [REQ-9.16.2.3] Kotlin API (collection-valued): interface type SHALL be a collection of `Reference<Any, T>`.
    - [REQ-9.16.2.4] Kotlin realisation (collection-valued): implementation holder SHALL be a mutable collection of mutable reference holders compatible with API `Reference<Any, T>`.
    - [REQ-9.16.2.4.1] Ram profile (collection-valued): implementation holder type SHALL be a mutable managed collection of `ManagedReference<Any, T>`.
    - [REQ-9.16.2.5] The managed collection and reference resolution lifecycle SHALL maintain opposite-end consistency.

- [REQ-9.17] Reference type hierarchy SHALL be as follows.
  - [REQ-9.17.1] `Reference<Any, T>` SHALL be read-only and expose `val reference: Any?` and `val resolved: T?`.
  - [REQ-9.17.2] `MutableReference<Any, T>` SHALL extend `Reference<Any, T>` and expose mutable `reference` and `resolved`.
  - [REQ-9.17.3] `ManagedReference<Any, T>` SHALL extend `MutableReference<Any, T>`.
    - [REQ-9.17.3.1] `ManagedReference` SHALL invoke a callback when `.resolved` is set.
    - [REQ-9.17.3.2] The callback SHALL support automatic opposite-end setting for bidirectional associations.
    - [REQ-9.17.3.3] Ram profile: `ManagedReference` SHALL be used in Ram implementations, while APIs SHALL expose `Reference<Any, T>`.

- [REQ-9.18] For single required references (`lowerBound=1`, `upperBound=1`), resolved-value getter SHALL throw `IllegalStateException` if `.resolved` is null.
- [REQ-9.19] For optional references (`lowerBound=0`, `upperBound=1`), resolved-value getter SHALL return `.resolved` directly (nullable).
- [REQ-9.20] For reference collections, resolved-value property SHALL be a getter that extracts `.resolved` values from `${propertyName}Reference`.
  - [REQ-9.20.1] If any referenced element in a collection is unresolved at read time, the getter SHALL throw `IllegalStateException`.
  - [REQ-9.20.2] This unresolved-element behavior SHALL be the same for all collection references.

- [REQ-9.21] Primitive and enum typed attributes SHALL be treated as composite.
- [REQ-9.22] Class interfaces and realizations SHALL expose getters only.
- [REQ-9.23] Collection attributes SHALL append collection type suffix to generated names.
- [REQ-9.24] Reference aggregation SHALL use Kotlin `Reference` implementations resolved against a reference store (Factory).
- [REQ-9.25] Managed collection types SHALL be used for mutable collection implementations.
  - [REQ-9.25.1] Managed collections SHALL perform runtime type checking on element add.
  - [REQ-9.25.2] Managed collections SHALL augment mutations with callbacks for opposite-end setting.
- [REQ-9.26] Ram profile: Single reference attributes in Ram SHALL use `ManagedReference`.
- [REQ-9.27] Collection and reference attributes in implementations SHALL be mutable.
  - [REQ-9.27.1] Composite collections SHALL be initialized using `ManagedCollection<T>`, `ManagedSet<T>`, `ManagedList<T>`, or `ManagedOrderedSet<T>` based on `isUnique` and `isOrdered`.
  - [REQ-9.27.2] Reference collections SHALL store mutable `ManagedReference<Any, T>` items.
  - [REQ-9.27.3] Mutable access SHALL be available via property accessors and/or `.mutableCollection()`, `.mutableSet()`, `.mutableList()`, `.mutableOrderedSet()` style extension functions.
  - [REQ-9.27.4] Managed collections SHALL invoke callbacks on add/remove to support automatic opposite-end setting.
  - [REQ-9.27.5] Opposite-end updates for collection references SHALL occur when references become resolved, are re-resolved, or are removed.
  - [REQ-9.27.6] If a reference is unresolved, no opposite-end relationship SHALL be created from that reference entry.

- [REQ-9.28] Generation SHALL precompute the following values.
  - [REQ-9.28.1] `Type.isPrimitive := Enum | String | Integer | Boolean | Real | UnlimitedNatural`.
  - [REQ-9.28.2] `Attribute.isSingle := (upperBound == 1)`.
  - [REQ-9.28.3] `Attribute.isCollection := (upperBound == -1 || upperBound > 1)`.
  - [REQ-9.28.4] `Attribute.isOptional := (lowerBound == 0 && isSingle)`.
  - [REQ-9.28.5] `Attribute.genType` SHALL be derived as follows.
    - [REQ-9.28.5.1] Single optional -> `${type}?`.
    - [REQ-9.28.5.2] Single required -> `${type}`.
    - [REQ-9.28.5.3] Collection non-unique non-ordered -> `Collection<${type.validName}>`.
    - [REQ-9.28.5.4] Collection unique non-ordered -> `Set<${type.validName}>`.
    - [REQ-9.28.5.5] Collection non-unique ordered -> `List<${type.validName}>`.
    - [REQ-9.28.5.6] Collection unique ordered -> `OrderedSet<${type.validName}>`.
  - [REQ-9.28.6] `Attribute.genName` SHALL be derived by suffixing collection names.
    - [REQ-9.28.6.1] Non-unique/non-ordered -> `${name}Collection`.
    - [REQ-9.28.6.2] Unique/non-ordered -> `${name}Set`.
    - [REQ-9.28.6.3] Non-unique/ordered -> `${name}List`.
    - [REQ-9.28.6.4] Unique/ordered -> `${name}OrderedSet`.
    - [REQ-9.28.6.5] Resulting names SHALL be validated against Kotlin keywords and problematic types.

### 9.29 Kotlin Interface Accessor
- [REQ-9.29.1] Interface accessors SHALL be read-only `val` properties.
  - [REQ-9.29.1.1] This SHALL allow covariant overriding in subtypes.
- [REQ-9.29.2] Accessor types SHALL be generated as follows.
  - [REQ-9.29.2.1] Composite attributes SHALL use `genType`.
  - [REQ-9.29.2.2] Reference attributes SHALL generate two accessors.
    - [REQ-9.29.2.2.1] Resolved value accessor type SHALL be `T` (required) or `T?` (optional).
    - [REQ-9.29.2.2.2] Required unresolved reference access SHALL throw `IllegalStateException`.
    - [REQ-9.29.2.2.3] Optional unresolved reference access SHALL return null.
    - [REQ-9.29.2.2.4] For single-valued references, `${propertyName}Reference` SHALL have interface type `Reference<Any, T>`.
    - [REQ-9.29.2.2.5] For collection-valued references, `${propertyName}Reference` SHALL have interface type as a collection of `Reference<Any, T>`.
- [REQ-9.29.3] If `A` redefines `B` and both share `validName`, Kotlin `override` SHALL be used.
- [REQ-9.29.4] Subsetting (`subsettedProperty`) SHALL enforce runtime subset invariants but SHALL NOT imply Kotlin `override`.

### 9.30 Kotlin Interface Mutator
- [REQ-9.30.1] Mutators SHALL NOT be member functions of interfaces.
- [REQ-9.30.2] Mutators SHALL be generated as top-level Kotlin extension functions in the same file as class implementation.
- [REQ-9.30.3] Extension mutator naming SHALL be `fun ${className}.${propertyName}_set(value: ${genType})`.
- [REQ-9.30.4] No mutator SHALL be generated for `isDerived=true` or `isDerivedUnion=true` attributes.

### 9.31 Kotlin Realisation Accessor
- [REQ-9.31.1] Each Kotlin realisation class SHALL implement the API interface.
- [REQ-9.31.1.1] Ram profile: RAM class (`${name}Ram`) SHALL implement the API interface.
- [REQ-9.31.2] Single composite properties SHALL be backed by private/internal fields of `genType` and exposed via public `override val` getters.
  - [REQ-9.31.2.1] Optional properties SHALL initialize to `null`.
  - [REQ-9.31.2.2] Required properties SHALL initialize to a default value.
- [REQ-9.31.3] Composite and reference collections SHALL be backed by mutable implementation collections and exposed via public `override val` getters.
  - [REQ-9.31.3.1] Ram profile (collection-valued references): the managed collection element type SHALL be `ManagedReference<Any, T>`.
- [REQ-9.31.4] Single reference properties SHALL be backed by mutable implementation reference holders and exposed through API-compatible `Reference<Any, T>` accessors.
  - [REQ-9.31.4.1] Ram profile: single reference properties SHALL be backed by `ManagedReference<Any, T>` and exposed via `override val ${propertyName}Reference: ManagedReference<Any, T>`.
  - [REQ-9.31.4.2] Callback configuration SHALL occur at construction time to set opposite end when `.resolved` is assigned.

### 9.32 Kotlin Realisation Mutator
- [REQ-9.32.1] If `isDerivedUnion=true`, realization SHALL NOT generate a mutator.
- [REQ-9.32.2] Mutators SHALL be generated only for single composite properties.
  - [REQ-9.32.2.1] Single composite mutator SHALL be `fun ${className}.${propertyName}_set(value: ${genType})`.
  - [REQ-9.32.2.2] Single reference mutator SHALL NOT be generated; mutation SHALL occur through mutable implementation reference holders.
  - [REQ-9.32.2.3] Collection mutator SHALL NOT be generated; mutation SHALL occur via collection operations (`add`, `remove`, etc.).
  - [REQ-9.32.2.4] Single composite mutator naming SHALL use `${name}_set(value: ${genType})` to avoid Kotlin setter name clashes.
- [REQ-9.32.3] Subject to `REQ-9.32.2`, if a redefined attribute has both different type and different name, its mutator SHALL be generated as a distinct extension function (no override attempt).
- [REQ-9.32.4] Each generated mutator SHALL be implemented as a top-level Kotlin extension function in generated implementation code.
  - [REQ-9.32.4.1] Ram profile: generated mutators SHALL be implemented in generated Ram code.
- [REQ-9.32.5] Opposite-end update logic SHALL be invoked when setting composite single-valued properties that define an opposite.
- [REQ-9.32.6] For collection-valued references, opposite-end update logic SHALL be invoked on reference resolve, re-resolve, and removal transitions.
- [REQ-9.32.7] Derived and derived-union properties SHALL have no mutator; attempted mutation paths SHALL fail with `IllegalStateException`.

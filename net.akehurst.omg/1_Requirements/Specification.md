# Requirement Specification

The purpose of this specification is to define how to generate Kotlin multiplatform/common code from OMG MOF XMI files.



## 1. General Requirements

- [REQ-1.1] The generator SHALL use external libraries to reduce the size of generated code.
- [REQ-1.2] The generator SHALL use Kotlin language features to reduce the size of generated code.

## 2. API Requirements

This section defines the Kotlin API contract that generators SHALL produce and that all implementations SHALL honor.
It covers externally visible types, accessors, mutator surface, invariants, and error behavior expected by API consumers.

### 2.1 Generator configuration and input model
- [REQ-2.1.1] The generator SHALL support the following configuration parameters.
  - [REQ-2.1.1.1] `TARGET_PACKAGE` SHALL define the prefix for all generated Kotlin packages.
  - [REQ-2.1.1.2] `COPYRIGHT` SHALL define the copyright text added to generated files.
- [REQ-2.1.2] External type mapping SHALL be supported.
  - [REQ-2.1.2.1] External type mapping SHALL include URL -> type-name mapping.
  - [REQ-2.1.2.2] External type mapping SHALL support types referenced by URL from outside the source XMI.
  - [REQ-2.1.2.3] If an external referenced type has no mapping, generation SHALL fail with a diagnostic that includes the unresolved URL.
    - [REQ-2.1.2.3.1] Generation SHALL emit a diagnostic with severity "error" and NOT generate any output files.
    - [REQ-2.1.2.3.2] The generator SHALL exit with a non-zero status code on the host platform (for example, 1 on Unix-like systems).
- [REQ-2.1.3] The MOF model SHALL be defined by a main XMI file.

### 2.2 Package-level API artifacts
- [REQ-2.2.1] For each mapped model, a top-level API builder entrypoint SHALL be generated.
- [REQ-2.2.2] For each mapped model, a top-level API Factory that references sub-package factories (if any) SHALL be generated.
- [REQ-2.2.3] For each mapped package, the API SHALL include:
  - [REQ-2.2.3.1] a Factory interface,
  - [REQ-2.2.3.2] a Builder DSL API,
  - [REQ-2.2.3.3] a Resolver API,
  - [REQ-2.2.3.4] a Serialisation API.

### 2.3 Type and classifier mapping (API)
- [REQ-2.3.1] Primitive type mapping SHALL be:
  - [REQ-2.3.1.1] `UML.String` -> `kotlin.String`.
  - [REQ-2.3.1.2] `UML.Integer` -> `kotlin.Long`.
  - [REQ-2.3.1.3] `UML.Boolean` -> `kotlin.Boolean`.
  - [REQ-2.3.1.4] `UML.Real` -> `kotlin.Double`.
  - [REQ-2.3.1.5] `UML.UnlimitedNatural` -> `kotlin.Long`.
  - [REQ-2.3.1.6] `UnlimitedNatural` literal `*` SHALL map to `-1`.
- [REQ-2.3.2] Enums SHALL be treated as primitive types for attribute typing rules.
- [REQ-2.3.3] API enums SHALL be generated in the Kotlin API module and mapped to Kotlin enums.
- [REQ-2.3.4] API interfaces SHALL be generated in the Kotlin API module and mapped to Kotlin interfaces.

### 2.4 Class and association contracts (API)
- [REQ-2.4.1] Each MOF class SHALL map to a Kotlin API interface with Kotlin-valid naming.
- [REQ-2.4.2] For root API types (no supertypes), `val _identity: Any` SHALL be declared.
  - [REQ-2.4.2.1] Subtypes SHALL inherit `_identity` from supertypes.
  - [REQ-2.4.2.2] `_identity` SHALL be non-null.
  - [REQ-2.4.2.3] `_identity` SHALL be immutable.
  - [REQ-2.4.2.4] `_identity` values SHALL be unique per model Factory instance at runtime.
  - [REQ-2.4.2.5] `_identity` SHALL be stable for object lifetime.
- [REQ-2.4.3] Association-owned ends SHALL be non-navigable in API.
- [REQ-2.4.4] Class-owned member ends SHALL be exposed as API attributes.

### 2.5 Attribute resolution and naming (API)
- [REQ-2.5.1] Attributes SHALL be resolved in this order.
  - [REQ-2.5.1.1] Collect `ownedAttribute` plus inherited attributes.
  - [REQ-2.5.1.2] Apply redefinition rules for each `A` redefining `B`.
    - [REQ-2.5.1.2.1] If same name, same type, and same collection status, `A` SHALL override `B` in Kotlin API.
    - [REQ-2.5.1.2.2] If `A` narrows type or multiplicity, `A` SHALL override `B`.
    - [REQ-2.5.1.2.3] If name differs or `genType` differs, `A` SHALL be separate and SHALL NOT override `B`.
  - [REQ-2.5.1.3] Redefined targets SHALL be filtered from final API attribute set.
  - [REQ-2.5.1.4] Duplicate names after filtering that collide on `validName` SHALL fail generation.
  - [REQ-2.5.1.5] Final API attribute set SHALL be deduplicated by `validName` and grouped by originating parent for documentation.
  - [REQ-2.5.1.6] Failures under [REQ-2.5.1.4] SHALL include a diagnostic identifying the colliding source properties and owning classifiers.
  - [REQ-2.5.1.7] Collision detection SHALL include conflicts with:
    - [REQ-2.5.1.7.1] Reference holder generated names (e.g., if property `P` generates `${P}Reference`, collision with any other property or holder named `${P}Reference`).
    - [REQ-2.5.1.7.2] Collection suffix names (e.g., `${name}List`, `${name}Set`, `${name}OrderedSet` colliding with direct property names or other suffixed names).
- [REQ-2.5.2] API collection attribute names SHALL append collection suffixes.
  - [REQ-2.5.2.1] non-unique/non-ordered -> `${name}Collection`
  - [REQ-2.5.2.2] unique/non-ordered -> `${name}Set`
  - [REQ-2.5.2.3] non-unique/ordered -> `${name}List`
  - [REQ-2.5.2.4] unique/ordered -> `${name}OrderedSet`

### 2.6 Attribute behavior and invariants (API)
- [REQ-2.6.1] `isID` SHALL be supported.
  - [REQ-2.6.1.1] If source MOF specifies `isID=true`, generation SHALL use attributes with `isID==true` as the identity of an object.
  - [REQ-2.6.1.2] If no property with `isID=true` is defined, `_identity:Any` SHALL be the object identifier contract.
- [REQ-2.6.2] `isReadOnly` SHALL not be supported.
  - [REQ-2.6.2.1] Source `isReadOnly` values SHALL be ignored.
  - [REQ-2.6.2.2] No warnings or diagnostics SHALL be emitted for ignored `isReadOnly`.
- [REQ-2.6.3] `isDerived=true` and `isDerivedUnion=false` attributes SHALL be omitted from API.
- [REQ-2.6.4] `isDerivedUnion=true` attributes SHALL be API read-only computed getters with no mutator.
- [REQ-2.6.5] API attribute properties SHALL be Kotlin `val`.
- [REQ-2.6.6] Subsetting invariants SHALL be enforced at runtime.
  - [REQ-2.6.6.1] For subset `p` of `q`, runtime SHALL enforce `p ⊆ q`.
  - [REQ-2.6.6.2] Adding/setting into `p` SHALL ensure membership in `q`.
  - [REQ-2.6.6.3] Removing/unsetting from `q` SHALL be rejected if `p ⊆ q` would be violated.
    - [REQ-2.6.6.3.1] Rejection means the operation fails (no cascade removal from `p` occurs).
    - [REQ-2.6.6.3.2] Error message SHALL identify which subset(s) would be violated.
  - [REQ-2.6.6.4] Violations SHALL throw `IllegalStateException`.

### 2.7 Reference contracts (API)
- [REQ-2.7.1] For reference attributes of type `T`, two API properties SHALL be generated.
  - [REQ-2.7.1.1] A resolved-value API accessor.
  - [REQ-2.7.1.2] A `${propertyName}Reference` API accessor.
- [REQ-2.7.2] API reference interfaces SHALL be read-only:
  - [REQ-2.7.2.1] `Reference<Any, T>` with immutable properties `val reference: Any?` and `val resolved: T?`.
  - [REQ-2.7.2.2] No `MutableReference` type SHALL appear in the API contract (mutation is a realisation concern).
- [REQ-2.7.3] For single-valued references, API `${propertyName}Reference` type SHALL be `Reference<Any, T>`.
- [REQ-2.7.4] For collection-valued references, API `${propertyName}Reference` type SHALL be a collection of `Reference<Any, T>`.
- [REQ-2.7.5] For collection-valued references, `${propertyName}Reference` SHALL preserve collection kind from source semantics:
  - [REQ-2.7.5.1] non-unique/non-ordered -> `Collection<Reference<Any, T>>`
  - [REQ-2.7.5.2] unique/non-ordered -> `Set<Reference<Any, T>>`
  - [REQ-2.7.5.3] non-unique/ordered -> `List<Reference<Any, T>>`
  - [REQ-2.7.5.4] unique/ordered -> `OrderedSet<Reference<Any, T>>`
- [REQ-2.7.6] Single required resolved-value getter (`lowerBound=1`, `upperBound=1`) SHALL throw `IllegalStateException` if unresolved.
  - [REQ-2.7.6.1] Exception message text is implementation-defined and SHALL NOT be standardized by this specification.
- [REQ-2.7.7] Single optional resolved-value getter (`lowerBound=0`, `upperBound=1`) SHALL return nullable resolved value.
- [REQ-2.7.8] Collection resolved-value getter SHALL extract resolved values from `${propertyName}Reference`.
  - [REQ-2.7.8.1] If any element is unresolved, getter SHALL throw `IllegalStateException`.
  - [REQ-2.7.8.2] This behavior SHALL be uniform for all collection references.
  - [REQ-2.7.8.3] Extraction occurs on each getter invocation (lazy evaluation).
  - [REQ-2.7.8.4] Implementations MAY cache extracted results, but only if reference store mutations are tracked and cache is invalidated on change.

- [REQ-2.7.9] Composite value-holder contract (API): For every single-valued composite attribute of type `T` the API SHALL expose both a resolved-value accessor and a read-only value-holder accessor.
  - [REQ-2.7.9.1] A resolved-value accessor SHALL be generated (`val {name}: T` for required, `val {name}: T?` for optional).
  - [REQ-2.7.9.2] A value-holder accessor SHALL be generated with the name `{name}Value` and type `Value<{genType}>` (e.g., `Value<T>` for required, `Value<T?>` for optional).
  - [REQ-2.7.9.3] The API-level `Value` contract SHALL be read-only (no mutator methods) and SHALL provide access to the stored value; implementations MAY provide `MutableValue`/`ManagedValue` in realisations.
  - [REQ-2.7.9.4] The resolved-value accessor SHALL obtain its result from the `{name}Value` holder and SHALL throw `IllegalStateException` for required composites when the holder is empty.
  - [REQ-2.7.9.5] The generated `{name}Value` holder name SHALL be considered during name-collision detection and is subject to the same collision rules as reference holders and collection suffixes (see REQ-2.5.1.7).

### 2.8 API accessor shape
- [REQ-2.8.1] API accessors SHALL be read-only `val` and support covariant override.
- [REQ-2.8.2] If redefining attribute `A` and redefined `B` share `validName`, Kotlin `override` SHALL be used.
- [REQ-2.8.3] Subsetting SHALL NOT imply Kotlin `override`.
- [REQ-2.8.4] Collection attributes SHALL expose immutable collection types in the API.
  - [REQ-2.8.4.1] Collection properties return `Collection<T>`, `Set<T>`, `List<T>`, or `OrderedSet<T>` (immutable contract).
- [REQ-2.8.5] Single reference attributes SHALL expose `Reference<Any, T>` in the API (immutable contract).
  - [REQ-2.8.5.1] No `MutableReference` type SHALL appear in API; only `Reference<Any, T>`.
- [REQ-2.8.6] API SHALL NOT include mutator extension functions; mutation is the responsibility of the realisation via extensions.
 - [REQ-2.8.7] API SHALL be permitted to include read-only `Value<*>` holder properties for single-valued composite attributes (see REQ-2.7.9). These holder properties are part of the API contract but do not expose mutators; mutability is a realisation concern.

### 2.9 Resolver API contract
- [REQ-2.9.1] For each mapped model, a top-level Resolver entrypoint SHALL be generated.
- [REQ-2.9.2] Resolver APIs SHALL accept one or more model root objects and a reference store/context needed for reference resolution.
- [REQ-2.9.3] Resolver execution SHALL attempt reference resolution for all reachable reference holders from the provided roots.
- [REQ-2.9.4] Resolver execution SHALL be idempotent for unchanged model state and unchanged reference store/context.
- [REQ-2.9.5] Unresolved optional references SHALL NOT by themselves cause Resolver execution to fail.

## 3. Realisation Requirements

This section defines implementation-level requirements for any Kotlin realisation of the generated API.
It specifies storage, mutability, runtime invariant enforcement, mutation behavior, 
and generation-time derived values independent of any specific implementation profile.

### 3.1 Realisation profiles and conformance
- [REQ-3.1.1] One or more Kotlin realisation profiles MAY be provided.
- [REQ-3.1.2] Every realisation class SHALL implement the generated API interfaces.
- [REQ-3.1.3] Every realisation profile SHALL preserve API runtime invariants and error semantics.

### 3.2 Construction and mutability
- [REQ-3.2.1] Realisation constructors for generated classes SHALL accept only `_identity: Any`.
- [REQ-3.2.2] Realisation backing state SHALL be mutable in general.
- [REQ-3.2.3] API properties SHALL remain `val` with immutable return types; mutability SHALL be accessed through realisation-level extension functions and internal mutable backing storage.
- [REQ-3.2.4] For collection attributes, API property returns immutable `Collection<T>` (or appropriate kind), but realisation backing store is mutable.
- [REQ-3.2.5] `isDerivedUnion=true` realization attributes SHALL not be mutable.
  - [REQ-3.2.5.1] Derived union attributes SHALL be implemented as read-only `override val` properties with computed getters.
  - [REQ-3.2.5.2] Implementation logic (computation) is provided by hand; no automatic generation of derived union logic is performed.

### 3.3 Accessor and storage behavior
- [REQ-3.3.1] Realisation storage strategy MAY be flattened.
  - [REQ-3.3.1.1] If flattened, implementation SHALL include all final resolved attributes after applying API resolution rules.
- [REQ-3.3.2] Single composite attributes SHALL be backed by fields and exposed via `override val` getters.
  - [REQ-3.3.2.1] Optional fields SHALL initialize to `null`.
  - [REQ-3.3.2.2] Required fields SHALL be initialized by generated construction paths before first observable read.
  - [REQ-3.3.2.3] If a required field is read before initialization, the getter SHALL throw `IllegalStateException`.
  - [REQ-3.3.2.4] For single-valued composite attributes the API SHALL include a read-only `Value<{genType}>` holder property named `{name}Value` (see REQ-2.7.9). Realisation classes SHALL provide a mutable backing implementation (e.g., `MutableValue<{genType}>` or `ManagedValue<{genType}>`) for this holder.
  - [REQ-3.3.2.5] Realisation implementations SHALL ensure the resolved-value getter (`{name}`) delegates to the corresponding `{name}Value` holder and enforces required/optional semantics (throwing `IllegalStateException` for uninitialized required values).
  - [REQ-3.3.2.6] RAM realisations that need to maintain opposite-end consistency SHALL use `ManagedValue` (or an equivalent implementation) as the backing `MutableValue` so that changes to composite holders can invoke callbacks and run opposite-end update logic.
- [REQ-3.3.3] Composite and reference collections SHALL be backed by mutable implementation collections and exposed via immutable `override val` getters returning API types.
- [REQ-3.3.4] Single references SHALL be backed by mutable reference holders (e.g., `MutableReference<Any, T>` in realisation) and exposed via immutable `Reference<Any, T>` API accessors.
- [REQ-3.3.5] For reference collections, implementation SHALL use mutable collections of mutable reference holders internally, exposed as immutable `Collection<Reference<Any, T>>` (or appropriate collection kind) in the API.

### 3.4 Mutator behavior and opposite-end consistency

**Note:** Mutation in the realisation layer is a realisation concern. The API contract exposes only immutable accessor types.

The realisation backing store is mutable and mutation SHALL occur via the realisation's mutable holders and APIs (for example
`MutableValue`/`ManagedValue` for composites, `MutableReference`/`ManagedReference` for references, or mutable backing
collections exposed via wrapper APIs). Generator implementations SHALL NOT emit a standard set of top-level mutator extension
functions as part of the generated output; any convenience mutators are an implementation detail of a realisation and optional.

- [REQ-3.4.2] Collection mutation SHALL occur through realisation-provided extensions or direct casting to the mutable backing type.
- [REQ-3.4.3] Single reference mutation SHALL occur through realisation-level access to `MutableReference` properties (not exposed in API).
- [REQ-3.4.4] Redefinition mutators with different type and name MAY be generated as distinct extension functions.
- [REQ-3.4.5] Opposite-end update logic SHALL run for composite single-valued mutations when opposite exists.
- [REQ-3.4.6] For collection references, opposite-end update logic SHALL run on resolve, re-resolve, and removal transitions.
- [REQ-3.4.7] If reference entry is unresolved, no opposite-end relationship SHALL be created for that entry.
- [REQ-3.4.8] Attempted mutation paths for derived/derived-union attributes SHALL fail with `IllegalStateException`.
  - [REQ-3.4.8.1] Opposite-end synchronization SHALL preserve subsetting invariants; operations that would violate [REQ-2.6.6] SHALL be rejected with `IllegalStateException`.

### 3.5 Precomputed generation values
- [REQ-3.5.1] Generation SHALL precompute these values.
  - [REQ-3.5.1.1] `Type.isPrimitive := Enum | String | Integer | Boolean | Real | UnlimitedNatural`.
  - [REQ-3.5.1.2] `Attribute.isSingle := (upperBound == 1)`.
  - [REQ-3.5.1.3] `Attribute.isCollection := (upperBound == -1 || upperBound > 1)`.
  - [REQ-3.5.1.4] `Attribute.isOptional := (lowerBound == 0 && isSingle)`.
  - [REQ-3.5.1.5] `Attribute.isRequired := (lowerBound == 1 && isSingle)`.
  - [REQ-3.5.1.6] `Attribute.genType` SHALL derive to:
    - [REQ-3.5.1.6.1] `${type}?` for single optional.
    - [REQ-3.5.1.6.2] `${type}` for single required.
    - [REQ-3.5.1.6.3] `Collection<${type.validName}>` for non-unique/non-ordered.
    - [REQ-3.5.1.6.4] `Set<${type.validName}>` for unique/non-ordered.
    - [REQ-3.5.1.6.5] `List<${type.validName}>` for non-unique/ordered.
    - [REQ-3.5.1.6.6] `OrderedSet<${type.validName}>` for unique/ordered.
  - [REQ-3.5.1.7] Resulting names SHALL be validated against Kotlin keywords and problematic types.
  - [REQ-3.5.1.8] For single-valued composite attributes, generators SHALL precompute the value-holder name `{name}Value` and its type `Value<{genType}>` (where `{genType}` follows the rules in [REQ-3.5.1.6]). The computed holder name SHALL be included in name-collision detection (see REQ-2.5.1.7).

## 4. RAM Realisation Requirements

This section defines the Ram realisation profile as an illustrative and useful concrete implementation of the API 
and realisation contracts. It specializes the general realisation rules with Ram-specific holder types, 
callback behavior, and generated mutator conventions.

### 4.1 RAM profile scope
- [REQ-4.1.1] Ram realisation SHALL be an illustrative and useful Kotlin realisation profile.
- [REQ-4.1.2] Ram profile SHALL conform to all API and general realisation requirements.

### 4.2 RAM factories and object retention
- [REQ-4.2.1] Ram profile MAY generate a `Ram` Factory implementation per mapped package/model.
- [REQ-4.2.2] When generated, Ram factory SHALL store created object references until it is garbage collected.

### 4.3 RAM reference and collection mechanics
- [REQ-4.3.1] Ram single reference holders SHALL use `ManagedReference<Any, T>`.
- [REQ-4.3.2] Ram collection-valued reference holders SHALL use mutable managed collections of `ManagedReference<Any, T>`.
- [REQ-4.3.3] `ManagedReference` SHALL invoke callbacks when `.resolved` changes.
- [REQ-4.3.4] Managed collections SHALL invoke callbacks on add/remove.
- [REQ-4.3.5] Combined managed reference/collection callback lifecycle SHALL maintain opposite-end consistency.
 - [REQ-4.3.6] Ram single composite holders SHALL use `ManagedValue<T>` (or equivalent) to back API `Value<T>` holders. `ManagedValue` SHALL invoke callbacks when the stored value changes so that opposite-end update logic and subsetting invariants can be enforced by the Ram realisation.

### 4.4 RAM mutator realization
- [REQ-4.4.1] Generator implementations SHALL NOT emit a mandatory set of top-level mutator extension functions for Ram realisations. Mutation in Ram shall be achieved via mutable backing holders (e.g., `ManagedValue`, `ManagedReference`, managed collections) and realisation-specific APIs that enforce opposite-end consistency and invariants.
  
  

# Requirement Specification

The purpose of this specification is to define how to generate Kotlin multiplatform/common code from OMG MOF XMI files.

## 1. API Requirements

This section defines the Kotlin API contract that generators SHALL produce and that all implementations SHALL honor.
It covers externally visible types, accessors, mutator surface, invariants, and error behavior expected by API consumers.

### 1.1 Generator configuration and input model
- [REQ-1.1] The generator SHALL support the following configuration parameters.
  - [REQ-1.1.1] `TARGET_PACKAGE` SHALL define the prefix for all generated Kotlin packages.
  - [REQ-1.1.2] `COPYRIGHT` SHALL define the copyright text added to generated files.
  - [REQ-1.1.3] External type mapping SHALL be supported.
    - [REQ-1.1.3.1] External type mapping SHALL include URL -> type-name mapping.
    - [REQ-1.1.3.2] External type mapping SHALL support types referenced by URL from outside the source XMI.
- [REQ-1.2] The MOF model SHALL be defined by a main XMI file.

### 1.2 Package-level API artifacts
- [REQ-1.3] For each mapped model, a top-level API builder entrypoint SHALL be generated.
- [REQ-1.4] For each mapped model, a top-level API Factory that references sub-package factories (if any) SHALL be generated.
- [REQ-1.5] For each mapped package, the API SHALL include:
  - [REQ-1.5.1] a Factory interface,
  - [REQ-1.5.2] a Builder DSL API,
  - [REQ-1.5.3] a Resolver API,
  - [REQ-1.5.4] an `AsString` API.

### 1.3 Type and classifier mapping (API)
- [REQ-1.6] Primitive type mapping SHALL be:
  - [REQ-1.6.1] `UML.String` -> `kotlin.String`.
  - [REQ-1.6.2] `UML.Integer` -> `kotlin.Long`.
  - [REQ-1.6.3] `UML.Boolean` -> `kotlin.Boolean`.
  - [REQ-1.6.4] `UML.Real` -> `kotlin.Double`.
  - [REQ-1.6.5] `UML.UnlimitedNatural` -> `kotlin.Long`.
- [REQ-1.7] Enums SHALL be treated as primitive types for attribute typing rules.
- [REQ-1.8] API enums SHALL be generated in the Kotlin API module and mapped to Kotlin enums.
- [REQ-1.9] API interfaces SHALL be generated in the Kotlin API module and mapped to Kotlin interfaces.

### 1.4 Class and association contracts (API)
- [REQ-1.10] Each MOF class SHALL map to a Kotlin API interface with Kotlin-valid naming.
- [REQ-1.11] For root API types (no supertypes), `val _identity: Any` SHALL be declared.
  - [REQ-1.11.1] Subtypes SHALL inherit `_identity` from supertypes.
  - [REQ-1.11.2] `_identity` SHALL be non-null.
  - [REQ-1.11.3] `_identity` SHALL be immutable.
  - [REQ-1.11.4] `_identity` values SHALL be unique per Factory instance at runtime.
  - [REQ-1.11.5] `_identity` SHALL be stable for object lifetime.
- [REQ-1.12] Association-owned ends SHALL be non-navigable in API.
- [REQ-1.13] Class-owned member ends SHALL be exposed as API attributes.

### 1.5 Attribute resolution and naming (API)
- [REQ-1.14] Attributes SHALL be resolved in this order.
  - [REQ-1.14.1] Collect `ownedAttribute` plus inherited attributes.
  - [REQ-1.14.2] Apply redefinition rules for each `A` redefining `B`.
    - [REQ-1.14.2.1] If same name, same type, and same collection status, `A` SHALL override `B` in Kotlin API.
    - [REQ-1.14.2.2] If `A` narrows type or multiplicity, `A` SHALL override `B`.
    - [REQ-1.14.2.3] If name differs or `genType` differs, `A` SHALL be separate and SHALL NOT override `B`.
  - [REQ-1.14.3] Redefined targets SHALL be filtered from final API attribute set.
  - [REQ-1.14.4] Duplicate names after filtering SHALL undergo collision resolution; irresolvable collisions SHALL fail generation.
  - [REQ-1.14.5] Final API attribute set SHALL be deduplicated by `validName` and grouped by originating parent for documentation.
- [REQ-1.15] API collection attribute names SHALL append collection suffixes.
  - [REQ-1.15.1] non-unique/non-ordered -> `${name}Collection`
  - [REQ-1.15.2] unique/non-ordered -> `${name}Set`
  - [REQ-1.15.3] non-unique/ordered -> `${name}List`
  - [REQ-1.15.4] unique/ordered -> `${name}OrderedSet`

### 1.6 Attribute behavior and invariants (API)
- [REQ-1.16] `isID` SHALL not be supported; `_identity` SHALL be the object identifier contract.
- [REQ-1.17] `isReadOnly` SHALL not be supported.
  - [REQ-1.17.1] Source `isReadOnly` values SHALL be ignored.
  - [REQ-1.17.2] No warnings or diagnostics SHALL be emitted for ignored `isReadOnly`.
- [REQ-1.18] `isDerived=true` and `isDerivedUnion=false` attributes SHALL be omitted from API.
- [REQ-1.19] `isDerivedUnion=true` attributes SHALL be API read-only computed getters with no mutator.
- [REQ-1.20] API attribute properties SHALL be Kotlin `val`.
- [REQ-1.21] Subsetting invariants SHALL be enforced at runtime.
  - [REQ-1.21.1] For subset `p` of `q`, runtime SHALL enforce `p ⊆ q`.
  - [REQ-1.21.2] Adding/setting into `p` SHALL ensure membership in `q`.
  - [REQ-1.21.3] Removing/unsetting from `q` SHALL be rejected if `p ⊆ q` would be violated.
  - [REQ-1.21.4] Violations SHALL throw `IllegalStateException`.

### 1.7 Reference contracts (API)
- [REQ-1.22] For reference attributes of type `T`, two API properties SHALL be generated.
  - [REQ-1.22.1] A resolved-value API accessor.
  - [REQ-1.22.2] A `${propertyName}Reference` API accessor.
- [REQ-1.23] API reference interfaces SHALL be:
  - [REQ-1.23.1] `Reference<Any, T>` with `val reference: Any?` and `val resolved: T?`.
  - [REQ-1.23.2] `MutableReference<Any, T>` extending `Reference<Any, T>` with mutable `reference` and `resolved`.
- [REQ-1.24] For single-valued references, API `${propertyName}Reference` type SHALL be `Reference<Any, T>`.
- [REQ-1.25] For collection-valued references, API `${propertyName}Reference` type SHALL be a collection of `Reference<Any, T>`.
- [REQ-1.26] Single required resolved-value getter (`lowerBound=1`, `upperBound=1`) SHALL throw `IllegalStateException` if unresolved.
- [REQ-1.27] Single optional resolved-value getter (`lowerBound=0`, `upperBound=1`) SHALL return nullable resolved value.
- [REQ-1.28] Collection resolved-value getter SHALL extract resolved values from `${propertyName}Reference`.
  - [REQ-1.28.1] If any element is unresolved, getter SHALL throw `IllegalStateException`.
  - [REQ-1.28.2] This behavior SHALL be uniform for all collection references.

### 1.8 API accessor/mutator shape
- [REQ-1.29] API accessors SHALL be read-only `val` and support covariant override.
- [REQ-1.30] If redefining attribute `A` and redefined `B` share `validName`, Kotlin `override` SHALL be used.
- [REQ-1.31] Subsetting SHALL NOT imply Kotlin `override`.
- [REQ-1.32] Mutators SHALL NOT be interface members.
- [REQ-1.33] API mutators SHALL be top-level Kotlin extension functions named `fun ${className}.${propertyName}_set(value: ${genType})`.
- [REQ-1.34] No API mutator SHALL be generated for `isDerived=true` or `isDerivedUnion=true`.

## 2. Realisation Requirements

This section defines implementation-level requirements for any Kotlin realisation of the generated API.
It specifies storage, mutability, runtime invariant enforcement, mutation behavior, 
and generation-time derived values independent of any specific implementation profile.

### 2.1 Realisation profiles and conformance
- [REQ-2.1] One or more Kotlin realisation profiles MAY be provided.
- [REQ-2.2] Every realisation class SHALL implement the generated API interfaces.
- [REQ-2.3] Every realisation profile SHALL preserve API runtime invariants and error semantics.

### 2.2 Construction and mutability
- [REQ-2.4] Realisation constructors for generated classes SHALL accept only `_identity: Any`.
- [REQ-2.5] Realisation backing state SHALL be mutable in general.
- [REQ-2.6] API properties SHALL remain `val`; mutability SHALL be provided via backing state and mutating operations.
- [REQ-2.7] For collection attributes, property SHALL be `val` while exposed collection instance SHALL be mutable.
- [REQ-2.8] `isDerivedUnion=true` realization attributes SHALL not be mutable.

### 2.3 Accessor and storage behavior
- [REQ-2.9] Realisation storage strategy MAY be flattened.
  - [REQ-2.9.1] If flattened, implementation SHALL include all final resolved attributes after applying API resolution rules.
- [REQ-2.10] Single composite attributes SHALL be backed by fields and exposed via `override val` getters.
  - [REQ-2.10.1] Optional fields SHALL initialize to `null`.
  - [REQ-2.10.2] Required fields SHALL initialize to a default value.
- [REQ-2.11] Composite and reference collections SHALL be backed by mutable implementation collections and exposed via `override val` getters.
- [REQ-2.12] Single references SHALL be backed by mutable reference holders and exposed as API-compatible `Reference<Any, T>`.
- [REQ-2.13] For reference collections, implementation SHALL use mutable collections of mutable reference holders compatible with API `Reference<Any, T>`.

### 2.4 Mutator behavior and opposite-end consistency
- [REQ-2.14] Mutators SHALL be generated only for single composite attributes.
- [REQ-2.15] Single reference mutators SHALL NOT be generated; mutation SHALL occur through mutable reference holders.
- [REQ-2.16] Collection mutators SHALL NOT be generated; mutation SHALL occur through collection operations.
- [REQ-2.17] Redefinition mutators with different type and name SHALL be generated as distinct extension functions, subject to mutator-scope rules.
- [REQ-2.18] Opposite-end update logic SHALL run for composite single-valued mutations when opposite exists.
- [REQ-2.19] For collection references, opposite-end update logic SHALL run on resolve, re-resolve, and removal transitions.
- [REQ-2.20] If reference entry is unresolved, no opposite-end relationship SHALL be created for that entry.
- [REQ-2.21] Attempted mutation paths for derived/derived-union attributes SHALL fail with `IllegalStateException`.

### 2.5 Precomputed generation values
- [REQ-2.22] Generation SHALL precompute these values.
  - [REQ-2.22.1] `Type.isPrimitive := Enum | String | Integer | Boolean | Real | UnlimitedNatural`.
  - [REQ-2.22.2] `Attribute.isSingle := (upperBound == 1)`.
  - [REQ-2.22.3] `Attribute.isCollection := (upperBound == -1 || upperBound > 1)`.
  - [REQ-2.22.4] `Attribute.isOptional := (lowerBound == 0 && isSingle)`.
  - [REQ-2.22.5] `Attribute.genType` SHALL derive to:
    - [REQ-2.22.5.1] `${type}?` for single optional.
    - [REQ-2.22.5.2] `${type}` for single required.
    - [REQ-2.22.5.3] `Collection<${type.validName}>` for non-unique/non-ordered.
    - [REQ-2.22.5.4] `Set<${type.validName}>` for unique/non-ordered.
    - [REQ-2.22.5.5] `List<${type.validName}>` for non-unique/ordered.
    - [REQ-2.22.5.6] `OrderedSet<${type.validName}>` for unique/ordered.
  - [REQ-2.22.6] Resulting names SHALL be validated against Kotlin keywords and problematic types.

## 3. RAM Realisation Requirements

This section defines the RAM realisation profile as an illustrative and useful concrete implementation of the API 
and realisation contracts. It specializes the general realisation rules with RAM-specific holder types, 
callback behavior, and generated mutator conventions.

### 3.1 RAM profile scope
- [REQ-3.1] Ram realisation SHALL be an illustrative and useful Kotlin realisation profile.
- [REQ-3.2] Ram profile SHALL conform to all API and general realisation requirements.

### 3.2 RAM factories and object retention
- [REQ-3.3] Ram profile MAY generate a `Ram` Factory implementation per mapped package/model.
- [REQ-3.4] When generated, Ram factory SHALL store created object references until it is garbage collected.

### 3.3 RAM reference and collection mechanics
- [REQ-3.5] Ram single reference holders SHALL use `ManagedReference<Any, T>`.
- [REQ-3.6] Ram collection-valued reference holders SHALL use mutable managed collections of `ManagedReference<Any, T>`.
- [REQ-3.7] `ManagedReference` SHALL invoke callbacks when `.resolved` changes.
- [REQ-3.8] Managed collections SHALL invoke callbacks on add/remove.
- [REQ-3.9] Combined managed reference/collection callback lifecycle SHALL maintain opposite-end consistency.

### 3.4 RAM mutator realization
- [REQ-3.10] Ram generated mutators SHALL be top-level Kotlin extension functions in generated Ram code.
- [REQ-3.11] Ram mutators SHALL follow `${name}_set(value: ${genType})` naming to avoid Kotlin setter clashes.

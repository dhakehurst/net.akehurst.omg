The purpose of this specification is to define how to
generate kotlin multiplatform/common code from OMG MOF XMI files.

# Model
- The model, defined by the (main) XMI file
- E.g. UML, SysML, DD, etc
- Provides:
    - A top level builder function
    - A top level Factory referencing sub-package factories (if any)

# Package
- Provides:
    - a Factory interface
    - a Builder, to enable construction via a Kotlin builder DSL
    - a Resolver, to resolve reference properties in a complete structure
    - an 'AsString' object for creating a string representation of instances, covering each class in the package
    - a 'Ram' Factory implementation, that also stores a reference to each created object.
        - factory stores all references until it is garbage collected.

# Primitive Types
- UML.String -> kotlin.String
- UML.Integer -> kotlin.Long
- UML.Boolean -> kotlin.Boolean
- UML.Real -> kotlin.Double
- UML.UnlimitedNatural -> kotlin.Long
- Enums are considered to be primitive types

# Classes
- each class is mapped to:
    - interface: with kotlin valid interface name
    - an in memory (Ram) based implementation (kotlin name validation not needed as 'Ram' appended to name)
    - Ram implementation is 'flat' it implements allAttributes (owned and from super classes)
    - Every generated class interface MUST declare `val _identity: Any` property if it is a root type (has no supertypes).
        - Subtypes inherit this property from their supertypes.
        - This property is immutable and set at object construction time.
        - The value MUST be unique and stable for the lifetime of the object.
    - the attributes to implement are resolved using these rules in order:
        1. Collect all attributes from the class `ownedAttribute` list and all attributes inherited from supertypes.
        2. For each attribute `A` that redefines another attribute `B` (via `redefinedProperty`):
            - If `A` and `B` have the same name AND same type AND same collection status (both single or both collection), then `A` MUST override `B` in the interface (Kotlin `override` keyword on getter).
            - If `A` redefines `B` but has a narrower type (subtype covariance) or narrower multiplicity (e.g., collection→single), then `A` MUST override `B`.
            - If `A` has a different name than `B` or a different genType (due to collection suffix), then `A` is treated as a separate property; bridge-properties or aliases are generated but `A` does NOT override `B` in Kotlin (to avoid name/erasure conflicts). See Interface Mutator section.
        3. Filter out any attribute that is the target of a redefinition (i.e., if `A` redefines `B`, exclude `B` from the flattened list).
        4. For duplicate attribute names after filtering, apply collision resolution (type covariance, multiplicity narrowing; error if irresolvable).
        5. The final set of attributes to generate MUST be deduplicated by `validName` and grouped by originating parent class for documentation.


# Associations
- Association owned ends are not navigable
- Class owned member ends are implemented as Attributes
    - setting of the opposite end must be implemented
    - During mutable collection mutations (add/remove), automatically set opposite ends (reduces boilerplate in common case).

# Attributes

- 'isID' is not supported, all objects are identified by the '_identity' property
- Every object MUST have an `_identity` property that serves as a unique, stable identifier for the lifetime of the object.
- The `_identity` is set at construction time and is IMMUTABLE.
- Its value can be any hashable Kotlin object (typically a String or UUID).
- The `_identity` participates in reference resolution and equality checks.

- 'isReadOnly' is not supported (all attributes are potentially writable)
- if property is not writable, values can never be given except in a constructor
- Every generated class MUST accept only `_identity: Any` in its constructor.
- All other properties MUST be mutable and set to their default values (null for single optional properties, empty for collections, default value for primitives).
- Attribute values are set via the builder DSL or explicit `_set()` mutators AFTER construction, not in the constructor.

- isDerived is currently not supported, code generation for derived properties not currently supported.
- attributes marked with isDerived=true (but isDerivedUnion=false) SHALL be ignored; no interface property or realisation field is generated.

- isDerivedUnion
    - attributes marked with isDerivedUnion=true generate a read-only computed getter that returns the union of all subsetting properties at query time.
    - no mutator is generated for isDerivedUnion properties.

- subsetting
    - If property p subsets q, runtime MUST enforce p ⊆ q.
    - On add/set into subset p, implementation MUST ensure element exists in q (auto-add if missing).
    - On remove/unset from superset q, implementation MUST reject operation if it would violate p ⊆ q.
    - For single-valued subset/superset:
        -  setting p = x MUST set q = x if q is null;
        - if q != x, operation MUST fail unless replacement is explicitly allowed.
    - For collection-valued properties, enforcement occurs on each mutation (add, remove, clear, bulk ops).
    - Violations MUST throw a deterministic domain exception (not generic IllegalStateException).

- A reference attribute of type T generates two properties:
    - A resolved-value accessor (type `T`, `T?`, or a collection of `T`) — see Interface Accessor section.
    - A reference-holder accessor named `${propertyName}Reference`:
        - In the **interface**: type `Reference<Any, T>` (read-only view of the reference).
        - In the **Ram implementation**: type `ManagedReference<Any, T>` (see below).

The reference type hierarchy is:
- `Reference<Any, T>` — read-only interface:
    - `val reference: Any?` — the unresolved identifier (typically a String or XMI ID).
    - `val resolved: T?` — the resolved object reference (populated by the Resolver).
- `MutableReference<Any, T>` — extends `Reference<Any, T>` with mutable fields:
    - `var reference: Any?` — settable unresolved identifier.
    - `var resolved: T?` — settable resolved object reference.
- `ManagedReference<Any, T>` — extends `MutableReference<Any, T>`:
    - Registers a callback that is invoked when `.resolved` is set.
    - The callback is used to automatically set the opposite end of a bidirectional association (see Associations section).
    - Used exclusively in the Ram implementation; the interface only exposes `Reference<Any, T>`.

For single required references (lowerBound=1, upperBound=1):
- The resolved-value getter MUST throw an exception if `.resolved` is null (not yet resolved).
- Example in interface: `val prop1: T get() = prop1Reference.resolved ?: error("prop1 not resolved")`

For optional references (lowerBound=0, upperBound=1):
- The resolved-value getter returns `prop1Reference.resolved` directly (may be null).

For collections of references:
- The resolved-value property is a collection of `T`; resolution populates the collection in place.

Challenges:
1. The concept of aggregation does not exist in current OO programing languages such as Kotlin.
    - properties simply reference another object
    - no differentiation between composite & reference
2. Opposite ends need to be set
3. The MOF/UML concept of redefinition does not exist in current OO programing languages such as Kotlin.
   a. a subtype can 'override' a property, but not redefine it
   b. override can narrow the property type to a subtype if getter only
   c. cannot change name with an override
   d. cannot change between single and collection with override
4. It is necessary to handle combinations of:
    - aggregation: composite or reference
    - different multiplicities
    - different unique/ordered combinations
    - redefinition of name
    - redefinition  of type
    - redefinition of multiplicities
        - increase single -> collection
        - decrease collection -> single
    - subsetting

Solutions:
- attributes with primitive type or enum type are always considered composite
- (3.a, 3.b) class interface (and realisation) only contain getter
- (3.d) attributes with collection type have the collection type name appended to their name
- (1) a kotlin 'Reference' implementation is used for reference aggregation
    - can be resolved against a ReferenceStore - i.e. the Factory which stores reference to each object created
    - (3.b) 'Managed' collection types are used as mutable collection implementation
        - (3.b) they perform runtime type checking when element is added
        - (2) they augment collection mutations with a callback to allow for opposite end setting
    - (2) `ManagedReference` is used as the Ram implementation of single reference attributes
        - it invokes a callback when `.resolved` is set, to allow for opposite end setting
- Collection and reference attributes in an implementation class are always mutable:
- For composite collections (non-reference): The attribute is initialized as a `ManagedCollection<T>`, `ManagedSet<T>`, `ManagedList<T>`, or `ManagedOrderedSet<T>` depending on `isUnique` and `isOrdered` flags.
- For reference collections: The attribute is initialized to hold `MutableReference<Any, T>` items and is mutable.
- Clients access mutable versions via property accessors (the property IS the mutable collection) or via extension functions `.mutable{Collection,Set,List,OrderedSet}()` when immutability semantics need to be bridged. The exact extension function name follows the pattern `.mutable<CollectionType>()`, e.g., `.mutableSet()`, `.mutableList()`, `.mutableOrderedSet()`, `.mutableCollection()`.
- Managed collections call a registered callback when items are added or removed, to enable automatic opposite-end setting (see Associations section).

- Pre-computed these values for use in generation
    - Type.isPrimitive := self is one of {Enum, String, Integer, Boolean, Real, UnlimitedNatural} (from Primitive Types section)
    - Attribute.isSingle := 1==upperBound
    - Attribute.isCollection := -1==upperBound || 1 < upperBound
    - Attribute.isOptional := 0==lowerBound && isSingle // collections are not treated as optional, all can be empty
    - Attribute.genType := when
        - isSingle -> when
            - isOptional -> "${type}?" //nulable
            - else -> "$type" // not nullable
        - isCollection -> The generated type is one of:
            - `Collection<${type.validName}>` if `isUnique=false` and `isOrdered=false` (unordered, non-unique; UML calls this Bag)
            - `Set<${type.validName}>` if `isUnique=true` and `isOrdered=false` (unordered, unique)
            - `List<${type.validName}>` if `isUnique=false` and `isOrdered=true` (ordered, non-unique; UML calls this Sequence)
            - `OrderedSet<${type.validName}>` if `isUnique=true` and `isOrdered=true` (ordered, unique)
    - Attribute.genName := name, with suffix appended for collections:
        - If `upperBound > 1` (collection):
            - If non-unique and non-ordered: append "Collection" → genName = `${name}Collection`
            - If unique and non-ordered: append "Set" → genName = `${name}Set`
            - If non-unique and ordered: append "List" → genName = `${name}List`
            - If unique and ordered: append "OrderedSet" → genName = `${name}OrderedSet`
        - Then validate the resulting name against Kotlin keywords and problematic types.

## Interface Accessor

- Interface accessors are read-only `val` properties (not `var`).
    - This allows subclasses in the Kotlin type hierarchy to use covariant overriding:
        - a subclass can override a getter to return a narrower (subtype) type.
        - Read-only properties support this; mutable properties (vars) do not.

- the type of the accessor is:
    - For composite attributes: `genType` (the class type, nullable or not, single or collection).
    - For reference attributes (aggregation=reference) we generate 2 accessors:
        - One for the resolved value: type `T` (required) or `T?` (optional); throws or returns null if unresolved.
        - One reference-holder: named `${propertyName}Reference`, of type `Reference<Any, T>` in the interface.

- if attribute `A` redefines another attribute `B` (via `redefinedProperty`) AND `A` and `B` have the same `validName`, then use Kotlin `override` keyword (only redefinitions with name/type compatibility reach this point, per the attribute resolution rules in the Classes section).
- if attribute `A` subsets another attribute `B` (via `subsettedProperty`), apply the runtime subset invariant rules (`A ⊆ B`) defined in the Attributes section; this does not imply Kotlin `override`.
- Subsetting does not affect the `override` keyword choice.

## Interface Mutator
- Mutators (setters) are NOT member functions of the interface; they are provided as top-level Kotlin extension functions defined in the same file as the class implementation (e.g. the Ram file).
    - Interface accessors use read-only `val` covariance to allow subtype narrowing in overrides; matching setters would require parameter typing that cannot be safely expressed as interface overrides, so mutation behaviour is implementation-specific.
- Extension function naming: `fun ${className}.${propertyName}_set(value: ${genType}) { ... }`
- No mutator is generated for attributes with isDerived=true or isDerivedUnion=true.


## Realisation Accessor

- The RAM class (`${name}Ram`) MUST implement the interface. For each property accessor:
- **Single composite properties:** Backed by a private/internal field of type `genType` with a public `override val` getter.
    - For optional properties, initialized to `null`.
    - For required properties, MUST be initialised to a default value.
- **Collections (composite or reference):** Backed by a `Managed<CollectionType>` instance (initialized in the class body or constructor), exposed via a public `override val` getter.
- **Single reference properties:** Backed by a `ManagedReference<Any, T>` instance (initialized in the class body), exposed via a public `override val ${propertyName}Reference: ManagedReference<Any, T>` getter (covariant override of the interface's `Reference<Any, T>`). The callback is configured at construction time to set the opposite end when `.resolved` is assigned.
- **isDerived=true (non-union) properties:** not generated (ignored per Attributes section).
- **isDerivedUnion=true properties:** Backed by a computed getter (no backing field) that returns the union of all subsetting properties. No mutator is generated.

## Realisation Mutator
- if attribute isDerivedUnion=true then realisation MUST NOT have a generated mutator (getter only).

- Mutator functions are generated only for single composite properties:
    - Single composite: `fun ${className}.${propertyName}_set(value: ${genType}) { ... }` — extension function to set the property.
    - Single reference: No `_set()` mutator needed; the property is a `ManagedReference<Any, T>` which is mutable in-place (`.reference` and `.resolved` are mutable) and automatically handles opposite-end setting via its callback.
    - Collections (composite or reference): No direct mutator; the property getter returns a mutable collection object (Managed*Collection or equivalent), so mutation is done via `.add()`, `.remove()`, etc. on the collection itself. If an immutable view is exposed elsewhere, `.mutable${CollectionType}()` extension functions can be provided to convert to mutable form.
    - use a pattern `${name}_set(value: ${genType})` for single composite mutators so that its name does not clash with the kotlin setter

- If an attribute redefines another but has a different type AND a different name, the mutator is generated as a distinct extension function (not attempting to override the redefined mutator). Example:
   ```kotlin
   // Superclass
   fun MyClass.prop1_set(value: PropType) { ... }
   // Subclass with rename+retype redefinition
   fun MySubClass.redefinesProp1_set(value: PropTypeB) { ... }
   ```
  This avoids JVM type erasure issues that would arise if both were named `prop1_set(Any)`.

Each mutator extension function MUST be implemented in the RAM class:
- **Single composite properties:**
  ```kotlin
  override fun ${propertyName}_set(value: ${genType}) {
      _${propertyName} = value
      // TODO: set opposite end if this is part of a bidirectional association
  }
  ```
- **Single reference properties:** No override mutator needed; the property returns a `ManagedReference<Any, T>` which is mutable in-place.
- **Collections:** No mutator needed; the collection is mutable in-place.

**Opposite End Setting:** When a composite single-valued property is set, the mutator SHOULD invoke the opposite end's mutator (if an opposite is defined) to maintain bidirectional consistency. Example:
   ```kotlin
   override fun parent_set(value: Parent?) {
       _parent = value
       value?.children_set(this)  // or via Managed collection callback
   }
   ```
(See Associations section for detailed semantics.)

**Derived/Derived Union:** If the property is derived or derived union, no mutator is generated. If a mutator is accidentally called, it MUST raise `NotImplementedError` or perform a no-op.




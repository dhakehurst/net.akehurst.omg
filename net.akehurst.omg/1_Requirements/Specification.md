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
     - how/when to remove things from the factory ?

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
     - the attributes to implement are defined by resolving the redefinitions as follows:
     - 

# Associations
 - Association owned ends are not navigable
 - Class owned member ends are implemented as Attributes
   - setting of the opposite end must be implemented

# Attributes

- By design 'isID' is not supported, all objects are identified by the 'identifier_' property
- By design 'readOnly' is not supported (all attributes are potentially writable)
  - if property is not writable, values can never be given except in a constructor
  - we want only the object reference to be part of the constructor

- isDerived / isDerivedUnion ?
- subsetting ?

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
 - 
 - (3.b) 'Managed' collection types are used as mutable collection implementation
   - (3.b) they perform runtime type checking when element is added
   - (2) they augment collection mutations with a callback to allow for opposite end setting
- realisation of collection and reference attributes are always mutable
    - 'mutable' extension function used to convert and mutate the attribute
 - 

 - Pre-computed these values for use in generation
   - Type.isPrimitive := self is one of {Enum, String, Integer, Boolean, Real, UnlimitedNatural}
   - Attribute.isSingle := 1==upperBound
   - Attribute.isCollection := -1==upperBound || 1 < upperBound
   - Attribute.isOptional := 0==lowerBound && isSingle // collections are not treated as optional, all can be empty
   - Attribute.genType := when
     - isSingle -> when
        - isOptional -> "${type}?" //nulable
        - else -> "$type" // not nullable
     - isCollection -> unique & ordered determine one of Collection(Bag), Set, List(Sequence), OrderedSet
   - Attribute.genName := validated ( name + if upperBound > 1 collection type (non-mutable name) )
     - validation of names is to avoid kotlin specific keywords or problematic types


## Interface Accessor

 - getters are always readOnly, this allows covariant overriding
 - the type of the accessor is the genType (from above) whether it is composite or reference
 
 - if attribute redefines another or subsets another with the same name, use override
 - kotlin property "$maybeOverride val $genName: $genType"

## Interface Mutator
 - mutators are not put in the interface, they are implemented as extension functions
 - if attribute isDerived or isDerivedUnion then no mutator needed
 - mutator is only needed for single composite properties
   - reference and collection properties are handled by .mutableXXX extensions
   - use a pattern '$name_set(value: $genType)' for single composite mutators so that its name does not clash with the kotlin setter

 - when
   - type is different to redefined type 
     & genName is not same as redefined name 
     -> define setter as Extension fun to avoid overload type erasure issues 

## Realisation Accessor

## Realisation Mutator
 - if attribute isDerived or isDerivedUnion then realisation should not be mutable


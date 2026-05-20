# Model

# Package
 - For each package we generate
   - a Factory interface, covering each class in the package
   - a Builder, covering each class in the package
   - an 'AsString' object for creating a string representation of instances, covering each class in the package 

# Classes
 - each class is mapped to:
   - interface
   - an in memory (Ram) based implementation

# Attributes

- It is necessary to handle combinations of:
  - aggregation: composite or reference
  - different multiplicities
  - different unique/ordered combinations
  - redefinition of name and/or type and/or multiplicities
  - subsetting

 - Some pre-computed values are required
   - genType := when
     - 1==upperBound -> when
        - 0==lowerBound -> "${type}?" //nulable
        - else -> "$type" // not nullable
     - 1 < upperBound -> unique & ordered determine one of MutableCollection(Bag), MutableSet, MutableList(Sequence), MutableOrderedSet
   - genName := name + if upperBound > 1 collection type (non-mutable name)


## Interface Accessor

 - getters are always readOnly, this allows covariant overriding
 - the type of the accessor is the genType (from above) whether it is composite or reference
 
 - if attribute redefines another or subsets another with the same name, use override
 - kotlin property "$maybeOverride val $genName: $genType"

## Interface Mutator

 - use a pattern 'set_$name' for mutators so that its name does not clash with the kotlin setter
 - composite mutator is simple - set_$genName(value: $genType)
 - for a reference type, we generate 2 mutators:
   - same as composite
   - set_$genName(ref: Any)

 - when
   - type is different to redefined type 
     & genName is not same as redefined name 
     -> define setter as Extension fun to avoid overload type erasure issues 

## Realisation Accessor

## Realisation Mutator


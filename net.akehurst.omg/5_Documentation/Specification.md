

# Implementing Attributes

 - It is necessary to handle combinations of:
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
 
 - if attribute redefines another or subsets another with the same name, use override
 - kotlin property "$maybeOverride val $genName: $genType"

## Interface Mutator

 - when {
   - type is different to redefined type & genName is not same as redefined name -> define setter as Extension fun to avoid overload type erasure issues 

## Realisation Accessor

## Realisation Mutator


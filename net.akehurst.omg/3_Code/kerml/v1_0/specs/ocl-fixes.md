# OCL fixes in kerml_patched.xmi

The OCL in the OMG XMI (and in the formal spec text) has errors that stop it being translated to Kotlin. These are the edits made to the patched XMI. Each was checked against the property and operation names in the metamodel; where the intent was not clear from the metamodel, the formal spec prose or the OMG pilot implementation (Systems-Modeling/SysML-v2-Pilot-Implementation) was used.

Also: the result parameters named `result` (the `evaluate` operations and `Type::allSupertypes`) are given `direction="return"`, and `Type::isCompatibleWith` / `Feature::isCompatibleWith`, which have no result parameter in the XMI or the spec, are given a Boolean one.

| was | now |
|---|---|
| `-- A position index shall be converted to a decimal string representation -- consisting of only decimal digits, with no sign, leading zeros or leading -- or trailing whitespace.` | `(removed)` |
| `if ownedElement.qualifiedName <> null then ownedElement.qualifiedName + '/owningMembership'` | `if ownedMemberElement.qualifiedName <> null then ownedMemberElement.qualifiedName + '/owningMembership'` |
| `ownedSpecialization.general->closure(t \| if t.multiplicity <> null then OrderedSet{} else ownedSpecialization.general )` | `ownedSpecialization.general->closure(t \| if t.multiplicity <> null then OrderedSet{} else t.ownedSpecialization.general endif )` |
| `else chainingFeatures->at(2) endif` | `else chainingFeatures->at(2) endif endif` |
| `else importedNamespace.visibleMemberships(excluded, isRecursive, isImportAll)` | `else importedNamespace.visibleMemberships(excluded, isRecursive, isImportAll) endif` |
| `OrderedSet(Types) = OrderedSet{self}-> -- Note: The closure operation automatically handles circular relationships. closure(` | `OrderedSet(Type) = OrderedSet{self}-> closure(` |
| `types->exist(t2 \|` | `types->exists(t2 \|` |
| `Sequence{conjugator.originalType}` | `Sequence{ownedConjugator.originalType}` |
| `else if conjugator.originalType.oclIsKindOf(Feature) then OrderedSet{conjugator.originalType.oclAsType(Feature)}` | `else if ownedConjugator.originalType.oclIsKindOf(Feature) then OrderedSet{ownedConjugator.originalType.oclAsType(Feature)}` |
| `excludedType->including(self)` | `excludedTypes->including(self)` |
| `directionOfExcluding(f, Set{})` | `directionOfExcluding(feature, Set{})` |
| `featureMemberships-> selectByKind(ReturnParameterMembership)` | `featureMembership-> selectByKind(ReturnParameterMembership)` |
| `featuring.type->asOrderedSet()` | `ownedTypeFeaturing.featuringType->asOrderedSet()` |
| `visibleMemberships(excluded, true, importAll)` | `visibleMemberships(excluded, true, isImportAll)` |
| `inheritedFeature = inheritedMemberships->` | `inheritedFeature = inheritedMembership->` |
| `memberships->select(memberElement = element) in memberships.memberShortName-> union(memberships.memberName)` | `membership->select(memberElement = element) in elementMemberships.memberShortName-> union(elementMemberships.memberName)` |
| `else metaClassTypes->first()` | `else metaclassTypes->first()` |
| `ownedTyping = ownedGeneralization->` | `ownedTyping = ownedSpecialization->` |
| `else owningNamspace.resolveLocal(name)` | `else owningNamespace.resolveLocal(name)` |
| `subsetting->reject(s \| s.oclIsKindOf(CrossSubsetting)).subsettedFeatures in` | `ownedSubsetting->reject(s \| s.oclIsKindOf(CrossSubsetting)).subsettedFeature in` |
| `subsettedFeature->includes(chainingFeature->last())` | `subsettedFeatures->includes(chainingFeature->last())` |
| `subsettingFeature.resolveGlobal('Base::Anything').memberElement` | `resolveGlobal('Base::Anything').memberElement` |
| `featuringTypes->closure(t \| if not t.oclIsKindOf(Feature)` | `featuringType->closure(t \| if not t.oclIsKindOf(Feature)` |
| `else typeMembers->first().oclAsType(Type)` | `else members->first().oclAsType(Type)` |
| `select(closure(ownedRedefinition.redefinedFeature)-> includes(baseFeature))` | `select(f \| f->closure(ownedRedefinition.redefinedFeature)-> includes(baseFeature))` |
| `closure(ownedRedefinition.redefinedFeature).ownedMember-> selectAsKind(FeatureValue)` | `closure(ownedRedefinition.redefinedFeature).ownedMember-> selectByKind(FeatureValue)` |
| `allSuperTypes()->selectAsKind(Feature)` | `allSupertypes()->selectByKind(Feature)` |
| `referent.conformsTo('Anything::self')` | `referent.specializesFromLibrary('Base::Anything::self')` |
| `referent.owningType.isOclKindOf(MetaClass) or referent.owningType.isOclKindOf(MetadataFeature)` | `referent.owningType.oclIsKindOf(Metaclass) or referent.owningType.oclIsKindOf(MetadataFeature)` |
| `valueEval.oclAsKindOf(LiteralInteger).value` | `valueEval.oclAsType(LiteralInteger).value` |
| `result->size() = 1 and results->first()` | `results->size() = 1 and results->first()` |
| `if resultExpr->isEmpty() then Sequence{}` | `if resultExprs->isEmpty() then Sequence{}` |
| `select(importedMemberships(Set{})->includes(mem)). first().visibility` | `select(importedMemberships(Set{})->includes(mem))-> first().visibility` |
| `select(m \| m.name = name).indexOf(self) <> 1` | `select(m \| m.name = name)->indexOf(self) <> 1` |
| `if ownedMemberFeatures.isEmpty() then null` | `if ownedMemberFeatures->isEmpty() then null` |
| `membership.memberElement.oclIsType(Feature)` | `membership.memberElement.oclIsKindOf(Feature)` |
| `memberElement.oclKindOf(other.memberElement.oclType()) or other.memberElement.oclKindOf(memberElement.oclType())` | `memberElement.oclIsKindOf(other.memberElement.oclType()) or other.memberElement.oclIsKindOf(memberElement.oclType())` |
| `featuringType.size() = 1 and (featuringType.first().owner = self implies featuringType.first().oclIsKindOf(Feature) and featuringType.first().oclAsType(Feature)` | `featuringType->size() = 1 and (featuringType->first().owner = self implies featuringType->first().oclIsKindOf(Feature) and featuringType->first().oclAsType(Feature)` |
| `s.special = self` | `s.specific = self` |
| `closure(typingFeatures()).typing.type` | `closure(typingFeatures()).ownedTyping.type` |
| `selectByKind(FeatureMembership).memberFeature` | `selectByKind(FeatureMembership).ownedMemberFeature` |
| `ownedFeature.redefinition.redefinedFeature` | `ownedFeature.ownedRedefinition.redefinedFeature` |
| `ownedFeatures-> select(direction = _'in')` | `ownedFeature-> select(direction = _'in')` |
| `select(oclIsKindOf(MetadataFeature) and annotatedElement->includes(referencedElement))` | `select(oclIsKindOf(MetadataFeature) and oclAsType(MetadataFeature).annotatedElement->includes(referencedElement))` |
| `else featureValues->first().value.evaluate(self) endif` | `else featureValues->first().value.evaluate(self) endif endif` |
| `else ownedMemberFeatures->first() endif` | `else ownedMemberFeatures->first() endif endif` |
| `feature.redefinesFromLibrary('Occurrences::Occurrence::snapshots') endif` | `feature.redefinesFromLibrary('Occurrences::Occurrence::snapshots') endif endif` |
| `let anythingType: Element = resolveGlobal('Base::Anything').memberElement in` | `let anythingType: Type = resolveGlobal('Base::Anything').memberElement.oclAsType(Type) in` |
| `resolveGlobal(ns + "::'" + operator + "'")` | `resolveGlobal(ns + '::\'' + operator + '\'')` |
| `(shortMemberName = null or (shortMemberName <> other.shortMemberName and shortMemberName <> other.memberName)) and (memberName = null or (memberName <> other.shortMemberName and memberName <> other.memberName)))` | `(memberShortName = null or (memberShortName <> other.memberShortName and memberShortName <> other.memberName)) and (memberName = null or (memberName <> other.memberShortName and memberName <> other.memberName))` |
| `specializes(otherType) or supertype.oclIsKindOf(Feature) and` | `specializes(otherType) or otherType.oclIsKindOf(Feature) and` |
| `ownedRedefinitions.allRedefinedFeatures()->exists(f \|` | `allRedefinedFeatures()->exists(f \|` |

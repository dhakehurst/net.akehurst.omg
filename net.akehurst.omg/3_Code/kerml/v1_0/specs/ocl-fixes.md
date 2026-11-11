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
| `supertypes(false)->excluding(excludedSelf).` | `supertypes(false)->reject(t \| excludedSelf->includes(t)).` |

## Rules corrected so the derivation is found

A derivation is a `derive...` rule whose body starts `<property> =`. These rules had no such start, named the wrong property, or used an operation that OCL does not have.

| rule | was | now |
|---|---|---|
| deriveConnectorDefaultFeaturingType | `let commonFeaturingTypes : OrderedSet(Type) = relatedFeature->closure(featuringType)->select(t \| relatedFeature->forAll(f \| f.isFeaturedWithin(t)) ) in let nearestCommonFeaturingTypes : OrderedSet(Type) = commonFeaturingTypes->reject(t1 \| commonFeaturingTypes->exists(t2 \| t2 <> t1 and t2->closure(featuringType)->contains(t1) )) in if nearestCommonFeaturingTypes->isEmpty() then null else nearestCommonFeaturingTypes->first() endif` | `defaultFeaturingType = let commonFeaturingTypes : OrderedSet(Type) = relatedFeature->closure(featuringType)->select(t \| relatedFeature->forAll(f \| f.isFeaturedWithin(t)) ) in let nearestCommonFeaturingTypes : OrderedSet(Type) = commonFeaturingTypes->reject(t1 \| commonFeaturingTypes->exists(t2 \| t2 <> t1 and t2->closure(featuringType)->contains(t1) )) in if nearestCommonFeaturingTypes->isEmpty() then null else nearestCommonFeaturingTypes->first() endif` |
| deriveConnectorDefaultFeaturingType | `defaultFeaturingType = let commonFeaturingTypes : OrderedSet(Type) = relatedFeature->closure(featuringType)->select(t \| relatedFeature->forAll(f \| f.isFeaturedWithin(t)) ) in let nearestCommonFeaturingTypes : OrderedSet(Type) = commonFeaturingTypes->reject(t1 \| commonFeaturingTypes->exists(t2 \| t2 <> t1 and t2->closure(featuringType)->contains(t1) )) in if nearestCommonFeaturingTypes->isEmpty() then null else nearestCommonFeaturingTypes->first() endif` | `defaultFeaturingType = let commonFeaturingTypes : OrderedSet(Type) = relatedFeature->closure(featuringType)->select(t \| relatedFeature->forAll(f \| f.isFeaturedWithin(t)) ) in let nearestCommonFeaturingTypes : OrderedSet(Type) = commonFeaturingTypes->reject(t1 \| commonFeaturingTypes->exists(t2 \| t2 <> t1 and t2->closure(featuringType)->includes(t1) )) in if nearestCommonFeaturingTypes->isEmpty() then null else nearestCommonFeaturingTypes->first() endif` |
| deriveTypeOwnedIntersecting | `ownedRelationship->selectByKind(Intersecting)` | `ownedIntersecting = ownedRelationship->selectByKind(Intersecting)` |

## Derivations added

These derived properties have no derivation in the OMG XMI. A `derive<Class><Property>` rule was added for each, following the OMG pilot implementation (its setting delegates in `org.omg.sysml.delegate.setting`). The "source" column says where each one comes from.

A property that redefines another is read through the redefining one in a subclass (the generated RAM classes bridge them), so a derivation must not use the property it redefines, or it would call itself. That is why:

- properties that redefine `Feature::type` (directly or through e.g. `Usage::definition`) repeat the `Feature::type` derivation, `let types : OrderedSet(Type) = OrderedSet{self}->closure(typingFeatures()).ownedTyping.type->asOrderedSet() in types->reject(t1 | types->exists(t2 | t2 <> t1 and t2.specializes(t1)))`, written TYPES below;
- the end and parameter properties repeat the derivation of `Type::endFeature` or `Type::directedFeature`;
- the member properties of Membership subclasses use `ownedRelatedElement`, and the owning properties of Relationship subclasses use `owningRelatedElement`, not the properties they redefine.

| property | derivation | source |
|---|---|---|
| AnnotatingElement::owningAnnotatingRelationship | `if owningRelationship.oclIsKindOf(Annotation) then owningRelationship.oclAsType(Annotation) else null endif` | pilot |
| Annotation::owningAnnotatedElement | `if owningRelatedElement = annotatedElement then owningRelatedElement else null endif` | pilot |
| Annotation::owningAnnotatingElement | `if owningRelatedElement.oclIsKindOf(AnnotatingElement) then owningRelatedElement.oclAsType(AnnotatingElement) else null endif` | pilot |
| Conjugation::owningType | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| Differencing::typeDifferenced | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| Disjoining::owningType | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| Intersecting::typeIntersected | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| Unioning::typeUnioned | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| FeatureChaining::featureChained | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| FeatureInverting::owningFeature | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| FeatureTyping::owningFeature | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| Specialization::owningType | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| Subclassification::owningClassifier | `if owningRelatedElement.oclIsKindOf(Classifier) then owningRelatedElement.oclAsType(Classifier) else null endif` | pilot |
| Subsetting::owningFeature | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| CrossSubsetting::crossingFeature | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| ReferenceSubsetting::referencingFeature | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| TypeFeaturing::owningFeatureOfType | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
| Import::importOwningNamespace | `if owningRelatedElement.oclIsKindOf(Namespace) then owningRelatedElement.oclAsType(Namespace) else null endif` | pilot |
| Membership::membershipOwningNamespace | `if owningRelatedElement.oclIsKindOf(Namespace) then owningRelatedElement.oclAsType(Namespace) else null endif` | pilot |
| FeatureMembership::owningType | `if owningRelatedElement.oclIsKindOf(Type) then owningRelatedElement.oclAsType(Type) else null endif` | pilot |
| FeatureValue::featureWithValue | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot (via membershipOwningNamespace, which is the owningRelatedElement) |
| OwningMembership::ownedMemberElement | `ownedRelatedElement->first()` | pilot |
| OwningMembership::ownedMemberElementId | `ownedMemberElement.elementId` | pilot (as Membership::memberElementId) |
| FeatureMembership::ownedMemberFeature | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Feature) then e.oclAsType(Feature) else null endif` | pilot |
| EndFeatureMembership::ownedMemberFeature | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Feature) then e.oclAsType(Feature) else null endif` | pilot |
| ParameterMembership::ownedMemberParameter | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Feature) then e.oclAsType(Feature) else null endif` | pilot |
| ResultExpressionMembership::ownedResultExpression | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Expression) then e.oclAsType(Expression) else null endif` | pilot |
| FeatureValue::value | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Expression) then e.oclAsType(Expression) else null endif` | pilot |
| ElementFilterMembership::condition | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Expression) then e.oclAsType(Expression) else null endif` | pilot |
| Element::owningMembership | `if owningRelationship.oclIsKindOf(OwningMembership) then owningRelationship.oclAsType(OwningMembership) else null endif` | pilot |
| Feature::owningFeatureMembership | `if owningMembership.oclIsKindOf(FeatureMembership) then owningMembership.oclAsType(FeatureMembership) else null endif` | pilot |
| Feature::owningType | `if owningFeatureMembership = null then null else owningFeatureMembership.owningType endif` | pilot |
| Feature::endOwningType | `if isEnd then owningType else null endif` | pilot |
| Type::isConjugated | `ownedConjugator <> null` | pilot |
| Import::importedElement | `if self.oclIsKindOf(NamespaceImport) then self.oclAsType(NamespaceImport).importedNamespace else if self.oclIsKindOf(MembershipImport) then self.oclAsType(MembershipImport).importedMembership.memberElement else null endif endif` | pilot |
| Documentation::documentedElement | `owner` | pilot (as Element::owner) |
| TextualRepresentation::representedElement | `owner` | pilot (as Element::owner) |
| Connector::association | `TYPES->selectByKind(Association)` | pilot |
| Flow::interaction | `TYPES->selectByKind(Interaction)` | pilot |
| Expression::function | `TYPES->selectByKind(Function)->first()` | pilot |
| BooleanExpression::predicate | `TYPES->selectByKind(Predicate)->first()` | pilot |
| Association::associationEnd | `feature->select(isEnd)` | pilot (as Type::endFeature) |
| Connector::connectorEnd | `feature->select(isEnd)` | pilot (as Type::endFeature) |
| Behavior::parameter | `feature->select(f \| directionOf(f) <> null)` | as Type::directedFeature (pilot uses each feature's own direction) |
| Step::parameter | `feature->select(f \| directionOf(f) <> null)` | as Type::directedFeature (pilot uses each feature's own direction) |
| Function::expression | `step->selectByKind(Expression)` | pilot (subset of Behavior::step) |

Added 2026-11-06 (decided by David):

| property / operation | derivation | source |
|---|---|---|
| Element::escapedName() | Kotlin body (language `Kotlin`; the generator copies it as is): `name`, else `shortName`; returned as is if it is a basic name `[a-zA-Z_][a-zA-Z0-9_]*`, otherwise surrounded by `'` with `\b \t \n \f \r " ' \\` escaped | spec description; pilot `Element_escapedName_InvocationDelegate` / `ElementUtil.escapeName` (OCL has no pattern matching) |
| Function::isModelLevelEvaluable | `Set{...}->includes(qualifiedName)`, the set being the qualified names of the functions marked "Yes" in KerML Tables 5 and 7 (8.2.5.8) | spec; same list as the pilot's `ModelLevelLibraryFunctionFactory` |
| Feature::valuation | `ownedMembership->selectByKind(FeatureValue)->first()` | spec description of the end. It was a non-navigable end owned by `A_featureWithValue_valuation`; it is now an owned attribute of Feature, so the rules that use it can navigate to it |

Rules corrected 2026-11-06. The spec puts the argument rules on the subclasses, but the property is declared on InstantiationExpression; as for `Import::importedElement`, one rule on InstantiationExpression combines them, choosing by `oclIsKindOf` (as the pilot's `ExpressionUtil.getArgumentOf` does). The subclass rules are kept, with their names fixed:

| rule | was | now |
|---|---|---|
| deriveInvocationExpressionArgument | `instantiatedType.input->collect(inp \| ownedFeatures->select(redefines(inp)).valuation->select(v \| v <> null).value )` | `argument = instantiatedType.input->collect(inp \| ownedFeature->select(redefines(inp)).valuation->select(v \| v <> null).value )` |
| deriveInstantiationExpressionArgument (added) | (none) | `argument = if instantiatedType = null then OrderedSet{} else if self.oclIsKindOf(InvocationExpression) then <InvocationExpression rule>->asOrderedSet() else if self.oclIsKindOf(ConstructorExpression) then <ConstructorExpression rule>->asOrderedSet() else OrderedSet{} endif endif endif` |
| deriveConstructorExpressionArgument | `instantiatedType.feature->collect(f \| result.ownedFeatures->select(redefines(f)).valuation->select(v \| v <> null).value )` | `argument = instantiatedType.feature->collect(f \| result.ownedFeature->select(redefines(f)).valuation->select(v \| v <> null).value )` |

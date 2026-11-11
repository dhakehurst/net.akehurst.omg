# Problems found in the KerML, SysML and UML specs and XMI

6 November 2026 · David Akehurst

## Summary

The OMG XMI for KerML 1.0 and SysML 2.0 has about 180 problems that stop its OCL being used as written; UML 2.5.1 has 35, and its XMI is left unpatched. Every fix lives in the patched XMI (`kerml_patched.xmi`, `sysml_patched.xmi`), and each one is also listed in the `specs/ocl-fixes.md` file next to it.

| Kind of problem | KerML 1.0 | SysML 2.0 |
| --- | --- | --- |
| OCL with wrong names, typos or missing `endif` | 53 | 14 |
| Derivation rule that names the wrong property, or has no `property =` start | 2 | 4 |
| Derived property with no OCL at all | 47 (+ `valuation`) | 52 |
| Operation with no body | 1 (`escapedName`) | 0 |
| XMI structure (missing or wrong result parameters) | 2 kinds | 0 |
| Rule whose logic is wrong | 0 | 1 (`satisfyingFeature`) |

Sources checked: the XMI files (KerML ptc-25-04-04, SysML ptc-25-02-15, UML ptc-18-01-01), the formal spec PDFs (KerML formal-26-03-01, SysML formal-26-03-02) and the OMG pilot implementation (Systems-Modeling/SysML-v2-Pilot-Implementation). Most errors in the XMI appear the same way in the formal spec text.

## KerML 1.0: errors in the OCL

53 OCL expressions in the KerML XMI use names that don't exist in the metamodel, use operations that OCL doesn't have, or are not well formed. Each was fixed in `kerml_patched.xmi` by checking the names against the metamodel, and against the spec text or the pilot where the intent was unclear.

| Problem | Was | Now |
| --- | --- | --- |
| Body is only a comment, no expression | `-- A position index shall be converted to a decimal string representation -- consisting of only decimal digits, with no sign, leading zeros or leading -- or trailing whitespace.` | `(removed)` |
| Wrong property name | `if ownedElement.qualifiedName <> null then ownedElement.qualifiedName + '/owningMembership'` | `if ownedMemberElement.qualifiedName <> null then ownedMemberElement.qualifiedName + '/owningMembership'` |
| Closure body ignores its variable t, and missing endif | `ownedSpecialization.general->closure(t \| if t.multiplicity <> null then OrderedSet{} else ownedSpecialization.general )` | `ownedSpecialization.general->closure(t \| if t.multiplicity <> null then OrderedSet{} else t.ownedSpecialization.general endif )` |
| Missing endif | `else chainingFeatures->at(2) endif` | `else chainingFeatures->at(2) endif endif` |
| Missing endif | `else importedNamespace.visibleMemberships(excluded, isRecursive, isImportAll)` | `else importedNamespace.visibleMemberships(excluded, isRecursive, isImportAll) endif` |
| Wrong type name, and a comment inside the expression | `OrderedSet(Types) = OrderedSet{self}-> -- Note: The closure operation automatically handles circular relationships. closure(` | `OrderedSet(Type) = OrderedSet{self}-> closure(` |
| Typo: exist | `types->exist(t2 \|` | `types->exists(t2 \|` |
| Wrong property name: conjugator | `Sequence{conjugator.originalType}` | `Sequence{ownedConjugator.originalType}` |
| Wrong property name: conjugator | `else if conjugator.originalType.oclIsKindOf(Feature) then OrderedSet{conjugator.originalType.oclAsType(Feature)}` | `else if ownedConjugator.originalType.oclIsKindOf(Feature) then OrderedSet{ownedConjugator.originalType.oclAsType(Feature)}` |
| Wrong variable name | `excludedType->including(self)` | `excludedTypes->including(self)` |
| Wrong variable name | `directionOfExcluding(f, Set{})` | `directionOfExcluding(feature, Set{})` |
| Wrong property name (plural) | `featureMemberships-> selectByKind(ReturnParameterMembership)` | `featureMembership-> selectByKind(ReturnParameterMembership)` |
| Wrong property names | `featuring.type->asOrderedSet()` | `ownedTypeFeaturing.featuringType->asOrderedSet()` |
| Wrong variable name | `visibleMemberships(excluded, true, importAll)` | `visibleMemberships(excluded, true, isImportAll)` |
| Wrong property name (plural) | `inheritedFeature = inheritedMemberships->` | `inheritedFeature = inheritedMembership->` |
| Wrong property and variable names | `memberships->select(memberElement = element) in memberships.memberShortName-> union(memberships.memberName)` | `membership->select(memberElement = element) in elementMemberships.memberShortName-> union(elementMemberships.memberName)` |
| Wrong name: metaClassTypes | `else metaClassTypes->first()` | `else metaclassTypes->first()` |
| Wrong property name: ownedGeneralization | `ownedTyping = ownedGeneralization->` | `ownedTyping = ownedSpecialization->` |
| Typo: owningNamspace | `else owningNamspace.resolveLocal(name)` | `else owningNamespace.resolveLocal(name)` |
| Wrong property names | `subsetting->reject(s \| s.oclIsKindOf(CrossSubsetting)).subsettedFeatures in` | `ownedSubsetting->reject(s \| s.oclIsKindOf(CrossSubsetting)).subsettedFeature in` |
| Wrong variable name | `subsettedFeature->includes(chainingFeature->last())` | `subsettedFeatures->includes(chainingFeature->last())` |
| Wrong receiver | `subsettingFeature.resolveGlobal('Base::Anything').memberElement` | `resolveGlobal('Base::Anything').memberElement` |
| Wrong property name (plural) | `featuringTypes->closure(t \| if not t.oclIsKindOf(Feature)` | `featuringType->closure(t \| if not t.oclIsKindOf(Feature)` |
| Wrong variable name | `else typeMembers->first().oclAsType(Type)` | `else members->first().oclAsType(Type)` |
| Closure body is not an iterator expression | `select(closure(ownedRedefinition.redefinedFeature)-> includes(baseFeature))` | `select(f \| f->closure(ownedRedefinition.redefinedFeature)-> includes(baseFeature))` |
| Not an OCL operation: selectAsKind | `closure(ownedRedefinition.redefinedFeature).ownedMember-> selectAsKind(FeatureValue)` | `closure(ownedRedefinition.redefinedFeature).ownedMember-> selectByKind(FeatureValue)` |
| Wrong operation and type names | `allSuperTypes()->selectAsKind(Feature)` | `allSupertypes()->selectByKind(Feature)` |
| Not an OCL operation, wrong library name | `referent.conformsTo('Anything::self')` | `referent.specializesFromLibrary('Base::Anything::self')` |
| Not OCL operations, wrong type name: isOclKindOf, MetaClass | `referent.owningType.isOclKindOf(MetaClass) or referent.owningType.isOclKindOf(MetadataFeature)` | `referent.owningType.oclIsKindOf(Metaclass) or referent.owningType.oclIsKindOf(MetadataFeature)` |
| Not an OCL operation: oclAsKindOf | `valueEval.oclAsKindOf(LiteralInteger).value` | `valueEval.oclAsType(LiteralInteger).value` |
| Wrong variable name | `result->size() = 1 and results->first()` | `results->size() = 1 and results->first()` |
| Wrong variable name | `if resultExpr->isEmpty() then Sequence{}` | `if resultExprs->isEmpty() then Sequence{}` |
| `.` used where `->` is needed | `select(importedMemberships(Set{})->includes(mem)). first().visibility` | `select(importedMemberships(Set{})->includes(mem))-> first().visibility` |
| `.` used where `->` is needed | `select(m \| m.name = name).indexOf(self) <> 1` | `select(m \| m.name = name)->indexOf(self) <> 1` |
| `.` used where `->` is needed | `if ownedMemberFeatures.isEmpty() then null` | `if ownedMemberFeatures->isEmpty() then null` |
| Not an OCL operation: oclIsType | `membership.memberElement.oclIsType(Feature)` | `membership.memberElement.oclIsKindOf(Feature)` |
| Not an OCL operation: oclKindOf | `memberElement.oclKindOf(other.memberElement.oclType()) or other.memberElement.oclKindOf(memberElement.oclType())` | `memberElement.oclIsKindOf(other.memberElement.oclType()) or other.memberElement.oclIsKindOf(memberElement.oclType())` |
| `.` used where `->` is needed | `featuringType.size() = 1 and (featuringType.first().owner = self implies featuringType.first().oclIsKindOf(Feature) and featuringType.first().oclAsType(Feature)` | `featuringType->size() = 1 and (featuringType->first().owner = self implies featuringType->first().oclIsKindOf(Feature) and featuringType->first().oclAsType(Feature)` |
| Wrong property name: special | `s.special = self` | `s.specific = self` |
| Wrong property name: typing | `closure(typingFeatures()).typing.type` | `closure(typingFeatures()).ownedTyping.type` |
| Wrong property name: memberFeature | `selectByKind(FeatureMembership).memberFeature` | `selectByKind(FeatureMembership).ownedMemberFeature` |
| Wrong property name: redefinition | `ownedFeature.redefinition.redefinedFeature` | `ownedFeature.ownedRedefinition.redefinedFeature` |
| Wrong property name (plural) | `ownedFeatures-> select(direction = _'in')` | `ownedFeature-> select(direction = _'in')` |
| Missing oclAsType | `select(oclIsKindOf(MetadataFeature) and annotatedElement->includes(referencedElement))` | `select(oclIsKindOf(MetadataFeature) and oclAsType(MetadataFeature).annotatedElement->includes(referencedElement))` |
| Missing endif | `else featureValues->first().value.evaluate(self) endif` | `else featureValues->first().value.evaluate(self) endif endif` |
| Missing endif | `else ownedMemberFeatures->first() endif` | `else ownedMemberFeatures->first() endif endif` |
| Missing endif | `feature.redefinesFromLibrary('Occurrences::Occurrence::snapshots') endif` | `feature.redefinesFromLibrary('Occurrences::Occurrence::snapshots') endif endif` |
| Wrong type for the let variable | `let anythingType: Element = resolveGlobal('Base::Anything').memberElement in` | `let anythingType: Type = resolveGlobal('Base::Anything').memberElement.oclAsType(Type) in` |
| Double-quoted strings (OCL uses single quotes) | `resolveGlobal(ns + "::'" + operator + "'")` | `resolveGlobal(ns + '::\'' + operator + '\'')` |
| Wrong property names: shortMemberName, and an extra bracket | `(shortMemberName = null or (shortMemberName <> other.shortMemberName and shortMemberName <> other.memberName)) and (memberName = null or (memberName <> other.shortMemberName and memberName <> other.memberName)))` | `(memberShortName = null or (memberShortName <> other.memberShortName and memberShortName <> other.memberName)) and (memberName = null or (memberName <> other.memberShortName and memberName <> other.memberName))` |
| Wrong variable name | `specializes(otherType) or supertype.oclIsKindOf(Feature) and` | `specializes(otherType) or otherType.oclIsKindOf(Feature) and` |
| Wrong receiver: ownedRedefinitions | `ownedRedefinitions.allRedefinedFeatures()->exists(f \|` | `allRedefinedFeatures()->exists(f \|` |

Also, in `Type::directionOfExcluding`, `supertypes(false)->excluding(excludedSelf)` passes a Set to `excluding`, which takes one element. It is now `supertypes(false)->reject(t | excludedSelf->includes(t))`. Until this fix, `directionOf`, and with it `Type::input`, `output`, `directedFeature` and `parameter`, could not be computed.

Two derivation rules could not be recognised as derivations:

| Rule | Problem | Fix |
| --- | --- | --- |
| deriveConnectorDefaultFeaturingType | Body has no `defaultFeaturingType =` start, and uses `contains`, which is not an OCL operation | Added the start; `contains` → `includes` |
| deriveTypeOwnedIntersecting | Body has no `ownedIntersecting =` start | Added the start |

Problems in the XMI structure:

- The result parameters named `result` of the `evaluate` operations and of `Type::allSupertypes` have no `direction="return"`, so they read as input parameters. Fixed in the patched XMI.
- `Type::isCompatibleWith` and `Feature::isCompatibleWith` are Boolean queries but have no result parameter, in the XMI or in the spec. A Boolean result was added.

## KerML 1.0: derivations and bodies missing from the spec

Four items have no usable OCL in the spec, and each needed a decision on how to read it (decided 6 Nov 2026):

| Item | Problem in the spec | Fix in the patched XMI |
| --- | --- | --- |
| `Element::escapedName()` | No body at all, only a description in words. It needs pattern matching and character escaping, which OCL can't express in a reasonable way. Because of this, `Element::qualifiedName` can't be computed either. | A body in Kotlin (language `Kotlin`), copied as is by the generator. It follows the pilot: name, else shortName; kept as is if it is a basic name `[a-zA-Z_][a-zA-Z0-9_]*`, else quoted with `'` and special characters escaped. |
| `Function::isModelLevelEvaluable` | No OCL. The spec says only that "certain Functions from the Kernel Functions Library" are model-level evaluable, and points to the concrete syntax section. | OCL: the Function's `qualifiedName` is one of the functions marked "Yes" in KerML Tables 5 and 7 (8.2.5.8). |
| `Feature::valuation` | Defined only as the non-navigable association end opposite `FeatureValue::featureWithValue`, with no OCL. Six rules navigate to it anyway. | Made a navigable derived property of Feature: `ownedMembership->selectByKind(FeatureValue)->first()`, as its description says. |
| `InstantiationExpression::argument` | The rules on `InvocationExpression` and `ConstructorExpression` use `valuation` (above), and say `ownedFeatures` for `ownedFeature`. | Names fixed; works once `valuation` exists. |

46 other derived properties have no derivation in the XMI. A `derive<Class><Property>` rule was added for each, following the pilot's setting delegates (`org.omg.sysml.delegate.setting`). TYPES stands for the `Feature::type` derivation, repeated because a property must not use the property it redefines: `let types : OrderedSet(Type) = OrderedSet{self}->closure(typingFeatures()).ownedTyping.type->asOrderedSet() in types->reject(t1 | types->exists(t2 | t2 <> t1 and t2.specializes(t1)))`.

| Property | Derivation added | Source |
| --- | --- | --- |
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
| FeatureValue::featureWithValue | `if owningRelatedElement.oclIsKindOf(Feature) then owningRelatedElement.oclAsType(Feature) else null endif` | pilot |
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
| Behavior::parameter | `feature->select(f \| directionOf(f) <> null)` | as Type::directedFeature |
| Step::parameter | `feature->select(f \| directionOf(f) <> null)` | as Type::directedFeature |
| Function::expression | `step->selectByKind(Expression)` | pilot |

## SysML 2.0: errors in the OCL

14 OCL expressions in the SysML XMI have misspelt or wrong names, wrong types or missing brackets, fixed in `sysml_patched.xmi`:

| Problem | Was | Now |
| --- | --- | --- |
| Typo: featureMembersip | `featureMembersip->` | `featureMembership->` |
| Typo: featureMemberhsip | `framedConcern.featureMemberhsip->` | `framedConcern.featureMembership->` |
| Typo: refrencedFeature | `refrencedFeature.oclAsType(` | `referencedFeature.oclAsType(` |
| Typo: StakholderMembership | `selectByKind(StakholderMembership)` | `selectByKind(StakeholderMembership)` |
| Typo: AStakholderMembership | `selectByKind(AStakholderMembership)` | `selectByKind(StakeholderMembership)` |
| Type does not exist: FlowConnectionUsage | `selectByKind(FlowConnectionUsage)` | `selectByKind(FlowUsage)` |
| Wrong property name: ownedRequirement | `satisfiedViewpoint = ownedRequirement->` | `satisfiedViewpoint = nestedRequirement->` |
| Wrong property name: ownedUseCase | `includedUseCase = ownedUseCase->` | `includedUseCase = nestedUseCase->` |
| Wrong property name (plural) | `rendering = usages->` | `rendering = usage->` |
| Wrong property name, navigated twice | `selectByKind(ObjectiveMembership). ownedRequirement in if objectives->isEmpty() then null else objectives->first().ownedObjectiveRequirement` | `selectByKind(ObjectiveMembership). ownedObjectiveRequirement in if objectives->isEmpty() then null else objectives->first()` |
| select gives Membership, not FeatureValue | `ownedMembership->select(oclIsKindOf(FeatureValue)) in` | `ownedMembership->selectByKind(FeatureValue) in` |
| Wrong type: Action, where ActionUsage is meant | `parameter.oclIsKindOf(Action) then parameter.oclAsType(Action)` | `parameter.oclIsKindOf(ActionUsage) then parameter.oclAsType(ActionUsage)` |
| Missing endif | `else sourceFeature.featureTarget.oclAsType(ActionUsage)` | `else sourceFeature.featureTarget.oclAsType(ActionUsage) endif` |
| Wrong operation names and missing bracket | `mult.allSuperTypes()->exists( oclisKindOf(MultiplicityRange) and oclAsType(MultiplicityRange).hasBounds(lower, upper) endif` | `mult.allSupertypes()->exists( oclIsKindOf(MultiplicityRange) and oclAsType(MultiplicityRange).hasBounds(lower, upper)) endif` |

Five derivation rules have the wrong content or wrong logic:

| Rule | Problem | Fix |
| --- | --- | --- |
| derivePartUsagePartDefinition | No `partDefinition =` start | Added the start |
| deriveTransitionUsageEffectAction | A copy of the triggerAction rule: it defines `triggerAction` and selects the trigger kind | `effectAction = … select(kind = TransitionFeatureKind::effect).transitionFeature->selectByKind(ActionUsage)` |
| deriveTransitionUsageGuardExpression | Selects `TransitionFeatureKind::trigger` instead of `guard` | `guard` |
| deriveUsageNestedEnumeration | Defines `ownedNested`, which does not exist | `nestedEnumeration = nestedUsage->selectByKind(EnumerationUsage)` |
| deriveSatisfyRequirementUsageSatisfyingFeature | Typo `exits` for `exists`; the let variable is typed `BindingConnector` but holds a collection; and the condition is the wrong way round, so the rule gives null whenever a binding to another feature exists, which is always. It also relies on a BindingConnector that a tool must add as an implied relationship; a model read from the textual notation has only the subject's FeatureValue. | The other end of the BindingConnector to the subjectParameter (spec rule, corrected); if there is none, the Feature referenced by the subject's FeatureValue (as the pilot does). |

## SysML 2.0: derived properties with no OCL

52 derived properties have no derivation in the SysML XMI. A rule was added for each, following the pilot, except where noted. TYPES is the `Feature::type` derivation, as in the KerML section.

| Property | Derivation added | Source |
| --- | --- | --- |
| PortConjugation::conjugatedPortDefinition | `if owningRelatedElement.oclIsKindOf(ConjugatedPortDefinition) then owningRelatedElement.oclAsType(ConjugatedPortDefinition) else null endif` | pilot |
| ActorMembership::ownedActorParameter | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(PartUsage) then e.oclAsType(PartUsage) else null endif` | pilot |
| StakeholderMembership::ownedStakeholderParameter | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(PartUsage) then e.oclAsType(PartUsage) else null endif` | pilot |
| SubjectMembership::ownedSubjectParameter | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Usage) then e.oclAsType(Usage) else null endif` | pilot |
| ObjectiveMembership::ownedObjectiveRequirement | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(RequirementUsage) then e.oclAsType(RequirementUsage) else null endif` | pilot |
| RequirementConstraintMembership::ownedConstraint | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(ConstraintUsage) then e.oclAsType(ConstraintUsage) else null endif` | pilot |
| FramedConcernMembership::ownedConcern | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(ConcernUsage) then e.oclAsType(ConcernUsage) else null endif` | pilot |
| RequirementVerificationMembership::ownedRequirement | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(RequirementUsage) then e.oclAsType(RequirementUsage) else null endif` | pilot |
| StateSubactionMembership::action | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(ActionUsage) then e.oclAsType(ActionUsage) else null endif` | pilot |
| TransitionFeatureMembership::transitionFeature | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Step) then e.oclAsType(Step) else null endif` | pilot |
| VariantMembership::ownedVariantUsage | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(Usage) then e.oclAsType(Usage) else null endif` | pilot |
| ViewRenderingMembership::ownedRendering | `let e : Element = ownedRelatedElement->first() in if e.oclIsKindOf(RenderingUsage) then e.oclAsType(RenderingUsage) else null endif` | pilot |
| Usage::owningDefinition | `if owningType.oclIsKindOf(Definition) then owningType.oclAsType(Definition) else null endif` | pilot |
| Usage::owningUsage | `if owningType.oclIsKindOf(Usage) then owningType.oclAsType(Usage) else null endif` | pilot |
| ConjugatedPortDefinition::ownedPortConjugator | `ownedRelationship->selectByKind(PortConjugation)->first()` | pilot |
| ConjugatedPortDefinition::originalPortDefinition | `ownedPortConjugator.originalPortDefinition` | pilot |
| Usage::definition | `TYPES->selectByKind(Classifier)` | pilot |
| OccurrenceUsage::occurrenceDefinition | `TYPES->selectByKind(Class)` | pilot |
| ActionUsage::actionDefinition | `TYPES->selectByKind(Behavior)` | spec type Behavior (pilot narrows to ActionDefinition) |
| StateUsage::stateDefinition | `TYPES->selectByKind(Behavior)` | spec type Behavior (pilot narrows to StateDefinition) |
| AttributeUsage::attributeDefinition | `TYPES->selectByKind(DataType)` | pilot |
| EnumerationUsage::enumerationDefinition | `TYPES->selectByKind(EnumerationDefinition)->first()` | pilot |
| ConnectionUsage::connectionDefinition | `TYPES->selectByKind(AssociationStructure)` | pilot |
| AllocationUsage::allocationDefinition | `TYPES->selectByKind(AllocationDefinition)` | pilot |
| InterfaceUsage::interfaceDefinition | `TYPES->selectByKind(InterfaceDefinition)` | pilot |
| FlowUsage::flowDefinition | `TYPES->selectByKind(Interaction)` | pilot (as Flow::interaction) |
| PortUsage::portDefinition | `TYPES->selectByKind(PortDefinition)` | pilot |
| MetadataUsage::metadataDefinition | `TYPES->selectByKind(Metaclass)->first()` | pilot |
| CalculationUsage::calculationDefinition | `TYPES->selectByKind(Function)->first()` | pilot |
| CaseUsage::caseDefinition | `TYPES->selectByKind(CaseDefinition)->first()` | pilot |
| AnalysisCaseUsage::analysisCaseDefinition | `TYPES->selectByKind(AnalysisCaseDefinition)->first()` | pilot |
| VerificationCaseUsage::verificationCaseDefinition | `TYPES->selectByKind(VerificationCaseDefinition)->first()` | pilot |
| UseCaseUsage::useCaseDefinition | `TYPES->selectByKind(UseCaseDefinition)->first()` | pilot |
| ConstraintUsage::constraintDefinition | `TYPES->selectByKind(Predicate)->first()` | pilot |
| RequirementUsage::requirementDefinition | `TYPES->selectByKind(RequirementDefinition)->first()` | pilot |
| ConcernUsage::concernDefinition | `TYPES->selectByKind(ConcernDefinition)->first()` | pilot |
| ViewpointUsage::viewpointDefinition | `TYPES->selectByKind(ViewpointDefinition)->first()` | pilot |
| RenderingUsage::renderingDefinition | `TYPES->selectByKind(RenderingDefinition)->first()` | pilot |
| ViewUsage::viewDefinition | `TYPES->selectByKind(ViewDefinition)->first()` | pilot |
| ConnectionDefinition::connectionEnd | `feature->select(isEnd)->selectByKind(Usage)` | pilot |
| FlowDefinition::flowEnd | `feature->select(isEnd)->selectByKind(Usage)` | pilot (as Type::endFeature) |
| InterfaceDefinition::interfaceEnd | `feature->select(isEnd)->selectByKind(PortUsage)` | pilot |
| EnumerationDefinition::enumeratedValue | `variantMembership.ownedVariantUsage->selectByKind(EnumerationUsage)` | pilot (as Definition::variant) |
| AttributeUsage::isReference | `true` | spec: "Always true for an AttributeUsage" |
| EventOccurrenceUsage::isReference | `true` | spec: "Always true for an EventOccurrenceUsage" |
| ReferenceUsage::isReference | `true` | spec: "Always true for a ReferenceUsage" |
| PerformActionUsage::performedAction | `let r : Feature = referencedFeatureTarget() in if r.oclIsKindOf(ActionUsage) then r.oclAsType(ActionUsage) else self endif` | pilot |
| ExhibitStateUsage::exhibitedState | `let r : Feature = referencedFeatureTarget() in if r.oclIsKindOf(StateUsage) then r.oclAsType(StateUsage) else self endif` | pilot |
| IncludeUseCaseUsage::useCaseIncluded | `let r : Feature = referencedFeatureTarget() in if r.oclIsKindOf(UseCaseUsage) then r.oclAsType(UseCaseUsage) else self endif` | pilot |
| SatisfyRequirementUsage::satisfiedRequirement | `let r : Feature = referencedFeatureTarget() in if r.oclIsKindOf(RequirementUsage) then r.oclAsType(RequirementUsage) else self endif` | pilot |
| FramedConcernMembership::referencedConcern | `let r : Feature = ownedConcern.referencedFeatureTarget() in if r.oclIsKindOf(ConcernUsage) then r.oclAsType(ConcernUsage) else ownedConcern endif` | pilot |
| RequirementVerificationMembership::verifiedRequirement | `let r : Feature = ownedRequirement.referencedFeatureTarget() in if r.oclIsKindOf(RequirementUsage) then r.oclAsType(RequirementUsage) else ownedRequirement endif` | pilot |

## UML 2.5.1: problems found (XMI not patched)

The UML XMI (ptc-18-01-01) is used as published, so these problems are left in place: 6 errors in its OCL, and 29 more rules or bodies that can't be used for other reasons. Together they leave 43 TODOs in the generated UML api, because some rules serve both a derived property and its operation.

Errors in the spec OCL:

| Rule | Problem |
| --- | --- |
| Message::messageKind (derivation) | Defined as `messageKind`, the property itself, so it is circular |
| Property::isCompatibleWith | Uses `TypeElement`, which doesn't exist; `TypedElement` is meant |
| Package::makesVisible | Compares `ei.importedElement = VisibilityKind::public`; `ei.visibility` is meant |
| Transition::isConsistentWith | Uses `source.redefinedTransition`, but `source` is a Vertex, and `redefinedTransition` belongs to Transition |
| Region::isRedefinitionContextValid | Uses `State::redefinedState`, which is not in the metamodel |
| Vertex::isConsistentWith | Calls `oclIsKindOf` on `redefinedElement`, which is a collection |

Rules that navigate non-navigable association ends (the same kind of problem as KerML's `valuation`): `Property::isAttribute`, `isNavigable` and `subsettingContext` (`classifier`); `ConnectorEnd::definingEnd` (`connector`); `MessageEnd::oppositeEnd` (`messageEnd`); `Gate::isInsideCF`, `isOutsideCF`, `getOperand` and `MessageEnd::enclosingFragment` (`combinedFragment`); `Gate::isFormal` and `Action::containingBehavior` (`interaction`); `Gate::isActual` (`interactionUse`); `Classifier::directlyUsedInterfaces` (`supplierDependency`); `Behavior::context` (`nestingClass`).

Rules that use `allInstances()`, which needs a whole model rather than one element: `NamedElement::clientDependency`, `Vertex::incoming` and `outgoing`, `Class::extension` and `ConnectableElement::end`.

No OCL at all: `ExtensionEnd::lower`, `CallAction::inputParameters` and `outputParameters`, and `Behavior::behavioredClassifier`.

Valid OCL that the generator doesn't translate yet: `oclType()` in `Classifier::maySpecializeType`, `NamedElement::isDistinguishableFrom`, `ParameterableElement::isCompatibleWith` and `ValueSpecification::isCompatibleWith`; `isUnique` in `BehavioralFeature::isDistinguishableFrom`; and the comparison with the primitive type `String` in `Parameter::default`.

## SysML 1.7 (ptc/24-01-02): problems in the XMI

- The profile imports the UML StandardProfile with `href="https://www.omg.org/spec/UML/20161101/StandardProfile.xmi#StandardProfile"` (2 places). In the StandardProfile XMI (ptc/18-01-03) the profile's xmi:id is `_0`, so that reference doesn't resolve. The references to its stereotypes (`#Trace`, `#Refine`) do resolve.
- It refers to the StandardProfile by `https`, but the StandardProfile's own URI is `http://www.omg.org/spec/UML/20161101/StandardProfile`. The XMI reader maps the `https` form to the `http` one by default.

## Where the spec and the pilot disagree

The pilot was used to fill gaps, but where the spec says something different, the spec was followed:

| Item | Spec | Pilot | Followed |
| --- | --- | --- | --- |
| ActionUsage::actionDefinition, StateUsage::stateDefinition | Type is Behavior | Selects only ActionDefinition / StateDefinition | Spec |
| isReference of AttributeUsage, EventOccurrenceUsage, ReferenceUsage | "Always true" | `not isComposite` | Spec |
| Behavior::parameter, Step::parameter | Directed features (`directionOf`) | Each feature's own direction | Spec |
| Element::qualifiedName with no name | Falls through to `escapedName()`, which uses shortName | Returns null | Spec |
| Element::escapedName of `$` | Not mentioned | Leaves the global scope symbol unquoted (an identity check that rarely matches) | Spec |
| SatisfyRequirementUsage::satisfyingFeature | BindingConnector to the subjectParameter | The subject's FeatureValue | Spec first, pilot as fallback |

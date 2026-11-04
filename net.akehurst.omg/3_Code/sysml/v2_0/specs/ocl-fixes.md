# OCL fixes in sysml_patched.xmi

The OCL in the OMG XMI (and in the formal spec text) has errors that stop it being translated to Kotlin. These are the edits made to the patched XMI. Each was checked against the property and operation names in the metamodel; where the intent was not clear from the metamodel, the formal spec prose or the OMG pilot implementation (Systems-Modeling/SysML-v2-Pilot-Implementation) was used.

| was | now |
|---|---|
| `featureMembersip->` | `featureMembership->` |
| `framedConcern.featureMemberhsip->` | `framedConcern.featureMembership->` |
| `refrencedFeature.oclAsType(` | `referencedFeature.oclAsType(` |
| `selectByKind(StakholderMembership)` | `selectByKind(StakeholderMembership)` |
| `selectByKind(AStakholderMembership)` | `selectByKind(StakeholderMembership)` |
| `selectByKind(FlowConnectionUsage)` | `selectByKind(FlowUsage)` |
| `satisfiedViewpoint = ownedRequirement->` | `satisfiedViewpoint = nestedRequirement->` |
| `includedUseCase = ownedUseCase->` | `includedUseCase = nestedUseCase->` |
| `rendering = usages->` | `rendering = usage->` |
| `selectByKind(ObjectiveMembership). ownedRequirement in if objectives->isEmpty() then null else objectives->first().ownedObjectiveRequirement` | `selectByKind(ObjectiveMembership). ownedObjectiveRequirement in if objectives->isEmpty() then null else objectives->first()` |
| `ownedMembership->select(oclIsKindOf(FeatureValue)) in` | `ownedMembership->selectByKind(FeatureValue) in` |
| `parameter.oclIsKindOf(Action) then parameter.oclAsType(Action)` | `parameter.oclIsKindOf(ActionUsage) then parameter.oclAsType(ActionUsage)` |
| `else sourceFeature.featureTarget.oclAsType(ActionUsage)` | `else sourceFeature.featureTarget.oclAsType(ActionUsage) endif` |
| `mult.allSuperTypes()->exists( oclisKindOf(MultiplicityRange) and oclAsType(MultiplicityRange).hasBounds(lower, upper) endif` | `mult.allSupertypes()->exists( oclIsKindOf(MultiplicityRange) and oclAsType(MultiplicityRange).hasBounds(lower, upper)) endif` |

## Rules corrected so the derivation is found

A derivation is a `derive...` rule whose body starts `<property> =`. These rules had no such start, named the wrong property, or used an operation that OCL does not have.

| rule | was | now |
|---|---|---|
| derivePartUsagePartDefinition | `itemDefinition->selectByKind(PartDefinition)` | `partDefinition = itemDefinition->selectByKind(PartDefinition)` |
| deriveTransitionUsageEffectAction | `triggerAction = ownedFeatureMembership-> selectByKind(TransitionFeatureMembership)-> select(kind = TransitionFeatureKind::trigger).transitionFeatures-> selectByKind(AcceptActionUsage)` | `effectAction = ownedFeatureMembership->selectByKind(TransitionFeatureMembership)->select(kind = TransitionFeatureKind::effect).transitionFeature->selectByKind(ActionUsage)` |
| deriveUsageNestedEnumeration | `ownedNested = nestedUsage->selectByKind(EnumerationUsage)` | `nestedEnumeration = nestedUsage->selectByKind(EnumerationUsage)` |
| deriveTransitionUsageGuardExpression | `guardExpression = ownedFeatureMembership-> selectByKind(TransitionFeatureMembership)-> select(kind = TransitionFeatureKind::trigger).transitionFeature-> selectByKind(Expression)` | `guardExpression = ownedFeatureMembership-> selectByKind(TransitionFeatureMembership)-> select(kind = TransitionFeatureKind::guard).transitionFeature-> selectByKind(Expression)` |

## Derivations added

These derived properties have no derivation in the OMG XMI. A `derive<Class><Property>` rule was added for each, following the OMG pilot implementation (its setting delegates in `org.omg.sysml.delegate.setting`). The "source" column says where each one comes from.

A property that redefines another is read through the redefining one in a subclass (the generated RAM classes bridge them), so a derivation must not use the property it redefines, or it would call itself. That is why:

- properties that redefine `Feature::type` (directly or through e.g. `Usage::definition`) repeat the `Feature::type` derivation, `let types : OrderedSet(Type) = OrderedSet{self}->closure(typingFeatures()).ownedTyping.type->asOrderedSet() in types->reject(t1 | types->exists(t2 | t2 <> t1 and t2.specializes(t1)))`, written TYPES below;
- the end and parameter properties repeat the derivation of `Type::endFeature` or `Type::directedFeature`;
- the member properties of Membership subclasses use `ownedRelatedElement`, and the owning properties of Relationship subclasses use `owningRelatedElement`, not the properties they redefine.

Where the pilot narrows the type further than the metamodel (`ActionUsage::actionDefinition` and `StateUsage::stateDefinition`: the pilot selects ActionDefinition and StateDefinition), the metamodel type is used, because the spec says other kinds of Behavior are allowed.

| property | derivation | source |
|---|---|---|
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

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

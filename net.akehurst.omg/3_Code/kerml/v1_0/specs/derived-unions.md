# Derived-union report — KerML

Summary: multiple `isDerivedUnion="true"` attributes found. See `kerml_patched.xmi` for details.
Many derived-unions have explicit `<subsettedProperty>` references,
so auto-generation is possible with checks for type/collection compatibility and OCL filters.

Key derived-unions (examples):
- `Root-Namespaces-A_membership_membershipNamespace-membershipNamespace` (association ownedEnd)
- `Root-Namespaces-Namespace-membership` (attribute, ordered)
- `Root-Elements-A_relatedElement_relationship-relationship` (association ownedEnd)

Recommendation: generator should emit stubs by default and optionally auto-generate getters where safety checks pass.


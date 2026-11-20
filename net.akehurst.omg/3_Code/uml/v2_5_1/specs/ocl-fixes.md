# OCL fixes in uml_patched.xmi

`uml_patched.xmi` is a copy of the OMG UML 2.5.1 XMI (`UML_2-5-1_ASM_ptc-18-01-01.xmi`, kept as published next to it) with the edits below. The UML api, ram and agl-types modules are generated from the patched file.

| Rule | was | now | Why |
|---|---|---|---|
| Classifier::directlyUsedInterfaces (body) | `supplierDependency->select(oclIsKindOf(Usage) and client->forAll(oclIsKindOf(Interface)))->collect(client.oclAsType(Interface))->asSet()` | `clientDependency->select(oclIsKindOf(Usage) and supplier->forAll(oclIsKindOf(Interface)))->collect(supplier.oclAsType(Interface))->asSet()` | The published rule navigates `supplierDependency`, a non-navigable association end, and reads backwards: it collects the clients of the Usages whose supplier is this Classifier. The operation's comment ("The Interfaces directly used by this Classifier"), the spec text for Port::required and the Eclipse UML2 implementation all mean the Interfaces that are suppliers of this Classifier's own Usages. |

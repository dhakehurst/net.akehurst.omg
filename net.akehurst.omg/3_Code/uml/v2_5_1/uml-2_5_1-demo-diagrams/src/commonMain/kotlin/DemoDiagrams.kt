/**
 * Copyright (C) 2026 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.akehurst.omg.uml.v2_5_1.demo

import net.akehurst.omg.uml.v2_5_1.api.UML.Packages.Package
import net.akehurst.omg.uml.v2_5_1.xmi.UmlXmiReader

/**
 * Example UML models, one per diagram type, written as UML 2.5.1 XMI and read with the XMI reader.
 * Each example names the package that the diagram shows. Element names are unique within a diagram,
 * because the mapping uses them as node ids.
 */
object DemoDiagrams {

    class Example(val title: String, val packageId: String, val xmi: String)

    private const val HEAD = """<?xml version="1.0" encoding="UTF-8"?>
<xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">"""
    private const val TAIL = "</xmi:XMI>"

    /** primitive types used by the examples, so that they don't need the OMG PrimitiveTypes library */
    private const val TYPES = """
    <packagedElement xmi:type="uml:Package" xmi:id="types" name="Types">
      <packagedElement xmi:type="uml:PrimitiveType" xmi:id="Integer" name="Integer"/>
      <packagedElement xmi:type="uml:PrimitiveType" xmi:id="String" name="String"/>
      <packagedElement xmi:type="uml:PrimitiveType" xmi:id="Boolean" name="Boolean"/>
    </packagedElement>"""

    val classDiagram = Example("Class", "vehicles", """$HEAD
  <uml:Model xmi:id="m" name="Demo">$TYPES
    <packagedElement xmi:type="uml:Package" xmi:id="vehicles" name="Vehicles">
      <packagedElement xmi:type="uml:Class" xmi:id="Vehicle" name="Vehicle" isAbstract="true">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Vehicle.wheels" name="wheels" type="Integer"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Vehicle.colour" name="colour" type="Colour"/>
        <ownedOperation xmi:type="uml:Operation" xmi:id="Vehicle.start" name="start"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Class" xmi:id="Car" name="Car">
        <generalization xmi:type="uml:Generalization" xmi:id="Car.g" general="Vehicle"/>
        <interfaceRealization xmi:type="uml:InterfaceRealization" xmi:id="Car.ir" client="Car" supplier="Drivable" contract="Drivable"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.engine" name="engine" type="Engine" aggregation="composite" association="CarEngine"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Class" xmi:id="Bicycle" name="Bicycle">
        <generalization xmi:type="uml:Generalization" xmi:id="Bicycle.g" general="Vehicle"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Class" xmi:id="Engine" name="Engine">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Engine.power" name="power" type="Integer"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Class" xmi:id="Person" name="Person">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Person.name" name="name" type="String"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Enumeration" xmi:id="Colour" name="Colour">
        <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="Colour.red" name="red"/>
        <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="Colour.green" name="green"/>
        <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="Colour.blue" name="blue"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Interface" xmi:id="Drivable" name="Drivable">
        <ownedOperation xmi:type="uml:Operation" xmi:id="Drivable.drive" name="drive"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Association" xmi:id="CarEngine" memberEnd="CarEngine.car Car.engine">
        <ownedEnd xmi:type="uml:Property" xmi:id="CarEngine.car" name="car" type="Car" association="CarEngine"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Association" xmi:id="Owns" name="owns" memberEnd="Owns.owner Owns.vehicles">
        <ownedEnd xmi:type="uml:Property" xmi:id="Owns.owner" name="owner" type="Person" association="Owns"/>
        <ownedEnd xmi:type="uml:Property" xmi:id="Owns.vehicles" name="vehicles" type="Vehicle" association="Owns"/>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val packageDiagram = Example("Package", "shop", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="shop" name="Shop">
      <packagedElement xmi:type="uml:Package" xmi:id="catalog" name="Catalog">
        <packagedElement xmi:type="uml:Class" xmi:id="Product" name="Product"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Package" xmi:id="orders" name="Orders">
        <packageImport xmi:type="uml:PackageImport" xmi:id="orders.i1" importedPackage="catalog"/>
        <packageImport xmi:type="uml:PackageImport" xmi:id="orders.i2" importedPackage="payments"/>
        <packagedElement xmi:type="uml:Class" xmi:id="Order" name="Order"/>
        <packagedElement xmi:type="uml:Class" xmi:id="OrderLine" name="OrderLine"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Package" xmi:id="payments" name="Payments">
        <packagedElement xmi:type="uml:Class" xmi:id="Payment" name="Payment"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Package" xmi:id="offers" name="Offers">
        <packageMerge xmi:type="uml:PackageMerge" xmi:id="offers.m" mergedPackage="catalog"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Dependency" xmi:id="shop.d1" client="payments" supplier="orders"/>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val objectDiagram = Example("Object", "garage", """$HEAD
  <uml:Model xmi:id="m" name="Demo">$TYPES
    <packagedElement xmi:type="uml:Package" xmi:id="classes" name="Classes">
      <packagedElement xmi:type="uml:Class" xmi:id="Car" name="Car">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.colour" name="colour" type="String"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.wheels" name="wheels" type="Integer"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Class" xmi:id="Person" name="Person">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Person.name" name="name" type="String"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Association" xmi:id="Owns" name="owns" memberEnd="Owns.owner Owns.car">
        <ownedEnd xmi:type="uml:Property" xmi:id="Owns.owner" name="owner" type="Person" association="Owns"/>
        <ownedEnd xmi:type="uml:Property" xmi:id="Owns.car" name="car" type="Car" association="Owns"/>
      </packagedElement>
    </packagedElement>
    <packagedElement xmi:type="uml:Package" xmi:id="garage" name="Garage">
      <packagedElement xmi:type="uml:InstanceSpecification" xmi:id="myCar" name="myCar" classifier="Car">
        <slot xmi:type="uml:Slot" xmi:id="myCar.s1" definingFeature="Car.colour">
          <value xmi:type="uml:LiteralString" xmi:id="myCar.s1.v" value="red"/>
        </slot>
        <slot xmi:type="uml:Slot" xmi:id="myCar.s2" definingFeature="Car.wheels">
          <value xmi:type="uml:LiteralInteger" xmi:id="myCar.s2.v" value="4"/>
        </slot>
      </packagedElement>
      <packagedElement xmi:type="uml:InstanceSpecification" xmi:id="bob" name="bob" classifier="Person">
        <slot xmi:type="uml:Slot" xmi:id="bob.s1" definingFeature="Person.name">
          <value xmi:type="uml:LiteralString" xmi:id="bob.s1.v" value="Bob"/>
        </slot>
      </packagedElement>
      <packagedElement xmi:type="uml:InstanceSpecification" xmi:id="bobsCar" name="bobsCar" classifier="Owns">
        <slot xmi:type="uml:Slot" xmi:id="bobsCar.s1" definingFeature="Owns.owner">
          <value xmi:type="uml:InstanceValue" xmi:id="bobsCar.s1.v" instance="bob"/>
        </slot>
        <slot xmi:type="uml:Slot" xmi:id="bobsCar.s2" definingFeature="Owns.car">
          <value xmi:type="uml:InstanceValue" xmi:id="bobsCar.s2.v" instance="myCar"/>
        </slot>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val componentDiagram = Example("Component", "orderSystem", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="orderSystem" name="OrderSystem">
      <packagedElement xmi:type="uml:Component" xmi:id="WebShop" name="WebShop"/>
      <packagedElement xmi:type="uml:Component" xmi:id="OrderService" name="OrderService">
        <interfaceRealization xmi:type="uml:InterfaceRealization" xmi:id="OrderService.ir" client="OrderService" supplier="IOrder" contract="IOrder"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Component" xmi:id="PaymentService" name="PaymentService">
        <interfaceRealization xmi:type="uml:InterfaceRealization" xmi:id="PaymentService.ir" client="PaymentService" supplier="IPayment" contract="IPayment"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Interface" xmi:id="IOrder" name="IOrder">
        <ownedOperation xmi:type="uml:Operation" xmi:id="IOrder.place" name="placeOrder"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Interface" xmi:id="IPayment" name="IPayment">
        <ownedOperation xmi:type="uml:Operation" xmi:id="IPayment.pay" name="pay"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Usage" xmi:id="u1" client="WebShop" supplier="IOrder"/>
      <packagedElement xmi:type="uml:Usage" xmi:id="u2" client="OrderService" supplier="IPayment"/>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val compositeStructureDiagram = Example("Composite structure", "carStructure", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="parts" name="Parts">
      <packagedElement xmi:type="uml:Class" xmi:id="Engine" name="Engine"/>
      <packagedElement xmi:type="uml:Class" xmi:id="Gearbox" name="Gearbox"/>
      <packagedElement xmi:type="uml:Class" xmi:id="Wheel" name="Wheel"/>
    </packagedElement>
    <packagedElement xmi:type="uml:Package" xmi:id="carStructure" name="CarStructure">
      <packagedElement xmi:type="uml:Class" xmi:id="Car" name="Car">
        <ownedAttribute xmi:type="uml:Port" xmi:id="Car.ignition" name="ignition" aggregation="composite"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.engine" name="engine" type="Engine" aggregation="composite"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.gearbox" name="gearbox" type="Gearbox" aggregation="composite"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Car.wheels" name="wheels" type="Wheel" aggregation="composite"/>
        <ownedConnector xmi:type="uml:Connector" xmi:id="Car.c0" name="start">
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c0.e1" role="Car.ignition"/>
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c0.e2" role="Car.engine"/>
        </ownedConnector>
        <ownedConnector xmi:type="uml:Connector" xmi:id="Car.c1" name="shaft">
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c1.e1" role="Car.engine"/>
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c1.e2" role="Car.gearbox"/>
        </ownedConnector>
        <ownedConnector xmi:type="uml:Connector" xmi:id="Car.c2" name="axle">
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c2.e1" role="Car.gearbox"/>
          <end xmi:type="uml:ConnectorEnd" xmi:id="Car.c2.e2" role="Car.wheels"/>
        </ownedConnector>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val deploymentDiagram = Example("Deployment", "datacentre", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="datacentre" name="Datacentre">
      <packagedElement xmi:type="uml:Device" xmi:id="AppServer" name="AppServer">
        <nestedNode xmi:type="uml:ExecutionEnvironment" xmi:id="JVM" name="JVM">
          <deployment xmi:type="uml:Deployment" xmi:id="JVM.d1" client="JVM" supplier="shop.jar" location="JVM" deployedArtifact="shop.jar"/>
        </nestedNode>
      </packagedElement>
      <packagedElement xmi:type="uml:Device" xmi:id="DbServer" name="DbServer">
        <deployment xmi:type="uml:Deployment" xmi:id="DbServer.d1" client="DbServer" supplier="schema.sql" location="DbServer" deployedArtifact="schema.sql"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Artifact" xmi:id="shop.jar" name="shop.jar" fileName="shop.jar"/>
      <packagedElement xmi:type="uml:Artifact" xmi:id="schema.sql" name="schema.sql" fileName="schema.sql"/>
      <packagedElement xmi:type="uml:CommunicationPath" xmi:id="lan" name="LAN" memberEnd="lan.a lan.b">
        <ownedEnd xmi:type="uml:Property" xmi:id="lan.a" name="app" type="AppServer" association="lan"/>
        <ownedEnd xmi:type="uml:Property" xmi:id="lan.b" name="db" type="DbServer" association="lan"/>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val profileDiagram = Example("Profile", "shopProfile", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="UML" name="UML">
      <packagedElement xmi:type="uml:Class" xmi:id="UML.Class" name="Class"/>
      <packagedElement xmi:type="uml:Class" xmi:id="UML.Component" name="Component"/>
    </packagedElement>
    <packagedElement xmi:type="uml:Profile" xmi:id="shopProfile" name="ShopProfile">
      <metaclassReference xmi:type="uml:ElementImport" xmi:id="shopProfile.r1" importedElement="UML.Class"/>
      <metaclassReference xmi:type="uml:ElementImport" xmi:id="shopProfile.r2" importedElement="UML.Component"/>
      <packagedElement xmi:type="uml:Stereotype" xmi:id="Entity" name="Entity">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Entity.base_Class" name="base_Class" type="UML.Class" association="E_Entity"/>
        <ownedAttribute xmi:type="uml:Property" xmi:id="Entity.table" name="table"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Stereotype" xmi:id="Service" name="Service">
        <ownedAttribute xmi:type="uml:Property" xmi:id="Service.base_Component" name="base_Component" type="UML.Component" association="E_Service"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Extension" xmi:id="E_Entity" name="E_Entity" memberEnd="E_Entity.ext Entity.base_Class">
        <ownedEnd xmi:type="uml:ExtensionEnd" xmi:id="E_Entity.ext" name="extension_Entity" type="Entity" aggregation="composite" association="E_Entity"/>
      </packagedElement>
      <packagedElement xmi:type="uml:Extension" xmi:id="E_Service" name="E_Service" memberEnd="E_Service.ext Service.base_Component">
        <ownedEnd xmi:type="uml:ExtensionEnd" xmi:id="E_Service.ext" name="extension_Service" type="Service" aggregation="composite" association="E_Service"/>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val activityDiagram = Example("Activity", "ordering", """$HEAD
  <uml:Model xmi:id="m" name="Demo">
    <packagedElement xmi:type="uml:Package" xmi:id="ordering" name="Ordering">
      <packagedElement xmi:type="uml:Activity" xmi:id="ProcessOrder" name="ProcessOrder" partition="customer shop">
        <group xmi:type="uml:ActivityPartition" xmi:id="customer" name="Customer" node="start placeOrder pay"/>
        <group xmi:type="uml:ActivityPartition" xmi:id="shop" name="Shop" node="check ok fork ship invoice join done"/>
        <node xmi:type="uml:InitialNode" xmi:id="start" name="start" inPartition="customer"/>
        <node xmi:type="uml:OpaqueAction" xmi:id="placeOrder" name="place order" inPartition="customer"/>
        <node xmi:type="uml:OpaqueAction" xmi:id="check" name="check stock" inPartition="shop"/>
        <node xmi:type="uml:DecisionNode" xmi:id="ok" name="in stock?" inPartition="shop"/>
        <node xmi:type="uml:ForkNode" xmi:id="fork" name="fork" inPartition="shop"/>
        <node xmi:type="uml:OpaqueAction" xmi:id="ship" name="ship" inPartition="shop"/>
        <node xmi:type="uml:OpaqueAction" xmi:id="invoice" name="send invoice" inPartition="shop"/>
        <node xmi:type="uml:AcceptEventAction" xmi:id="pay" name="payment received" inPartition="customer"/>
        <node xmi:type="uml:JoinNode" xmi:id="join" name="join" inPartition="shop"/>
        <node xmi:type="uml:ActivityFinalNode" xmi:id="done" name="done" inPartition="shop"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f1" source="start" target="placeOrder"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f2" source="placeOrder" target="check"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f3" source="check" target="ok"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f4" source="ok" target="fork">
          <guard xmi:type="uml:LiteralString" xmi:id="f4.g" value="yes"/>
        </edge>
        <edge xmi:type="uml:ControlFlow" xmi:id="f5" source="ok" target="done">
          <guard xmi:type="uml:LiteralString" xmi:id="f5.g" value="no"/>
        </edge>
        <edge xmi:type="uml:ControlFlow" xmi:id="f6" source="fork" target="ship"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f7" source="fork" target="invoice"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f8" source="invoice" target="pay"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f9" source="ship" target="join"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f10" source="pay" target="join"/>
        <edge xmi:type="uml:ControlFlow" xmi:id="f11" source="join" target="done"/>
      </packagedElement>
    </packagedElement>
  </uml:Model>
$TAIL""")

    val examples = listOf(
        classDiagram, packageDiagram, objectDiagram, componentDiagram,
        compositeStructureDiagram, deploymentDiagram, profileDiagram, activityDiagram
    )

    /** a new XMI reader (each example is read by its own reader, as the examples reuse ids) */
    fun reader() = UmlXmiReader()

    /** reads the example's XMI and returns the package the diagram shows */
    fun read(example: Example): Read {
        val reader = reader()
        val res = reader.read("${example.packageId}.xmi", example.xmi)
        val pkg = res.elementById(example.packageId) as? Package ?: error("No package '${example.packageId}' in example '${example.title}'")
        return Read(pkg, reader, res.issues.map { it.toString() })
    }

    class Read(val pkg: Package, val reader: UmlXmiReader, val issues: List<String>)
}

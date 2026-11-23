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

package net.akehurst.omg.uml.v2_5_1.xmi

/**
 * A small hand-written model of the fictional spacecraft "Testsat", in the style of a tool export.
 * All names are fictional.
 */
object TestsatModel {

    /** namespace of the stand-in SysML profile below (not the OMG URI, see [SYSML_STANDIN]) */
    const val SYSML_NS = "http://example.org/testsat/sysml-standin"
    const val TEAM_NS = "http://example.org/testsat/profile"
    const val LEGACY_NS = "http://example.org/othertool/legacy"
    const val TOOL_NS = "http://example.org/sometool/xmi-extensions"

    /**
     * A tiny stand-in for the SysML 1.x profile, so the common tests don't need the OMG file:
     * only the stereotypes and tags the Testsat model uses.
     */
    val SYSML_STANDIN = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20161101">
          <uml:Profile xmi:id="SysML" name="SysML" URI="$SYSML_NS">
            <packagedElement xmi:type="uml:Package" xmi:id="SysML.Blocks" name="Blocks">
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.Block" name="Block">
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Block.base_Class" name="base_Class" association="SysML.E_Block">
                  <type href="http://www.omg.org/spec/UML/20161101/UML.xmi#Class"/>
                </ownedAttribute>
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Block.isEncapsulated" name="isEncapsulated">
                  <type href="http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#Boolean"/>
                  <lowerValue xmi:type="uml:LiteralInteger" xmi:id="SysML.Block.isEncapsulated.lower"/>
                </ownedAttribute>
              </packagedElement>
              <packagedElement xmi:type="uml:Extension" xmi:id="SysML.E_Block" name="E_Block" memberEnd="SysML.E_Block.extension_Block SysML.Block.base_Class">
                <ownedEnd xmi:type="uml:ExtensionEnd" xmi:id="SysML.E_Block.extension_Block" name="extension_Block" type="SysML.Block" aggregation="composite" association="SysML.E_Block"/>
              </packagedElement>
            </packagedElement>
            <packagedElement xmi:type="uml:Package" xmi:id="SysML.Requirements" name="Requirements">
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.Requirement" name="Requirement">
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Requirement.base_Class" name="base_Class">
                  <type href="http://www.omg.org/spec/UML/20161101/UML.xmi#Class"/>
                </ownedAttribute>
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Requirement.id" name="id">
                  <type href="http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String"/>
                </ownedAttribute>
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Requirement.text" name="text">
                  <type href="http://www.omg.org/spec/UML/20161101/PrimitiveTypes.xmi#String"/>
                </ownedAttribute>
              </packagedElement>
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.Trace" name="Trace">
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.Trace.base_Abstraction" name="base_Abstraction">
                  <type href="http://www.omg.org/spec/UML/20161101/UML.xmi#Abstraction"/>
                </ownedAttribute>
              </packagedElement>
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.Satisfy" name="Satisfy">
                <generalization xmi:type="uml:Generalization" xmi:id="SysML.Satisfy.g" general="SysML.Trace"/>
              </packagedElement>
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.Verify" name="Verify">
                <generalization xmi:type="uml:Generalization" xmi:id="SysML.Verify.g" general="SysML.Trace"/>
              </packagedElement>
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.DeriveReqt" name="DeriveReqt">
                <generalization xmi:type="uml:Generalization" xmi:id="SysML.DeriveReqt.g" general="SysML.Trace"/>
              </packagedElement>
              <packagedElement xmi:type="uml:Stereotype" xmi:id="SysML.TestCase" name="TestCase">
                <ownedAttribute xmi:type="uml:Property" xmi:id="SysML.TestCase.base_Behavior" name="base_Behavior">
                  <type href="http://www.omg.org/spec/UML/20161101/UML.xmi#Behavior"/>
                </ownedAttribute>
              </packagedElement>
            </packagedElement>
          </uml:Profile>
        </xmi:XMI>
    """.trimIndent()

    /** the team's own profile; SafetyRequirement specialises SysML Requirement */
    val TEAM_PROFILE = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20131001">
          <uml:Profile xmi:id="TP" name="TestsatProfile" URI="$TEAM_NS">
            <packagedElement xmi:type="uml:Enumeration" xmi:id="TP.HazardLevel" name="HazardLevel">
              <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="TP.HazardLevel.minor" name="minor"/>
              <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="TP.HazardLevel.major" name="major"/>
              <ownedLiteral xmi:type="uml:EnumerationLiteral" xmi:id="TP.HazardLevel.catastrophic" name="catastrophic"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Stereotype" xmi:id="TP.SafetyRequirement" name="SafetyRequirement">
              <generalization xmi:type="uml:Generalization" xmi:id="TP.SafetyRequirement.g">
                <general href="SysML.xmi#SysML.Requirement"/>
              </generalization>
              <ownedAttribute xmi:type="uml:Property" xmi:id="TP.SafetyRequirement.hazardLevel" name="hazardLevel" type="TP.HazardLevel"/>
              <ownedAttribute xmi:type="uml:Property" xmi:id="TP.SafetyRequirement.reviewer" name="reviewer">
                <type href="http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String"/>
                <upperValue xmi:type="uml:LiteralUnlimitedNatural" xmi:id="TP.SafetyRequirement.reviewer.upper" value="*"/>
              </ownedAttribute>
              <ownedAttribute xmi:type="uml:Property" xmi:id="TP.SafetyRequirement.margin" name="margin">
                <type href="http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real"/>
              </ownedAttribute>
              <ownedAttribute xmi:type="uml:Property" xmi:id="TP.SafetyRequirement.owner" name="owner">
                <type href="http://www.omg.org/spec/UML/20131001/UML.xmi#NamedElement"/>
              </ownedAttribute>
            </packagedElement>
          </uml:Profile>
        </xmi:XMI>
    """.trimIndent()

    val MODEL = """
        <?xml version="1.0" encoding="UTF-8"?>
        <xmi:XMI xmi:version="2.5.1" xmlns:xmi="http://www.omg.org/spec/XMI/20131001" xmlns:uml="http://www.omg.org/spec/UML/20131001"
                 xmlns:sysml="$SYSML_NS" xmlns:testsat="$TEAM_NS" xmlns:legacy="$LEGACY_NS" xmlns:tool="$TOOL_NS">
          <xmi:Documentation exporter="Some Tool" exporterVersion="1.0"/>
          <uml:Model xmi:type="uml:Model" xmi:id="_model" name="Testsat Model">
            <xmi:Extension extender="Some Tool"><tool:diagrams count="3"/></xmi:Extension>
            <profileApplication xmi:type="uml:ProfileApplication" xmi:id="_pa1">
              <appliedProfile href="SysML.xmi#SysML"/>
            </profileApplication>
            <profileApplication xmi:type="uml:ProfileApplication" xmi:id="_pa2">
              <appliedProfile href="TestsatProfile.xmi#TP"/>
            </profileApplication>
            <packagedElement xmi:type="uml:Package" xmi:id="_structure" name="Structure">
              <packagedElement xmi:type="uml:Class" xmi:id="_testsat" name="Testsat" tool:colour="blue">
                <ownedAttribute xmi:type="uml:Property" xmi:id="_testsat.power" name="power" aggregation="composite" type="_power"/>
                <ownedAttribute xmi:type="uml:Property" xmi:id="_testsat.obc" name="obc" aggregation="composite" type="_obc"/>
                <ownedAttribute xmi:type="uml:Port" xmi:id="_testsat.tm" name="tmPort" type="_tmIf"/>
                <ownedAttribute xmi:type="uml:Property" xmi:id="_testsat.mass" name="mass">
                  <type href="http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real"/>
                  <defaultValue xmi:type="uml:LiteralReal" xmi:id="_testsat.mass.default" value="11.5"/>
                </ownedAttribute>
                <ownedAttribute xmi:type="uml:Property" xmi:id="_testsat.lib" name="calibration">
                  <type href="Missing_Library.xmi#_lib_calibration"/>
                </ownedAttribute>
                <ownedConnector xmi:type="uml:Connector" xmi:id="_testsat.c1" name="powerBus">
                  <end xmi:type="uml:ConnectorEnd" xmi:id="_testsat.c1.e1" role="_testsat.power"/>
                  <end xmi:type="uml:ConnectorEnd" xmi:id="_testsat.c1.e2" role="_testsat.obc"/>
                </ownedConnector>
                <xmi:Extension extender="Some Tool"><tool:position x="10" y="20"/></xmi:Extension>
              </packagedElement>
              <packagedElement xmi:type="uml:Class" xmi:id="_power" name="PowerSubsystem"/>
              <packagedElement xmi:type="uml:Class" xmi:id="_obc" name="OnBoardComputer"/>
              <packagedElement xmi:type="uml:Class" xmi:id="_tmIf" name="TelemetryInterface"/>
            </packagedElement>
            <packagedElement xmi:type="uml:Package" xmi:id="_reqs" name="Requirements">
              <packagedElement xmi:type="uml:Class" xmi:id="_req1" name="Mass Limit"/>
              <packagedElement xmi:type="uml:Class" xmi:id="_req2" name="Power Budget"/>
              <packagedElement xmi:type="uml:Class" xmi:id="_req3" name="Battery Safety"/>
              <packagedElement xmi:type="uml:Abstraction" xmi:id="_sat1" client="_power" supplier="_req2"/>
              <packagedElement xmi:type="uml:Abstraction" xmi:id="_ver1" client="_massTest" supplier="_req1"/>
              <packagedElement xmi:type="uml:Abstraction" xmi:id="_der1">
                <client xmi:idref="_req2"/>
                <supplier xmi:idref="_req1"/>
              </packagedElement>
            </packagedElement>
            <packagedElement xmi:type="uml:Activity" xmi:id="_massTest" name="Mass Test"/>
          </uml:Model>
          <sysml:Block xmi:id="_s1" base_Class="_testsat"/>
          <sysml:Block xmi:id="_s2" base_Class="_power" isEncapsulated="true"/>
          <sysml:Block xmi:id="_s3" base_Class="_obc"/>
          <sysml:Requirement xmi:id="_s4" base_Class="_req1" id="TS-001" text="The Testsat mass shall be below 12 kg."/>
          <sysml:Requirement xmi:id="_s5" base_Class="_req2" id="TS-002">
            <text>The power budget shall have 10 % margin &amp; more.</text>
          </sysml:Requirement>
          <testsat:SafetyRequirement xmi:id="_s6" base_Class="_req3" id="TS-003" text="The battery shall not vent." hazardLevel="catastrophic" margin="0.25" owner="_power">
            <reviewer>Reviewer A</reviewer>
            <reviewer>Reviewer B</reviewer>
          </testsat:SafetyRequirement>
          <sysml:Satisfy xmi:id="_s7" base_Abstraction="_sat1"/>
          <sysml:Verify xmi:id="_s8" base_Abstraction="_ver1"/>
          <sysml:DeriveReqt xmi:id="_s9" base_Abstraction="_der1"/>
          <sysml:TestCase xmi:id="_s10" base_Behavior="_massTest"/>
          <legacy:Tracked xmi:id="_s11" base_Element="_testsat" trackingId="T-77"><note>kept raw</note></legacy:Tracked>
          <tool:Settings xmi:id="_t1" mode="dark"/>
          <xmi:Extension extender="Some Tool"><tool:projectOptions/></xmi:Extension>
        </xmi:XMI>
    """.trimIndent()
}

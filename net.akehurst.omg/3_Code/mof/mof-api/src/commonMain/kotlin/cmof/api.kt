package net.akehurst.omg.mof.api.cmof

//---------------------------------------------------------------------------------------------
// Forward declaration of common EMOF types or placeholders for UML types
// These would typically come from your EMOF API or a UML base library.
// For this CMOF generation, they act as type references.
//---------------------------------------------------------------------------------------------

interface Element {} // Base for many UML elements, corresponds to UML::Element
interface Classifier : Element {} // Corresponds to UML::Classifier
interface StructuralFeature : Element {} // Corresponds to UML::StructuralFeature
interface TypedElement : Element {} // Corresponds to UML::TypedElement
interface MultiplicityElement : Element {} // Corresponds to UML::MultiplicityElement
interface Namespace : Element {} // Corresponds to UML::Namespace

// Specific UML types referenced by CMOF
interface UmlClass : Classifier {}
interface UmlProperty : StructuralFeature {}
interface UmlOperation : Element {} // UML::Operation also inherits Namespace, TypedElement, MultiplicityElement
interface UmlPackage : Namespace {}
interface UmlDataType : Classifier {} // UML::DataType also inherits Namespace
interface UmlAssociation : Classifier {} // UML::Association also inherits Namespace, Relationship

// EMOF types from your project's style (if they were to be used directly)
// For example, if AggregationKind is defined in your EMOF API:
// import net.akehurst.omg.mof.api.emof.AggregationKind

// For now, let's define a placeholder if not imported
enum class AggregationKind { NONE, SHARED, COMPOSITE }
enum class ParameterDirectionKind { IN, OUT, INOUT, RETURN }


//---------------------------------------------------------------------------------------------
// MOF::Common Package
// Based on MOF XMI structure (e.g., _MOF-Common-*)
//---------------------------------------------------------------------------------------------

/**
 * Base for all MOF reflective objects.
 * Corresponds to _MOF-Reflection-Object in XMI.
 * Style adapted from project context.
 */
interface MofObject {
    /**
     * Generic property getter.
     * +get(property: UmlProperty) : MofObject [0..1]
     * XMI ID: _MOF-Reflection-Object-get
     */
    fun get(property: UmlProperty): MofObject?

    /**
     * Generic property setter.
     * +set(property: UmlProperty, value: MofObject)
     * XMI ID: _MOF-Reflection-Object-set
     */
    fun set(property: UmlProperty, value: MofObject?)

    /**
     * Checks if a property is set.
     * +isSet(property: UmlProperty) : Boolean
     * XMI ID: _MOF-Reflection-Object-isSet
     */
    fun isSet(property: UmlProperty): Boolean

    /**
     * Unsets a property.
     * +unset(property: UmlProperty)
     * XMI ID: _MOF-Reflection-Object-unset
     */
    fun unset(property: UmlProperty)

    /**
     * Invokes an operation.
     * +invoke(op: UmlOperation, arguments: List<CmofArgument>) : MofObject [0..1]
     * XMI ID: _MOF-Reflection-Object-invoke
     */
    fun invoke(op: UmlOperation, arguments: List<CmofArgument>): MofObject?

    // equals is already part of Kotlin Any, but MOF defines its own semantics.
    // The XMI shows 'element' as parameter name.
    /**
     * Checks equality with another MofObject based on MOF semantics.
     * +equals(element: MofObject) : Boolean
     * XMI ID: _MOF-Reflection-Object-equals
     */
    fun mofEquals(element: MofObject?): Boolean
}

/**
 * Base for MOF reflective elements that have a metaclass.
 * Corresponds to _MOF-Reflection-Element in XMI.
 */
interface MofElement : MofObject { // In XMI, _MOF-Reflection-Element generalizes _MOF-Reflection-Object
    /**
     * +/metaclass : UmlClass [1..1] {readOnly, derived}
     *   opposite: (Conceptual) UmlClass.instance (not explicitly in CMOF XMI for UmlClass)
     *   XMI ID: _MOF-Reflection-Element-metaclass
     *   Association: _MOF-Reflection-A_metaclass_element
     */
    val metaclass: UmlClass? // Though [1..1] in XMI, derived properties are often nullable in interfaces

    /**
     * Gets the metaclass of this element.
     * +getMetaClass() : UmlClass [1..1]
     * XMI ID: _MOF-Reflection-Element-getMetaClass
     */
    fun getMetaClass(): UmlClass?

    /**
     * Gets the container of this element.
     * +container() : MofElement [0..1]
     * XMI ID: _MOF-Reflection-Element-container
     */
    fun container(): MofElement?

    /**
     * Checks if this element is an instance of a given type.
     * +isInstanceOfType(type: UmlClass, includesSubtypes: Boolean) : Boolean
     * XMI ID: _MOF-Reflection-Element-isInstanceOfType
     */
    fun isInstanceOfType(type: UmlClass?, includesSubtypes: Boolean): Boolean
}


/**
 * Represents a reflective collection in MOF.
 * Corresponds to _MOF-Common-ReflectiveCollection in XMI.
 */
interface ReflectiveCollection<T : MofObject> : MofObject {
    /**
     * +add(object: T) : Boolean
     * XMI ID: _MOF-Common-ReflectiveCollection-add
     */
    fun add(objectToAdd: T?): Boolean // Parameter name from XMI is 'object'

    /**
     * +addAll(objects: ReflectiveCollection<T>) : Boolean
     * XMI ID: _MOF-Common-ReflectiveCollection-addAll
     */
    fun addAll(objects: ReflectiveCollection<T>?): Boolean

    /**
     * +clear()
     * XMI ID: _MOF-Common-ReflectiveCollection-clear
     */
    fun clear()

    /**
     * +remove(object: T) : Boolean
     * XMI ID: _MOF-Common-ReflectiveCollection-remove
     */
    fun remove(objectToRemove: T?): Boolean // Parameter name from XMI is 'object'

    /**
     * +size() : Integer
     * XMI ID: _MOF-Common-ReflectiveCollection-size
     */
    fun size(): Int // MOF Integer maps to Kotlin Int
}

/**
 * Represents a reflective sequence (ordered list) in MOF.
 * Corresponds to _MOF-Common-ReflectiveSequence in XMI.
 */
interface ReflectiveSequence<T : MofObject> : ReflectiveCollection<T> {
    /**
     * +add(index: Integer, object: T)
     * XMI ID: _MOF-Common-ReflectiveSequence-add (overloaded)
     */
    fun add(index: Int, objectToAdd: T?) // Parameter name from XMI is 'object'

    /**
     * +get(index: Integer) : T [0..1]
     * XMI ID: _MOF-Common-ReflectiveSequence-get
     */
    fun get(index: Int): T?

    /**
     * +remove(index: Integer) : Boolean
     *   (XMI return is Boolean, though typically remove(index) returns the element)
     * XMI ID: _MOF-Common-ReflectiveSequence-remove (overloaded)
     */
    fun remove(index: Int): Boolean

    /**
     * +set(index: Integer, object: T) : T [0..1]
     * XMI ID: _MOF-Common-ReflectiveSequence-set
     */
    fun set(index: Int, objectToSet: T?): T? // Parameter name from XMI is 'object'
}


//---------------------------------------------------------------------------------------------
// MOF::Identifiers Package
//---------------------------------------------------------------------------------------------

/**
 * Represents an extent, a collection of model elements.
 * XMI Class ID: _MOF-Identifiers-Extent
 */
interface Identifiers_Extent : MofObject { // Generalizes MofObject (implicitly via XMI structure)
    /**
     * +useContainment() : Boolean
     * XMI ID: _MOF-Identifiers-Extent-useContainment
     */
    fun useContainment(): Boolean

    /**
     * +elements() : ReflectiveSequence<MofObject> [0..1]
     * XMI ID: _MOF-Identifiers-Extent-elements
     */
    fun elements(): ReflectiveSequence<MofObject>?
}

/**
 * Represents an extent of model elements identified by URIs.
 * XMI Class ID: _MOF-Identifiers-URIExtent
 */
interface Identifiers_URIExtent : Identifiers_Extent {
    /**
     * +contextURI() : String [0..1]
     * XMI ID: _MOF-Identifiers-URIExtent-contextURI
     */
    fun contextURI(): String?

    /**
     * +uri(object: MofElement) : String [0..1]
     * XMI ID: _MOF-Identifiers-URIExtent-uri
     */
    fun uri(objectParam: MofElement?): String? // Parameter name from XMI is 'object'

    /**
     * +element(uri: String) : MofElement [0..1]
     * XMI ID: _MOF-Identifiers-URIExtent-element
     */
    fun element(uri: String?): MofElement?
}

//---------------------------------------------------------------------------------------------
// MOF::Extension Package
//---------------------------------------------------------------------------------------------

/**
 * Represents a Tag in MOF Extension.
 * XMI Class ID: _MOF-Extension-Tag
 */
interface Extension_Tag : MofElement { // Generalizes MofElement
    /**
     * +name : String [1..1]
     * XMI ID: _MOF-Extension-Tag-name
     */
    var name: String? // Assuming String can represent the EMOF String type

    /**
     * +value : String [1..1]
     * XMI ID: _MOF-Extension-Tag-value
     */
    var value: String?

    /**
     * +element [0..*]
     *   opposite: (Conceptual) MofElement.tag (via A_element_tag)
     *   XMI ID: _MOF-Extension-Tag-element
     *   Association: _MOF-Extension-A_element_tag
     */
    var element: List<MofElement> // [0..*] typically a List if order might matter or no Bag type
}

//---------------------------------------------------------------------------------------------
// MOF::Reflection Package
//---------------------------------------------------------------------------------------------

/**
 * Factory for creating reflective MOF elements and converting data types.
 * XMI Class ID: _MOF-Reflection-Factory
 */
interface Reflection_Factory : MofElement { // Generalizes MofElement
    /**
     * +package : UmlPackage [1..1]
     *   opposite: (Conceptual) UmlPackage.factory (via A_package_factory)
     *   XMI ID: _MOF-Reflection-Factory-package
     *   Association: _MOF-Reflection-A_package_factory
     */
    var package_: UmlPackage? // Renamed to avoid keyword clash

    /**
     * +createFromString(dataType: UmlDataType, string: String) : MofObject [0..1]
     * XMI ID: _MOF-Reflection-Factory-createFromString
     */
    fun createFromString(dataType: UmlDataType?, string: String?): MofObject?

    /**
     * +convertToString(dataType: UmlDataType, object: MofObject) : String [0..1]
     * XMI ID: _MOF-Reflection-Factory-convertToString
     */
    fun convertToString(dataType: UmlDataType?, objectToConvert: MofObject?): String? // Param name 'object'

    /**
     * +create(metaClass: UmlClass) : MofElement [0..1]
     * XMI ID: _MOF-Reflection-Factory-create
     */
    fun create(metaClass: UmlClass?): MofElement?
}

/**
 * Abstract representation of a type in MOF Reflection.
 * XMI Class ID: _MOF-Reflection-Type
 * Note: This is an abstract class in XMI, not directly instantiable.
 */
interface Reflection_Type { // No explicit generalization in XMI for this specific element
    /**
     * +isInstance(object: MofObject) : Boolean
     * XMI ID: _MOF-Reflection-Type-isInstance
     */
    fun isInstance(objectToCheck: MofObject?): Boolean // Param name 'object'
}

// _MOF-Reflection-Object is MofObject
// _MOF-Reflection-Element is MofElement

//---------------------------------------------------------------------------------------------
// MOF::CMOFReflection Package
//---------------------------------------------------------------------------------------------

/**
 * Base for CMOF reflective elements.
 * Corresponds to _MOF-CMOFReflection-Element in XMI.
 * This element generalizes MofObject in the XMI.
 */
interface CmofElement : MofObject {
    /**
     * +delete()
     * XMI ID: _MOF-CMOFReflection-Element-delete
     */
    fun delete()
}

/**
 * Factory for creating CMOF elements and links.
 * XMI Class ID: _MOF-CMOFReflection-Factory
 * Note: This is an abstract class in XMI.
 */
interface CmofReflection_Factory { // No explicit generalization in XMI for this specific element
    /**
     * +createElement(class: UmlClass, arguments: List<CmofArgument>) : CmofElement [0..1]
     * XMI ID: _MOF-CMOFReflection-Factory-createElement
     */
    fun createElement(classArg: UmlClass?, arguments: List<CmofArgument>): CmofElement? // Param 'class'

    /**
     * +createLink(association: UmlAssociation, firstElement: CmofElement, secondElement: CmofElement) : CmofLink [0..1]
     * XMI ID: _MOF-CMOFReflection-Factory-createLink
     */
    fun createLink(association: UmlAssociation?, firstElement: CmofElement?, secondElement: CmofElement?): CmofLink?
}

/**
 * Represents an argument for operations or factory methods.
 * XMI Class ID: _MOF-CMOFReflection-Argument
 */
interface CmofArgument { // No explicit generalization in XMI
    /**
     * +name : String [1..1]
     * XMI ID: _MOF-CMOFReflection-Argument-name
     */
    var name: String?

    /**
     * +value : MofObject [1..1]
     * XMI ID: _MOF-CMOFReflection-Argument-value
     */
    var value: MofObject?
}

/**
 * Represents an extent in CMOF, providing query capabilities.
 * XMI Class ID: _MOF-CMOFReflection-Extent
 * Note: This is an abstract class in XMI.
 */
interface CmofReflection_Extent { // No explicit generalization in XMI
    /**
     * +elementsOfType(type: UmlClass, includesSubtypes: Boolean) : List<CmofElement> [0..*]
     * XMI ID: _MOF-CMOFReflection-Extent-elementsOfType
     */
    fun elementsOfType(type: UmlClass?, includesSubtypes: Boolean): List<CmofElement>

    /**
     * +linksOfType(type: UmlAssociation, includesSubtypes: Boolean) : List<CmofLink> [0..*]
     * XMI ID: _MOF-CMOFReflection-Extent-linksOfType
     */
    fun linksOfType(type: UmlAssociation?, includesSubtypes: Boolean): List<CmofLink>

    /**
     * +linkedElements(association: UmlAssociation, endElement: CmofElement, end1ToEnd2Direction: Boolean) : List<CmofElement> [0..*]
     * XMI ID: _MOF-CMOFReflection-Extent-linkedElements
     */
    fun linkedElements(association: UmlAssociation?, endElement: CmofElement?, end1ToEnd2Direction: Boolean): List<CmofElement>

    /**
     * +linkExists(association: UmlAssociation, firstElement: CmofElement, secondElement: CmofElement) : Boolean
     * XMI ID: _MOF-CMOFReflection-Extent-linkExists
     */
    fun linkExists(association: UmlAssociation?, firstElement: CmofElement?, secondElement: CmofElement?): Boolean
}

/**
 * Represents a link (an instance of an association) in CMOF.
 * XMI Class ID: _MOF-CMOFReflection-Link
 */
interface CmofLink : MofObject { // Generalizes MofObject
    /**
     * +firstElement : CmofElement [1..1]
     *   opposite: (Conceptual) CmofElement.link (via A_firstElement_link)
     *   XMI ID: _MOF-CMOFReflection-Link-firstElement
     *   Association: _MOF-CMOFReflection-A_firstElement_link
     */
    var firstElement: CmofElement?

    /**
     * +secondElement : CmofElement [1..1]
     *   opposite: (Conceptual) CmofElement.link (via A_secondElement_link)
     *   XMI ID: _MOF-CMOFReflection-Link-secondElement
     *   Association: _MOF-CMOFReflection-A_secondElement_link
     */
    var secondElement: CmofElement?

    /**
     * +association : UmlAssociation [1..1]
     *   opposite: (Conceptual) UmlAssociation.link (via A_association_link)
     *   XMI ID: _MOF-CMOFReflection-Link-association
     *   Association: _MOF-CMOFReflection-A_association_link
     */
    var association: UmlAssociation?

    /**
     * Compares this link with another link for equality.
     * +equals(otherLink: CmofLink) : Boolean
     * XMI ID: _MOF-CMOFReflection-Link-equals
     */
    fun cmofLinkEquals(otherLink: CmofLink?): Boolean // Renamed to avoid clash with MofObject.mofEquals or Any.equals

    /**
     * +delete()
     * XMI ID: _MOF-CMOFReflection-Link-delete
     */
    fun delete()
}

/**
 * Represents an exception in CMOF.
 * XMI Class ID: _MOF-CMOFReflection-Exception
 */
interface CmofReflection_Exception { // No explicit generalization in XMI
    /**
     * +objectInError : CmofElement [1..1] (deprecated in some versions, elementInError preferred)
     * XMI ID: _MOF-CMOFReflection-Exception-objectInError
     */
    var objectInError: CmofElement?

    /**
     * +elementInError : CmofElement [1..1]
     * XMI ID: _MOF-CMOFReflection-Exception-elementInError
     */
    var elementInError: CmofElement?

    /**
     * +description : String [1..1]
     * XMI ID: _MOF-CMOFReflection-Exception-description
     */
    var description: String?
}


//---------------------------------------------------------------------------------------------
// MOF::CMOFExtension Package
//---------------------------------------------------------------------------------------------

/**
 * Represents a Tag in CMOF, extending UML Element.
 * XMI Class ID: _MOF-CMOFExtension-Tag
 */
interface CmofExtension_Tag : Element { // Generalizes UML::Element (our placeholder 'Element')
    /**
     * +tagOwner : UmlElement [0..1] {subsets Element.owner}
     *   opposite: (Conceptual) UmlElement.ownedTag (via A_ownedTag_tagOwner)
     *   XMI ID: _MOF-CMOFExtension-Tag-tagOwner
     *   Association: _MOF-CMOFExtension-A_ownedTag_tagOwner
     */
    var tagOwner: Element? // Type is UmlElement in XMI, using our placeholder
}

package net.akehurst.omg.mof.gen

import java.io.File

class KotlinCodeGenerator(private val model: MofModel) {

    fun generateToFiles(outputDir: File) {
        if (!outputDir.exists()) outputDir.mkdirs()

        val rootMofPackage = model.packages.values.find { it.name == "MOF" && it.parentPackage == null }
        rootMofPackage?.let {
            generatePackageRecursive(it, outputDir, "")
        } ?: model.packages.values.filter { it.parentPackage == null }.forEach { pkg ->
            // Fallback if specific "MOF" root not found or multiple roots
            generatePackageRecursive(pkg, outputDir, "")
        }
    }

    private fun generatePackageRecursive(pkg: MofPackage, baseDir: File, currentPackagePath: String) {
        val safePackageName = pkg.name.lowercase().replace("-", "_")
        val newPackagePath = if (currentPackagePath.isEmpty()) safePackageName else "$currentPackagePath.$safePackageName"

        val packageDir = File(baseDir, newPackagePath.replace('.', File.separatorChar))
        if (!packageDir.exists()) packageDir.mkdirs()

        // Generate classes in this package
        pkg.classes.forEach { cls ->
            val classContent = generateClass(cls, newPackagePath)
            File(packageDir, "${cls.name.capitalize()}.kt").writeText(classContent)
        }

        // Associations are typically represented as properties on classes.
        // We need to ensure these properties are generated on the correct classes.
        // The MofXmiParser attempts to link association ends (MofProperty) to their MofAssociation
        // and potentially to their owning MofClass if they are defined as ownedAttributes.
        // Navigable ownedEnds of an association also become properties.

        // Recursively generate for sub-packages
        pkg.subPackages.forEach { subPkg ->
            generatePackageRecursive(subPkg, baseDir, newPackagePath)
        }
    }


    private fun generateClass(cls: MofClass, packageName: String): String {
        val sb = StringBuilder()
        sb.append("package $packageName\n\n")

        // Basic imports - a real generator would manage these more dynamically
        val imports = mutableSetOf<String>()

        val classType = if (cls.isAbstract) "interface" else "open class"

        sb.append("/**\n * Generated from XMI element: ${cls.xmiId}\n */\n")
        sb.append("${cls.parentPackage?.let { model.primitiveTypes[it.name] } ?: "public"} $classType ${cls.name.capitalize()}")

        // Generalization (Inheritance)
        if (cls.generalizations.isNotEmpty()) {
            val superTypes = cls.generalizations.mapNotNull { genId ->
                model.findClassById(genId)?.let { superClass ->
                    val superClassFullPackage = model.getFullPackagePath(superClass.parentPackage)
                    if (superClassFullPackage.isNotEmpty() && superClassFullPackage != packageName) {
                        imports.add("$superClassFullPackage.${superClass.name.capitalize()}")
                    }
                    superClass.name.capitalize()
                }
            }.joinToString(", ")
            if (superTypes.isNotEmpty()) {
                sb.append(" : $superTypes")
            }
        }
        sb.append(" {\n\n")

        // Attributes explicitly owned by the class
        cls.attributes.forEach {
            sb.append(generateProperty(it, packageName, imports))
        }

        // Association ends that should be properties on this class
        // This requires careful resolution. An association end (MofProperty) might be:
        // 1. An ownedAttribute of this class, referencing an association.
        // 2. An ownedEnd of an association, where the *other* end's type is this class (making this end navigable from here).
        model.associations.values.filter { it.parentPackage == cls.parentPackage }.forEach { assoc ->
            assoc.memberEnds.forEach { prop ->
                // If this property is an end of an association and its type is NOT this class,
                // AND (it's an ownedAttribute of this class OR the other end is this class and this end is navigable)
                // This logic is complex. For simplicity, we assume properties are already in cls.attributes if they belong there.
                // A more robust way is to check if prop.parentClass points to 'cls'.
                // Or, if prop is an ownedEnd of 'assoc', and its 'type' is cls.name, then the *other* end of 'assoc' might be a property on cls.
                // The current parser puts ownedAttributes into cls.attributes.
                // For ownedEnds of associations, they are parsed as MofProperty but their .parentClass might be null.
                // We need to decide on which class to generate the property for an association's ownedEnd.
                // Typically, an ownedEnd implies navigability from the class at the *other* end of the association.
                // Let's assume for now that if a MofProperty has an associationXmiId and its parentClass is this cls, it's already handled.
                // If its parentClass is null but it's an ownedEnd of an association, we need to determine its "effective" owner for generation.
                if (prop.parentClass == null && prop.associationXmiId == assoc.xmiId) {
                    // This is an ownedEnd of an association. Determine if it should be on this class.
                    // This usually means the *other* end of the association points to this class.
                    // For example, if A_element_tag has ownedEnd 'tag' of type 'Tag', and memberEnd 'element' of type 'Element',
                    // then on class 'Element', we might want a property 'tag: Tag' (or List<Tag>).
                    // And on class 'Tag', we might want 'element: Element' (or List<Element>).
                    // The MofProperty 'tag' (ownedEnd) would have type 'Tag'. Its implicit "owner" for generation is 'Element'.
                    // The MofProperty 'element' (attribute of Tag) would have type 'Element'. Its owner is 'Tag'.

                    // This part requires careful modeling of navigability.
                    // For now, we only generate from cls.attributes.
                }
            }
        }


        // Operations
        cls.operations.forEach {
            sb.append(generateOperation(it, packageName, imports))
        }

        sb.append("}\n")

        // Prepend imports
        val importBlock = imports.sorted().joinToString("\n") { "import $it" }
        return if (importBlock.isNotEmpty()) "$importBlock\n\n$sb" else sb.toString()
    }

    private fun resolveTypeName(typeXmiId: String?, typeHref: String?, currentPackageName: String, imports: MutableSet<String>): String {
        typeHref?.let { href ->
            model.primitiveTypes[href]?.let { return it }
            if (href.contains("UML.xmi#")) { // e.g. UML.xmi#Class
                val typeName = href.substringAfterLast("#").capitalize()
                // Assuming standard UML types don't need import if they are basic enough or globally known
                // e.g. "Class" from UML might map to a specific reflection class if generating for a meta-level.
                // For now, just the name.
                return typeName
            }
        }
        typeXmiId?.let { id ->
            model.findClassById(id)?.let { referencedClass ->
                val referencedPackagePath = model.getFullPackagePath(referencedClass.parentPackage)
                if (referencedPackagePath.isNotEmpty() && referencedPackagePath != currentPackageName) {
                    imports.add("$referencedPackagePath.${referencedClass.name.capitalize()}")
                }
                return referencedClass.name.capitalize()
            }
            // Fallback for other known MOF types by ID
            return when(id) {
                "_MOF-Common-ReflectiveSequence" -> { imports.add("kotlin.collections.List"); "List<Any>" } // Or MutableList
                "_MOF-Reflection-Object", "_MOF-Reflection-Element", "_MOF-CMOFReflection-Element" -> "Any" // Placeholder
                "_MOF-CMOFReflection-Link" -> "Any /* Link */"
                "_MOF-CMOFReflection-Argument" -> "Any /* Argument */"
                else -> "Any /* Unknown ID: $id */"
            }
        }
        return "Any" // Default fallback
    }

    private fun generateProperty(prop: MofProperty, currentPackageName: String, imports: MutableSet<String>): String {
        val visibility = "public" // from XMI if available, else default
        val propName = prop.name.decapitalize().let { if (it == "class") "`class`" else it } // escape keywords

        val typeName = resolveTypeName(prop.typeXmiId, prop.typeHref, currentPackageName, imports)

        val finalTypeName = if (prop.upperBound == -1 || prop.upperBound > 1) {
            imports.add("kotlin.collections.List") // Or MutableList
            if (prop.isReadOnly) "List<$typeName>" else { imports.add("kotlin.collections.MutableList"); "MutableList<$typeName>" }
        } else {
            typeName + if (prop.lowerBound == 0 && prop.upperBound == 1) "?" else ""
        }

        val mutability = if (prop.isReadOnly || prop.isDerived) "val" else "var"
        val comment = " // XMI ID: ${prop.xmiId}" + if (prop.associationXmiId != null) ", Association: ${prop.associationXmiId}" else ""


        var propertyString = "    $visibility $mutability $propName: $finalTypeName$comment\n"
        if (prop.isDerived) {
            // Derived properties in interfaces are just declared. In classes, they need a getter.
            // For simplicity, if it's a class and derived, we'll make it abstract or provide a TODO getter.
            if (prop.parentClass?.isAbstract == false) {
                propertyString = "    $visibility $mutability $propName: $finalTypeName$comment\n        get() = TODO(\"Derived property ${prop.name}\")\n"
            }
        }
        return propertyString
    }

    private fun generateOperation(op: MofOperation, currentPackageName: String, imports: MutableSet<String>): String {
        val visibility = op.visibility.ifEmpty { "public" }
        val funName = op.name.decapitalize()

        val paramsString = op.parameters.joinToString(", ") { param ->
            val paramTypeName = resolveTypeName(param.typeXmiId, param.typeHref, currentPackageName, imports)
            "${param.name.decapitalize()}: $paramTypeName"
        }

        val returnTypeString = op.returnParameter?.let {
            resolveTypeName(it.typeXmiId, it.typeHref, currentPackageName, imports)
        } ?: "Unit"

        val comment = " // XMI ID: ${op.xmiId}"

        var signature = "    $visibility fun $funName($paramsString): $returnTypeString$comment"
        if (op.parentClass?.isAbstract == true || op.isQuery && op.parentClass?.isAbstract == false) { // Abstract methods in interfaces, or query methods in open classes
            return "$signature\n\n" // No body for abstract/interface methods
        } else {
            return "$signature {\n        TODO(\"Implement $funName\")\n    }\n\n"
        }
    }
}

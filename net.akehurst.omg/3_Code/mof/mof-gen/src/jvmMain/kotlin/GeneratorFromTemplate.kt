package net.akehurst.omg.mof.gen

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.expressions.processor.ObjectGraphAccessorMutatorByReflection
import net.akehurst.language.agl.processor.FormatOptionsDefault
import net.akehurst.language.api.processor.FormatString
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.types.api.TypesDomain
import net.akehurst.language.types.asm.StdLibDefault
import java.io.File

class GeneratorFromTemplate(
    private val templateString: String,
    private val types: TypesDomain,
) {
    val issues = IssueHolder(LanguageProcessorPhase.FORMAT)
    val template = Agl.formatDomain(FormatString(templateString),types).let {
        check(it.allIssues.errors.isEmpty()) { it.allIssues.toString()}
        it.asm ?: error("Should not be null")
    }
    val objectGraph = ObjectGraphAccessorMutatorByReflection(types, issues)

    fun generateToFiles(model: MofModel, outputDir: File) {
        if (!outputDir.exists()) outputDir.mkdirs()
        val typedModel = objectGraph.toTypedObject(model, StdLibDefault.NothingType)

        for(pkg in model.packages.values) {
            val self = objectGraph.toTypedObject(pkg, StdLibDefault.NothingType)
            val options = FormatOptionsDefault(
                mapOf($$"$MODEL" to typedModel)
            )
            val result = Agl.format(template, objectGraph, self, options)
            check(result.issues.errors.isEmpty()) { result.issues.toString() }
            val file = File(outputDir, pkg.name+".kt")
            file.writeText(result.sentence!!)
            println("Generated file: ${file.absolutePath}")
        }
    }

}
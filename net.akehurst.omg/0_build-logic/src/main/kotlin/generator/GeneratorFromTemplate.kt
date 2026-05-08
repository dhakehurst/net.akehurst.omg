package generator

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.expressions.processor.ObjectGraphAccessorMutatorByReflection
import net.akehurst.language.agl.processor.FormatOptionsDefault
import net.akehurst.language.agl.processor.FormatResultDefault
import net.akehurst.language.api.processor.FormatResult
import net.akehurst.language.api.processor.FormatString
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.types.api.TypesDomain
import net.akehurst.language.types.asm.StdLibDefault
import org.gradle.api.logging.Logger
import java.io.File

class GeneratorFromTemplate(
    val logger: Logger,
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

        val options = FormatOptionsDefault(
            mapOf($$"$MODEL" to typedModel)
        )
        val self = objectGraph.toTypedObject(model, StdLibDefault.NothingType)
        val result = Agl.format(template, objectGraph, self, options)
        check(result.issues.errors.isEmpty()) { result.issues.toString() }
        result.output.filterNot{it.key== FormatResultDefault.DEFAULT }.forEach { (n,v) ->
            val file = File(outputDir, n)
            file.parentFile.mkdirs()
             file.writeText(v)
            logger.info("i: Generated file: ${file.absolutePath}")
        }

    }

}
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

package net.akehurst.omg._simple_mof_for_xmi

import net.akehurst.language.agl.Agl
import net.akehurst.language.agl.expressions.processor.ObjectGraphAccessorMutatorByReflection
import net.akehurst.language.agl.processor.FormatOptionsDefault
import net.akehurst.language.agl.processor.FormatResultDefault
import net.akehurst.language.api.processor.FormatString
import net.akehurst.language.issues.api.LanguageProcessorPhase
import net.akehurst.language.issues.ram.IssueHolder
import net.akehurst.language.types.api.TypesDomain
import net.akehurst.language.types.asm.StdLibDefault
import org.slf4j.Logger
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

    fun generateToFiles(model: MofModel, parameters:Map<String,Any>, outputDir: File) {
        if (!outputDir.exists()) outputDir.mkdirs()
        val output = generate(model, parameters)
        output.filterNot{it.key== FormatResultDefault.DEFAULT }.forEach { (n,v) ->
            val file = File(outputDir, n)
            file.parentFile.mkdirs()
            file.writeText(v)
            logger.info("i: Generated file: ${file.absolutePath}")
        }
    }

    fun generate(model: MofModel, parameters:Map<String,Any>): Map<String, String> {
        val self = objectGraph.toTypedObject(model, StdLibDefault.NothingType)
        val typedParams = parameters.map { (k, v) -> Pair(k,objectGraph.toTypedObject(v, StdLibDefault.AnyType)) }.toMap()
        val options = FormatOptionsDefault(environment = typedParams)
        val result = Agl.format(template, objectGraph, self, options)
        check(result.issues.errors.isEmpty()) { result.issues.toString() }
        return result.output
    }
}
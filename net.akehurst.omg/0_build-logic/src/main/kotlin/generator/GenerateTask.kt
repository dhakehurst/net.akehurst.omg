package generator

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class GenerateTask : DefaultTask() {

    // The source XMI file
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sourceXmi: RegularFileProperty

    // The AGL Format template files
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val generateDir: DirectoryProperty

    // values to pass to the generator
    @get:Input
    abstract var parameters: Map<String, Any>

    // The folder where Kotlin files will be written
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val xmiFile = sourceXmi.get().asFile
        val targetDir = outputDir.get().asFile
        val templateDir = generateDir.get().asFile

        // Clean the directory before generation to avoid stale files
        targetDir.deleteRecursively()
        targetDir.mkdirs()

        logger.lifecycle("Parsing XMI file: ${xmiFile.absolutePath}...")
        if (!xmiFile.exists()) {
            logger.error("e: XMI File not found at $xmiFile")
            throw GradleException("Generate failed")
        }
        val parser = MofXmiParser()
        val mofModel = parser.parse(xmiFile)
        val templateFiles = templateDir.listFiles { it.extension == "agl-fmt" }

        templateFiles.forEach {
            generateForTemplate(mofModel, targetDir, it)
        }

    }

    private fun generateForTemplate(mofModel: MofModel, outputDir: File, aglFormatFile: File) {
        if (!aglFormatFile.exists()) {
            logger.error("e: AglFormat file not found at $aglFormatFile")
            throw GradleException("Generate failed")
        }

        try {
            val templateText = aglFormatFile.readText()
            logger.lifecycle("Generating to directory: ${outputDir.absolutePath}...")
            val generator = GeneratorFromTemplate(logger, templateText, SimpleMof.types)
            generator.generateToFiles(mofModel, parameters, outputDir)
            logger.lifecycle("Generation complete.")
        } catch (e: Exception) {
            logger.error("e: An error occurred while generating from template file '$aglFormatFile'.")
            e.printStackTrace()
            throw GradleException("Generate failed")
        }
    }

}
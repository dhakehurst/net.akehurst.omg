package generator

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class GenerateTask : DefaultTask() {

    // name of the model to generate
    @get:Input
    abstract var modelName: String

    @get:Input
    var instanceRoots: List<String> = emptyList()

    // The source XMI files
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    var sourceXmiPaths: List<RegularFile> = emptyList()

    // The AGL Format template files
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val generateDir: DirectoryProperty

    // values to pass to the generator
    @get:Input
    abstract var parameters: Map<String, Any>

    // values to pass to the generator
    @get:Input
    var referencedTypeMapping: Map<String, String> = emptyMap()

    // The folder where Kotlin files will be written
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val xmiFiles = sourceXmiPaths.map { it.asFile }

        if (xmiFiles.isEmpty()) {
            logger.error("e: No XMI file paths were configured")
            throw GradleException("Generate failed")
        }

        val targetDir = outputDir.get().asFile
        val templateDir = generateDir.get().asFile

        // Clean the directory before generation to avoid stale files
        targetDir.deleteRecursively()
        targetDir.mkdirs()

        val parser = MofXmiParser(modelName, instanceRoots, referencedTypeMapping)
        for(xmiFile in xmiFiles) {
            logger.lifecycle("Parsing XMI file: ${xmiFile.absolutePath}...")
            if (!xmiFile.exists()) {
                logger.error("e: XMI File not found at $xmiFile")
                throw GradleException("Generate failed")
            }
            parser.parse(xmiFile)
        }
        val mofModel = parser.model

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
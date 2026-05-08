import generator.GenerateTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.kotlin)
}

// 1. Register the task
val generate = tasks.register<GenerateTask>("generate") {
    group = "generation"
    description = "Generates things from an OMG-MOF XMI file using AGL format."

    // Set default paths (the target project can override these if needed)
    sourceXmi.set(layout.projectDirectory.file("src/specs/omg.xmi"))

    // Generate into the build directory so we don't pollute version control
    generateDir.set(layout.projectDirectory.dir("src/generator/agl"))

    // Generate into the build directory so we don't pollute version control
    outputDir.set(layout.buildDirectory.dir("generated/sources/generator/commonMain/kotlin"))
}

// 2. Wire into Kotlin Multiplatform dynamically
pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    // Configure the Kotlin Multiplatform extension
    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
        sourceSets.getByName("commonMain") {
            // By passing the Provider (the `.map` block), Gradle automatically
            // understands that the Kotlin compiler depends on 'generate'.
            // You don't even need a manual dependsOn() call!
            kotlin.srcDir(generate.map { it.outputDir })
        }
    }
}

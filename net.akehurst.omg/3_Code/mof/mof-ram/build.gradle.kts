plugins {
    id("project-conventions")
}

project.version = "2.5.1"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":mof-api"))
            }
        }
    }
}
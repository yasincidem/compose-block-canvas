plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinMultiplatform) apply false
}

apiValidation {
    ignoredProjects.addAll(listOf("composeApp"))
}

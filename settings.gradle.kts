pluginManagement {
    val springBootVersion: String = providers.gradleProperty("springBootVersion").get()
    val spotlessPluginVersion: String = providers.gradleProperty("spotlessPluginVersion").get()
    val errorPronePluginVersion: String = providers.gradleProperty("errorPronePluginVersion").get()
    val deployerPluginVersion: String = providers.gradleProperty("deployerPluginVersion").get()

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("com.diffplug.spotless") version spotlessPluginVersion
        id("net.ltgt.errorprone") version errorPronePluginVersion
        id("io.github.danielliu1123.deployer") version deployerPluginVersion
    }
}

rootProject.name = "template-gradle"

include(":module-1")

// Auto install git hooks
val hooksDir = File(rootDir, ".git/hooks")
if (hooksDir.exists() && hooksDir.isDirectory) {
    File(rootDir, ".githooks").listFiles()?.forEach { file ->
        if (file.isFile) {
            java.nio.file.Files.copy(
                file.toPath(),
                File(hooksDir, file.name).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}

import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.springframework.boot") apply false
    id("com.diffplug.spotless") apply false
    id("net.ltgt.errorprone") apply false
    id("io.github.danielliu1123.deployer")
}

val errorProneCoreVersion: String = providers.gradleProperty("errorProneCoreVersion").get()
val nullAwayVersion: String = providers.gradleProperty("nullAwayVersion").get()

deploy {
    dirs = subprojects.map { it.layout.buildDirectory.dir("repo").get().asFile }.filter { it.exists() }
    username = System.getenv("MAVENCENTRAL_USERNAME")
    password = System.getenv("MAVENCENTRAL_PASSWORD")
    publishingType = io.github.danielliu1123.deployer.PublishingType.WAIT_FOR_PUBLISHED
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-parameters", "-XDaddTypeAnnotationsToSymbol=true"))
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc>().configureEach {
        // Suppress warnings about missing Javadoc comments
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    val optional by configurations.creating
    configurations.named("compileOnly") { extendsFrom(optional) }
    val provided by configurations.creating
    configurations.named("compileOnly") { extendsFrom(provided) }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok:+")
        "annotationProcessor"("org.projectlombok:lombok:+")
        "testCompileOnly"("org.projectlombok:lombok:+")
        "testAnnotationProcessor"("org.projectlombok:lombok:+")

        "testImplementation"(platform("org.junit:junit-bom:+"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        "testImplementation"("org.assertj:assertj-core:+")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            targetExclude("**/generated/**")

            toggleOffOn()
            removeUnusedImports()
            forbidWildcardImports()
            importOrder()
            formatAnnotations()
            trimTrailingWhitespace()
            endWithNewline()
            palantirJavaFormat()
        }
    }

    apply(plugin = "net.ltgt.errorprone")
    dependencies {
        "errorprone"("com.google.errorprone:error_prone_core:$errorProneCoreVersion")
        "errorprone"("com.uber.nullaway:nullaway:$nullAwayVersion")
    }
    tasks.withType<JavaCompile>().configureEach {
        options.errorprone {
            isEnabled = project.findProperty("errorprone.enabled")?.toString()?.toBoolean() != false
            // https://github.com/tbroyer/gradle-errorprone-plugin?tab=readme-ov-file#properties
            excludedPaths = ".*/generated/.*"
            // https://github.com/uber/NullAway/wiki/Configuration
            check("NullAway", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.example")
            option("NullAway:HandleTestAssertionLibraries", "true")
        }
    }
}

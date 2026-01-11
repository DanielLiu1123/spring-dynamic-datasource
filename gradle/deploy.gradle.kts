apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

// see https://docs.gradle.org/9.1.0/userguide/configuration_cache_requirements.html#config_cache:requirements:external_processes
val githubUrl = providers.exec {
    commandLine("git", "-C", rootDir, "config", "--get", "remote.origin.url")
}.standardOutput.asText

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])

            // see https://docs.gradle.org/current/userguide/publishing_maven.html
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                url.set(githubUrl)
                name.set(project.name)
                description.set(project.description ?: project.name)
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Freeman")
                        name.set("Freeman Liu")
                        email.set("llw599502537@gmail.com")
                    }
                }
                scm {
                    connection.set(githubUrl.map { "scm:git:git://${it.substring(8)}.git" })
                    developerConnection.set(githubUrl.map { "scm:git:ssh@${it.substring(8)}.git" })
                    url.set(githubUrl)
                }
                // Capture optional dependencies at configuration time for Configuration Cache compatibility
                val optionalDeps = getDependencies("optional")
                val providedDeps = getDependencies("provided")
                withXml {
                    // Generate optional dependencies for optional configuration
                    val dependencies = asNode().get("dependencies") as? groovy.util.NodeList
                    val dependenciesNode = (dependencies?.get(0) as? groovy.util.Node) ?: asNode().appendNode("dependencies")
                    for (dep in optionalDeps) {
                        val dependency = dependenciesNode.appendNode("dependency")
                        dependency.appendNode("groupId", dep["group"])
                        dependency.appendNode("artifactId", dep["name"])
                        dependency.appendNode("version", dep["version"])
                        dependency.appendNode("optional", "true")
                    }
                    for (dep in providedDeps) {
                        val dependency = dependenciesNode.appendNode("dependency")
                        dependency.appendNode("groupId", dep["group"])
                        dependency.appendNode("artifactId", dep["name"])
                        dependency.appendNode("version", dep["version"])
                        dependency.appendNode("scope", "provided")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            if (project.version.toString().endsWith("-SNAPSHOT")) {
                url = uri("https://central.sonatype.com/repository/maven-snapshots")
                credentials {
                    username = System.getenv("MAVENCENTRAL_USERNAME")
                    password = System.getenv("MAVENCENTRAL_PASSWORD")
                }
            } else {
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
    }
}

configure<SigningExtension> {
    if (!version.toString().endsWith("-SNAPSHOT")) {
        val signingKey = System.getenv("GPG_SECRET_KEY")
        val signingPassphrase = System.getenv("GPG_PASSPHRASE")
        if (signingKey == null || signingPassphrase == null) {
            throw GradleException("Need to set GPG_SECRET_KEY and GPG_PASSPHRASE env for signing.")
        }
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign(extensions.getByType<PublishingExtension>().publications["maven"])
    }
}

fun getDependencies(configurationName: String): List<Map<String, String?>> {
    val result = mutableListOf<Map<String, String?>>()
    val cfg = project.configurations.findByName(configurationName) ?: return result

    val resolvedDeps = cfg.resolvedConfiguration.resolvedArtifacts
        .stream()
        .map { it.moduleVersion.id }
        .toList()
        .associate { (it.group + ":" + it.name) to it.version }

    for (dep in cfg.allDependencies) {
        val version = resolvedDeps[dep.group + ":" + dep.name]
        if (dep.group != null && version != null) {
            result.add(mapOf("group" to dep.group, "name" to dep.name, "version" to version))
        }
    }
    return result
}

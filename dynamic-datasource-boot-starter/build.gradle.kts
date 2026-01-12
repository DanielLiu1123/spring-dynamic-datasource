description = "Spring Boot Starter for datasource dynamic routing"

val mybatisSpringBootVersion: String = providers.gradleProperty("mybatisSpringBootVersion").get()

dependencies {

}

apply(from = "${rootDir}/gradle/deploy.gradle.kts")

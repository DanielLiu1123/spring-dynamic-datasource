description = "Spring Boot Starter for datasource dynamic routing"

val mybatisSpringBootVersion: String = providers.gradleProperty("mybatisSpringBootVersion").get()

dependencies {
    api("org.springframework.boot:spring-boot-starter")

    optional("org.springframework.boot:spring-boot-starter-data-jpa")
    optional("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisSpringBootVersion")
}

apply(from = "${rootDir}/gradle/deploy.gradle.kts")

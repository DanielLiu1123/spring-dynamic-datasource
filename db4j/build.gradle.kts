description = "Java library for database operations"

val mybatisSpringBootVersion: String = providers.gradleProperty("mybatisSpringBootVersion").get()

dependencies {
    api("org.springframework.boot:spring-boot-starter")

    optional("org.springframework.boot:spring-boot-starter-jdbc")
    optional("org.springframework.boot:spring-boot-starter-data-jpa")
    optional("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisSpringBootVersion")

    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.zaxxer:HikariCP")
}

apply(from = "${rootDir}/gradle/deploy.gradle.kts")

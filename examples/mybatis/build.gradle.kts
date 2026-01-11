val mybatisSpringBootVersion: String = providers.gradleProperty("mybatisSpringBootVersion").get()
val mybatisDynamicSqlVersion: String = providers.gradleProperty("mybatisDynamicSqlVersion").get()

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation(project(":dynamic-datasource-boot-starter"))
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisSpringBootVersion")
    testRuntimeOnly("org.postgresql:postgresql")
}

val mybatisSpringBootVersion: String = providers.gradleProperty("mybatisSpringBootVersion").get()
val mybatisDynamicSqlVersion: String = providers.gradleProperty("mybatisDynamicSqlVersion").get()

dependencies {
    implementation(project(":dynamic-datasource-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisSpringBootVersion")
    implementation("org.mybatis.dynamic-sql:mybatis-dynamic-sql:$mybatisDynamicSqlVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

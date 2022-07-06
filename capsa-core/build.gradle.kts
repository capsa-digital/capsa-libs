dependencies {
    implementation("com.atlassian.oai:swagger-request-validator-wiremock:${CoreVersion.SWG_VLD_WIREMOCK}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springdoc:springdoc-openapi-ui")
    implementation("com.auth0:java-jwt:${CoreVersion.JAVA_JWT}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

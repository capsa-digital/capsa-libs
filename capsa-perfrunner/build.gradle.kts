import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.ws:spring-ws-core")
    implementation("org.springframework.cloud:spring-cloud-function-web:${CoreVersion.SPRING_CLOUD_FW}")
    implementation("org.apache.jmeter:ApacheJMeter_http")
    implementation("kg.apc:jmeter-plugins-casutg")
    implementation("commons-io:commons-io:${CoreVersion.COMMONS_IO}")
    implementation("com.willowtreeapps.assertk:assertk-jvm")
    implementation(project(":capsa-it"))
}
// TODO: Uncomment to build jar for perfrunner spring app.
//tasks.getByName<BootJar>("bootJar") {
//    enabled = true
//    archiveVersion.set("latest")
//}
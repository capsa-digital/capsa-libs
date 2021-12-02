import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.ws:spring-ws-core")
    implementation("org.springframework.cloud:spring-cloud-function-web:3.1.5")
    implementation("org.apache.jmeter:ApacheJMeter_http")
    implementation("kg.apc:jmeter-plugins-casutg")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.willowtreeapps.assertk:assertk-jvm")
    implementation(project(":capsa-it"))
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
    archiveVersion.set("latest")
}
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
    idea
    java
    `maven-publish`
}

group = "digital.capsa.core"

tasks.withType<Detekt> {
    failFast = false
    jvmTarget = "11"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

repositories {
    maven { setUrl("https://repo1.maven.org/maven2") }
    mavenCentral()
    google()
    jcenter()
    maven { setUrl("https://mvnrepository.com/artifact") }
    maven { setUrl("https://dl.bintray.com/kotlin/kotlinx.html") }
}

dependencyManagement {
    imports {
        mavenBom("io.projectreactor:reactor-bom:${CoreVersion.REACTOR_BOM}")
        mavenBom("org.springframework.boot:spring-boot-starter-parent:${CoreVersion.SPRING_BOOT}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${CoreVersion.SPRING_CLOUD}")
    }
    dependencies {
        dependency("ch.qos.logback.contrib:logback-json-classic:${CoreVersion.LOGBACK_CONTRIB}")
        dependency("ch.qos.logback.contrib:logback-jackson:${CoreVersion.LOGBACK_CONTRIB}")
        dependency("com.willowtreeapps.assertk:assertk-jvm:${CoreVersion.ASSERTK_JVM}")
        dependency("org.hamcrest:java-hamcrest:${CoreVersion.JAVA_HAMCREST}")
        dependency("com.github.tomakehurst:wiremock-jre8:${CoreVersion.WIREMOCK_JRE8}")
        dependency("org.seleniumhq.selenium:selenium-java:${CoreVersion.SELENIUM}")
        dependency("org.seleniumhq.selenium:selenium-chrome-driver:${CoreVersion.SELENIUM}")
        dependency("org.seleniumhq.selenium:selenium-firefox-driver:${CoreVersion.SELENIUM}")
        dependency("com.google.guava:guava:${CoreVersion.GUAVA}")
        dependency("org.springdoc:springdoc-openapi-ui:${CoreVersion.OPENAPI}")
        dependency("com.sun.xml.bind:jaxb-core:${CoreVersion.JAXB_IMPL}")
        dependency("com.sun.xml.bind:jaxb-impl:${CoreVersion.JAXB_IMPL}")
        dependency("javax.xml.bind:jaxb-api:${CoreVersion.JAXB_API}")
    }
}

dependencies {
    implementation("ch.qos.logback.contrib:logback-jackson")
    implementation("ch.qos.logback.contrib:logback-json-classic")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.tomakehurst:wiremock-jre8")
    implementation("com.google.guava:guava")
    implementation("com.willowtreeapps.assertk:assertk-jvm")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.httpcomponents:httpmime")
    implementation("org.hamcrest:java-hamcrest")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime")
    implementation("org.jetbrains.kotlin:kotlin-script-util")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.junit.platform:junit-platform-launcher")
    implementation("org.mockito:mockito-core")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver")
    implementation("org.seleniumhq.selenium:selenium-firefox-driver")
    implementation("org.seleniumhq.selenium:selenium-java")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.ws:spring-ws-core")
    implementation("org.springframework:spring-context")
    runtimeOnly("com.sun.xml.bind:jaxb-core")
    runtimeOnly("com.sun.xml.bind:jaxb-impl")
    runtimeOnly("javax.xml.bind:jaxb-api")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/capsa-digital/capsa-core")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
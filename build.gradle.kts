import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("io.gitlab.arturbosch.detekt") apply false
    id("io.spring.dependency-management")
    id("org.springframework.boot") apply false
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    idea
    java
    `maven-publish`
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    group = "digital.capsa"

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

    tasks.named<Test>("test") {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
            excludeEngines("junit-vintage")
        }
        //testLogging.showStandardStreams = false
        testLogging {
            showCauses = true
            showExceptions = true
            showStackTraces = true
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            afterSuite(KotlinClosure2<TestDescriptor, TestResult, Any>({ desc, result ->
                if (desc.parent == null) { // will match the outermost suite
                    val output =
                        "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
                    println(output)
                }
            }))
        }
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
                url = uri("https://maven.pkg.github.com/telus/capsa-telus-libs")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
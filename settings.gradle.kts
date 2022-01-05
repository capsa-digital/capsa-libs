pluginManagement {
    val gradlePluginVersion = object {
        val SPRING_BOOT_GRADLE_VERSION = "2.6.2"
        val SPRING_DEPENDENCY_MANAGEMENT_VERSION = "1.0.11.RELEASE"
        val KOTLIN_GRADLE_VERSION = "1.6.10"
        val DETEKT_VERSION = "1.19.0"
        val GRADLE_ENTERPRISE_VERSION = "3.8"
        val MOOWORK_VERSION = "1.3.1"
    }
    plugins {
        id("org.springframework.boot") version gradlePluginVersion.SPRING_BOOT_GRADLE_VERSION
        id("io.spring.dependency-management") version gradlePluginVersion.SPRING_DEPENDENCY_MANAGEMENT_VERSION
        id("io.gitlab.arturbosch.detekt") version gradlePluginVersion.DETEKT_VERSION
        id("com.gradle.enterprise") version gradlePluginVersion.GRADLE_ENTERPRISE_VERSION
        id("com.moowork.node") version gradlePluginVersion.MOOWORK_VERSION
        kotlin("jvm") version gradlePluginVersion.KOTLIN_GRADLE_VERSION
        kotlin("plugin.allope") version gradlePluginVersion.KOTLIN_GRADLE_VERSION
        kotlin("plugin.noarg") version gradlePluginVersion.KOTLIN_GRADLE_VERSION
        kotlin("plugin.spring") version gradlePluginVersion.KOTLIN_GRADLE_VERSION
    }
}

plugins {
    id("com.gradle.enterprise")
}

rootProject.name = "capsa-telus-libs"
include("capsa-core")
include("capsa-it")
include("capsa-perfrunner")


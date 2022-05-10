plugins {
    java
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.6.7"
}

group = "org.metalscraps.lostark"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    maven {
        name = "GCS Maven Central mirror Asia Pacific"
        setUrl("https://maven-central-asia.storage-download.googleapis.com/maven2")
    }
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    runtimeOnly("org.springframework.boot:spring-boot-starter-log4j2")
    runtimeOnly("org.apache.logging.log4j:log4j-spring-boot:2.17.2")
    implementation("com.github.Whya5448:telegram-log4j2:1.0.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}
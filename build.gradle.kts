import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.6.20"
}

group = "org.metalscraps.log.appender"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // 6.0.1
    implementation("org.telegram:telegrambots:6.0.+")

    // 2.17.2
    implementation("org.apache.logging.log4j:log4j-core:2.17.+")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }
}
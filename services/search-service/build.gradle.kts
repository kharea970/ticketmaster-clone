plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "search-service"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("co.elastic.clients:elasticsearch-java:8.15.2")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:8.15.2")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
}

configurations.all {
    exclude(group = "org.elasticsearch.client", module = "elasticsearch-rest-high-level-client")
    exclude(group = "org.elasticsearch", module = "elasticsearch")
}

tasks.test { useJUnitPlatform() }

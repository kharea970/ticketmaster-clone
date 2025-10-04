plugins {
    id("org.springframework.boot") version "3.3.4" apply false
    id("io.spring.dependency-management") version "1.1.6"
    java
}

subprojects {
    repositories { mavenCentral() }
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

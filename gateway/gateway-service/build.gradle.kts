plugins {
	id("java")
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "gateway-service"

java {
	toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
	options.release.set(17)
}

repositories {
	mavenCentral()
	maven("https://repo.spring.io/release")
}

extra["springCloudVersion"] = "2023.0.3" // Spring Cloud BOM

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Rate limiting (Redis-based)
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

	// Circuit breaker (Resilience4J)
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.test {
	useJUnitPlatform()
}

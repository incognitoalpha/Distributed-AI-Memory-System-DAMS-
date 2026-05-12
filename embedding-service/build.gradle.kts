plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":shared-lib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.batch:spring-batch-core")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-core")

    // OpenAI for embeddings
    implementation("com.openai:openai-java:0.28.0")

    // Resilience4j for circuit breaker
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("bootJar") {
    (this as org.springframework.boot.gradle.tasks.bundling.BootJar).apply {
        archiveBaseName.set("embedding-service")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("user.timezone", "UTC")
}
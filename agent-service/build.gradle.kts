plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":shared-lib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("net.devh:grpc-client-spring-boot-starter:2.15.0.RELEASE")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j:3.1.1")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // LangChain4j for LLM orchestration
    implementation("dev.langchain4j:langchain4j:0.31.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.31.0")

    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("bootJar") {
    (this as org.springframework.boot.gradle.tasks.bundling.BootJar).apply {
        archiveBaseName.set("agent-service")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("user.timezone", "UTC")
}
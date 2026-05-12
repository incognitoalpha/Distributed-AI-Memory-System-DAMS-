plugins {
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

subprojects {
    group = "com.aimemory"
    version = "1.0.0"

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

tasks.register("allTests") {
    group = "verification"
    description = "Run all tests across all subprojects"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

tasks.register("checkstyleAll") {
    group = "verification"
    description = "Run checkstyle on all subprojects"
    dependsOn(subprojects.map { it.tasks.named("checkstyleMain") })
}
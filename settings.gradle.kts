pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "distributed-ai-memory"

include("shared-lib")
include("gateway")
include("auth-service")
include("agent-service")
include("memory-service")
include("retrieval-service")
include("embedding-service")
include("ranking-engine")
include("pruning-engine")
include("compliance-service")
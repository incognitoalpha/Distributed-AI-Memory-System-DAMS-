#!/bin/bash
# start.sh
# Helper script for Linux/Mac users to stand up infrastructure and build the project

echo -e "\033[0;36m🚀 Starting DAMS Setup...\033[0m"

echo -e "\033[0;33m📦 Starting infrastructure via Docker Compose...\033[0m"
docker-compose up -d

echo -e "\033[0;90m⏳ Waiting for infrastructure to initialize (15s)...\033[0m"
sleep 15

echo -e "\033[0;33m🔨 Building microservices and generating gRPC stubs...\033[0m"
./gradlew build -x test

if [ $? -eq 0 ]; then
    echo -e "\033[0;32m✅ Build Successful! You are ready to run the services.\033[0m"
    echo -e "\033[0;90mExample: ./gradlew :memory-service:bootRun\033[0m"
else
    echo -e "\033[0;31m❌ Build Failed. Please check the logs.\033[0m"
fi

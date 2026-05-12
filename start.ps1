# start.ps1
# Helper script for Windows users to stand up infrastructure and build the project

Write-Host "🚀 Starting DAMS Setup..." -ForegroundColor Cyan

Write-Host "📦 Starting infrastructure via Docker Compose..." -ForegroundColor Yellow
docker-compose up -d

Write-Host "⏳ Waiting for infrastructure to initialize (15s)..." -ForegroundColor Gray
Start-Sleep -Seconds 15

Write-Host "🔨 Building microservices and generating gRPC stubs..." -ForegroundColor Yellow
./gradlew build -x test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build Successful! You are ready to run the services." -ForegroundColor Green
    Write-Host "Example: ./gradlew :memory-service:bootRun" -ForegroundColor Gray
} else {
    Write-Host "❌ Build Failed. Please check the logs." -ForegroundColor Red
}

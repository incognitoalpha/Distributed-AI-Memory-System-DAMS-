$body = @{
    tenantId = "550e8400-e29b-41d4-a716-446655440000"
    userId = "550e8400-e29b-41d4-a716-446655440001"
    roles = @("ROLE_USER")
} | ConvertTo-Json

Write-Host "Getting token directly from Auth..."
$response = Invoke-WebRequest -Uri http://localhost:8081/auth/token -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $($token.Substring(0, 10))..."

$bodyMem = @{
    content = "Test memory"
    memoryType = "SEMANTIC"
    sourceConversationId = "550e8400-e29b-41d4-a716-446655440002"
    sourceSessionId = "550e8400-e29b-41d4-a716-446655440003"
} | ConvertTo-Json

Write-Host "Creating memory..."
$responseMem = Invoke-WebRequest -Uri http://localhost:8080/api/v1/memories -Method POST `
    -ContentType "application/json" `
    -Body $bodyMem `
    -Header @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
Write-Host "Response: $($responseMem.Content)"

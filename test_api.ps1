# 1. Get Token (via Gateway)
$body = @{
    tenantId = "550e8400-e29b-41d4-a716-446655440000"
    userId = "550e8400-e29b-41d4-a716-446655440001"
    roles = @("ROLE_USER")
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri http://localhost:8080/auth/token -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token obtained: $($token.Substring(0, 20))..."

# 2. Create Memory (via Gateway)
$body = @{
    content = "I learned about virtual threads in Java 21"
    memoryType = "SEMANTIC"
    sourceConversationId = "550e8400-e29b-41d4-a716-446655440002"
    sourceSessionId = "550e8400-e29b-41d4-a716-446655440003"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri http://localhost:8080/api/v1/memories -Method POST `
    -ContentType "application/json" `
    -Body $body `
    -Header @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
$memoryId = ($response.Content | ConvertFrom-Json).memoryId
Write-Host "Create Memory Success. ID: $memoryId"

# 3. Get Memory by ID (via Gateway)
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/memories/$memoryId" -Method GET `
    -Header @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
Write-Host "Get Memory Success: $($response.StatusCode)"

# 4. Update Memory (via Gateway)
$body = @{
    content = "Updated content: Virtual threads are amazing in Java 21"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/memories/$memoryId" -Method PUT `
    -ContentType "application/json" `
    -Body $body `
    -Header @{"Authorization"="Bearer $token"} `
    -UseBasicParsing
Write-Host "Update Memory Success: $($response.StatusCode)"

# 5. Search Memories (Retrieval via Gateway)
$body = @{
    query = "Java virtual threads"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri http://localhost:8080/api/v1/retrieval -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -Header @{"Authorization"="Bearer $token"} `
        -UseBasicParsing
    Write-Host "Retrieval Success: $($response.Content)"
} catch {
    Write-Host "Retrieval failed: $($_.Exception.Message)"
}

# 6. Chat with Agent (via Gateway)
$body = @{
    message = "What do I know about Java?"
    sessionId = "session-001"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri http://localhost:8080/api/v1/agent/converse -Method POST `
        -ContentType "application/json" `
        -Body $body `
        -Header @{"Authorization"="Bearer $token"} `
        -UseBasicParsing
    Write-Host "Agent Converse Success: $($response.Content)"
} catch {
    Write-Host "Agent Converse Failed: $($_.Exception.Message)"
}

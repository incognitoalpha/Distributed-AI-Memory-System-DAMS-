$body = @{
    tenantId = "550e8400-e29b-41d4-a716-446655440000"
    userId = "550e8400-e29b-41d4-a716-446655440001"
    roles = @("ROLE_USER")
} | ConvertTo-Json

Write-Host "Testing Auth directly (port 8081)..."
try {
    $response = Invoke-WebRequest -Uri http://localhost:8081/auth/token -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
    Write-Host "Auth direct Success: $($response.Content)"
} catch {
    Write-Host "Auth direct Failed: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Error Body: $respBody"
    }
}

Write-Host "`nTesting Gateway (port 8080)..."
try {
    $responseG = Invoke-WebRequest -Uri http://localhost:8080/auth/token -Method POST -ContentType "application/json" -Body $body -UseBasicParsing
    Write-Host "Gateway Success: $($responseG.Content)"
} catch {
    Write-Host "Gateway Failed: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Error Body: $respBody"
    }
}

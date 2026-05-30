
# AI- generated script for SonarCloud Diagnostic & Execution

$ErrorActionPreference = "Continue"

function Write-Status($message, $success) {
    if ($success) {
        Write-Host "[OK] $message" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $message" -ForegroundColor Red
    }
}

Write-Host "--- Starting SonarCloud Health Check ---" -ForegroundColor Cyan

# 1. Check .env file
if (Test-Path ".env") {
    Write-Status "Found .env file" $true
    Get-Content .env | ForEach-Object {
        if ($_ -match '^(.*?)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Content "env:$name" $value
        }
    }
} else {
    Write-Status "Missing .env file" $false
}

# 2. Check SONAR_TOKEN
if ($env:SONAR_TOKEN) {
    Write-Status "SONAR_TOKEN is set in environment" $true
    
    # Validate Token with API
    try {
        $tokenBase64 = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($env:SONAR_TOKEN):"))
        $response = Invoke-RestMethod -Method Get -Uri "https://sonarcloud.io/api/authentication/validate" -Headers @{ Authorization = "Basic $tokenBase64" }
        if ($response.valid) {
            Write-Status "SONAR_TOKEN is valid (confirmed with SonarCloud API)" $true
        } else {
            Write-Status "SONAR_TOKEN is NOT valid" $false
        }
    } catch {
        Write-Status "Could not reach SonarCloud API to validate token" $false
    }
} else {
    Write-Status "SONAR_TOKEN is missing" $false
}

# 3. Docker & Port Checks
Write-Host "`n--- Docker & Port Diagnostics ---" -ForegroundColor Cyan

# 3.1 Verify Docker Container is Running
$containerStatus = docker ps --filter "name=booking-postgres" --format "{{.Status}}"
if ($containerStatus -match "Up") {
    Write-Status "Docker container 'booking-postgres' is RUNNING ($containerStatus)" $true
} else {
    Write-Status "Docker container 'booking-postgres' is NOT RUNNING" $false
    Write-Host "  -> Attempting to start the database container..." -ForegroundColor Yellow
    docker-compose -f database/docker-compose.psql.yml up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  -> Container started. Waiting 10 seconds for PostgreSQL to initialize..." -ForegroundColor Cyan
        Start-Sleep -Seconds 10
        # Re-verify
        $containerStatus = docker ps --filter "name=booking-postgres" --format "{{.Status}}"
        if ($containerStatus -match "Up") {
             Write-Status "Docker container 'booking-postgres' is now RUNNING" $true
        }
    } else {
        Write-Host "  [ERROR] Failed to start docker container. Please ensure Docker Desktop is running and you have permissions." -ForegroundColor Red
    }
}

# 3.2 Check for Port Conflicts
$portUsage = Get-NetTCPConnection -LocalPort 5434 -ErrorAction SilentlyContinue
if ($portUsage) {
    $processId = $portUsage[0].OwningProcess
    $processName = (Get-Process -Id $processId).ProcessName
    Write-Status "Port 5434 is in use by process: $processName (PID: $processId)" $true
    if ($processName -notmatch "com.docker.backend" -and $processName -notmatch "docker") {
         Write-Host "  [WARNING] Port 5434 is being used by something OTHER than Docker ($processName). This will cause issues!" -ForegroundColor Yellow
    }
} else {
    Write-Status "Port 5434 is free (no conflicts detected)" $true
}

# 4. General Database Connectivity (Port 5434)
$dbCheck = Test-NetConnection -ComputerName 127.0.0.1 -Port 5434 -InformationLevel Quiet
if ($dbCheck) {
    Write-Status "Database is listening and reachable on port 5434" $true
} else {
    Write-Status "Database is NOT reachable on port 5434" $false
}

# 5. Check Maven Wrapper
if (Test-Path "./mvnw") {
    Write-Status "Maven wrapper (mvnw) found" $true
} else {
    Write-Status "Maven wrapper (mvnw) NOT found" $false
}

# 6. Run a Smoke Test (Connectivity Test)
Write-Host "`n--- Running Database Smoke Test ---" -ForegroundColor Cyan
./mvnw test -Dtest=BookingRepositoryIntegrationTest -B
if ($LASTEXITCODE -eq 0) {
    Write-Status "Database integration test passed" $true
} else {
    Write-Status "Database integration test failed. Fix this before running Sonar." $false
}

# 7. Run Sonar Analysis
Write-Host "`n--- Running SonarCloud Analysis ---" -ForegroundColor Cyan
Write-Host "This might take a minute..."

# Capture output to check for common "Automatic Analysis" conflict
$tempFile = New-TemporaryFile
./mvnw verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -B -e | Tee-Object -FilePath $tempFile.FullName

if ($LASTEXITCODE -eq 0) {
    Write-Status "SonarCloud Analysis SUCCESSFUL" $true
    Write-Host "`nResults can be found at: https://sonarcloud.io/dashboard?id=KEAArimaaProject_Booking-System-Software-Quality" -ForegroundColor Cyan
    Write-Host "`nYou should have access, if you have access to the projects github (use github to sign in to Sonar Qube Cloud)"
} else {
    $logs = Get-Content $tempFile.FullName
    if ($logs -match "You are running manual analysis while Automatic Analysis is enabled") {
        Write-Status "SonarCloud Analysis FAILED: Automatic Analysis Conflict" $false
        Write-Host "`n[FIX REQUIRED] SonarCloud Automatic Analysis is enabled." -ForegroundColor Red
        Write-Host "Manual (Maven) analysis is blocked by SonarCloud when Automatic Analysis is active." -ForegroundColor Red
        Write-Host "`nTo fix this:" -ForegroundColor Yellow
        Write-Host "1. Go to: https://sonarcloud.io/project/configuration?id=KEAArimaaProject_Booking-System-Software-Quality&analysisMode" -ForegroundColor Yellow
        Write-Host "2. OR: Administration > Analysis Method" -ForegroundColor Yellow
        Write-Host "3. Turn OFF 'Automatic Analysis'" -ForegroundColor Yellow
        Write-Host "4. Re-run this script.`n" -ForegroundColor Yellow
    } else {
        Write-Status "SonarCloud Analysis FAILED" $false
        Write-Host "Check the logs above for the specific Maven error." -ForegroundColor Yellow
    }
}

# Cleanup
Remove-Item $tempFile.FullName -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0) { exit 1 }

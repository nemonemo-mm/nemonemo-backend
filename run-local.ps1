# .env 파일에서 환경 변수 로드 및 Spring Boot 애플리케이션 실행 (로컬 개발용)

if (Test-Path ".env") {
    Write-Host "Loading environment variables from .env file..." -ForegroundColor Green
    
    Get-Content .env | ForEach-Object {
        # export KEY=VALUE 또는 KEY=VALUE 형식 처리
        if ($_ -match '^\s*(?:export\s+)?([^#=]+?)\s*=\s*(.*)$') {
            $key = $matches[1].Trim() -replace '^\s*export\s+', ''
            $key = $key.Trim()
            $value = $matches[2].Trim() -replace '^["'']|["'']$', ''
            
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  $key = ***" -ForegroundColor Gray
        }
    }
    
    Write-Host "Environment variables loaded successfully!" -ForegroundColor Green
} else {
    Write-Host "Warning: .env file not found!" -ForegroundColor Yellow
}

Write-Host "`nStarting Spring Boot application...`n" -ForegroundColor Cyan
.\gradlew.bat bootRun
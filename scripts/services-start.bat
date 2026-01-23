@echo off
REM ============================================================================
REM Local Services (Redis + Elasticsearch) - Start Script for Windows
REM ============================================================================

echo =====================================
echo Local Dev Services - Starting
echo =====================================
echo.

cd /d "%~dp0.."

REM Check Docker
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop and try again.
    exit /b 1
)

echo [1/2] Starting Redis...
docker-compose -f docker-compose-services.yml up -d redis
echo [OK] Redis container started

echo.
echo [2/2] Starting Elasticsearch...
docker-compose -f docker-compose-services.yml up -d elasticsearch

echo.
echo Waiting for Elasticsearch to be ready...
:wait_es
timeout /t 5 /nobreak >nul
curl -s http://localhost:9200/_cluster/health >nul 2>&1
if %errorlevel% neq 0 (
    echo|set /p="."
    goto wait_es
)
echo.
echo [OK] Elasticsearch is ready

echo.
echo =====================================
echo [SUCCESS] Local Dev Services Started!
echo =====================================
echo.
echo Services available:
echo   - Redis:         localhost:6379
echo   - Elasticsearch: http://localhost:9200
echo.
echo Now you can run Spring Boot apps from IDE with profile: dev
echo.

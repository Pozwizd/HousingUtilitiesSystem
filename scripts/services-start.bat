@echo off
REM ============================================================================
REM Local Services (Redis) - Start Script for Windows
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

echo Starting Redis...
docker-compose -f docker-compose-services.yml up -d redis
echo [OK] Redis container started

echo.
echo =====================================
echo [SUCCESS] Local Dev Services Started!
echo =====================================
echo.
echo Services available:
echo   - Redis: localhost:6379
echo.
echo Now you can run Spring Boot apps from IDE with profile: dev
echo.

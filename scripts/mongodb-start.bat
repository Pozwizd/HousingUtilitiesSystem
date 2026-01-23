@echo off
REM ============================================================================
REM MongoDB Replica Set - Start Script (Windows)
REM ============================================================================

echo =====================================
echo MongoDB Replica Set - Starting
echo =====================================
echo.

cd /d "%~dp0.."

REM Check Docker
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running!
    echo Please start Docker Desktop and try again.
    exit /b 1
)

echo [OK] Docker is running
echo.

REM Start MongoDB Replica Set
echo Starting MongoDB Replica Set (3 nodes)...
docker-compose up -d mongo1 mongo2 mongo3

echo.
echo Waiting for MongoDB nodes to be healthy (30 seconds)...
timeout /t 30 /nobreak >nul

REM Initialize Replica Set
echo Initializing Replica Set...
docker-compose up mongo-init

echo.
echo =====================================
echo [SUCCESS] MongoDB Replica Set started!
echo =====================================
echo.
echo Nodes:
echo   - mongo1 (PRIMARY):   localhost:27017
echo   - mongo2 (SECONDARY): localhost:27018
echo   - mongo3 (SECONDARY): localhost:27019
echo.

pause

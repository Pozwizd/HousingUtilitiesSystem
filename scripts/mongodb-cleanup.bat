@echo off
REM ============================================================================
REM MongoDB Replica Set - Cleanup Script (Windows)
REM ============================================================================

echo =====================================
echo MongoDB Replica Set - Data Cleanup
echo =====================================
echo.
echo This will:
echo   - Stop MongoDB containers (if running)
echo   - Remove MongoDB containers
echo   - Remove all MongoDB data volumes
echo.
echo WARNING: All data will be permanently deleted!
echo.

set /p confirmation="Are you sure? (yes/no): "
if /i not "%confirmation%"=="yes" (
    echo Operation cancelled
    pause
    exit /b 0
)

cd /d "%~dp0.."

echo.
echo Step 1: Stopping MongoDB containers...
docker-compose stop mongo1 mongo2 mongo3 2>nul

echo.
echo Step 2: Removing MongoDB containers...
docker-compose rm -f mongo1 mongo2 mongo3 mongo-init 2>nul

echo.
echo Step 3: Removing MongoDB data volumes...

docker volume rm housingutilitiessystem_mongo1-data 2>nul && echo   [OK] mongo1-data removed || echo   [-] mongo1-data not found
docker volume rm housingutilitiessystem_mongo2-data 2>nul && echo   [OK] mongo2-data removed || echo   [-] mongo2-data not found
docker volume rm housingutilitiessystem_mongo3-data 2>nul && echo   [OK] mongo3-data removed || echo   [-] mongo3-data not found

docker volume rm housingutilitiessystem_mongo1-config 2>nul && echo   [OK] mongo1-config removed || echo   [-] mongo1-config not found
docker volume rm housingutilitiessystem_mongo2-config 2>nul && echo   [OK] mongo2-config removed || echo   [-] mongo2-config not found
docker volume rm housingutilitiessystem_mongo3-config 2>nul && echo   [OK] mongo3-config removed || echo   [-] mongo3-config not found

echo.
echo =====================================
echo [SUCCESS] MongoDB Replica Set cleanup completed
echo =====================================
echo.
echo To start fresh run: mongodb-start.bat
echo.

pause

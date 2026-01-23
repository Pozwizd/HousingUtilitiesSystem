@echo off
REM ============================================================================
REM MongoDB Replica Set - Stop Script (Windows)
REM ============================================================================

echo =====================================
echo MongoDB Replica Set - Stopping
echo =====================================
echo.

cd /d "%~dp0.."

echo Stopping MongoDB containers...
docker-compose stop mongo1 mongo2 mongo3
docker-compose rm -f mongo1 mongo2 mongo3 mongo-init

echo.
echo [SUCCESS] MongoDB Replica Set stopped
echo.
echo Note: Data is preserved in Docker volumes
echo To remove data run: mongodb-cleanup.bat

pause

@echo off
REM ============================================================================
REM Local Services (Redis) - Stop Script for Windows
REM ============================================================================

echo =====================================
echo Local Dev Services - Stopping
echo =====================================
echo.

cd /d "%~dp0.."

docker-compose -f docker-compose-services.yml down

echo.
echo =====================================
echo [SUCCESS] Local Dev Services Stopped!
echo =====================================
echo.

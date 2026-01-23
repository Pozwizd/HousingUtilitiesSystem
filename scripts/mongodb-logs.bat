@echo off
REM ============================================================================
REM MongoDB Replica Set - Logs Script (Windows)
REM ============================================================================

set NODE=%1
if "%NODE%"=="" set NODE=mongo1

echo =====================================
echo MongoDB Logs - %NODE%
echo =====================================
echo Press Ctrl+C to exit
echo.

docker logs -f %NODE%

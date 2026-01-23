@echo off
REM ============================================================================
REM MongoDB Replica Set - Status Script (Windows)
REM ============================================================================

echo =====================================
echo MongoDB Replica Set - Status
echo =====================================
echo.

echo Containers:
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr /i "mongo NAMES"
if errorlevel 1 echo No MongoDB containers running

echo.
echo Replica Set Status:
docker exec mongo1 mongosh --quiet --eval "rs.status().members.forEach(m => print('  ' + m.name + ' - ' + m.stateStr))" 2>nul
if errorlevel 1 echo   Replica Set not available
echo.

pause

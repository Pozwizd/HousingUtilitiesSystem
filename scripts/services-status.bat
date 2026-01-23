@echo off
REM ============================================================================
REM Local Services (Redis + Elasticsearch) - Status Script for Windows
REM ============================================================================

echo =====================================
echo Local Dev Services - Status
echo =====================================
echo.

cd /d "%~dp0.."

echo Container Status:
echo -----------------
docker-compose -f docker-compose-services.yml ps

echo.
echo Health Checks:
echo --------------

REM Redis check
docker exec redis-services redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Redis: healthy (localhost:6379)
) else (
    echo [--] Redis: not running
)

REM Elasticsearch check
curl -s http://localhost:9200/_cluster/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Elasticsearch: running (http://localhost:9200)
) else (
    echo [--] Elasticsearch: not running
)

echo.

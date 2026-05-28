@echo off
setlocal enabledelayedexpansion

:: Resolve directories
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."

echo ====================================================
echo  Deploying Local Development Services (Windows Docker) 
echo ====================================================

cd /d "%ROOT_DIR%"

:: Check for docker-compose command support
set "COMPOSE_CMD=docker-compose"
where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    docker compose version >nul 2>nul
    if %errorlevel% equ 0 (
        set "COMPOSE_CMD=docker compose"
    ) else (
        echo ERROR: Docker Compose docker-compose was not found on your system.
        exit /b 1
    )
)

:: Bring up the environment
echo Starting containers...
call !COMPOSE_CMD! up -d
if %errorlevel% neq 0 (
    echo ERROR: Failed to bring up development services.
    exit /b 1
)

echo.
echo Verifying service status...
call !COMPOSE_CMD! ps

echo.
echo Development environment services deployed successfully!
echo ====================================================
cd /d "%ROOT_DIR%"

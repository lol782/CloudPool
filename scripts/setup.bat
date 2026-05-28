@echo off
setlocal enabledelayedexpansion

:: Resolve directories
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."

echo ====================================================
echo  Starting CloudPool Platform Setup (Windows Command) 
echo ====================================================

:: 1. Compile native Rust module
echo.
echo [1/2] Compiling Native Rust Modules (FFI)...
cd /d "%ROOT_DIR%\backend\rust"
where cargo >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Rust compiler 'cargo' was not found on your PATH.
    exit /b 1
)
call cargo build --release
if %errorlevel% neq 0 (
    echo ERROR: Native Rust compilation failed.
    exit /b 1
)
echo Native Rust modules compiled successfully!

:: 2. Build Spring Boot Java project
echo.
echo [2/2] Resolving Maven and Compiling Spring Boot Backend...
cd /d "%ROOT_DIR%\backend\spring-boot"

:: Resolve maven executable
set "MVN_CMD=mvn"
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    if exist "%ROOT_DIR%\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MVN_CMD=%ROOT_DIR%\apache-maven-3.9.6\bin\mvn.cmd"
        echo Using bundled Maven at: !MVN_CMD!
    ) else (
        echo ERROR: Maven mvn was not found on your PATH and no bundled maven was found.
        exit /b 1
    )
)

call "%MVN_CMD%" clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Spring Boot Java compilation failed.
    exit /b 1
)
echo Spring Boot Java backend compiled successfully!

echo.
echo ====================================================
echo  Setup complete! CloudPool is ready for launch.     
echo ====================================================
cd /d "%ROOT_DIR%"

@echo off
setlocal enabledelayedexpansion

:: Resolve directories
set "SCRIPT_DIR=%~dp0"
set "ROOT_DIR=%SCRIPT_DIR%.."

echo ====================================================
echo  Executing CloudPool Verification Suites (Windows) 
echo ====================================================

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

:: Run tests
call "%MVN_CMD%" clean test
if %errorlevel% neq 0 (
    echo ERROR: Verification suite tests failed.
    exit /b 1
)

echo.
echo Verification suites passed successfully!
echo ====================================================
cd /d "%ROOT_DIR%"

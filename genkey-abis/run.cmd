@echo off
echo =====================================================
echo   Fingerprint Capture Service - Startup Script
echo =====================================================
echo.

REM Set JAVA_HOME if needed
SET JAVA_HOME=C:\Program Files\Java\jdk-17
SET PATH=%JAVA_HOME%\bin;%PATH%

REM Set native library path for ABIS SDK
SET JAVA_TOOL_OPTIONS=-Djava.library.path=C:\xampp\htdocs\AFIS\partner-sdk-examples\abisclient-dll

REM Navigate to project directory
cd /d "%~dp0"

echo Starting Fingerprint Capture Service...
echo.
echo JAVA_HOME: %JAVA_HOME%
echo Library Path: %JAVA_TOOL_OPTIONS%
echo.

REM Run with Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.library.path=C:\xampp\htdocs\AFIS\partner-sdk-examples\abisclient-dll"

pause

@echo off
title CBCAT Fix - Test Environment

echo ============================================================
echo Starting CBCAT Fix Test Environment via Gradle...
echo ============================================================
echo.

call gradlew.bat runClient

if errorlevel 1 (
    echo.
    echo ERROR: Gradle failed to launch the client.
    pause
)


@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

title Student Result Management System

echo =====================================
echo STUDENT RESULT MANAGEMENT SYSTEM
echo =====================================

if not exist out mkdir out

set "SOURCES="

for /r "src" %%F in (*.java) do (
    set "SOURCES=!SOURCES! "%%F""
)

if not defined SOURCES (
    echo No Java files found!
    pause
    exit /b 1
)

echo.
echo Compiling Java files...

javac --add-modules jdk.httpserver -encoding UTF-8 -d out !SOURCES!

if errorlevel 1 (
    echo.
    echo Compilation failed! Check errors above.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo Starting server...

java --add-modules jdk.httpserver -cp out app.Main

pause

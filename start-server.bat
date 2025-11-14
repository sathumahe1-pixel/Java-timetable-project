@echo off
echo ========================================
echo Timetable Generator Login Server
echo ========================================
echo.

echo Compiling LoginServer.java...
javac LoginServer.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Compilation failed!
    echo Make sure Java JDK is installed and javac is in your PATH.
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Starting server...
echo.

start http://127.0.0.1:8080/

java LoginServer

pause

@echo off
echo ========================================
echo Running Application with Maven
echo This bypasses VS Code's classpath cache
echo ========================================
echo.

cd /d "%~dp0"

echo Cleaning previous build...
call mvn clean

echo.
echo Compiling and running application...
call mvn spring-boot:run

pause

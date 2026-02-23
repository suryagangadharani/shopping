@echo off
echo Starting Men's Fashion Store Application...
echo.
cd /d "%~dp0"
call tools\apache-maven-3.9.5\bin\mvn.cmd spring-boot:run

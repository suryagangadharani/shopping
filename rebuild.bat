@echo off
echo ========================================
echo Cleaning and Rebuilding Project
echo ========================================

echo.
echo Step 1: Deleting target folder...
if exist target (
    rmdir /S /Q target
    echo Target folder deleted.
) else (
    echo Target folder not found.
)

echo.
echo Step 2: Deleting IDE cache files...
if exist .vscode\.factorypath del /F /Q .vscode\.factorypath
if exist .classpath del /F /Q .classpath

echo.
echo ========================================
echo DONE! Now close and reopen VS Code,
echo then run your application.
echo ========================================
pause

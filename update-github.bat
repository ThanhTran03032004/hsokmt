@echo off
setlocal

cd /d "%~dp0"
set "BRANCH=main"

echo ========================================
echo Auto update hsokmt to GitHub
echo Folder: %CD%
echo Branch: %BRANCH%
echo ========================================
echo.

git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
    echo ERROR: This folder is not a Git repository.
    pause
    exit /b 1
)

git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo ERROR: Git remote "origin" was not found.
    pause
    exit /b 1
)

echo [1/4] Adding all file changes...
git add -A
if errorlevel 1 goto fail

echo [2/4] Checking for changes to commit...
git diff --cached --quiet
set "DIFF_EXIT=%ERRORLEVEL%"

if "%DIFF_EXIT%"=="0" (
    echo No new file changes to commit.
) else (
    if "%DIFF_EXIT%"=="1" (
        for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HH-mm-ss"') do set "TS=%%I"
        echo [3/4] Creating commit...
        git commit -m "Auto update hsokmt %TS%"
        if errorlevel 1 goto fail
    ) else (
        goto fail
    )
)

echo [4/4] Pushing to GitHub...
git push origin %BRANCH%
if errorlevel 1 goto fail

echo.
echo Done. GitHub is up to date.
pause
exit /b 0

:fail
echo.
echo ERROR: Update failed. Check the message above.
pause
exit /b 1

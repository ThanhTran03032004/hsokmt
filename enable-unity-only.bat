@echo off
setlocal
cd /d "%~dp0"

set /p "HSO_BUILD_ID=Nhap Build ID Unity (vi du build-2026-11): "
set /p "HSO_PACKAGE=Nhap Package Name Unity (vi du com.hso.kmt): "

if "%HSO_BUILD_ID%"=="" goto :invalid
if "%HSO_PACKAGE%"=="" goto :invalid

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\configure-unity-only.ps1" -BuildId "%HSO_BUILD_ID%" -PackageName "%HSO_PACKAGE%" -EnvironmentScope Machine
if errorlevel 1 goto :failed

echo.
echo Cau hinh thanh cong. Hay luu key vua hien thi vao UnityBuildManifest.
pause
exit /b 0

:invalid
echo Build ID va Package Name khong duoc de trong.
pause
exit /b 1

:failed
echo Khong cau hinh duoc. Hay chay file nay bang Run as administrator.
pause
exit /b 1

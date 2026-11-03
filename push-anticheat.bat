@echo off
setlocal
cd /d "%~dp0"

git add hso.conf docs unity-client enable-unity-only.bat push-anticheat.bat scripts\configure-unity-only.ps1 src\main\java\Game\security src\main\java\Game\io\Session.java src\main\java\Game\client\MessageHandler.java src\main\java\Game\map\MapService.java src\main\java\Game\core\Manager.java src\main\java\Game\core\AdminToolServer.java
if errorlevel 1 goto :failed

git commit -m "Add Unity-only anti-cheat framework"
if errorlevel 1 goto :failed

git push origin main
if errorlevel 1 goto :failed

echo Anti-cheat da duoc push len GitHub.
pause
exit /b 0

:failed
echo Push that bai. Xem loi phia tren.
pause
exit /b 1

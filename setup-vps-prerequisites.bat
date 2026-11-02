@echo off
setlocal
title HSOKMT - Install Git, Java 17 and MySQL

net session >nul 2>&1
if not "%errorlevel%"=="0" (
    echo [ERROR] Right-click this file and choose "Run as administrator".
    pause
    exit /b 1
)

echo.
echo Installing prerequisites for HSOKMT on Windows Server 2012 R2...
echo.

where choco >nul 2>&1
if not "%errorlevel%"=="0" (
    echo Installing Chocolatey...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
    if not "%errorlevel%"=="0" (
        echo [ERROR] Chocolatey installation failed.
        pause
        exit /b 1
    )
)

call refreshenv >nul 2>&1
echo Enabling TLS 1.2 for package downloads...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12; New-Item -Path 'HKLM:\SOFTWARE\Microsoft\.NETFramework\v4.0.30319' -Force | Out-Null; Set-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\.NETFramework\v4.0.30319' -Name SchUseStrongCrypto -Value 1 -Type DWord; New-Item -Path 'HKLM:\SOFTWARE\Wow6432Node\Microsoft\.NETFramework\v4.0.30319' -Force | Out-Null; Set-ItemProperty -Path 'HKLM:\SOFTWARE\Wow6432Node\Microsoft\.NETFramework\v4.0.30319' -Name SchUseStrongCrypto -Value 1 -Type DWord"

echo Installing Git for Windows...
choco install git -y --no-progress
if not "%errorlevel%"=="0" goto :failed

echo Installing Java 17...
choco install temurin17 -y --no-progress
if not "%errorlevel%"=="0" goto :failed

echo Installing MySQL Server...
choco install mysql -y --no-progress
if not "%errorlevel%"=="0" goto :failed

call refreshenv >nul 2>&1
echo.
echo Installation completed.
echo Close this window, open a new Administrator PowerShell, then verify:
echo   git --version
echo   java -version
echo   mysql --version
echo.
echo Next step:
echo   cd C:\HSOKMT
echo   .\scripts\setup-vps-windows.ps1
pause
exit /b 0

:failed
echo.
echo [ERROR] A package installation failed. Run this file again after checking the error above.
pause
exit /b 1

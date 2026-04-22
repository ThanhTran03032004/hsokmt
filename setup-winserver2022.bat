@echo off
setlocal EnableExtensions EnableDelayedExpansion

if /i "%~1"=="--help" goto :usage
if /i "%~1"=="/?" goto :usage

set "APP_NAME=hsokmt"
set "JAVA_MAJOR=17"
set "JDK_URL=https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
set "JDK_ZIP=%TEMP%\temurin-jdk17.zip"
set "JDK_EXTRACT_DIR=%TEMP%\temurin-jdk17-extract"
set "JDK_INSTALL_DIR=%ProgramFiles%\Eclipse Adoptium\hsokmt-jdk17"
set "JAR_FILE=target\hsokmt-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
set "START_BAT=start-server.bat"
set "JAVA_XMS=512m"
set "JAVA_XMX=2g"

rem Database is intentionally not installed or imported by this setup.
rem Edit hso.conf yourself before running the generated start-server.bat.
set "MYSQL_HOST="
set "MYSQL_PORT="
set "MYSQL_DATABASE="
set "MYSQL_USER="

rem Defaults are overwritten from hso.conf when present.
set "GAME_PORT=19129"
set "ADMIN_PORT=19130"

rem Open only the game port by default. Admin tool is bound to 127.0.0.1 in hso.conf.
set "OPEN_GAME_FIREWALL=1"
set "OPEN_ADMIN_FIREWALL=0"

rem Optional. Set to 1 before running if you want a startup task as LocalSystem.
set "CREATE_STARTUP_TASK=0"
set "START_AFTER_SETUP=0"

set "PS=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"
set "FLTMC=%SystemRoot%\System32\fltmc.exe"
set "NET=%SystemRoot%\System32\net.exe"
set "NETSH=%SystemRoot%\System32\netsh.exe"
set "SCHTASKS=%SystemRoot%\System32\schtasks.exe"
if not exist "%PS%" set "PS=powershell.exe"
if not exist "%FLTMC%" set "FLTMC=fltmc.exe"
if not exist "%NET%" set "NET=net.exe"
if not exist "%NETSH%" set "NETSH=netsh.exe"
if not exist "%SCHTASKS%" set "SCHTASKS=schtasks.exe"

cd /d "%~dp0"

call :require_admin
if errorlevel 100 exit /b 0
if errorlevel 1 goto :failed
call :read_hso_conf

echo.
echo === %APP_NAME% Windows Server 2022 setup ===
echo Project: %CD%
echo Java: JDK %JAVA_MAJOR%+
echo Game port: %GAME_PORT%
echo Admin port: %ADMIN_PORT%
if defined MYSQL_HOST echo Database: %MYSQL_HOST%:%MYSQL_PORT% / %MYSQL_DATABASE% / user=%MYSQL_USER%
echo.

call :ensure_jdk
if errorlevel 1 goto :failed

call :build_jar
if errorlevel 1 goto :failed

call :write_start_bat
if errorlevel 1 goto :failed

call :configure_firewall
if errorlevel 1 goto :failed

if "%CREATE_STARTUP_TASK%"=="1" (
    call :create_startup_task
    if errorlevel 1 goto :failed
)

call :check_database_port

echo.
echo Setup completed.
echo Run the server with:
echo   %START_BAT%
echo.
echo Notes:
echo - This setup does not install or configure MySQL.
echo - Make sure hso.conf points to your database before starting the server.
echo - If your VPS provider has an external firewall, also allow TCP %GAME_PORT% there.
echo.

if "%START_AFTER_SETUP%"=="1" (
    call "%START_BAT%"
)

exit /b 0

:usage
echo Usage:
echo   setup-winserver2022.bat
echo.
echo What it does:
echo   - Installs/uses JDK 17+.
echo   - Sets JAVA_HOME for the machine.
echo   - Builds the runnable jar with mvnw.cmd.
echo   - Creates start-server.bat.
echo   - Opens Windows Firewall for the game port from hso.conf.
echo.
echo What it does not do:
echo   - It does not install MySQL.
echo   - It does not create/import the database.
echo.
echo Optional variables at the top of this file:
echo   CREATE_STARTUP_TASK=1   create a Windows startup task
echo   START_AFTER_SETUP=1     start the server after setup
echo   OPEN_ADMIN_FIREWALL=1   open admin port too
exit /b 0

:require_admin
"%FLTMC%" >nul 2>&1
if "%ERRORLEVEL%"=="0" exit /b 0
"%NET%" session >nul 2>&1
if "%ERRORLEVEL%"=="0" exit /b 0

echo Requesting Administrator permission...
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "$cmd=$env:ComSpec; if ([string]::IsNullOrWhiteSpace($cmd)) { $cmd=Join-Path $env:SystemRoot 'System32\cmd.exe' }; Start-Process -FilePath $cmd -ArgumentList '/c ""%~f0""' -WorkingDirectory '%~dp0' -Verb RunAs"
exit /b 100

:read_hso_conf
if not exist "hso.conf" (
    echo Warning: hso.conf not found. Using default ports.
    exit /b 0
)

for /f "usebackq tokens=1,* delims=:" %%A in ("hso.conf") do (
    set "key=%%A"
    set "value=%%B"
    for /f "tokens=* delims= " %%K in ("!key!") do set "key=%%K"
    for /f "tokens=* delims= " %%V in ("!value!") do set "value=%%V"

    if not "!key!"=="" if not "!key:~0,1!"=="#" (
        if /i "!key!"=="port" set "GAME_PORT=!value!"
        if /i "!key!"=="admin_tool_port" set "ADMIN_PORT=!value!"
        if /i "!key!"=="mysql-host" set "MYSQL_HOST=!value!"
        if /i "!key!"=="mysql-port" set "MYSQL_PORT=!value!"
        if /i "!key!"=="mysql-database" set "MYSQL_DATABASE=!value!"
        if /i "!key!"=="mysql-user" set "MYSQL_USER=!value!"
    )
)
exit /b 0

:ensure_jdk
set "JDK_HOME="

if defined JAVA_HOME (
    call :accept_jdk "%JAVA_HOME%"
    if defined JDK_HOME goto :configure_java
)

call :find_jdk
if defined JDK_HOME goto :configure_java

echo JDK %JAVA_MAJOR%+ not found. Downloading Temurin JDK %JAVA_MAJOR%...
set "JDK_URL=%JDK_URL%"
set "JDK_ZIP=%JDK_ZIP%"
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri $env:JDK_URL -OutFile $env:JDK_ZIP"
if errorlevel 1 (
    echo Failed to download JDK from:
    echo   %JDK_URL%
    exit /b 1
)

echo Extracting JDK to %JDK_INSTALL_DIR%...
set "JDK_EXTRACT_DIR=%JDK_EXTRACT_DIR%"
set "JDK_INSTALL_DIR=%JDK_INSTALL_DIR%"
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "$zip=$env:JDK_ZIP; $tmp=$env:JDK_EXTRACT_DIR; $dest=$env:JDK_INSTALL_DIR; Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue; New-Item -ItemType Directory -Force -Path $tmp | Out-Null; Expand-Archive -Path $zip -DestinationPath $tmp -Force; $jdk=Get-ChildItem -Path $tmp -Directory | Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } | Select-Object -First 1; if ($null -eq $jdk) { exit 1 }; Remove-Item $dest -Recurse -Force -ErrorAction SilentlyContinue; New-Item -ItemType Directory -Force -Path (Split-Path $dest) | Out-Null; Move-Item -Path $jdk.FullName -Destination $dest; exit 0"
if errorlevel 1 (
    echo Failed to extract JDK.
    exit /b 1
)

call :accept_jdk "%JDK_INSTALL_DIR%"
if not defined JDK_HOME (
    echo JDK was extracted, but setup could not use it.
    echo Set JAVA_HOME manually and run this file again.
    exit /b 1
)

:configure_java
set "JAVA_HOME=%JDK_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using JAVA_HOME=%JAVA_HOME%
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "$javaHome=$env:JAVA_HOME; [Environment]::SetEnvironmentVariable('JAVA_HOME',$javaHome,'Machine'); $old=[Environment]::GetEnvironmentVariable('Path','Machine'); if ($null -eq $old) { $old='' }; if ($old -notlike ('*' + $javaHome + '\bin*')) { [Environment]::SetEnvironmentVariable('Path', $javaHome + '\bin;' + $old, 'Machine') }"
if errorlevel 1 (
    echo Warning: could not update machine JAVA_HOME/PATH. Current setup session will still use this JDK.
)

"%JAVA_HOME%\bin\java.exe" -version
exit /b 0

:find_jdk
for %%R in ("%ProgramFiles%\Eclipse Adoptium" "%ProgramFiles%\Java" "%ProgramFiles%\Microsoft" "%USERPROFILE%\.jdks") do (
    for /l %%M in (%JAVA_MAJOR%,1,30) do (
        for /d %%D in ("%%~R\jdk-%%M*") do (
            call :accept_jdk "%%~fD"
            if defined JDK_HOME exit /b 0
        )
        for /d %%D in ("%%~R\jdk%%M*") do (
            call :accept_jdk "%%~fD"
            if defined JDK_HOME exit /b 0
        )
        for /d %%D in ("%%~R\jbr-%%M*") do (
            call :accept_jdk "%%~fD"
            if defined JDK_HOME exit /b 0
        )
    )
)
exit /b 1

:accept_jdk
set "CANDIDATE_JDK=%~1"
if not defined CANDIDATE_JDK exit /b 1
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "$java=Join-Path $env:CANDIDATE_JDK 'bin\java.exe'; if (!(Test-Path $java)) { exit 1 }; $line=(& $java -version 2>&1 | Select-Object -First 1); $q=[char]34; if ($line -match ('version ' + $q + '(?<major>[0-9]+)')) { if ([int]$Matches.major -ge [int]$env:JAVA_MAJOR) { exit 0 } }; exit 1"
if errorlevel 1 exit /b 1
set "JDK_HOME=%~1"
exit /b 0

:build_jar
if not exist "mvnw.cmd" (
    echo mvnw.cmd not found. Run this setup from the project root.
    exit /b 1
)

echo.
echo Building server jar...
call "mvnw.cmd" -DskipTests package
if errorlevel 1 (
    echo Maven build failed.
    exit /b 1
)

if not exist "%JAR_FILE%" (
    echo Build finished, but jar was not found:
    echo   %JAR_FILE%
    exit /b 1
)

echo Built %JAR_FILE%
exit /b 0

:write_start_bat
echo Creating %START_BAT%...
(
    echo @echo off
    echo setlocal EnableExtensions
    echo cd /d "%%~dp0"
    echo set "JAVA_HOME=%JAVA_HOME%"
    echo set "PATH=%%JAVA_HOME%%\bin;%%PATH%%"
    echo set "JAR_FILE=%JAR_FILE%"
    echo if not exist "%%JAR_FILE%%" ^(
    echo     echo Missing jar: %%JAR_FILE%%
    echo     echo Run setup-winserver2022.bat again to rebuild.
    echo     pause
    echo     exit /b 1
    echo ^)
    echo :restart
    echo "%%JAVA_HOME%%\bin\java.exe" -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar "%%JAR_FILE%%"
    echo echo.
    echo echo Server stopped. Restarting in 5 seconds...
    echo "%SystemRoot%\System32\timeout.exe" /t 5 /nobreak ^>nul
    echo goto restart
) > "%START_BAT%"

if not exist "%START_BAT%" (
    echo Failed to create %START_BAT%.
    exit /b 1
)
exit /b 0

:configure_firewall
if "%OPEN_GAME_FIREWALL%"=="1" (
    echo Opening Windows Firewall TCP %GAME_PORT% for game server...
    "%NETSH%" advfirewall firewall delete rule name="%APP_NAME% game tcp %GAME_PORT%" >nul 2>&1
    "%NETSH%" advfirewall firewall add rule name="%APP_NAME% game tcp %GAME_PORT%" dir=in action=allow protocol=TCP localport=%GAME_PORT% profile=any
    if errorlevel 1 exit /b 1
)

if "%OPEN_ADMIN_FIREWALL%"=="1" (
    echo Opening Windows Firewall TCP %ADMIN_PORT% for admin tool...
    "%NETSH%" advfirewall firewall delete rule name="%APP_NAME% admin tcp %ADMIN_PORT%" >nul 2>&1
    "%NETSH%" advfirewall firewall add rule name="%APP_NAME% admin tcp %ADMIN_PORT%" dir=in action=allow protocol=TCP localport=%ADMIN_PORT% profile=any
    if errorlevel 1 exit /b 1
)
exit /b 0

:create_startup_task
echo Creating Windows startup task %APP_NAME%-server...
"%SCHTASKS%" /Create /TN "%APP_NAME%-server" /SC ONSTART /RU SYSTEM /RL HIGHEST /TR "\"%~dp0%START_BAT%\"" /F
if errorlevel 1 (
    echo Failed to create startup task.
    exit /b 1
)
exit /b 0

:check_database_port
if not defined MYSQL_HOST exit /b 0
if not defined MYSQL_PORT exit /b 0

set "DB_CHECK_HOST=%MYSQL_HOST%"
set "DB_CHECK_PORT=%MYSQL_PORT%"
"%PS%" -NoProfile -ExecutionPolicy Bypass -Command "$client=New-Object Net.Sockets.TcpClient; try { $async=$client.BeginConnect($env:DB_CHECK_HOST,[int]$env:DB_CHECK_PORT,$null,$null); if ($async.AsyncWaitHandle.WaitOne(1500,$false)) { $client.EndConnect($async); exit 0 }; exit 1 } catch { exit 1 } finally { $client.Close() }"
if errorlevel 1 (
    echo Warning: database port is not reachable now: %MYSQL_HOST%:%MYSQL_PORT%
    echo You said database is set up separately, so fix hso.conf/MySQL before starting.
) else (
    echo Database port reachable: %MYSQL_HOST%:%MYSQL_PORT%
)
exit /b 0

:failed
echo.
echo Setup failed. Fix the error above and run setup-winserver2022.bat again.
exit /b 1

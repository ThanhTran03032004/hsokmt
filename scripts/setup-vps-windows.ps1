param(
    [string]$RepoUrl = '',
    [string]$Branch = 'main',
    [string]$AppDir = 'C:\HSOKMT',
    [string]$TaskName = 'HSOKMT Server'
)
$ErrorActionPreference = 'Stop'

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run PowerShell as Administrator.'
}

if (-not (Get-Command git.exe -ErrorAction SilentlyContinue)) { throw 'Install Git for Windows first.' }
if (-not (Get-Command java.exe -ErrorAction SilentlyContinue)) { throw 'Install Java 17 first and add java.exe to PATH.' }
if (-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue) -and -not (Test-Path (Join-Path $AppDir 'mvnw.cmd'))) { throw 'Maven or mvnw.cmd was not found.' }

if (-not (Test-Path (Join-Path $AppDir '.git'))) {
    if ([string]::IsNullOrWhiteSpace($RepoUrl)) { throw 'Provide -RepoUrl for the first installation.' }
    New-Item -ItemType Directory -Force -Path (Split-Path $AppDir) | Out-Null
    git clone --branch $Branch $RepoUrl $AppDir
} else {
    git -C $AppDir fetch origin $Branch
    git -C $AppDir checkout $Branch
    git -C $AppDir pull --ff-only origin $Branch
}

Set-Location $AppDir
& (Join-Path $AppDir 'mvnw.cmd') '-q' '-DskipTests' 'clean' 'package'
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }

$runScript = Join-Path $AppDir 'scripts\run-server.ps1'
$action = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$runScript`""
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId 'SYSTEM' -LogonType ServiceAccount -RunLevel Highest
Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Principal $principal -Force | Out-Null
Start-ScheduledTask -TaskName $TaskName
Write-Host "Installed and started: $TaskName"

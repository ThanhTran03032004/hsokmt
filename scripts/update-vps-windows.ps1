param(
    [string]$Branch = 'main',
    [string]$AppDir = 'C:\HSOKMT',
    [string]$TaskName = 'HSOKMT Server'
)
$ErrorActionPreference = 'Stop'

if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw 'Run PowerShell as Administrator.'
}

Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
git -C $AppDir fetch origin $Branch
git -C $AppDir checkout $Branch
git -C $AppDir pull --ff-only origin $Branch
Set-Location $AppDir
& (Join-Path $AppDir 'mvnw.cmd') '-q' '-DskipTests' 'clean' 'package'
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed. The server was left stopped.' }
New-NetFirewallRule -DisplayName 'HSOKMT Game Server' -Direction Inbound -Protocol TCP -LocalPort 19129 -Action Allow -ErrorAction SilentlyContinue | Out-Null
New-NetFirewallRule -DisplayName 'HSOKMT Admin Panel' -Direction Inbound -Protocol TCP -LocalPort 19130 -Action Allow -ErrorAction SilentlyContinue | Out-Null
Start-ScheduledTask -TaskName $TaskName
Write-Host 'Updated and restarted HSOKMT.'

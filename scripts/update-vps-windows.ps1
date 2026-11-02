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
$serverProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine.IndexOf($AppDir, [StringComparison]::OrdinalIgnoreCase) -ge 0 }
foreach ($serverProcess in $serverProcesses) {
    Stop-Process -Id $serverProcess.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2
git -C $AppDir fetch origin $Branch
git -C $AppDir checkout $Branch
git -C $AppDir pull --ff-only origin $Branch
Set-Location $AppDir
& (Join-Path $AppDir 'mvnw.cmd') '-q' '-DskipTests' 'clean' 'package'
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed. The server was left stopped.' }
$principal = New-ScheduledTaskPrincipal -UserId 'SYSTEM' -LogonType ServiceAccount -RunLevel Highest
$updateScript = Join-Path $AppDir 'scripts\update-vps-windows.ps1'
$updateAction = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$updateScript`""
Register-ScheduledTask -TaskName 'HSOKMT Updater' -Action $updateAction -Principal $principal -Force | Out-Null
$restartScript = Join-Path $AppDir 'scripts\restart-server.ps1'
$restartAction = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$restartScript`""
Register-ScheduledTask -TaskName 'HSOKMT Restart' -Action $restartAction -Principal $principal -Force | Out-Null
New-NetFirewallRule -DisplayName 'HSOKMT Game Server' -Direction Inbound -Protocol TCP -LocalPort 19129 -Action Allow -ErrorAction SilentlyContinue | Out-Null
New-NetFirewallRule -DisplayName 'HSOKMT Admin Panel' -Direction Inbound -Protocol TCP -LocalPort 19130 -Action Allow -ErrorAction SilentlyContinue | Out-Null
Start-ScheduledTask -TaskName $TaskName
Write-Host 'Updated and restarted HSOKMT.'

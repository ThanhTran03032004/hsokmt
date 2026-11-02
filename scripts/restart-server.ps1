param(
    [string]$TaskName = 'HSOKMT Server'
)
$ErrorActionPreference = 'Stop'

Stop-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3
Start-ScheduledTask -TaskName $TaskName

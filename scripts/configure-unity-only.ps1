param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9._-]{3,64}$')]
    [string]$BuildId,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9._-]{3,128}$')]
    [string]$PackageName,

    [ValidateSet('Machine', 'User')]
    [string]$EnvironmentScope = 'Machine'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $projectRoot 'hso.conf'
if (-not (Test-Path -LiteralPath $configPath)) {
    throw "hso.conf was not found at $configPath"
}

$keyBytes = New-Object byte[] 32
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $random.GetBytes($keyBytes)
}
finally {
    $random.Dispose()
}
$base64Key = [Convert]::ToBase64String($keyBytes)

[Environment]::SetEnvironmentVariable('HSO_UNITY_BUILD_ID', $BuildId, $EnvironmentScope)
[Environment]::SetEnvironmentVariable('HSO_UNITY_BUILD_SECRET', "b64:$base64Key", $EnvironmentScope)

$lines = [Collections.Generic.List[string]](Get-Content -LiteralPath $configPath)
function Set-ConfigValue([string]$Key, [string]$Value) {
    for ($index = 0; $index -lt $lines.Count; $index++) {
        if ($lines[$index] -match ('^\s*' + [Regex]::Escape($Key) + '\s*:')) {
            $lines[$index] = "${Key}: $Value"
            return
        }
    }
    $lines.Add("${Key}: $Value")
}

Set-ConfigValue 'anticheat-enabled' 'true'
Set-ConfigValue 'unity-only' 'true'
Set-ConfigValue 'unity-packages' $PackageName
Set-Content -LiteralPath $configPath -Value $lines -Encoding UTF8

Write-Host 'Unity-only has been configured.' -ForegroundColor Green
Write-Host "Build ID: $BuildId"
Write-Host "Package:  $PackageName"
Write-Host "Unity manifest key (base64): $base64Key" -ForegroundColor Yellow
Write-Host 'Copy this key into UnityBuildManifest, rebuild the Unity client, then restart the server.'

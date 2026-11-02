$ErrorActionPreference = 'Stop'
$AppDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Jar = Join-Path $AppDir 'target\hsokmt-0.0.1-SNAPSHOT-jar-with-dependencies.jar'
Set-Location $AppDir

while ($true) {
    if (-not (Test-Path $Jar)) { throw "Jar not found: $Jar" }
    & java.exe '-Xms256m' '-Xmx1024m' '-jar' $Jar
    $exitCode = $LASTEXITCODE
    if ($exitCode -eq 0) { break }
    Start-Sleep -Seconds 5
}

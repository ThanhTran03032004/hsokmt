param(
    [string]$MySqlBin = 'C:\AppServ\MySQL\bin\mysqld.exe',
    [string]$MyIni = 'C:\AppServ\MySQL\my.ini',
    [string]$DbHost = '127.0.0.1',
    [int]$Port = 3307
)

function Test-Port {
    param(
        [string]$TargetHost,
        [int]$TargetPort
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($TargetHost, $TargetPort, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(1000, $false)) {
            return $false
        }
        $client.EndConnect($async) | Out-Null
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

if (Test-Port -TargetHost $DbHost -TargetPort $Port) {
    exit 0
}

if (-not (Test-Path $MySqlBin)) {
    Write-Error "MySQL binary not found: $MySqlBin"
    exit 1
}

if (-not (Test-Path $MyIni)) {
    Write-Error "MySQL ini not found: $MyIni"
    exit 1
}

$binDir = Split-Path -Parent $MySqlBin
$binDirEscaped = $binDir.Replace("'", "''")
$binEscaped = $MySqlBin.Replace("'", "''")
$iniEscaped = $MyIni.Replace("'", "''")
$command = "Set-Location '$binDirEscaped'; & '$binEscaped' '--defaults-file=$iniEscaped' '--console'"

Start-Process -FilePath "powershell.exe" `
    -ArgumentList @("-NoLogo", "-NonInteractive", "-WindowStyle", "Hidden", "-Command", $command) `
    -WindowStyle Hidden | Out-Null

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ProjectRoot = Split-Path $PSScriptRoot -Parent
$RuntimeDir = Join-Path $ProjectRoot 'runtime'

function Read-EnvFile([string]$Path) {
    $values = @{}
    if (!(Test-Path -LiteralPath $Path)) { throw 'Missing .env. Copy .env.example to .env and fill in the required values.' }
    $number = 0
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        $number++
        $text = $line.Trim()
        if (!$text -or $text.StartsWith('#')) { continue }
        if ($text -notmatch '^([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') { throw "Invalid .env line $number. Expected KEY=value." }
        $key = $Matches[1]
        $value = $Matches[2].Trim()
        if ($values.ContainsKey($key)) { throw "Duplicate .env key: $key" }
        if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $values[$key] = $value
    }
    return $values
}

function Import-Settings {
    $values = Read-EnvFile (Join-Path $ProjectRoot '.env')
    foreach ($key in $values.Keys) { [Environment]::SetEnvironmentVariable($key, $values[$key], 'Process') }
}

function Get-Setting([string]$Name, [string]$Default = '') {
    $value = [Environment]::GetEnvironmentVariable($Name, 'Process')
    if ([string]::IsNullOrWhiteSpace($value)) { return $Default }
    return $value
}

function Get-Port([string]$Name, [int]$Default) {
    $portNumber = 0
    if (![int]::TryParse((Get-Setting $Name "$Default"), [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 65535) { throw "Invalid port: $Name" }
    return $portNumber
}

function Test-Tcp([string]$Address, [int]$Port) {
    $client = [Net.Sockets.TcpClient]::new()
    try {
        $connection = $client.BeginConnect($Address, $Port, $null, $null)
        if (!$connection.AsyncWaitHandle.WaitOne(1500)) { return $false }
        $client.EndConnect($connection)
        return $true
    } catch { return $false } finally { $client.Dispose() }
}

function Test-Http([string]$Url) {
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
        return $response.StatusCode -eq 200
    } catch { return $false }
}

function Wait-Http([string]$Url, [Diagnostics.Process]$Process, [string]$Name) {
    $deadline = (Get-Date).AddSeconds(120)
    do {
        $Process.Refresh()
        if ($Process.HasExited) { throw "$Name exited. Check runtime/$Name.err.log and runtime/$Name.out.log." }
        if (Test-Http $Url) { return }
        Start-Sleep -Milliseconds 500
    } while ((Get-Date) -lt $deadline)
    throw "$Name did not become ready in 120 seconds. Check runtime logs."
}

function Get-OwnedProcess($Entry) {
    $process = Get-Process -Id $Entry.Id -ErrorAction SilentlyContinue
    if ($process -and $process.StartTime.ToUniversalTime().Ticks.ToString() -eq $Entry.StartTicks -and $process.Path -eq $Entry.Path) { return $process }
    return $null
}

function Save-Process([string]$Name, [Diagnostics.Process]$Process) {
    $entry = @{ Id = $Process.Id; StartTicks = $Process.StartTime.ToUniversalTime().Ticks.ToString(); Path = $Process.Path }
    $entry | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $RuntimeDir "$Name.json") -Encoding UTF8
}

function Stop-Owned([string]$Name) {
    $path = Join-Path $RuntimeDir "$Name.json"
    if (!(Test-Path -LiteralPath $path)) { return }
    $entry = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
    $process = Get-OwnedProcess $entry
    if ($process) { Stop-Process -InputObject $process; $process.WaitForExit(10000) | Out-Null; Write-Host "Stopped $Name." }
    Remove-Item -LiteralPath $path
}

function Start-Owned([string]$Name, [string]$Exe, [string[]]$Arguments, [string]$Directory) {
    $process = Start-Process -FilePath $Exe -ArgumentList $Arguments -WorkingDirectory $Directory -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $RuntimeDir "$Name.out.log") -RedirectStandardError (Join-Path $RuntimeDir "$Name.err.log")
    try { Save-Process $Name $process } catch { $process | Stop-Process -ErrorAction SilentlyContinue; throw }
    return $process
}

function Lock-Launcher {
    New-Item -ItemType Directory -Path $RuntimeDir -Force | Out-Null
    try { return [IO.File]::Open((Join-Path $RuntimeDir 'launcher.lock'), 'OpenOrCreate', 'ReadWrite', 'None') }
    catch { throw 'Another start/stop command is running. Wait for it to finish.' }
}

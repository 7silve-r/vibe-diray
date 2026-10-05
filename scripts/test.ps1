. "$PSScriptRoot/common.ps1"
$sandbox = Join-Path ([IO.Path]::GetTempPath()) ('vibe-launcher-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $sandbox | Out-Null
$RuntimeDir = $sandbox
$child = $null
function Assert-True($Value, [string]$Message) { if (!$Value) { throw $Message } }
function Assert-Fails([scriptblock]$Action) {
    $failed = $false
    try { & $Action | Out-Null } catch { $failed = $true }
    Assert-True $failed 'Expected a failure.'
}
try {
    $fixture = Join-Path $sandbox 'test.env'
    [IO.File]::WriteAllText($fixture, '# comment' + "`n" + 'PASSWORD="a b#c=$value&x"' + "`nEMPTY=`nPORT=8081")
    $values = Read-EnvFile $fixture
    Assert-True ($values.PASSWORD -ceq 'a b#c=$value&x') 'Special characters must stay literal.'
    Assert-True ($values.EMPTY -eq '') 'Empty value must be supported.'
    Assert-True ($values.PORT -eq '8081') 'Port parsing failed.'
    [IO.File]::WriteAllText($fixture, "A=1`nA=2")
    Assert-Fails { Read-EnvFile $fixture }
    [IO.File]::WriteAllText($fixture, 'not an assignment')
    Assert-Fails { Read-EnvFile $fixture }
    Assert-Fails { Read-EnvFile (Join-Path $sandbox 'missing.env') }
    $env:VIBE_TEST_PORT = '70000'
    Assert-Fails { Get-Port 'VIBE_TEST_PORT' 5173 }
    $env:VIBE_TEST_PORT = '1234'
    Assert-True ((Get-Port 'VIBE_TEST_PORT' 5173) -eq 1234) 'Port override failed.'
    $lock = Lock-Launcher
    try { Assert-Fails { Lock-Launcher } } finally { $lock.Dispose() }
    $program = Join-Path $sandbox 'server.js'
    [IO.File]::WriteAllText($program, "require('http').createServer((q,s)=>s.end('ok')).listen(0,'127.0.0.1',function(){require('fs').writeFileSync('port.txt',String(this.address().port))});")
    $child = Start-Owned 'test' (Get-Command node.exe).Source @(('"' + $program + '"')) $sandbox
    $deadline = (Get-Date).AddSeconds(10)
    while (!(Test-Path (Join-Path $sandbox 'port.txt')) -and (Get-Date) -lt $deadline) { Start-Sleep -Milliseconds 100 }
    $portNumber = [int](Get-Content (Join-Path $sandbox 'port.txt'))
    Wait-Http "http://127.0.0.1:$portNumber" $child 'test'
    $entry = Get-Content (Join-Path $sandbox 'test.json') -Raw | ConvertFrom-Json
    Assert-True ($null -ne (Get-OwnedProcess $entry)) 'Owned process was not recognized.'
    $entry.StartTicks = '0'
    $entry | ConvertTo-Json | Set-Content (Join-Path $sandbox 'test.json')
    Stop-Owned 'test'
    $child.Refresh()
    Assert-True (!$child.HasExited) 'Stale PID record must not stop an unrelated process.'
    Save-Process 'test' $child
    Stop-Owned 'test'
    $child.Refresh()
    Assert-True $child.HasExited 'Owned process must stop.'
    Stop-Owned 'test'
    Write-Host 'Launcher tests passed: env parsing, ports, lock, HTTP readiness, process identity and repeated stop.'
} finally {
    if ($child -and !$child.HasExited) { Stop-Process -InputObject $child }
    Remove-Item Env:VIBE_TEST_PORT -ErrorAction SilentlyContinue
    $resolved = [IO.Path]::GetFullPath($sandbox)
    $tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
    if ($resolved.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase) -and (Split-Path $resolved -Leaf).StartsWith('vibe-launcher-')) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}

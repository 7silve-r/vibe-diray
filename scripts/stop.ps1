. "$PSScriptRoot/common.ps1"
$lock = $null
try {
    $lock = Lock-Launcher
    Stop-Owned 'frontend'
    Stop-Owned 'backend'
    Write-Host 'Application stopped. Existing MySQL, MinIO and IDE processes were not changed.'
} catch { Write-Host $_.Exception.Message -ForegroundColor Red; exit 1 }
finally { if ($lock) { $lock.Dispose() } }

<#
.SYNOPSIS
  Para as instâncias do serviço TriagemSender listadas em .\run\pids.txt
.DESCRIPTION
  Lê PIDs de .\run\pids.txt e encerra os processos.
.PARAMETER DryRun
  Se presente, apenas imprime os comandos ao invés de executá-los.
.PARAMETER Force
  Se presente, usa Stop-Process -Force (equivalente a kill -9)
#>
param(
    [switch]$DryRun,
    [switch]$Force
)

$PidsFile = Join-Path "run" "pids.txt"
if (-not (Test-Path $PidsFile)) { Write-Host "No pids file found at $PidsFile. Nothing to stop."; exit 0 }
$pids = Get-Content -Path $PidsFile | Where-Object { $_ -match '\d+' }
if (-not $pids) { Write-Host "No PIDs found in $PidsFile"; exit 0 }

foreach ($pid in $pids) {
    if ($DryRun) {
        if ($Force) { Write-Host "DRY-RUN: Stop-Process -Id $pid -Force" } else { Write-Host "DRY-RUN: Stop-Process -Id $pid" }
    } else {
        try {
            if ($Force) { Stop-Process -Id $pid -Force -ErrorAction Stop; Write-Host "Killed -Force $pid" } else { Stop-Process -Id $pid -ErrorAction Stop; Write-Host "Stopped $pid" }
        } catch {
            Write-Host "Process $pid could not be stopped: $_"
        }
    }
}

if (-not $DryRun) { Remove-Item -Path $PidsFile -ErrorAction SilentlyContinue; Write-Host "Removed $PidsFile" }


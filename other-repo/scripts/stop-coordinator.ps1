<#
.SYNOPSIS
  Para apenas o processo que atualmente é coordenador (conforme logs).
.DESCRIPTION
  Procura nos arquivos em .\logs\ mensagens que indicam coordenador,
  extrai o ID lógico e delega para stop-process.ps1 para parar essa instância.
.PARAMETER DryRun
  Se presente, apenas mostra as ações.
.PARAMETER Force
  Se presente, usa -Force no Stop-Process.
#>
param(
    [switch]$DryRun,
    [switch]$Force,
    [int]$Timeout = 5
)

$LogDir = "logs"
$PIDMap = Join-Path "run" "pid_map.txt"
$PidsFile = Join-Path "run" "pids.txt"

if (-not (Test-Path $LogDir)) { Write-Host "Diretório de logs '$LogDir' não encontrado. Nada a fazer."; exit 1 }

# get last matching line across all logs
$matches = Get-ChildItem -Path $LogDir -Filter '*.log' -ErrorAction SilentlyContinue | ForEach-Object {
    try { Select-String -Path $_.FullName -Pattern 'VIROU COORDENADOR|NOVO COORDENADOR|\bCOORDENADOR\b' -ErrorAction SilentlyContinue } catch { }
} | Select-Object -Last 1

if (-not $matches) { Write-Host "Não foi encontrada nenhuma indicação de coordenador nos logs."; exit 1 }
$leaderLine = $matches.Line

# extract numeric tokens
[regex]$re = '\d+'
$nums = @()
foreach ($m in $re.Matches($leaderLine)) { $nums += [int]$m.Value }

# prefer a number that exists in pid_map or pids.txt
$leaderId = $null
if ($nums.Count -gt 0) {
    for ($i = $nums.Count - 1; $i -ge 0; $i--) {
        $n = $nums[$i]
        if (Test-Path $PIDMap) {
            if (Get-Content $PIDMap | Where-Object { $_ -match "^$n\s+" }) { $leaderId = $n; break }
        }
        if (Test-Path $PidsFile) {
            $cand = (Get-Content $PidsFile | Select-Object -Index ($n - 1) -ErrorAction SilentlyContinue)
            if ($cand -and ($cand -match '^\d+$')) { $leaderId = $n; break }
        }
    }
}

if (-not $leaderId) { if ($nums.Count -gt 0) { $leaderId = $nums[-1] } }
if (-not $leaderId) { Write-Host "Falha ao extrair ID do coordenador a partir da linha: $leaderLine"; exit 1 }

Write-Host "Coordenador detectado (a partir dos logs): ID=$leaderId"

# delegate to stop-process.ps1
$scriptToCall = Join-Path $PSScriptRoot 'stop-process.ps1'
if (-not (Test-Path $scriptToCall)) {
    Write-Host "stop-process.ps1 not found in script folder; cannot stop by ID"; exit 1
}

# build args
$args = @('-Id',$leaderId)
if ($DryRun) { $args += '-DryRun' }
if ($Force) { $args += '-Force' }
$args += '-Timeout'; $args += $Timeout

# invoke
Write-Host "Delegando para: $scriptToCall $($args -join ' ')"
& $scriptToCall @args

exit $LASTEXITCODE


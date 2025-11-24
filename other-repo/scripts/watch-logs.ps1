<#
.SYNOPSIS
  Segue logs em tempo real.
.PARAMETER All
  Segue todos os arquivos em logs\*.log
.PARAMETER Id
  Segue somente logs\instance-<id>.log
.PARAMETER Pattern
  Segue arquivos que batam com o glob dentro de logs (ex: '*cardio*.log')
#>
param(
    [switch]$All,
    [int]$Id,
    [string]$Pattern
)

$LogDir = 'logs'
if (-not (Test-Path $LogDir)) { Write-Host "Diretório de logs '$LogDir' não encontrado. Inicie instâncias primeiro."; exit 1 }

if ($All) {
    Write-Host "Seguindo todos os logs em $LogDir/*.log (CTRL-C para sair)"
    Get-Content -Path (Join-Path $LogDir '*.log') -Wait -Tail 10
    exit 0
}

if ($Pattern) {
    $files = Get-ChildItem -Path $LogDir -Filter $Pattern -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    if (-not $files) { Write-Host "Nenhum arquivo correspondente ao padrão encontrado: $LogDir/$Pattern"; exit 1 }
    Write-Host "Seguindo arquivos: $($files -join ', ')"
    Get-Content -Path $files -Wait -Tail 10
    exit 0
}

if ($Id) {
    $file = Join-Path $LogDir "instance-$Id.log"
    if (-not (Test-Path $file)) { Write-Host "Arquivo $file não encontrado."; exit 1 }
    Write-Host "Seguindo $file (CTRL-C para sair)"
    Get-Content -Path $file -Wait -Tail 10
    exit 0
}

# default follow all
Write-Host "Seguindo todos os logs em $LogDir/*.log (CTRL-C para sair)"
Get-Content -Path (Join-Path $LogDir '*.log') -Wait -Tail 10

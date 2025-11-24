<#
.SYNOPSIS
  Inicia N instâncias do serviço TriagemSender no Windows PowerShell.
.DESCRIPTION
  Cada instância recebe variáveis de ambiente TRIAGEM_ID e TRIAGEM_IDS.
  Logs são salvos em .\logs\instance-<id>.log e PIDs em .\run\pids.txt
.PARAMETER Count
  Número de instâncias (default 5)
.PARAMETER Jar
  Caminho para o jar a executar (default: target\microservices-trab3-ds-1.0-SNAPSHOT.jar)
.PARAMETER DryRun
  Se presente, imprime os comandos ao invés de executá-los.
#>
param(
    [int]$Count = 5,
    [string]$Jar = "target\microservices-trab3-ds-1.0-SNAPSHOT.jar",
    [switch]$DryRun
)

$LogDir = "logs"
$RunDir = "run"
$PidsFile = Join-Path $RunDir "pids.txt"

if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir | Out-Null }
if (-not (Test-Path $RunDir)) { New-Item -ItemType Directory -Path $RunDir | Out-Null }
"" | Out-File -FilePath $PidsFile -Encoding UTF8

$triagemIds = (1..$Count) -join ","
Write-Host "Starting $Count instances (TRIAGEM_IDS=$triagemIds)"

for ($i = 1; $i -le $Count; $i++) {
    $log = Join-Path $LogDir "instance-$i.log"
    $env = @{ 'TRIAGEM_ID' = "$i"; 'TRIAGEM_IDS' = $triagemIds }
    $startInfo = @{
        FilePath = 'java'
        ArgumentList = "-jar", $Jar
        RedirectStandardOutput = $log
        RedirectStandardError = $log
        NoNewWindow = $true
    }

    if ($DryRun) {
        Write-Host "DRY-RUN: (env TRIAGEM_ID=$i TRIAGEM_IDS=$triagemIds) java -jar $Jar > $log 2>&1"
    } else {
        # Start process with environment variables
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = 'java'
        $psi.Arguments = "-jar `"$Jar`""
        $psi.UseShellExecute = $false
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError = $true
        foreach ($k in $env.Keys) { $psi.EnvironmentVariables[$k] = $env[$k] }
        $proc = New-Object System.Diagnostics.Process
        $proc.StartInfo = $psi
        $proc.Start() | Out-Null
        $pid = $proc.Id
        Add-Content -Path $PidsFile -Value $pid
        Write-Host "Started instance $i -> PID $pid (log: $log)"
        Start-Sleep -Milliseconds 200
    }
}

if (-not $DryRun) { Write-Host "All instances started. PIDs saved in $PidsFile" }


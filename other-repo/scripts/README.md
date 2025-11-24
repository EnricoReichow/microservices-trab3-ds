# Scripts de execução - TriagemSender

Este diretório contém scripts para facilitar iniciar, parar e inspecionar várias instâncias do serviço `TriagemSender` localmente.

Arquivos úteis

- `run-instances.sh` (Linux/macOS - bash)
  - Inicia N instâncias (padrão 5). Cada instância recebe as variáveis de ambiente `TRIAGEM_ID` e `TRIAGEM_IDS`.
  - Logs por instância em `logs/instance-<id>.log`.
  - PIDs salvos em `run/pids.txt` (linha 1 -> ID 1, linha 2 -> ID 2, ...).
  - Opções: `--count N`, `--no-build`, `--dry-run`, `--jar <path>`.

- `stop-instances.sh` (Linux/macOS - bash)
  - Para processos listados em `run/pids.txt`.
  - Opções: `--dry-run`, `--force` (kill -9).

- `stop-process.sh` (Linux/macOS - bash)  <-- NOVO
  - Para uma instância específica por ID lógico (ex: 1..N).
  - Usa `run/pid_map.txt` (formato: `<ID> <PID>`). Remove o par `<ID PID>` do `pid_map` e atualiza `run/pids.txt` para refletir os PIDs ativos.
  - Opções: `--id N` (obrigatório), `--dry-run`, `--force` (kill -9), `--timeout N`.
  - Exemplo (dry-run):
    ```bash
    ./scripts/stop-process.sh --id 3 --dry-run
    ```
  - Exemplo (real):
    ```bash
    ./scripts/stop-process.sh --id 3
    ```

- `stop-coordinator.sh` (Linux/macOS - bash)
  - Mata somente a instância que atualmente é coordenador, conforme detectado nos logs.
  - Estratégia: procura por linhas nos logs com mensagens indicando que um processo virou coordenador, extrai o ID, e usa esse ID para localizar o PID correspondente em `run/pid_map.txt` ou `run/pids.txt`.

- `stop-instances.ps1`, `run-instances.ps1`, `stop-coordinator.ps1` (PowerShell)
  - Versões equivalentes para Windows PowerShell. Veja a seção "PowerShell (Windows)" abaixo para exemplos.

- `watch-logs.sh` (segue logs com tail -F)

Como os scripts mapeiam ID -> PID

- `run-instances.sh` grava os PIDs em `run/pid_map.txt` (uma linha por instância: `<ID> <PID>`) e em `run/pids.txt` (apenas PIDs, uma linha por PID). Os scripts de parada usam esse `pid_map.txt` para encontrar o PID associado a um ID lógico.

Uso típico (bash)

1) Iniciar 5 instâncias:
```bash
./scripts/run-instances.sh --count 5
```

2) Parar a instância de ID 3 (simulação):
```bash
# Parar instancia
./scripts/stop-process.sh --id 3 --dry-run
# se ok
./scripts/stop-process.sh --id 3
```

3) Parar todas as instâncias:
```bash
./scripts/stop-instances.sh
```

PowerShell (Windows)

Os scripts PowerShell equivalentes estão disponíveis e seguem a mesma lógica:

- `run-instances.ps1` — inicia N instâncias e salva PIDs em `run\pids.txt`.
- `stop-instances.ps1` — para os PIDs de `run\pids.txt`.
- `stop-process.ps1` — para um processo por ID lógico (usa `run\pid_map.txt`, remove a entrada e reconstrói `run\pids.txt`).
- `stop-coordinator.ps1` — procura o líder nos logs e delega para `stop-process.ps1`.
- `watch-logs.ps1` — segue logs via `Get-Content -Wait`.

Exemplos PowerShell (abrir um PowerShell/Pwsh na pasta do projeto):

1) Iniciar 5 instâncias (dry-run):
```powershell
pwsh -NoProfile -File ./scripts/run-instances.ps1 -Count 5 -DryRun
```

2) Iniciar 5 instâncias (real):
```powershell
pwsh -NoProfile -File ./scripts/run-instances.ps1 -Count 5
```

3) Parar a instância ID=3 (dry-run):
```powershell
pwsh -NoProfile -File ./scripts/stop-process.ps1 -Id 3 -DryRun
```

4) Parar a instância ID=3 (real):
```powershell
pwsh -NoProfile -File ./scripts/stop-process.ps1 -Id 3
```

5) Parar apenas o coordenador (detectado pelos logs):
```powershell
pwsh -NoProfile -File ./scripts/stop-coordinator.ps1 -DryRun
# depois, sem dry-run
pwsh -NoProfile -File ./scripts/stop-coordinator.ps1
```

6) Seguir logs em tempo real (PowerShell):
```powershell
# todos os logs
pwsh -NoProfile -File ./scripts/watch-logs.ps1 -All
# ou um id específico
pwsh -NoProfile -File ./scripts/watch-logs.ps1 -Id 2
```

Notas

- Sempre prefira usar os scripts para iniciar/parar porque eles mantêm `run/pid_map.txt` e `run/pids.txt` consistentes. Se você iniciar manualmente os processos sem gravar esses arquivos, os scripts podem não localizar os PIDs corretamente.
- `stop-process.sh` / `stop-process.ps1` remove a entrada do `run/pid_map.txt` e reconstrói `run/pids.txt` a partir dos mapeamentos restantes. Se quiser preservar os arquivos antes de testes destrutivos, faça backup manual.

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
  - Versões equivalentes para Windows PowerShell.

- `watch-logs.sh` (segue logs com tail -F)

Como os scripts mapeiam ID -> PID

- `run-instances.sh` grava os PIDs em `run/pid_map.txt` (uma linha por instância: `<ID> <PID>`) e em `run/pids.txt` (apenas PIDs, uma linha por PID). Os scripts de parada usam esse `pid_map.txt` para encontrar o PID associado a um ID lógico.

Uso típico

1) Iniciar 5 instâncias:
```bash
./scripts/run-instances.sh --count 5
```

2) Parar a instância de ID 3 (simulação):
```bash
# Parar instancia
./scripts/stop-process.sh --id 3
```

3) Parar todas as instâncias:
```bash
./scripts/stop-instances.sh
```

Notas

- Sempre prefira usar os scripts para iniciar/parar porque eles mantêm `run/pid_map.txt` e `run/pids.txt` consistentes. Se você iniciar manualmente os processos sem gravar esses arquivos, os scripts podem não localizar os PIDs corretamente.
- `stop-process.sh` remove a entrada do `run/pid_map.txt` e reconstrói `run/pids.txt` a partir dos mapeamentos restantes. Se quiser preservar os arquivos antes de testes destrutivos, faça backup manual.

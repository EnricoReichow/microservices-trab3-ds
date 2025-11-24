#!/usr/bin/env bash
# scripts/stop-coordinator.sh
# Mata apenas o processo que atualmente é coordenador (conforme últimos logs)

set -euo pipefail

DRY_RUN=false
FORCE=false

usage() {
  cat <<EOF
Usage: $0 [--dry-run] [--force]

Options:
  --dry-run    Apenas mostra o que faria (não mata processo)
  --force      Usa kill -9 em vez de kill (SIGTERM)
  -h|--help    Mostra esta ajuda

Descrição:
  O script procura no diretório logs/ pela última ocorrencia das mensagens
  que indicam "VIROU COORDENADOR" ou "NOVO COORDENADOR", extrai o ID do
  processo coordenador e tenta mapear esse ID para o PID usando várias estratégias:
   1) `run/pid_map.txt` (formato: "<ID> <PID>")
   2) `run/pids.txt` (linha N -> PID)
   3) arquivo de log `logs/instance-<ID>.log` (quem tem o arquivo aberto via lsof)
   4) procurar em /proc/*/environ por `TRIAGEM_ID=<ID>`

  Se encontrar um PID válido, envia SIGTERM (ou SIGKILL com --force).
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run)
      DRY_RUN=true; shift;;
    --force)
      FORCE=true; shift;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

LOG_DIR="logs"
PIDS_FILE="run/pids.txt"
PID_MAP="run/pid_map.txt"

if [[ ! -d "$LOG_DIR" ]]; then
  echo "Diretório de logs '$LOG_DIR' não encontrado. Nada a fazer.";
  exit 1
fi

# Remove mapping helper: remove mapping for a given ID from PID_MAP and rebuild PIDS_FILE
remove_mapping() {
  local rem_id="$1"
  if [[ "$DRY_RUN" = true ]]; then
    echo "DRY-RUN: would remove mapping for ID=$rem_id from $PID_MAP and update $PIDS_FILE"
    return 0
  fi
  if [[ ! -f "$PID_MAP" ]]; then
    return 0
  fi
  tmpfile=$(mktemp)
  grep -v -E "^${rem_id}[[:space:]]+" "$PID_MAP" > "$tmpfile" || true
  mv "$tmpfile" "$PID_MAP"
  if [[ -f "$PID_MAP" ]]; then
    # sort by ID numeric, output only PID column
    awk '{print $2" "$1}' "$PID_MAP" | sort -k2n -t' ' | awk '{print $1}' > "$PIDS_FILE" || true
  else
    rm -f "$PIDS_FILE" || true
  fi
  echo "Removed stale mapping for ID=$rem_id from $PID_MAP and updated $PIDS_FILE"
}

# Procura a última linha que indica coordenador
leader_line=$(grep -h -E "VIROU COORDENADOR|NOVO COORDENADOR|\bCOORDENADOR\b" "$LOG_DIR"/*.log 2>/dev/null | tail -n 1 || true)

if [[ -z "$leader_line" ]]; then
  echo "Não foi encontrada nenhuma indicação de coordenador nos logs.";
  echo "Verifique logs/ ou use './scripts/run-instances.sh' primeiro.";
  exit 1
fi

# Extrai todos números presentes na linha (podem ser ids ou PIDs)
mapfile -t candidate_nums < <(echo "$leader_line" | grep -oE '[0-9]+' || true)

# Prefer a number that exists as an ID in pid_map ou pids file (try from last to first)
leader_id=""
if [[ ${#candidate_nums[@]} -gt 0 ]]; then
  for (( idx=${#candidate_nums[@]}-1; idx>=0; idx-- )); do
    num=${candidate_nums[$idx]}
    # check pid_map
    if [[ -f "$PID_MAP" ]] && grep -q -E "^${num}[[:space:]]+" "$PID_MAP"; then
      leader_id="$num"
      break
    fi
    # check pids file (line exists and is numeric)
    if [[ -f "$PIDS_FILE" ]]; then
      cand=$(sed -n "${num}p" "$PIDS_FILE" | tr -d '\r' || true)
      if [[ -n "$cand" && "$cand" =~ ^[0-9]+$ ]]; then
        leader_id="$num"
        break
      fi
    fi
  done
fi

# fallback: use last numeric token
if [[ -z "$leader_id" ]]; then
  leader_id=$(echo "$leader_line" | grep -oE '[0-9]+' | tail -n 1 || true)
fi

if [[ -z "$leader_id" ]]; then
  echo "Falha ao extrair ID do coordenador a partir da linha: $leader_line";
  exit 1
fi

echo "Coordenador detectado (a partir dos logs): ID=$leader_id"

# Function: check pid alive
is_alive() { kill -0 "$1" 2>/dev/null; }

found_pid=""

# Strategy 1: pid_map.txt (format: <id> <pid>)
if [[ -f "$PID_MAP" ]]; then
  line=$(grep -E "^${leader_id}[[:space:]]+" "$PID_MAP" | tail -n1 || true)
  if [[ -n "$line" ]]; then
    cand=$(echo "$line" | awk '{print $2}')
    if [[ -n "$cand" ]] && is_alive "$cand"; then
      found_pid="$cand"
      echo "Encontrado PID via $PID_MAP: $found_pid"
    elif [[ -n "$cand" ]]; then
      echo "PID via $PID_MAP: $cand (não está em execução)"
      # mapping is stale — remove it so other scripts won't be misled
      remove_mapping "$leader_id"
    fi
  fi
fi

# Strategy 2: run/pids.txt (nth line)
if [[ -z "$found_pid" && -f "$PIDS_FILE" ]]; then
  cand=$(sed -n "${leader_id}p" "$PIDS_FILE" | tr -d '\r' || true)
  if [[ -n "$cand" && "$cand" =~ ^[0-9]+$ ]]; then
    if is_alive "$cand"; then
      found_pid="$cand"
      echo "Encontrado PID via $PIDS_FILE (linha $leader_id): $found_pid"
    else
      echo "PID via $PIDS_FILE (linha $leader_id) = $cand (não está em execução)"
      # remove stale entry from pids file and corresponding pid_map if present
      if [[ -f "$PID_MAP" ]]; then
        remove_mapping "$leader_id"
      else
        # remove the specific line from pids file
        if [[ "$DRY_RUN" = false ]]; then
          tmp=$(mktemp)
          # write all lines except the one we want to remove
          awk 'NR!='"${leader_id}"' {print}' "$PIDS_FILE" > "$tmp" || true
          mv "$tmp" "$PIDS_FILE"
          echo "Removed stale PID entry from $PIDS_FILE (linha $leader_id)"
        else
          echo "DRY-RUN: would remove line ${leader_id} from $PIDS_FILE"
        fi
      fi
    fi
  fi
fi

# Strategy 3: lsof on log file to find who has it open
if [[ -z "$found_pid" ]]; then
  logfile="$LOG_DIR/instance-${leader_id}.log"
  if [[ -f "$logfile" ]]; then
    if command -v lsof >/dev/null 2>&1; then
      lpid=$(lsof -t "$logfile" 2>/dev/null | head -n1 || true)
      if [[ -n "$lpid" && "$lpid" =~ ^[0-9]+$ ]]; then
        if is_alive "$lpid"; then
          found_pid="$lpid"
          echo "Encontrado PID via lsof no arquivo $logfile: $found_pid"
        else
          echo "PID via lsof ($lpid) não está em execução"
        fi
      fi
    else
      echo "lsof não disponível, pulando verificação por log aberto"
    fi
  fi
fi

# Strategy 4: search /proc/*/environ for TRIAGEM_ID=<leader_id>
if [[ -z "$found_pid" ]]; then
  echo "Tentando procurar em /proc/*/environ por TRIAGEM_ID=$leader_id (requer permissões do mesmo usuário)"
  for e in /proc/[0-9]*/environ; do
    if [[ -r "$e" ]]; then
      if tr '\0' '\n' < "$e" | grep -q "^TRIAGEM_ID=${leader_id}$" 2>/dev/null; then
        candpid=$(basename $(dirname "$e"))
        if is_alive "$candpid"; then
          found_pid="$candpid"
          echo "Encontrado PID via /proc/*/environ: $found_pid"
          break
        fi
      fi
    fi
  done
fi

if [[ -z "$found_pid" ]]; then
  echo "Não foi possível localizar um PID ativo correspondente ao ID $leader_id.";
  echo "Possíveis causas: run/pids.txt está desatualizado, o processo foi reiniciado (PID mudou), ou as instâncias foram iniciadas por outro usuário/sessão.";
  echo "Sugestões:"
  echo "  - Verifique processos Java ativos: ps -eo pid,cmd | grep microservices-trab3-ds"
  echo "  - Verifique quem abre o log: lsof logs/instance-${leader_id}.log"
  echo "  - Use ./scripts/force-stop-instances.sh --yes para parar por padrão todos os processos do jar"
  exit 1
fi

# Now we have found_pid
pid=$found_pid

if [[ "$DRY_RUN" = true ]]; then
  if [[ "$FORCE" = true ]]; then
    echo "DRY-RUN: kill -9 $pid  # (forçar)"
  else
    echo "DRY-RUN: kill $pid"
  fi
  exit 0
fi

if [[ "$FORCE" = true ]]; then
  echo "Matando coordenador ID=$leader_id PID=$pid com SIGKILL (-9)"
  kill -9 "$pid" || true
else
  echo "Enviando SIGTERM ao coordenador ID=$leader_id PID=$pid"
  kill "$pid" || true
fi

# wait a bit and report
sleep 1
if is_alive "$pid"; then
  echo "PID $pid ainda está em execução após o sinal; tente --force ou use force-stop-instances.sh"
else
  echo "PID $pid encerrado (coordenador $leader_id)"
fi

exit 0

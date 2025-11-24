#!/usr/bin/env bash
# scripts/stop-process.sh
# Para um processo específico por TRIAGEM_ID (ID lógico 1..N).
# Usa run/pid_map.txt (formato: "<ID> <PID>"). Remove o registro após matar o processo
# Atualiza run/pids.txt para refletir os PIDs ativos (ordem por ID crescente).

set -euo pipefail

PID_MAP="run/pid_map.txt"
PIDS_FILE="run/pids.txt"
DRY_RUN=false
FORCE=false
TIMEOUT=5

usage() {
  cat <<EOF
Usage: $0 --id N [--dry-run] [--force] [--timeout N]

Options:
  --id N        ID lógico do processo a parar (ex: 1,2,3)
  --dry-run     Mostra as ações sem executar
  --force       Usa SIGKILL em vez de SIGTERM
  --timeout N   Segundos para aguardar SIGTERM antes de SIGKILL (default: $TIMEOUT)
  -h|--help     Mostra este help

Behavior:
  - Procura o PID associado ao ID em $PID_MAP
  - Envia SIGTERM (ou SIGKILL com --force), espera até timeout, e força se necessário
  - Remove o par <ID PID> de $PID_MAP e reescreve $PIDS_FILE com PIDs restantes em ordem de ID asc
EOF
}

ID=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --id)
      ID="$2"; shift 2;;
    --dry-run)
      DRY_RUN=true; shift;;
    --force)
      FORCE=true; shift;;
    --timeout)
      TIMEOUT="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

if [[ -z "$ID" ]]; then
  echo "Error: --id N is required"
  usage
  exit 1
fi

if [[ ! -f "$PID_MAP" ]]; then
  echo "PID map not found: $PID_MAP"
  exit 1
fi

# Find line for ID
line=$(grep -E "^${ID}[[:space:]]+" "$PID_MAP" || true)
if [[ -z "$line" ]]; then
  echo "No mapping for ID=$ID in $PID_MAP"
  exit 1
fi

pid=$(echo "$line" | awk '{print $2}')
if [[ -z "$pid" || ! "$pid" =~ ^[0-9]+$ ]]; then
  echo "Invalid PID for ID=$ID in $PID_MAP: '$pid'"
  exit 1
fi

is_alive() { kill -0 "$1" 2>/dev/null; }

if [[ "$DRY_RUN" = true ]]; then
  if [[ "$FORCE" = true ]]; then
    echo "DRY-RUN: Would kill -9 PID=$pid for ID=$ID and remove mapping"
  else
    echo "DRY-RUN: Would send SIGTERM to PID=$pid for ID=$ID and remove mapping after it's gone"
  fi
  exit 0
fi

# Attempt to stop
if [[ "$FORCE" = true ]]; then
  echo "Killing -9 PID=$pid (ID=$ID)"
  kill -9 "$pid" 2>/dev/null || true
else
  echo "Sending SIGTERM to PID=$pid (ID=$ID)"
  kill "$pid" 2>/dev/null || true
  # wait up to TIMEOUT seconds
  waited=0
  while is_alive "$pid" && [[ $waited -lt $TIMEOUT ]]; do
    sleep 1
    waited=$((waited+1))
  done
  if is_alive "$pid"; then
    echo "PID $pid still alive after ${TIMEOUT}s; escalating to SIGKILL"
    kill -9 "$pid" 2>/dev/null || true
    sleep 0.1
  else
    echo "PID $pid exited gracefully"
  fi
fi

# Ensure process is gone
if is_alive "$pid"; then
  echo "Failed to stop PID $pid (ID $ID)"
  exit 2
fi

# Remove mapping from pid_map.txt
tmpfile=$(mktemp)
grep -v -E "^${ID}[[:space:]]+" "$PID_MAP" > "$tmpfile" || true
mv "$tmpfile" "$PID_MAP"

# Rebuild run/pids.txt from remaining pid_map entries, ordered by numeric ID
if [[ -f "$PID_MAP" ]]; then
  # sort by ID numeric, output only PID column
  awk '{print $2" "$1}' "$PID_MAP" | sort -k2n -t' ' | awk '{print $1}' > "$PIDS_FILE" || true
  echo "Removed mapping for ID=$ID. Updated $PID_MAP and $PIDS_FILE."
else
  rm -f "$PIDS_FILE" || true
  echo "Removed mapping for ID=$ID. $PID_MAP deleted; $PIDS_FILE removed."
fi

exit 0


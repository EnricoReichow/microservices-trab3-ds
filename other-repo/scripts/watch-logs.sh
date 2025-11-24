#!/usr/bin/env bash
# scripts/watch-logs.sh
# Segue logs em tempo real. Por padrão segue todos os logs em logs/*.log.

set -euo pipefail

LOG_DIR=logs
FOLLOW_ALL=false
INSTANCE_ID=""

usage() {
  cat <<EOF
Usage: $0 [--all] [--id N] [--pattern <glob>]

Options:
  --all          Segue todos os arquivos em $LOG_DIR (logs/*.log)
  --id N         Segue somente logs/instance-N.log
  --pattern GLOB Segue arquivos que batam com o glob dentro de $LOG_DIR (ex: "*cardio*.log")
  -h|--help      Mostra esta ajuda

Exemplos:
  $0 --all
  $0 --id 3
  $0 --pattern "instance-*.log"

Observação: usa `tail -F` para seguir novos arquivos/rotacionamentos. Se você preferir uma janela dividida
ou múltiplos painéis, recomendo usar tmux ou screen e executar este script em um dos painéis.
EOF
}

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --all)
      FOLLOW_ALL=true; shift;;
    --id)
      INSTANCE_ID="$2"; shift 2;;
    --pattern)
      PATTERN="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

if [[ ! -d "$LOG_DIR" ]]; then
  echo "Diretório de logs '$LOG_DIR' não encontrado. Inicie instâncias primeiro.";
  exit 1
fi

if [[ "$FOLLOW_ALL" = true ]]; then
  FILES=("$LOG_DIR"/*.log)
  if [[ ${#FILES[@]} -eq 0 ]]; then
    echo "Nenhum arquivo de log encontrado em $LOG_DIR"; exit 1
  fi
  echo "Seguindo todos os logs em $LOG_DIR/*.log (CTRL-C para sair)"
  exec tail -F "$LOG_DIR"/*.log
fi

if [[ -n "${PATTERN-}" ]]; then
  FILES=("$LOG_DIR"/$PATTERN)
  if [[ ${#FILES[@]} -eq 0 ]]; then
    echo "Nenhum arquivo correspondente ao padrão encontrado: $LOG_DIR/$PATTERN"; exit 1
  fi
  echo "Seguindo arquivos: ${FILES[*]}"
  exec tail -F "${FILES[@]}"
fi

if [[ -n "$INSTANCE_ID" ]]; then
  FILE="$LOG_DIR/instance-$INSTANCE_ID.log"
  if [[ ! -f "$FILE" ]]; then
    echo "Arquivo $FILE não encontrado."; exit 1
  fi
  echo "Seguindo $FILE (CTRL-C para sair)"
  exec tail -F "$FILE"
fi

# Default: follow all
FILES=("$LOG_DIR"/*.log)
if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "Nenhum arquivo de log encontrado em $LOG_DIR"; exit 1
fi

echo "Seguindo todos os logs em $LOG_DIR/*.log (CTRL-C para sair)"
exec tail -F "$LOG_DIR"/*.log


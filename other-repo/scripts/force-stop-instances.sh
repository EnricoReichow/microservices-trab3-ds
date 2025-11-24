#!/usr/bin/env bash
# scripts/force-stop-instances.sh
# Localiza processos que executam o jar microservices-trab3-ds e força parada.

set -euo pipefail

DRY_RUN=true
YES=false
TIMEOUT=5

usage() {
  cat <<EOF
Usage: $0 [--yes] [--timeout N] [--pattern <regex>]

Options:
  --yes         Actually perform the kills. By default script shows what would be done (dry-run).
  --timeout N   Seconds to wait after SIGTERM before SIGKILL (default: $TIMEOUT)
  --pattern P   Regex to match process commandline (default: 'java -jar .*microservices-trab3-ds')
  -h|--help     Show this help

This script only targets processes whose command line matches the given pattern
so it's safe to run on machines with other Java processes (it won't kill IDEs,
unless their command line matches the pattern).

Example:
  # Dry-run
  ./scripts/force-stop-instances.sh

  # Actually kill
  ./scripts/force-stop-instances.sh --yes
EOF
}

PATTERN='java -jar .*microservices-trab3-ds'

while [[ $# -gt 0 ]]; do
  case "$1" in
    --yes)
      DRY_RUN=false; YES=true; shift;;
    --timeout)
      TIMEOUT="$2"; shift 2;;
    --pattern)
      PATTERN="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# Find candidate PIDs and full cmd lines
mapfile -t CANDIDATES < <(ps -eo pid=,cmd= | grep -E "$PATTERN" | grep -v grep || true)

if [[ ${#CANDIDATES[@]} -eq 0 ]]; then
  echo "No running processes matched pattern: $PATTERN"
  exit 0
fi

echo "Found ${#CANDIDATES[@]} candidate(s):"
for c in "${CANDIDATES[@]}"; do
  pid=$(echo "$c" | awk '{print $1}')
  cmd=$(echo "$c" | cut -d' ' -f2-)
  echo "  PID=$pid  CMD=$cmd"
done

if [[ "$DRY_RUN" = true ]]; then
  echo "\nDry-run mode: no processes will be terminated. Re-run with --yes to execute."
  exit 0
fi

# Attempt graceful shutdown first
for c in "${CANDIDATES[@]}"; do
  pid=$(echo "$c" | awk '{print $1}')
  echo "Sending SIGTERM to PID $pid"
  kill "$pid" 2>/dev/null || echo "  Failed to send SIGTERM to $pid (maybe already gone)"
done

# Wait up to TIMEOUT secs for processes to exit
echo "Waiting up to $TIMEOUT seconds for processes to exit..."
end=$((SECONDS+TIMEOUT))
while [[ $SECONDS -lt $end ]]; do
  still=()
  for c in "${CANDIDATES[@]}"; do
    pid=$(echo "$c" | awk '{print $1}')
    if kill -0 "$pid" 2>/dev/null; then
      still+=("$pid")
    fi
  done
  if [[ ${#still[@]} -eq 0 ]]; then
    echo "All candidate processes exited after SIGTERM."
    break
  fi
  sleep 1
done

# Force-kill remaining
still_any=false
for c in "${CANDIDATES[@]}"; do
  pid=$(echo "$c" | awk '{print $1}')
  if kill -0 "$pid" 2>/dev/null; then
    echo "PID $pid still alive after timeout; sending SIGKILL"
    kill -9 "$pid" 2>/dev/null || echo "  Failed SIGKILL for $pid"
    still_any=true
  fi
done

# Final check
mapfile -t REMAINING < <(ps -eo pid=,cmd= | grep -E "$PATTERN" | grep -v grep || true)
if [[ ${#REMAINING[@]} -eq 0 ]]; then
  echo "No matching processes remain."
  exit 0
else
  echo "WARNING: the following matching processes remain:"
  for r in "${REMAINING[@]}"; do
    echo "  $r"
  done
  exit 3
fi


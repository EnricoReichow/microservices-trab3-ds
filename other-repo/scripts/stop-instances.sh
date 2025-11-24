#!/usr/bin/env bash
# scripts/stop-instances.sh
# Para as instâncias iniciadas pelo run-instances.sh (usa run/pids.txt)

set -euo pipefail

PIDS_FILE=run/pids.txt
DRY_RUN=false
FORCE=false
TIMEOUT=5

usage() {
  cat <<EOF
Usage: $0 [--dry-run] [--force] [--timeout N]

Options:
  --dry-run    Print kill commands instead of executing
  --force      Use SIGKILL (kill -9) immediately instead of SIGTERM
  --timeout N  Seconds to wait after SIGTERM before SIGKILL (default: $TIMEOUT)
  -h|--help    Show this help

This script reads PIDs from $PIDS_FILE and stops them. If the file is missing or
some PIDs are not running, it will attempt a fallback by locating Java processes
that match the TriagemSender main class and stopping them.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
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

stopped_any=false
failed_any=false

# helper to check process alive
is_alive() {
  local _pid=$1
  if kill -0 "$_pid" 2>/dev/null; then
    return 0
  else
    return 1
  fi
}

# helper to attempt graceful stop
attempt_stop() {
  local _pid=$1
  if [[ "$DRY_RUN" = true ]]; then
    if [[ "$FORCE" = true ]]; then
      echo "DRY-RUN: kill -9 $_pid"
    else
      echo "DRY-RUN: kill $_pid  (then wait $TIMEOUT s, kill -9 if still alive)"
    fi
    return 0
  fi

  if [[ "$FORCE" = true ]]; then
    echo "Killing -9 $_pid"
    kill -9 "$_pid" || true
    sleep 0.1
    if is_alive "$_pid"; then
      echo "PID $_pid still alive after SIGKILL"
      return 1
    else
      echo "PID $_pid killed"
      return 0
    fi
  fi

  echo "Sending SIGTERM to $_pid"
  kill "$_pid" || true

  # wait up to TIMEOUT seconds for process to die
  local waited=0
  while is_alive "$_pid" && [[ $waited -lt $TIMEOUT ]]; do
    sleep 0.5
    waited=$((waited + 1/2))
  done

  if is_alive "$_pid"; then
    echo "PID $_pid did not exit after ${TIMEOUT}s, escalating to SIGKILL"
    kill -9 "$_pid" || true
    sleep 0.1
    if is_alive "$_pid"; then
      echo "PID $_pid still alive after SIGKILL"
      return 1
    else
      echo "PID $_pid killed after escalation"
      return 0
    fi
  else
    echo "PID $_pid exited gracefully"
    return 0
  fi
}

if [[ -f "$PIDS_FILE" ]]; then
  mapfile -t PIDS < "$PIDS_FILE" || true
  if [[ ${#PIDS[@]} -eq 0 ]]; then
    echo "No PIDs found in $PIDS_FILE"
  else
    echo "Stopping PIDs from $PIDS_FILE: ${PIDS[*]}"
    for pid in "${PIDS[@]}"; do
      pid=$(echo "$pid" | tr -d '\r')
      if [[ -z "$pid" ]]; then
        continue
      fi

      if ! is_alive "$pid"; then
        echo "Process $pid not running"
        continue
      fi

      if attempt_stop "$pid"; then
        stopped_any=true
      else
        failed_any=true
      fi
    done
  fi
else
  echo "No pids file found at $PIDS_FILE. Will attempt fallback discovery."
fi

# Fallback: find Java processes matching TriagemSender (only if something still alive or no pids)
if ! $DRY_RUN && ( $failed_any || ! $stopped_any ); then
  echo "Attempting fallback: looking for Java processes running TriagemSender..."
  # list processes containing the main class or jar name
  mapfile -t CANDIDATES < <(ps -eo pid,args | grep -E 'TriagemSender\.TriagemSenderMain|microservices-trab3-ds' | grep -v grep | awk '{print $1" ",$0}') || true

  if [[ ${#CANDIDATES[@]} -eq 0 ]]; then
    echo "No fallback candidates found."
  else
    echo "Found fallback candidates:"
    for entry in "${CANDIDATES[@]}"; do
      echo "  $entry"
    done

    for entry in "${CANDIDATES[@]}"; do
      # first token is PID
      candpid=$(echo "$entry" | awk '{print $1}')
      if [[ -z "$candpid" ]]; then
        continue
      fi
      if ! is_alive "$candpid"; then
        echo "Candidate PID $candpid not running anymore"
        continue
      fi
      echo "Attempting to stop candidate PID $candpid"
      if attempt_stop "$candpid"; then
        stopped_any=true
      else
        failed_any=true
      fi
    done
  fi
fi

# Post-check: are there any Java TriagemSender processes left?
mapfile -t STILL_RUNNING < <(ps -eo pid,args | grep -E 'TriagemSender\.TriagemSenderMain|microservices-trab3-ds' | grep -v grep | awk '{print $1}') || true
if [[ ${#STILL_RUNNING[@]} -gt 0 ]]; then
  echo "Warning: the following TriagemSender-related PIDs are still running: ${STILL_RUNNING[*]}"
else
  echo "No TriagemSender-related Java processes detected"
fi

# Remove pids file only if not in dry-run
if [[ "$DRY_RUN" = false ]]; then
  if [[ ${#STILL_RUNNING[@]} -eq 0 ]]; then
    rm -f "$PIDS_FILE" || true
    echo "Stopped processes and removed $PIDS_FILE"
  else
    echo "Some processes remain; not removing $PIDS_FILE. Inspect logs and PIDs."
  fi
fi

if [[ "$failed_any" = true ]]; then
  echo "One or more processes could not be stopped cleanly."
  exit 2
fi

exit 0

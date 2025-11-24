#!/usr/bin/env bash
# scripts/run-instances.sh
# Inicia N instâncias do serviço TriagemSender (default 5)
# Cada instância roda em background, salva logs em ./logs and pids em ./run/pids.txt

set -euo pipefail

# Defaults
COUNT=5
BUILD=true
DRY_RUN=false
JAR=target/microservices-trab3-ds-1.0-SNAPSHOT.jar
LOG_DIR=logs
RUN_DIR=run
PIDS_FILE="$RUN_DIR/pids.txt"
TRIAGEM_IDS=""

usage() {
  cat <<EOF
Usage: $0 [--count N] [--no-build] [--dry-run] [--jar path]

Options:
  --count N       Number of instances to start (default: 5)
  --no-build      Skip 'mvn -DskipTests package' build step
  --dry-run       Print commands instead of executing them
  --jar PATH      Path to the jar to run (default: $JAR)
  -h|--help       Show this help

Each instance will be started with environment variables:
  TRIAGEM_ID=<id>
  TRIAGEM_IDS="1,2,...,N"

Logs: $LOG_DIR/instance-<id>.log
PIDs:  $PIDS_FILE
EOF
}

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --count)
      COUNT="$2"; shift 2;;
    --no-build)
      BUILD=false; shift 1;;
    --dry-run)
      DRY_RUN=true; shift 1;;
    --jar)
      JAR="$2"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "Unknown arg: $1"; usage; exit 1;;
  esac
done

# Compute TRIAGEM_IDS: 1..COUNT
for i in $(seq 1 "$COUNT"); do
  if [ -z "$TRIAGEM_IDS" ]; then
    TRIAGEM_IDS="$i"
  else
    TRIAGEM_IDS+=",$i"
  fi
done

# Build
if [ "$BUILD" = true ]; then
  if [ "$DRY_RUN" = true ]; then
    echo "DRY-RUN: mvn -DskipTests package"
  else
    echo "Building project... (mvn -DskipTests package)"
    mvn -DskipTests package
  fi
fi

# Prepare dirs
mkdir -p "$LOG_DIR"
mkdir -p "$RUN_DIR"
: > "$PIDS_FILE"
: > "$RUN_DIR/pid_map.txt"

# Start instances
echo "Starting $COUNT instances (TRIAGEM_IDS=$TRIAGEM_IDS)"
for id in $(seq 1 "$COUNT"); do
  INSTANCE_LOG="$LOG_DIR/instance-$id.log"
  CMD=("env" "TRIAGEM_ID=$id" "TRIAGEM_IDS=$TRIAGEM_IDS" "java" "-jar" "$JAR")

  if [ "$DRY_RUN" = true ]; then
    echo "DRY-RUN: ${CMD[*]} > $INSTANCE_LOG 2>&1 &"
  else
    echo "Starting instance $id -> log: $INSTANCE_LOG"
    # Start in background
    env TRIAGEM_ID="$id" TRIAGEM_IDS="$TRIAGEM_IDS" java -jar "$JAR" > "$INSTANCE_LOG" 2>&1 &
    pid=$!
    echo "$pid" >> "$PIDS_FILE"
    echo "$id $pid" >> "$RUN_DIR/pid_map.txt"
    echo "  PID=$pid"
    # small stagger to reduce race at startup
    sleep 0.2
  fi
done

if [ "$DRY_RUN" = true ]; then
  echo "Dry run finished. No processes started."
else
  echo "All instances started. PIDs saved in $PIDS_FILE"
  echo "Tails (last 20 lines) of logs:"
  for id in $(seq 1 "$COUNT"); do
    echo "--- instance-$id.log ---"
    tail -n 20 "$LOG_DIR/instance-$id.log" || true
  done
fi

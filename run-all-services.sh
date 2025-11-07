#!/bin/bash

# Script para iniciar TODOS os serviços em terminais separados

echo "======================================"
echo "Iniciando todos os microserviços"
echo "======================================"

# Verificar se está no diretório correto
if [ ! -f "pom.xml" ]; then
    echo "Erro: Execute este script a partir do diretório raiz do projeto"
    exit 1
fi

# Função para abrir terminal com comando
open_terminal() {
    local title=$1
    local script=$2
    
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --tab --title="$title" -- bash -c "$script; exec bash"
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -e bash -c "$script; exec bash" &
    elif command -v xterm &> /dev/null; then
        xterm -T "$title" -e bash -c "$script; exec bash" &
    else
        echo "Nenhum emulador de terminal suportado encontrado"
        echo "Execute manualmente: $script"
    fi
}

echo "Abrindo terminais para cada serviço..."

open_terminal "Recepcionista" "./run-recepcionista.sh"
sleep 2

open_terminal "Triagem" "./run-triagem.sh"
sleep 2

open_terminal "Cardiologista" "./run-cardiologista.sh"
sleep 2

open_terminal "Clínico Geral" "./run-clinicogeral.sh"
sleep 2

open_terminal "Neurologista" "./run-neurologista.sh"

echo ""
echo "Todos os serviços foram iniciados em terminais separados!"
echo ""
echo "Serviços disponíveis:"
echo "  - Recepcionista:   http://localhost:8081"
echo "  - Triagem:         http://localhost:8082"
echo "  - Cardiologista:   http://localhost:8083"
echo "  - Clínico Geral:   http://localhost:8084"
echo "  - Neurologista:    http://localhost:8085"
echo ""
echo "RabbitMQ Management: http://localhost:15672"
echo ""

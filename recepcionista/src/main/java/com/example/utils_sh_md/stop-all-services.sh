#!/bin/bash

echo "🛑 Parando todos os microserviços..."

# Para todos os processos Java Spring Boot
pkill -f "spring-boot:run"

# Aguarda um pouco para garantir que os processos foram finalizados
sleep 2

# Verifica se ainda há processos rodando
RUNNING=$(ps aux | grep "spring-boot:run" | grep -v grep | wc -l)

if [ $RUNNING -eq 0 ]; then
    echo "✅ Todos os serviços foram parados com sucesso!"
else
    echo "⚠️  Ainda há $RUNNING processo(s) rodando. Tentando forçar..."
    pkill -9 -f "spring-boot:run"
    sleep 1
    echo "✅ Serviços forçadamente parados!"
fi

# Opcional: Parar RabbitMQ também
read -p "Deseja parar o RabbitMQ também? (s/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    echo "🛑 Parando RabbitMQ..."
    cd src/docker
    docker-compose down
    echo "✅ RabbitMQ parado!"
fi

echo ""
echo "✨ Pronto!"

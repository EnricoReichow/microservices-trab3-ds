#!/bin/bash

# Script para iniciar o serviço Neurologista

echo "======================================"
echo "Iniciando Neurologista Service"
echo "Porta: 8085"
echo "======================================"

mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.config.additional-location=src/main/resources/application-neurologista.properties" \
  -Dstart-class=com.hospital.neurologista.NeurologistaApplication

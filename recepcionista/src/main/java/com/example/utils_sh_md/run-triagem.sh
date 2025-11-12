#!/bin/bash

# Script para iniciar o serviço Triagem

echo "======================================"
echo "Iniciando Triagem Service"
echo "Porta: 8082"
echo "======================================"

mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.config.additional-location=src/main/resources/application-triagem.properties" \
  -Dstart-class=com.hospital.triagem.TriagemApplication

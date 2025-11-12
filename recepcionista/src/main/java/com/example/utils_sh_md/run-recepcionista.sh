#!/bin/bash

# Script para iniciar o serviço Recepcionista

echo "======================================"
echo "Iniciando Recepcionista Service"
echo "Porta: 8081"
echo "======================================"

mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.config.additional-location=src/main/resources/application-recepcionista.properties" \
  -Dstart-class=com.hospital.recepcionista.RecepcionistaApplication

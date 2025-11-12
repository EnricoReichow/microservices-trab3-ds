#!/bin/bash

# Script para iniciar o serviço Cardiologista

echo "======================================"
echo "Iniciando Cardiologista Service"
echo "Porta: 8083"
echo "======================================"

mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.config.additional-location=src/main/resources/application-cardiologista.properties" \
  -Dstart-class=com.hospital.cardiologista.CardiologistaApplication

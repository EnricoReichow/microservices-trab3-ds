#!/bin/bash

# Script para iniciar o serviço Clínico Geral

echo "======================================"
echo "Iniciando Clínico Geral Service"
echo "Porta: 8084"
echo "======================================"

mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.config.additional-location=src/main/resources/application-clinicogeral.properties" \
  -Dstart-class=com.hospital.clinicogeral.ClinicoGeralApplication

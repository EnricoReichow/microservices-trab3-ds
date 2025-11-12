````markdown
# 🚀 Início Rápido - Sistema Hospitalar Spring Boot

## ⚡ Setup em 3 Passos

### 1️⃣ AWS RDS já configurado ✅

O sistema já está conectado ao AWS RDS PostgreSQL:
- **Endpoint:** `distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com`
- **Database:** `postgres` (compartilhado por todos os serviços)
- **Região:** `sa-east-1` (São Paulo)

Todos os serviços já estão configurados nos `application.properties`.

### 2️⃣ Iniciar RabbitMQ
```bash
cd src/docker
docker-compose up -d
```

Aguarde ~10 segundos para RabbitMQ iniciar.

### 3️⃣ Compilar Projeto
```bash
mvn clean install
```

### 4️⃣ Iniciar Microserviços

**Opção A: Script Automático (Recomendado)**
```bash
./run-all-services.sh
```

**Opção B: Manual (em terminais separados)**
```bash
# Terminal 1
./run-recepcionista.sh

# Terminal 2
./run-triagem.sh

# Terminal 3
./run-cardiologista.sh

# Terminal 4
./run-clinicogeral.sh

# Terminal 5
./run-neurologista.sh
```

## ✅ Verificar se está funcionando

### Health Checks
```bash
curl http://localhost:8081/api/recepcionista/health
curl http://localhost:8083/api/cardiologista/health
curl http://localhost:8084/api/clinico-geral/health
curl http://localhost:8085/api/neurologista/health
```

Todos devem retornar mensagens de sucesso.

### RabbitMQ Management
http://localhost:15672
- User: `guest`
- Password: `guest`

Você deve ver as filas criadas: `triagem`, `cardiologia`, `neurologia`, `geral`

## 🧪 Teste Rápido

### 1. Cadastrar um paciente com infarto
```bash
curl -X POST http://localhost:8081/api/recepcionista/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João da Silva",
    "sexoBiologico": "Masculino",
    "dataNascimento": "15/05/1980",
    "tipoSanguineo": "O+",
    "estadoCivil": "Casado",
    "sintomas": "Infarto"
  }'
```

**Resultado esperado:** 
- Status 201 Created
- JSON com dados do paciente incluindo ID

### 2. Verificar logs

Nos terminais dos serviços você verá:

**Recepcionista:**
```
Paciente João da Silva registrado no banco de dados com ID 1
Paciente João da Silva enviado para fila de triagem
```

**Triagem:**
```
Paciente recebido na triagem: João da Silva com sintoma: Infarto
Paciente João da Silva encaminhado para Cardiologista
```

**Cardiologista:**
```
Paciente recebido na cardiologia: João da Silva com sintoma: Infarto
Cardiologista atendendo paciente: João da Silva
(aguarda 10 segundos)
Paciente João da Silva consultado pelo Cardiologista e salvo com ID 1
```

### 3. Consultar paciente atendido
```bash
curl http://localhost:8083/api/cardiologista/pacientes/atendidos
```

**Resultado esperado:**
```json
[
  {
    "id": 1,
    "nome": "João da Silva",
    "status": "FINALIZADO",
    "medicoResponsavel": "Cardiologista",
    "dataCadastro": "2025-11-03T...",
    "dataConsulta": "2025-11-03T...",
    ...
  }
]
```

## 🎯 Fluxo Completo de Teste

Use o arquivo `api-examples.http` com extensão REST Client do VS Code ou Postman.

### Cenário: Paciente com Dor de Cabeça

1. **POST** `/api/recepcionista/pacientes` com sintoma "Dor de cabeça"
2. Paciente vai para **Triagem**
3. Triagem roteia para **Neurologista**
4. Neurologista atende (10s)
5. **GET** `/api/neurologista/pacientes/atendidos` mostra paciente finalizado

### Cenário: Paciente com Dor de Barriga

1. **POST** `/api/recepcionista/pacientes` com sintoma "Dor de barriga"
2. Paciente vai para **Triagem**
3. Triagem roteia para **Clínico Geral**
4. Clínico atende (10s)
5. **GET** `/api/clinico-geral/pacientes/atendidos` mostra paciente finalizado

## 📊 Portas dos Serviços

| Serviço | Porta | Endpoint Base |
|---------|-------|---------------|
| Recepcionista | 8081 | http://localhost:8081/api/recepcionista |
| Triagem | 8082 | (sem API REST - apenas listener) |
| Cardiologista | 8083 | http://localhost:8083/api/cardiologista |
| Clínico Geral | 8084 | http://localhost:8084/api/clinico-geral |
| Neurologista | 8085 | http://localhost:8085/api/neurologista |
| RabbitMQ | 15672 | http://localhost:15672 |

## 🗄️ Bancos de Dados AWS RDS

Database único compartilhado por todos os microserviços:
- **Host:** `distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com:5432`
- **Database:** `postgres`  
- **Tabela:** `pacientes` (comum a todos)

### Consultar pacientes no banco
```sql
SELECT id, nome, sintomas, medico_responsavel, status, data_consulta 
FROM pacientes 
ORDER BY data_cadastro DESC;
```

## ❌ Parar Tudo

### Parar Microserviços
Pressione `Ctrl+C` em cada terminal.

### Parar RabbitMQ
```bash
cd src/docker
docker-compose down
```

## 🐛 Troubleshooting Rápido

### Porta já em uso
```bash
# Ver processos usando as portas
lsof -i :8081
lsof -i :8082
# ... etc

# Matar processo
kill -9 <PID>
```

### RabbitMQ não conecta
```bash
# Verificar se está rodando
docker ps | grep rabbitmq

# Ver logs
docker logs rabbitmq

# Reiniciar
cd src/docker
docker-compose restart rabbitmq
```

### AWS RDS não conecta
- Verifique security groups (permita tráfego na porta 5432)
- Confirme credenciais no `application.properties`
- Teste conectividade: `telnet <rds-endpoint> 5432`
- Verifique se o RDS está em "Available" status

### Erro ao compilar
```bash
# Limpar e recompilar
mvn clean install -U

# Pular testes
mvn clean install -DskipTests
```

## 📚 Documentação Completa

- **README_SPRING_BOOT.md** - Documentação completa do sistema
- **MIGRATION_GUIDE.md** - Guia de migração detalhado
- **api-examples.http** - Exemplos de requisições HTTP

## 💡 Dicas

1. **Ordem de inicialização importa:**
   - RabbitMQ deve estar rodando
   - AWS RDS já está configurado
   - Recepcionista e Triagem antes dos especialistas

2. **Logs são seus amigos:**
   - Acompanhe os logs para entender o fluxo
   - Cada ação deixa rastro nos logs

3. **Use RabbitMQ Management:**
   - Visualize mensagens nas filas
   - Debug problemas de roteamento

4. **Teste incrementalmente:**
   - Inicie um serviço por vez
   - Teste cada endpoint antes de continuar

## 🎉 Pronto!

Agora você tem um sistema hospitalar completo funcionando com:
- ✅ APIs REST
- ✅ Banco de dados PostgreSQL (AWS RDS - database único)
- ✅ Mensageria RabbitMQ
- ✅ 5 microserviços totalmente independentes
- ✅ Arquitetura Spring Boot moderna
- ✅ Infraestrutura cloud-ready (AWS)

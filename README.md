```markdown
# Sistema Hospitalar - Microserviços com Spring Boot

**Projeto:** Third project of Distributed Systems (FURB) - RabbitMQ + Spring Boot + AWS RDS

**Grupo:** Erick Ian Teske, Enrico Reichow, Felipe Cizanosky, Pedro Albuquerque

## 🏥 Arquitetura

Sistema de gerenciamento hospitalar com 5 microserviços totalmente independentes:
- **Recepcionista** - REST API para cadastro de pacientes
- **Triagem** - Roteamento inteligente baseado em sintomas
- **Cardiologista** - Especialista em cardiologia
- **Neurologista** - Especialista em neurologia
- **Clínico Geral** - Atendimento geral

## 🚀 Stack Tecnológica

- **Spring Boot 3.2.0** - Framework base
- **Java 21** - Linguagem
- **AWS RDS PostgreSQL** - Banco de dados (instância única, database compartilhado)
- **RabbitMQ** - Message broker
- **Maven** - Build tool (multi-módulo)
- **Spring Data JPA** - Persistência
- **Spring AMQP** - Mensageria

## 📦 Início Rápido

```bash
# 1. RabbitMQ já está configurado via Docker Compose
cd src/docker && docker-compose up -d

# 2. AWS RDS já está configurado (database único)

# 3. Compilar
mvn clean install

# 4. Executar
./run-all-services.sh
```

## 📚 Documentação

- **[QUICKSTART.md](QUICKSTART.md)** - Guia rápido para iniciar o sistema
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Arquitetura detalhada do sistema
- **[AWS_RDS_SETUP.md](AWS_RDS_SETUP.md)** - Configuração do banco de dados RDS
- **[api-examples.http](api-examples.http)** - Exemplos de requisições HTTP

## 🌐 Serviços

| Serviço | Porta | Endpoint | Database |
|---------|-------|----------|----------|
| Recepcionista | 8081 | http://localhost:8081/api/recepcionista | AWS RDS (postgres) |
| Triagem | 8082 | (listener apenas) | AWS RDS (postgres) |
| Cardiologista | 8083 | http://localhost:8083/api/cardiologista | AWS RDS (postgres) |
| Neurologista | 8085 | http://localhost:8085/api/neurologista | AWS RDS (postgres) |
| Clínico Geral | 8084 | http://localhost:8084/api/clinico-geral | AWS RDS (postgres) |
| RabbitMQ UI | 15672 | http://localhost:15672 | - |

## 🎯 Características

✅ Microserviços com módulos Maven independentes
✅ APIs REST completas
✅ Persistência em AWS RDS PostgreSQL (database único compartilhado)
✅ Mensageria assíncrona com RabbitMQ
✅ Arquitetura event-driven
✅ Configurações independentes por serviço
✅ Documentação completa

---

**Desenvolvido com ❤️ usando Spring Boot**
```

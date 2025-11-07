# Diagrama da Arquitetura - Sistema Hospitalar

## 📐 Visão Geral

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENTE / FRONTEND                            │
│                  (Browser, Postman, cURL, etc)                       │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTP/REST
                             ↓
┌─────────────────────────────────────────────────────────────────────┐
│                     CAMADA DE API REST                               │
│  ┌────────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Recepcionista  │  │Cardiologista │  │ Neurologista │            │
│  │   :8081        │  │   :8083      │  │   :8085      │            │
│  └────────┬───────┘  └──────┬───────┘  └──────┬───────┘            │
│           │                  │                  │                    │
│  ┌────────────────┐                                                  │
│  │ Clínico Geral  │                                                  │
│  │   :8084        │                                                  │
│  └────────┬───────┘                                                  │
└───────────┼──────────────────┼──────────────────┼───────────────────┘
            │                  │                  │
            │ AMQP             │ AMQP             │ AMQP
            ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    MESSAGE BROKER (RabbitMQ)                         │
│                         :5672 / :15672                               │
│                                                                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │ triagem  │  │cardiologia│ │neurologia│  │  geral   │           │
│  │  queue   │  │  queue    │ │  queue   │  │  queue   │           │
│  └────┬─────┘  └────┬──────┘ └────┬─────┘  └────┬─────┘           │
│       │             │              │             │                   │
│  ┌────────────────────────────────────────────────────────┐        │
│  │            Exchange: exchangePA (direct)                │        │
│  └────────────────────────────────────────────────────────┘        │
│                                                                       │
│  Fallback Queues: fallbackCardio, fallbackNeuro, fallbackGeral     │
└───────┬──────────────────┬──────────────────┬──────────────────────┘
        │                  │                  │
        │ Consumer         │ Consumer         │ Consumer
        ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   CAMADA DE SERVIÇO (Spring Boot)                   │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ Recepcionista│  │   Triagem    │  │Cardiologista │             │
│  │   Service    │  │   Service    │  │   Service    │             │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘             │
│         │                  │                  │                      │
│  ┌──────────────┐  ┌──────────────┐                                │
│  │ Neurologista │  │Clínico Geral │                                │
│  │   Service    │  │   Service    │                                │
│  └──────┬───────┘  └──────┬───────┘                                │
└─────────┼──────────────────┼──────────────────┼─────────────────────┘
          │                  │                  │
          │ JPA              │ JPA              │ JPA
          ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│                  CAMADA DE PERSISTÊNCIA (AWS RDS PostgreSQL)        │
│                                                                       │
│          ┌──────────────────────────────────────┐                   │
│          │     PostgreSQL Database (Único)       │                   │
│          │  distributed-system-db-project        │                   │
│          │      sa-east-1.rds.amazonaws.com      │                   │
│          └──────────────────────────────────────┘                   │
│                                                                       │
│  Tabela: pacientes (compartilhada por todos os serviços)            │
└─────────────────────────────────────────────────────────────────────┘
```

## 🔄 Fluxo de Dados Detalhado

### 1️⃣ Cadastro de Paciente (Recepcionista)

```
Cliente
  │
  │ POST /api/recepcionista/pacientes
  ↓
RecepcionistaController
  │
  │ registrarPaciente()
  ↓
RecepcionistaService
  │
  ├─→ pacienteRepository.save()  ───→  AWS RDS PostgreSQL
  │                                     └─→ Paciente salvo com ID
  │
  └─→ rabbitTemplate.convertAndSend() ───→ RabbitMQ (fila: triagem)
                                             └─→ Mensagem JSON
```

### 2️⃣ Triagem de Paciente

```
RabbitMQ (fila: triagem)
  │
  │ Message: Paciente JSON
  ↓
TriagemListener.receberPaciente()
  │
  ↓
TriagemService.processarTriagem()
  │
  ├─→ Análise de sintomas
  │   │
  │   ├─ "Infarto" ou "Pressão alta" 
  │   │   └─→ medicoResponsavel = "Cardiologista"
  │   │       routingKey = "paciente.cardiologia"
  │   │
  │   ├─ "Dor de cabeça" ou "AVC"
  │   │   └─→ medicoResponsavel = "Neurologista"
  │   │       routingKey = "paciente.neurologia"
  │   │
  │   └─ Outros sintomas
  │       └─→ medicoResponsavel = "Clínico Geral"
  │           routingKey = "paciente.geral"
  │
  ├─→ paciente.setStatus("AGUARDANDO_CONSULTA")
  ├─→ pacienteRepository.save() ───→ AWS RDS PostgreSQL
  │
  └─→ rabbitTemplate.convertAndSend() ───→ RabbitMQ (fila especializada)
```

### 3️⃣ Atendimento Médico (Exemplo: Cardiologista)

```
RabbitMQ (fila: cardiologia)
  │
  │ Message: Paciente JSON
  ↓
CardiologistaListener.receberPaciente()
  │
  ↓
CardiologistaService.atenderPaciente()
  │
  ├─→ Thread.sleep(10000)  // Simula consulta
  │
  ├─→ paciente.setStatus("FINALIZADO")
  ├─→ paciente.setDataConsulta(now)
  │
  └─→ pacienteRepository.save() ───→ AWS RDS PostgreSQL
                                      └─→ Paciente finalizado
```

### 4️⃣ Consulta de Dados (API REST)

```
Cliente
  │
  │ GET /api/cardiologista/pacientes/atendidos
  ↓
CardiologistaController.listarPacientesAtendidos()
  │
  ↓
CardiologistaService.listarPacientesAtendidos()
  │
  └─→ pacienteRepository.findByStatus("FINALIZADO")
      │
      ↓
    AWS RDS PostgreSQL
      │
      │ SELECT * FROM pacientes WHERE status = 'FINALIZADO'
      ↓
    List<Paciente>
      │
      ↓
    ResponseEntity<List<Paciente>> ───→ Cliente (JSON)
```

## 🎯 Padrões de Integração

### Pattern 1: Request/Response (Síncrono)
```
Cliente ──HTTP──→ API REST ──JPA──→ Database
                    ↓
              Response JSON
```

### Pattern 2: Message Queue (Assíncrono)
```
Producer ──AMQP──→ RabbitMQ ──AMQP──→ Consumer
                     (Queue)
```

### Pattern 3: Event-Driven (Triagem)
```
Evento: Paciente Cadastrado
  ↓
Triagem escuta e processa
  ↓
Evento: Paciente Triado
  ↓
Especialista escuta e atende
  ↓
Evento: Paciente Atendido
```

## 📊 Estado do Paciente (State Machine)

```
┌─────────────────────┐
│ Paciente Cadastrado │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│ AGUARDANDO_TRIAGEM  │  ← Status inicial (Recepcionista)
└──────────┬──────────┘
           │ RabbitMQ (triagem)
           ↓
┌─────────────────────┐
│   EM_TRIAGEM        │  ← Processando triagem
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│AGUARDANDO_CONSULTA  │  ← Triado, aguardando médico
└──────────┬──────────┘
           │ RabbitMQ (especialista)
           ↓
┌─────────────────────┐
│   EM_CONSULTA       │  ← Sendo atendido
└──────────┬──────────┘
           │ Thread.sleep(10s)
           ↓
┌─────────────────────┐
│    FINALIZADO       │  ← Consulta concluída
└─────────────────────┘
```

## 🗄️ Modelo de Dados (Entidade Paciente)

```
┌─────────────────────────────────────┐
│           PACIENTE                   │
├─────────────────────────────────────┤
│ id (PK, Auto)        : Long          │
│ nome                 : String        │
│ sexoBiologico        : String        │
│ dataNascimento       : String        │
│ tipoSanguineo        : String        │
│ estadoCivil          : String        │
│ sintomas             : Text          │
│ medicoResponsavel    : String        │
│ dataCadastro         : LocalDateTime │
│ dataConsulta         : LocalDateTime │
│ status               : String        │
└─────────────────────────────────────┘
```

## 🔐 Pontos de Acesso

### APIs REST (HTTP)
- 🟢 `POST   /api/recepcionista/pacientes` - Criar
- 🔵 `GET    /api/recepcionista/pacientes` - Listar
- 🔵 `GET    /api/recepcionista/pacientes/{id}` - Buscar
- 🔵 `GET    /api/{especialista}/pacientes/atendidos` - Atendidos
- 🟡 `GET    /api/{especialista}/health` - Health Check

### RabbitMQ (AMQP)
- 📨 `triagem` - Fila de triagem
- 📨 `cardiologia` - Fila de cardiologia
- 📨 `neurologia` - Fila de neurologia
- 📨 `geral` - Fila de clínico geral
- 📨 `fallbackCardio`, `fallbackNeuro`, `fallbackGeral` - Fallbacks

### PostgreSQL (JDBC)
- 🗄️ `AWS RDS PostgreSQL` - Database único compartilhado
  - Host: `distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com`
  - Database: `postgres`
  - Tabela: `pacientes` (comum a todos os serviços)

## 🎨 Tecnologias por Camada

### Frontend Layer
- REST Client, Postman, cURL, ou futuro React/Angular

### API Layer (Controller)
- Spring Web MVC
- Jackson (JSON)
- Bean Validation

### Service Layer (Business Logic)
- Spring Core
- Spring AMQP (RabbitMQ)
- SLF4J (Logging)

### Data Layer (Repository)
- Spring Data JPA
- Hibernate
- PostgreSQL Driver

### Infrastructure
- RabbitMQ (Message Broker)
- AWS RDS PostgreSQL (RDBMS - Database Único)
- Docker (para RabbitMQ)

## 📈 Escalabilidade

Cada microserviço pode escalar independentemente:

```
Recepcionista x 1-3 instâncias (alta demanda de cadastro)
    ↓
Triagem x 1-2 instâncias (processamento rápido)
    ↓
Cardiologista x 3-5 instâncias (consultas demoradas)
Neurologista x 2-3 instâncias
Clínico Geral x 4-6 instâncias (maior volume)
```

## 🔄 Resiliência

### Retry Logic
- Conexões RabbitMQ com retry automático
- Transações com rollback automático

### Fallback Queues
- Mensagens não processadas vão para fallback
- Reprocessamento manual possível

### Circuit Breaker (Futuro)
- Pode adicionar Resilience4j
- Protege contra cascading failures

---

**Este diagrama representa a arquitetura completa implementada.**
**Cada caixa representa um componente real do sistema.**

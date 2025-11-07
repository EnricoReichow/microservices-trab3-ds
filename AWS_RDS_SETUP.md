# 🌩️ Configuração AWS RDS para Sistema Hospitalar

Este guia documenta a configuração do PostgreSQL no AWS RDS para o sistema hospitalar.

## 📋 Configuração Atual

O sistema utiliza uma **instância única AWS RDS PostgreSQL** com um **database compartilhado** por todos os microserviços.

### Detalhes da Instância RDS

| Propriedade | Valor |
|-------------|-------|
| **Endpoint** | `distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com` |
| **Porta** | `5432` |
| **Database** | `postgres` |
| **Username** | `admin` |
| **Password** | `senha` |
| **Região** | `sa-east-1` (São Paulo) |

### Tabela Compartilhada

Todos os serviços utilizam a mesma tabela `pacientes`:

```sql
CREATE TABLE pacientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sexo_biologico VARCHAR(50),
    data_nascimento VARCHAR(50),
    tipo_sanguineo VARCHAR(10),
    estado_civil VARCHAR(50),
    sintomas TEXT,
    medico_responsavel VARCHAR(100),
    data_cadastro TIMESTAMP,
    data_consulta TIMESTAMP,
    status VARCHAR(50)
);
```

## ⚙️ Configuração nos Microserviços

Todos os serviços estão configurados com as mesmas credenciais no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com:5432/postgres
spring.datasource.username=admin
spring.datasource.password=senha
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Serviços Conectados

- ✅ recepcionista-service (porta 8081)
- ✅ triagem-service (porta 8082)  
- ✅ cardiologista-service (porta 8083)
- ✅ clinicogeral-service (porta 8084)
- ✅ neurologista-service (porta 8085)

## 🧪 Testar Conexão

### Via psql (linha de comando)
```bash
psql -h distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com \
     -U admin \
     -d postgres \
     -p 5432
```

### Via DBeaver/pgAdmin
1. Host: `distributed-system-db-project.cr46koqmcn11.sa-east-1.rds.amazonaws.com`
2. Port: `5432`
3. Database: `postgres`
4. Username: `postgres`
5. Password: `paproject987#`

### Consultas Úteis

```sql
-- Ver todos os pacientes
SELECT * FROM pacientes ORDER BY data_cadastro DESC;

-- Ver pacientes por médico
SELECT nome, sintomas, medico_responsavel, status 
FROM pacientes 
WHERE medico_responsavel = 'Cardiologista';

-- Contar pacientes por status
SELECT status, COUNT(*) 
FROM pacientes 
GROUP BY status;

-- Ver pacientes atendidos hoje
SELECT nome, medico_responsavel, data_consulta 
FROM pacientes 
WHERE DATE(data_consulta) = CURRENT_DATE;
```

## � Boas Práticas de Segurança

1. **Security Group configurado**
   - Porta 5432 liberada para acesso necessário
   - Restrinja IPs em produção

2. **Credenciais seguras**
   - Username: `admin`
   - Password: Protegida e não commitada no Git

3. **Backups automáticos**
   - AWS RDS gerencia backups automaticamente
   - Snapshots disponíveis para restore

## 📊 Monitoramento

### CloudWatch Metrics
- CPU utilization
- Database connections
- Storage space
- Read/Write IOPS

## 📚 Referências

- [AWS RDS PostgreSQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Spring Boot Database Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.data)

---

**💡 Nota:** Esta configuração utiliza um database único compartilhado por todos os microserviços, simplificando a gestão e reduzindo custos enquanto mantém a independência dos serviços via RabbitMQ.

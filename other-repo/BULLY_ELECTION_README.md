# Serviço de Triagem com Eleição de Líder (Algoritmo Bully)

## 📋 Visão Geral

O serviço de triagem agora implementa o **Algoritmo de Bully** para eleição de líder distribuído. Isso garante que:

- ✅ **Apenas o coordenador eleito** processa mensagens da fila de triagem
- ✅ **Eleição automática** quando o líder atual falha
- ✅ **Alta disponibilidade** através de múltiplas instâncias em standby
- ✅ **Processo com maior ID** é sempre eleito como coordenador

## 🚀 Como Executar

### Pré-requisitos

1. **RabbitMQ** rodando (padrão: localhost, user/password)
2. **Java 17+** instalado
3. Projeto compilado: `mvn clean compile`

### Executando Múltiplas Instâncias

Cada instância precisa de **2 argumentos obrigatórios**:

```bash
java -cp target/classes TriagemSender.TriagemSenderMain <PROCESS_ID> "<KNOWN_IDS>"
```

**Parâmetros:**
- `PROCESS_ID`: ID único e incremental deste processo (ex: 1, 2, 3, 4, 5)
- `KNOWN_IDS`: Lista de todos os IDs conhecidos, separados por vírgula (ex: "1,2,3,4,5")

### Exemplo com 3 Instâncias

**Terminal 1 - Processo ID 1:**
```bash
java -cp target/classes TriagemSender.TriagemSenderMain 1 "1,2,3"
```

**Terminal 2 - Processo ID 2:**
```bash
java -cp target/classes TriagemSender.TriagemSenderMain 2 "1,2,3"
```

**Terminal 3 - Processo ID 3 (será eleito líder por ter maior ID):**
```bash
java -cp target/classes TriagemSender.TriagemSenderMain 3 "1,2,3"
```

### Exemplo com 5 Instâncias

```bash
# Terminal 1
java -cp target/classes TriagemSender.TriagemSenderMain 1 "1,2,3,4,5"

# Terminal 2
java -cp target/classes TriagemSender.TriagemSenderMain 2 "1,2,3,4,5"

# Terminal 3
java -cp target/classes TriagemSender.TriagemSenderMain 3 "1,2,3,4,5"

# Terminal 4
java -cp target/classes TriagemSender.TriagemSenderMain 4 "1,2,3,4,5"

# Terminal 5 (será eleito líder)
java -cp target/classes TriagemSender.TriagemSenderMain 5 "1,2,3,4,5"
```

## 🔄 Funcionamento do Algoritmo Bully

### 1. Inicialização
- Cada processo se conecta ao RabbitMQ
- Inicia processo de eleição automaticamente
- O processo com **maior ID** é eleito coordenador

### 2. Operação Normal
- ✅ **Coordenador**: Consome mensagens da fila de triagem e as roteia
- ⏸️ **Não-líderes**: Ficam em standby, apenas monitorando heartbeats

### 3. Detecção de Falha
- Coordenador envia **heartbeat a cada 2 segundos**
- Se não houver heartbeat por **6 segundos**, detecta-se falha
- Processo que detectar falha inicia nova eleição

### 4. Processo de Eleição

```
1. Processo detecta falha do líder
2. Envia mensagem ELECTION para processos com ID maior
3. Aguarda respostas OK por 3 segundos
4. Se receber OK: aguarda anúncio de novo coordenador
5. Se NÃO receber OK: declara-se coordenador
6. Novo coordenador envia COORDINATOR para todos
```

## 🧪 Testando o Sistema

### Teste 1: Eleição Inicial
1. Inicie 3 processos (IDs 1, 2, 3)
2. Observe que o processo **ID 3** é eleito coordenador
3. Apenas o processo 3 deve mostrar mensagens de processamento da triagem

### Teste 2: Falha do Líder
1. Com os 3 processos rodando, **mate o processo ID 3** (Ctrl+C)
2. Aguarde ~6 segundos
3. Observe que o processo **ID 2** detecta a falha e inicia eleição
4. Processo 2 se torna o novo coordenador
5. Processo 2 começa a processar a fila de triagem

### Teste 3: Retorno do Processo com Maior ID
1. Com processo 2 como líder, **reinicie o processo 3**
2. Processo 3 inicia eleição ao entrar
3. Processo 3 se torna coordenador novamente
4. Processo 2 para de consumir e volta para standby

### Teste 4: Múltiplas Falhas Sequenciais
1. Inicie 5 processos (IDs 1-5)
2. Processo 5 é eleito
3. Mate processo 5 → Processo 4 assume
4. Mate processo 4 → Processo 3 assume
5. Mate processo 3 → Processo 2 assume
6. Mate processo 2 → Processo 1 assume (último sobrevivente)

## 📊 Logs e Monitoramento

### Mensagens Importantes

```
[BULLY] Processo X iniciado. IDs conhecidos: [1, 2, 3]
[BULLY] ⚡ Processo X INICIANDO ELEIÇÃO ⚡
[BULLY] Processo X recebeu ELECTION de Y
[BULLY] Processo X recebeu OK de Y
[BULLY] Processo X recebeu COORDINATOR de Y
[BULLY] ★★★ Processo X É O NOVO COORDENADOR ★★★
[BULLY] ❤️ Coordenador X enviou heartbeat
[BULLY] ⚠️ LÍDER X FALHOU! Iniciando eleição...

[TRIAGEM] ✅ INICIOU consumo da fila de triagem
[TRIAGEM] ❌ PAROU consumo da fila de triagem
```

## 🏗️ Arquitetura

### Comunicação RabbitMQ

**Exchanges criadas:**
- `election_exchange` (fanout) - Mensagens de eleição (ELECTION, OK, COORDINATOR)
- `heartbeat_exchange` (fanout) - Heartbeats do coordenador
- `exchangePA` (direct) - Roteamento de pacientes (já existia)

**Filas:**
- `triagem` - Fila principal (apenas coordenador consome)
- Filas temporárias exclusivas - Uma por processo para eleição/heartbeat

### Parâmetros de Configuração

No arquivo `BullyElection.java`:

```java
HEARTBEAT_INTERVAL_MS = 2000   // Coordenador envia heartbeat a cada 2s
HEARTBEAT_TIMEOUT_MS = 6000    // Considera líder morto após 6s sem heartbeat
ELECTION_TIMEOUT_MS = 3000     // Timeout para resposta de eleição
```

## ⚙️ Compatibilidade

- ✅ **Linux** (testado)
- ✅ **Windows** (compatível, use os mesmos comandos Java)
- ✅ **macOS** (compatível)

A implementação usa apenas RabbitMQ para comunicação entre processos, então funciona em qualquer SO que rode Java 17+.

## 🔧 Troubleshooting

### Problema: "Canal RabbitMQ não está inicializado"
**Solução:** Certifique-se que o RabbitMQ está rodando em localhost

### Problema: Eleição em loop infinito
**Solução:** Verifique se todos os processos têm a mesma lista de IDs conhecidos

### Problema: Dois líderes ao mesmo tempo
**Solução:** Isso pode ocorrer brevemente durante transições. Aguarde alguns segundos, o algoritmo se auto-corrige

### Problema: Processo não detecta falha do líder
**Solução:** Verifique os logs de heartbeat. Pode ser latência de rede ou RabbitMQ sobrecarregado

## 📝 Notas Importantes

1. **IDs devem ser únicos e incrementais** (1, 2, 3, 4, 5...)
2. **Todos os processos devem conhecer a mesma lista de IDs**
3. **Fila de triagem continua existindo** - recepcionista ainda envia para ela
4. **Outros serviços não são afetados** - Cardiologia, Neurologia, Clínico Geral continuam funcionando normalmente
5. **Mensagens não se perdem** - Se líder cai, mensagens ficam na fila até novo líder assumir

## 🎯 Benefícios

- 🚀 **Escalabilidade**: Múltiplas instâncias em standby prontas para assumir
- 🛡️ **Alta disponibilidade**: Sistema continua funcionando se líder falhar
- ⚡ **Failover automático**: Novo líder é eleito automaticamente (~6-9 segundos)
- 🔄 **Sem perda de dados**: Mensagens ficam na fila durante transição
- 📊 **Distribuição de carga**: Quando líder volta, pode assumir novamente

---

**Implementado por:** GitHub Copilot  
**Algoritmo:** Bully Election Algorithm  
**Data:** Novembro 2025


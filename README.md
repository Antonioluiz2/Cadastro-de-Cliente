# Cliente API - Sistema de Cadastro de Clientes

Sistema de microserviços para gerenciamento de clientes com arquitetura orientada a eventos usando Apache Kafka.

## 🚀 Tecnologias

- **Java 23**
- **Spring Boot 3.3.11**
- **Apache Kafka 7.6.0**
- **H2 Database** (em memória)
- **Docker & Docker Compose**
- **Maven**
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)

## 📋 Pré-requisitos

- Docker e Docker Compose instalados
- Java 23 ou superior (para desenvolvimento local)
- Maven 3.8+ (para desenvolvimento local)
- 8GB RAM disponível (mínimo recomendado)

## 🏗️ Arquitetura

O sistema é composto por 3 componentes principais:

1. **API REST** (`cliente-api`) - Porta 8080
   - Gerencia operações CRUD de clientes
   - Produz eventos para Kafka
   - Banco de dados H2 em memória

2. **Worker Kafka** (`cliente-worker`)
   - Consome eventos do Kafka
   - Processa e transforma dados
   - Salva registros em arquivo e memória

3. **Apache Kafka** - Porta 9092
   - Broker de mensagens
   - Tópico: `cliente-events`

## 🔧 Configuração e Execução

### Usando Docker Compose (Recomendado)

1. Clone o repositório e navegue até a pasta do projeto

2. Inicie todos os serviços:
```bash
docker-compose up -d
```

3. Verifique se os serviços estão rodando:
```bash
docker-compose ps
```

4. Acesse os serviços:
   - API REST: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Kafka UI: http://localhost:8090
   - H2 Console: http://localhost:8080/h2-console

### Desenvolvimento Local

#### API REST

```bash
cd api-rest
mvn clean install
mvn spring-boot:run
```

#### Worker Kafka

```bash
cd kafka-worker
mvn clean install
mvn spring-boot:run
```

## 📡 Endpoints da API

### Criar Cliente (POST)
```http
POST /api/v1/clientes
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao.silva@email.com",
  "cpf": "12345678901",
  "telefone": "11987654321",
  "endereco": "Rua Exemplo, 123",
  "status": "ATIVO"
}
```

### Buscar Cliente por ID (GET)
```http
GET /api/v1/clientes/{id}
```

### Listar Todos os Clientes (GET)
```http
GET /api/v1/clientes
```

### Listar Clientes por Status (GET)
```http
GET /api/v1/clientes?status=ATIVO
```

### Atualizar Cliente (PUT)
```http
PUT /api/v1/clientes/{id}
Content-Type: application/json

{
  "nome": "João Silva Atualizado",
  "email": "joao.silva@email.com",
  "cpf": "12345678901",
  "telefone": "11987654321",
  "endereco": "Rua Nova, 456",
  "status": "ATIVO"
}
```

### Deletar Cliente (DELETE)
```http
DELETE /api/v1/clientes/{id}
```

## 🔍 Status dos Clientes

- `ATIVO` - Cliente ativo no sistema
- `INATIVO` - Cliente inativo
- `BLOQUEADO` - Cliente bloqueado

## 📊 Processamento do Worker

O Worker Kafka realiza as seguintes transformações:

1. **Nome**: Converte para MAIÚSCULAS
2. **Email**: Converte para minúsculas
3. **CPF**: Remove formatação (apenas números)
4. **Telefone**: Remove formatação (apenas números)
5. **Endereço**: Converte para Title Case

Os eventos processados são:
- Salvos em memória (HashMap)
- Persistidos em arquivos JSON no diretório `./data/processed`

## 📝 Eventos Kafka

### Tópico: `cliente-events`

**Tipos de Eventos:**
- `CLIENTE_CRIADO`
- `CLIENTE_ATUALIZADO`
- `CLIENTE_DELETADO`

**Estrutura do Evento:**
```json
{
  "eventType": "CLIENTE_CRIADO",
  "clienteId": 1,
  "nome": "João Silva",
  "email": "joao.silva@email.com",
  "cpf": "12345678901",
  "telefone": "11987654321",
  "endereco": "Rua Exemplo, 123",
  "status": "ATIVO",
  "timestamp": "2026-02-08T10:30:00"
}
```

## 🧪 Testando a Aplicação

### Teste Completo de Fluxo

1. **Criar um cliente:**
```bash
curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "email": "maria.santos@email.com",
    "cpf": "98765432100",
    "telefone": "(11) 98765-4321",
    "endereco": "avenida paulista, 1000"
  }'
```

2. **Verificar logs do Worker:**
```bash
docker-compose logs -f cliente-worker
```

3. **Listar clientes:**
```bash
curl http://localhost:8080/api/v1/clientes
```

4. **Verificar arquivo processado:**
```bash
docker exec cliente-worker ls -la /app/data/processed/
```

## 🛠️ Comandos Úteis

### Docker Compose

```bash
# Iniciar serviços
docker-compose up -d

# Parar serviços
docker-compose down

# Ver logs
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f cliente-api
docker-compose logs -f cliente-worker

# Rebuild e restart
docker-compose up -d --build

# Remover volumes (limpar dados)
docker-compose down -v
```

### Maven

```bash
# Compilar
mvn clean install

# Executar testes
mvn test

# Gerar JAR
mvn package

# Pular testes
mvn clean install -DskipTests
```

## 🐛 Troubleshooting

### Kafka não conecta
- Verifique se o Zookeeper está rodando: `docker-compose logs zookeeper`
- Aguarde alguns segundos após iniciar os serviços
- Reinicie os containers: `docker-compose restart`

### API não responde
- Verifique logs: `docker-compose logs cliente-api`
- Confirme se a porta 8080 está disponível
- Verifique health check: `curl http://localhost:8080/actuator/health`

### Worker não consome mensagens
- Verifique conexão com Kafka: `docker-compose logs cliente-worker`
- Confirme criação do tópico no Kafka UI
- Verifique consumer group no Kafka UI

## 📦 Estrutura do Projeto

```
Cadastro de clientes/
├── api-rest/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/testetecnico/cliente/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── domain/
│   │       │   ├── exception/
│   │       │   ├── repository/
│   │       │   └── service/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
├── kafka-worker/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/testetecnico/worker/
│   │       │   ├── config/
│   │       │   ├── consumer/
│   │       │   ├── dto/
│   │       │   └── service/
│   │       └── resources/
│   ├── Dockerfile
│   └── pom.xml
└── docker-compose.yml
```
## � Documentação Completa

Este projeto possui documentação extensiva para facilitar o entendimento e uso:

### 📖 Guias de Uso

- **[QUICKSTART.md](QUICKSTART.md)** - Guia rápido de 5 minutos para começar
- **[EXEMPLOS-REQUISICOES.md](docs/EXEMPLOS-REQUISICOES.md)** - Exemplos práticos de uso da API

### 🏗️ Arquitetura e Implementação

- **[C4-DIAGRAMAS.md](docs/C4-DIAGRAMAS.md)** - Diagramas C4 Model completos (4 níveis)
- **[IMPLEMENTACAO.md](docs/IMPLEMENTACAO.md)** - Guia detalhado de implementação
- **[INVENTARIO-TO-BE.md](docs/INVENTARIO-TO-BE.md)** - Inventário TO-BE da solução

### 📊 Visão Geral

- **[RESUMO-EXECUTIVO.md](RESUMO-EXECUTIVO.md)** - Resumo executivo do projeto
- **[ESTRUTURA-PROJETO.md](ESTRUTURA-PROJETO.md)** - Estrutura completa de arquivos e diretórios

## 🎯 Links Rápidos

| Documentação | Descrição |
|-------------|-----------|
| [Início Rápido](QUICKSTART.md) | Como começar em 5 minutos |
| [Diagramas C4](docs/C4-DIAGRAMAS.md) | Arquitetura visual completa |
| [Guia de Implementação](docs/IMPLEMENTACAO.md) | Detalhes técnicos profundos |
| [Exemplos de Uso](docs/EXEMPLOS-REQUISICOES.md) | cURL, Postman, Python |
| [Testes Unitários](TESTES-UNITARIOS.md) | 83 testes implementados |
| [Inventário TO-BE](docs/INVENTARIO-TO-BE.md) | Especificações completas |
| [Resumo Executivo](RESUMO-EXECUTIVO.md) | Visão geral do projeto |

## 📄 Licença

Este projeto foi desenvolvido para fins de avaliação técnica.


Sistema de Cadastro de Clientes

**Versão:** 1.0.0  
**Data:** Fevereiro 2026  
**Autor:** Antonio Luiz

---

*Desenvolvido por Antonio Luiz usando Java 23, Spring Boot 3.3.11 e Apache Kafka*
```


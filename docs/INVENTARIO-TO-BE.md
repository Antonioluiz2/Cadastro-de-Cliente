# Inventário TO-BE - Sistema de Cadastro de Clientes

## 📋 Visão Geral

Este documento descreve o inventário completo da solução TO-BE (estado futuro) do Sistema de Cadastro de Clientes, incluindo todos os componentes, tecnologias, infraestrutura e recursos necessários.

---

## 🏗️ Arquitetura TO-BE

### Padrões Arquiteturais Implementados

| Padrão | Descrição | Aplicação |
|--------|-----------|-----------|
| **Microserviços** | Serviços independentes e desacoplados | API REST + Worker Kafka como serviços separados |
| **Event-Driven Architecture** | Arquitetura orientada a eventos | Comunicação assíncrona via Apache Kafka |
| **RESTful API** | APIs seguindo princípios REST | Endpoints HTTP com métodos semânticos |
| **Layered Architecture** | Separação em camadas | Controller → Service → Repository |
| **Repository Pattern** | Abstração de acesso a dados | Spring Data JPA Repositories |
| **DTO Pattern** | Objetos de transferência de dados | Separação entre entidades e DTOs |

---

## 🖥️ Componentes de Software

### 1. API REST (cliente-api)

#### Características Técnicas
- **Linguagem:** Java 23
- **Framework:** Spring Boot 3.3.11
- **Build Tool:** Maven 3.8+
- **Porta:** 8080
- **Database:** H2 (in-memory)

#### Dependências Principais
| Dependência | Versão | Propósito |
|------------|--------|-----------|
| spring-boot-starter-web | 3.3.11 | Framework web REST |
| spring-boot-starter-data-jpa | 3.3.11 | Persistência de dados |
| spring-kafka | 3.3.11 | Integração com Kafka |
| h2database | runtime | Banco de dados em memória |
| lombok | optional | Redução de boilerplate |
| springdoc-openapi | 2.3.0 | Documentação OpenAPI/Swagger |
| spring-boot-starter-actuator | 3.3.11 | Monitoramento e health checks |

#### Estrutura de Pacotes
```
com.testetecnico.cliente
├── config/             # Configurações (Kafka, etc)
├── controller/         # Controladores REST
├── domain/
│   ├── dto/           # Data Transfer Objects
│   └── entity/        # Entidades JPA
├── exception/         # Exceções customizadas
├── repository/        # Repositórios JPA
└── service/           # Lógica de negócio
```

#### Endpoints Implementados

| Método | Endpoint | Descrição | Status Code |
|--------|----------|-----------|-------------|
| POST | `/api/v1/clientes` | Criar novo cliente | 201 Created |
| GET | `/api/v1/clientes` | Listar todos os clientes | 200 OK |
| GET | `/api/v1/clientes?status={status}` | Filtrar por status | 200 OK |
| GET | `/api/v1/clientes/{id}` | Buscar cliente por ID | 200 OK / 404 Not Found |
| PUT | `/api/v1/clientes/{id}` | Atualizar cliente | 200 OK / 404 Not Found |
| DELETE | `/api/v1/clientes/{id}` | Deletar cliente | 204 No Content / 404 Not Found |

#### Entidades de Domínio

**Cliente**
- `id` (Long) - Identificador único
- `nome` (String) - Nome completo
- `email` (String) - Email único
- `cpf` (String) - CPF único
- `telefone` (String) - Telefone de contato
- `endereco` (String) - Endereço
- `status` (Enum) - Status do cliente (ATIVO, INATIVO, BLOQUEADO)
- `dataCriacao` (LocalDateTime) - Data de criação
- `dataAtualizacao` (LocalDateTime) - Data da última atualização

#### Eventos Kafka Produzidos

| Evento | Trigger | Payload |
|--------|---------|---------|
| CLIENTE_CRIADO | POST bem-sucedido | ClienteEventDTO |
| CLIENTE_ATUALIZADO | PUT bem-sucedido | ClienteEventDTO |
| CLIENTE_DELETADO | DELETE bem-sucedido | ClienteEventDTO |

### 2. Worker Kafka (cliente-worker)

#### Características Técnicas
- **Linguagem:** Java 23
- **Framework:** Spring Boot 3.3.11
- **Build Tool:** Maven 3.8+
- **Consumer Group:** cliente-worker-group

#### Dependências Principais
| Dependência | Versão | Propósito |
|------------|--------|-----------|
| spring-boot-starter | 3.3.11 | Core Spring Boot |
| spring-kafka | 3.3.11 | Consumer Kafka |
| jackson-databind | latest | Serialização JSON |
| lombok | optional | Redução de boilerplate |

#### Estrutura de Pacotes
```
com.testetecnico.worker
├── config/             # Configurações do Consumer
├── consumer/           # Kafka Listeners
├── dto/               # Data Transfer Objects
└── service/           # Processamento de eventos
```

#### Processamento de Eventos

**Transformações Aplicadas:**
1. **Nome:** Conversão para MAIÚSCULAS
2. **Email:** Conversão para minúsculas
3. **CPF:** Remoção de formatação (apenas dígitos)
4. **Telefone:** Remoção de formatação (apenas dígitos)
5. **Endereço:** Conversão para Title Case

**Armazenamento:**
- In-Memory: HashMap com eventos processados
- File System: JSON files em `/data/processed/`

#### Configurações do Consumer

| Configuração | Valor | Descrição |
|-------------|-------|-----------|
| Group ID | cliente-worker-group | Identificador do grupo |
| Auto Offset Reset | earliest | Começa do início do tópico |
| Concurrency | 3 | 3 threads paralelas |
| Ack Mode | manual | Confirmação manual de mensagens |

---

## 🔧 Infraestrutura

### 3. Apache Kafka

#### Especificações
- **Versão:** 7.6.0 (Confluent Platform)
- **Imagem Docker:** confluentinc/cp-kafka:7.6.0
- **Portas:**
  - 9092 (host)
  - 29092 (interna, container)

#### Tópicos

| Nome | Partições | Réplicas | Propósito |
|------|-----------|----------|-----------|
| cliente-events | 3 | 1 | Eventos de clientes |

#### Configurações
- **Broker ID:** 1
- **Auto Create Topics:** Habilitado
- **Replication Factor:** 1 (desenvolvimento)

### 4. Apache Zookeeper

#### Especificações
- **Versão:** 7.6.0 (Confluent Platform)
- **Imagem Docker:** confluentinc/cp-zookeeper:7.6.0
- **Porta:** 2181

#### Configurações
- **Client Port:** 2181
- **Tick Time:** 2000ms

### 5. Kafka UI

#### Especificações
- **Imagem Docker:** provectuslabs/kafka-ui:latest
- **Porta:** 8090

#### Funcionalidades
- Visualização de tópicos
- Monitoramento de mensagens
- Gerenciamento de consumer groups
- Análise de performance

### 6. H2 Database

#### Especificações
- **Modo:** In-Memory
- **URL:** jdbc:h2:mem:clientedb
- **Console:** Habilitado em /h2-console
- **Dialect:** H2Dialect

#### Tabelas

**clientes**
| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| nome | VARCHAR(100) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| cpf | VARCHAR(14) | NOT NULL, UNIQUE |
| telefone | VARCHAR(15) | NULL |
| endereco | VARCHAR(200) | NULL |
| status | VARCHAR(20) | NOT NULL |
| data_criacao | TIMESTAMP | NOT NULL |
| data_atualizacao | TIMESTAMP | NULL |

---

## 🐳 Containerização

### Imagens Docker

| Serviço | Base Image | Tamanho Estimado |
|---------|------------|------------------|
| cliente-api | eclipse-temurin:23-jre-alpine | ~200MB |
| cliente-worker | eclipse-temurin:23-jre-alpine | ~180MB |
| kafka | confluentinc/cp-kafka:7.6.0 | ~800MB |
| zookeeper | confluentinc/cp-zookeeper:7.6.0 | ~800MB |
| kafka-ui | provectuslabs/kafka-ui:latest | ~150MB |

### Volumes

| Volume | Propósito | Mountpoint |
|--------|-----------|------------|
| worker-data | Persistência de eventos processados | /app/data |

### Rede

- **Nome:** cliente-network
- **Driver:** bridge
- **Comunicação:** Todos os containers na mesma rede

---

## 📊 Requisitos de Sistema

### Desenvolvimento Local

| Recurso | Mínimo | Recomendado |
|---------|--------|-------------|
| RAM | 4GB | 8GB |
| CPU | 2 cores | 4 cores |
| Disco | 5GB | 10GB |
| Java | 23 | 23 |
| Docker | 20.x | Latest |
| Docker Compose | 2.x | Latest |

### Produção (Estimativa)

| Componente | RAM | CPU | Disco |
|-----------|-----|-----|-------|
| API REST | 512MB | 1 core | 1GB |
| Worker Kafka | 512MB | 1 core | 5GB |
| Kafka | 2GB | 2 cores | 20GB |
| Zookeeper | 512MB | 1 core | 2GB |
| **Total** | **3.5GB** | **5 cores** | **28GB** |

---

## 🔐 Segurança

### Implementado

| Aspecto | Implementação |
|---------|---------------|
| Validação de Dados | Bean Validation (Jakarta) |
| Tratamento de Erros | GlobalExceptionHandler |
| Input Sanitization | @Valid annotations |

### Recomendações para Produção

| Aspecto | Recomendação |
|---------|--------------|
| Autenticação | OAuth2 / JWT |
| Autorização | Spring Security + RBAC |
| HTTPS | TLS 1.3 |
| Kafka Security | SASL/SSL |
| Database | PostgreSQL/MySQL com SSL |
| Secrets Management | Vault / AWS Secrets Manager |
| Rate Limiting | API Gateway |
| CORS | Configuração restritiva |

---

## 📈 Monitoramento e Observabilidade

### Health Checks

| Endpoint | Propósito |
|----------|-----------|
| `/actuator/health` | Status geral da API |
| `/actuator/info` | Informações da aplicação |
| `/actuator/metrics` | Métricas de runtime |

### Logs

| Componente | Nível | Formato |
|-----------|-------|---------|
| API REST | INFO/DEBUG | JSON (produção) |
| Worker Kafka | INFO/DEBUG | JSON (produção) |
| Spring Kafka | INFO | JSON (produção) |

### Recomendações TO-BE

| Ferramenta | Propósito |
|-----------|-----------|
| Prometheus | Coleta de métricas |
| Grafana | Dashboards e visualização |
| ELK Stack | Centralização de logs |
| Jaeger / Zipkin | Distributed tracing |
| Alertmanager | Alertas proativos |

---

## 🧪 Testes

### Cobertura de Testes (Recomendada)

| Tipo | Cobertura | Ferramentas |
|------|-----------|-------------|
| Unitários | 80%+ | JUnit 5, Mockito |
| Integração | 60%+ | Spring Boot Test, Testcontainers |
| E2E | Críticos | REST Assured |
| Performance | Load Testing | JMeter, Gatling |

### Testes Implementáveis

```
api-rest/src/test/java/
├── controller/    # Testes de endpoints
├── service/       # Testes de lógica de negócio
├── repository/    # Testes de persistência
└── integration/   # Testes de integração

kafka-worker/src/test/java/
├── consumer/      # Testes do consumer
├── service/       # Testes de processamento
└── integration/   # Testes com Kafka embarcado
```

---

## 📦 Pipeline CI/CD (Sugerido)

### Estágios

| Estágio | Ações |
|---------|-------|
| **Build** | Maven compile, package |
| **Test** | Testes unitários e integração |
| **Quality** | SonarQube, Code Coverage |
| **Security** | OWASP Dependency Check, Trivy |
| **Docker Build** | Build de imagens |
| **Push** | Push para registry |
| **Deploy** | Deploy em ambiente |

### Ferramentas Sugeridas

- **CI/CD:** GitHub Actions, GitLab CI, Jenkins
- **Registry:** Docker Hub, Amazon ECR, Azure ACR
- **Orchestration:** Kubernetes, Docker Swarm
- **IaC:** Terraform, Pulumi

---

## 🚀 Deployment

### Estratégias

| Estratégia | Descrição | Uso |
|-----------|-----------|-----|
| **Rolling Update** | Atualização gradual | Produção |
| **Blue-Green** | Duas versões simultâneas | Releases críticas |
| **Canary** | Teste com subset de usuários | Features experimentais |

### Ambientes

| Ambiente | Propósito | Infraestrutura |
|----------|-----------|----------------|
| **Desenvolvimento** | Desenvolvimento local | Docker Compose |
| **Teste** | Testes automatizados | CI/CD + Docker |
| **Staging** | Validação pré-produção | Kubernetes/Cloud |
| **Produção** | Ambiente real | Kubernetes/Cloud com HA |

---

## 📝 Documentação

### Documentos Criados

| Documento | Localização | Propósito |
|-----------|-------------|-----------|
| README.md | raiz | Documentação geral |
| C4-DIAGRAMAS.md | docs/ | Diagramas arquiteturais |
| INVENTARIO-TO-BE.md | docs/ | Este documento |
| IMPLEMENTACAO.md | docs/ | Guia de implementação |

### APIs Documentadas

| Tipo | URL | Descrição |
|------|-----|-----------|
| Swagger UI | http://localhost:8080/swagger-ui.html | Interface interativa |
| OpenAPI JSON | http://localhost:8080/api-docs | Especificação OpenAPI |

---

## 🔄 Backup e Disaster Recovery

### Estratégia TO-BE

| Componente | Estratégia | RPO | RTO |
|-----------|-----------|-----|-----|
| Database | Snapshots automáticos | 1h | 4h |
| Kafka | Replicação cross-region | 5min | 1h |
| Configurações | GitOps | 0 | 30min |
| Volumes | Backup incremental | 24h | 2h |

---

## 📊 Métricas de Sucesso

### KPIs Técnicos

| Métrica | Target | Medição |
|---------|--------|---------|
| Disponibilidade | 99.9% | Uptime monitors |
| Latência P95 | < 200ms | APM tools |
| Taxa de Erro | < 0.1% | Logs/Metrics |
| Throughput | 1000 req/s | Load testing |
| Lag do Consumer | < 5s | Kafka metrics |

### SLOs Sugeridos

| Serviço | SLO | Descrição |
|---------|-----|-----------|
| API REST | 99.9% availability | < 8.7h downtime/ano |
| Kafka | 99.95% availability | < 4.4h downtime/ano |
| Worker | 99% processing | < 1% eventos perdidos |

---

## 🎯 Roadmap Futuro

### Fase 2 - Melhorias Planejadas

| Item | Prioridade | Complexidade |
|------|-----------|--------------|
| Autenticação OAuth2 | Alta | Média |
| Database PostgreSQL | Alta | Baixa |
| Cache Redis | Média | Média |
| API Gateway | Média | Alta |
| Service Mesh (Istio) | Baixa | Alta |
| GraphQL API | Baixa | Média |

### Fase 3 - Features Avançadas

- Machine Learning para análise de clientes
- Webhooks para integrações
- Multi-tenancy
- Event Sourcing completo
- CQRS pattern

---

## 📞 Contatos e Suporte

| Papel | Contato |
|-------|---------|
| Tech Lead | [contato@nexti.com] |
| DevOps | [devops@nexti.com] |
| Suporte | [suporte@nexti.com] |

---

**Versão:** 1.0.0  
**Data:** Fevereiro 2026  
**Status:** TO-BE (Estado Futuro Desejado)  
**Aprovação:** Pendente

---

*Este inventário TO-BE serve como referência para a implementação e evolução do Sistema de Cadastro de Clientes.*

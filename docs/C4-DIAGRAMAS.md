# Diagramas C4 Model - Sistema de Cadastro de Clientes

## Nível 1 - Diagrama de Contexto

```mermaid
graph TB
    subgraph "Sistema de Cadastro de Clientes"
        System[Sistema de Gerenciamento<br/>de Clientes]
    end
    
    User[Usuário/Aplicação Cliente]
    Admin[Administrador]
    
    User -->|Gerencia clientes via REST API| System
    Admin -->|Monitora e administra| System
    System -->|Envia eventos de negócio| System
    
    style System fill:#1168bd,stroke:#0b4884,color:#ffffff
    style User fill:#08427b,stroke:#052e56,color:#ffffff
    style Admin fill:#08427b,stroke:#052e56,color:#ffffff
```

## Nível 2 - Diagrama de Containers

```mermaid
graph TB
    User[Usuário/Cliente]
    Admin[Administrador]
    
    subgraph "Sistema de Cadastro de Clientes"
        API[API REST<br/>Spring Boot 3.3.11<br/>Java 23<br/>:8080]
        Worker[Worker Kafka<br/>Spring Boot 3.3.11<br/>Java 23]
        DB[(H2 Database<br/>In-Memory)]
        Kafka[Apache Kafka<br/>Message Broker<br/>:9092]
        KafkaUI[Kafka UI<br/>:8090]
        FileStorage[File System<br/>JSON Storage]
    end
    
    User -->|HTTPS/JSON| API
    Admin -->|Monitora| KafkaUI
    Admin -->|Acessa| API
    
    API -->|Lê/Escreve| DB
    API -->|Produz eventos| Kafka
    Worker -->|Consome eventos| Kafka
    Worker -->|Armazena em memória| Worker
    Worker -->|Persiste JSON| FileStorage
    KafkaUI -->|Monitora| Kafka
    
    style API fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Worker fill:#1168bd,stroke:#0b4884,color:#ffffff
    style DB fill:#438dd5,stroke:#2e6295,color:#ffffff
    style Kafka fill:#438dd5,stroke:#2e6295,color:#ffffff
    style KafkaUI fill:#85bbf0,stroke:#5d82a8,color:#000000
    style FileStorage fill:#438dd5,stroke:#2e6295,color:#ffffff
    style User fill:#08427b,stroke:#052e56,color:#ffffff
    style Admin fill:#08427b,stroke:#052e56,color:#ffffff
```

## Nível 3 - Diagrama de Componentes (API REST)

```mermaid
graph TB
    subgraph "API REST Container"
        Controller[ClienteController<br/>REST Endpoints]
        Service[ClienteService<br/>Business Logic]
        Repository[ClienteRepository<br/>JPA Repository]
        KafkaProducer[KafkaProducerService<br/>Event Producer]
        ExceptionHandler[GlobalExceptionHandler<br/>Error Handling]
        Config[KafkaConfig<br/>Topic Configuration]
        
        Controller --> Service
        Service --> Repository
        Service --> KafkaProducer
        Controller --> ExceptionHandler
        KafkaProducer --> Config
    end
    
    subgraph "External Systems"
        DB[(H2 Database)]
        Kafka[Apache Kafka]
    end
    
    Repository -->|JDBC| DB
    KafkaProducer -->|Produce Events| Kafka
    
    style Controller fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Service fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Repository fill:#1168bd,stroke:#0b4884,color:#ffffff
    style KafkaProducer fill:#1168bd,stroke:#0b4884,color:#ffffff
    style ExceptionHandler fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Config fill:#1168bd,stroke:#0b4884,color:#ffffff
    style DB fill:#438dd5,stroke:#2e6295,color:#ffffff
    style Kafka fill:#438dd5,stroke:#2e6295,color:#ffffff
```

## Nível 3 - Diagrama de Componentes (Worker Kafka)

```mermaid
graph TB
    subgraph "Worker Kafka Container"
        Consumer[ClienteEventConsumer<br/>Kafka Listener]
        Processor[EventProcessorService<br/>Event Processing]
        ConsumerConfig[KafkaConsumerConfig<br/>Consumer Configuration]
        MemoryStorage[In-Memory HashMap<br/>Processed Events Cache]
        
        Consumer --> Processor
        Consumer --> ConsumerConfig
        Processor --> MemoryStorage
        Processor --> FileWriter[File Writer<br/>JSON Persistence]
    end
    
    subgraph "External Systems"
        Kafka[Apache Kafka]
        FileSystem[File System<br/>/data/processed]
    end
    
    Kafka -->|Consume Events| Consumer
    FileWriter -->|Write JSON| FileSystem
    
    style Consumer fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Processor fill:#1168bd,stroke:#0b4884,color:#ffffff
    style ConsumerConfig fill:#1168bd,stroke:#0b4884,color:#ffffff
    style MemoryStorage fill:#1168bd,stroke:#0b4884,color:#ffffff
    style FileWriter fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Kafka fill:#438dd5,stroke:#2e6295,color:#ffffff
    style FileSystem fill:#438dd5,stroke:#2e6295,color:#ffffff
```

## Nível 4 - Diagrama de Código (Fluxo de Criação de Cliente)

```mermaid
sequenceDiagram
    participant Client as Cliente HTTP
    participant Controller as ClienteController
    participant Service as ClienteService
    participant Repository as ClienteRepository
    participant Kafka as KafkaProducerService
    participant DB as H2 Database
    participant Broker as Kafka Broker
    participant Worker as ClienteEventConsumer
    participant Processor as EventProcessorService
    participant File as File System
    
    Client->>Controller: POST /api/v1/clientes
    Controller->>Controller: @Valid validação
    Controller->>Service: criarCliente(request)
    
    Service->>Repository: existsByEmail(email)
    Repository->>DB: SELECT query
    DB-->>Repository: false
    Repository-->>Service: false
    
    Service->>Repository: existsByCpf(cpf)
    Repository->>DB: SELECT query
    DB-->>Repository: false
    Repository-->>Service: false
    
    Service->>Repository: save(cliente)
    Repository->>DB: INSERT query
    DB-->>Repository: Cliente entity
    Repository-->>Service: Cliente saved
    
    Service->>Kafka: enviarEvento(clienteEventDTO)
    Kafka->>Broker: send(topic, key, value)
    Broker-->>Kafka: SendResult
    
    Service-->>Controller: ClienteResponseDTO
    Controller-->>Client: 201 CREATED + JSON
    
    Note over Broker,Worker: Processamento Assíncrono
    
    Broker->>Worker: consume(ClienteEventDTO)
    Worker->>Processor: processarEvento(evento)
    
    Processor->>Processor: transformarEvento()
    Note over Processor: Nome → UPPERCASE<br/>Email → lowercase<br/>CPF → sem formatação
    
    Processor->>Processor: armazenarEmMemoria()
    Processor->>File: salvarEmArquivo()
    File-->>Processor: JSON saved
    
    Processor-->>Worker: ProcessedEventDTO
    Worker->>Broker: acknowledge()
```

## Diagrama de Fluxo de Eventos

```mermaid
stateDiagram-v2
    [*] --> ClienteCriado: POST /clientes
    [*] --> ClienteAtualizado: PUT /clientes/{id}
    [*] --> ClienteDeletado: DELETE /clientes/{id}
    
    ClienteCriado --> EventoKafka: Produz CLIENTE_CRIADO
    ClienteAtualizado --> EventoKafka: Produz CLIENTE_ATUALIZADO
    ClienteDeletado --> EventoKafka: Produz CLIENTE_DELETADO
    
    KafkaConsumer --> ProcessamentoWorker: Consume evento
    
    ProcessamentoWorker --> Transformacao: Aplica transformações
    Transformacao --> ArmazenamentoMemoria: Salva em HashMap
    ArmazenamentoMemoria --> PersistenciaArquivo: Salva JSON em disco
    PersistenciaArquivo --> [*]: Evento processado
```

## Diagrama de Deployment

```mermaid
graph TB
    subgraph "Docker Host"
        subgraph "Network: cliente-network"
            subgraph "Container: cliente-api"
                API[API REST<br/>Port 8080<br/>Java 23]
            end
            
            subgraph "Container: cliente-worker"
                Worker[Worker Kafka<br/>Java 23]
                Volume[Volume: worker-data]
            end
            
            subgraph "Container: kafka"
                KafkaBroker[Kafka Broker<br/>Port 9092, 29092]
            end
            
            subgraph "Container: zookeeper"
                ZK[Zookeeper<br/>Port 2181]
            end
            
            subgraph "Container: kafka-ui"
                UI[Kafka UI<br/>Port 8090]
            end
        end
    end
    
    Client[Cliente Externo] -->|HTTP :8080| API
    Admin[Administrador] -->|HTTP :8090| UI
    
    API -->|PLAINTEXT :29092| KafkaBroker
    Worker -->|PLAINTEXT :29092| KafkaBroker
    Worker -.->|mount| Volume
    KafkaBroker -->|:2181| ZK
    UI -->|:29092| KafkaBroker
    
    style API fill:#1168bd,stroke:#0b4884,color:#ffffff
    style Worker fill:#1168bd,stroke:#0b4884,color:#ffffff
    style KafkaBroker fill:#438dd5,stroke:#2e6295,color:#ffffff
    style ZK fill:#438dd5,stroke:#2e6295,color:#ffffff
    style UI fill:#85bbf0,stroke:#5d82a8,color:#000000
    style Volume fill:#d4e8fc,stroke:#7fa8d1,color:#000000
    style Client fill:#08427b,stroke:#052e56,color:#ffffff
    style Admin fill:#08427b,stroke:#052e56,color:#ffffff
```

---

## Descrição dos Níveis C4

### Nível 1 - Contexto do Sistema
Visão geral do sistema mostrando como ele se encaixa no mundo ao seu redor, incluindo usuários e sistemas externos.

### Nível 2 - Containers
Mostra a arquitetura de alto nível do sistema com seus principais containers (aplicações, bancos de dados, etc.).

### Nível 3 - Componentes
Detalha os componentes dentro de cada container, mostrando suas responsabilidades e relacionamentos.

### Nível 4 - Código
Mostra como o código é organizado dentro dos componentes, com diagramas de sequência para fluxos principais.

---

## Tecnologias por Camada

| Camada | Tecnologia | Versão |
|--------|-----------|---------|
| Linguagem | Java | 23 |
| Framework | Spring Boot | 3.3.11 |
| Build Tool | Maven | 3.8+ |
| Message Broker | Apache Kafka | 7.6.0 |
| Database | H2 | In-Memory |
| API Documentation | SpringDoc OpenAPI | 2.3.0 |
| Containerization | Docker | Latest |
| Orchestration | Docker Compose | 3.8 |

---
**Vers\u00e3o:** 1.0.0  \n**Data:** Fevereiro 2026  \n**Autor:** Antonio Luiz\n\n---
**Nota:** Estes diagramas seguem o padrão C4 Model criado por Simon Brown e representam a arquitetura completa do sistema de cadastro de clientes.

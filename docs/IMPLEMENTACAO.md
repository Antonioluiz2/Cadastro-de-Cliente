# Documentação de Implementação - Sistema de Cadastro de Clientes

## 📚 Índice

1. [Visão Geral](#visão-geral)
2. [Requisitos](#requisitos)
3. [Instalação e Configuração](#instalação-e-configuração)
4. [Arquitetura Detalhada](#arquitetura-detalhada)
5. [Guia de Desenvolvimento](#guia-de-desenvolvimento)
6. [Deployment](#deployment)
7. [Testes](#testes)
8. [Monitoramento](#monitoramento)
9. [Troubleshooting](#troubleshooting)
10. [Boas Práticas](#boas-práticas)

---

## 1. Visão Geral

### 1.1 Objetivo

Sistema de microserviços para gerenciamento de cadastro de clientes com arquitetura orientada a eventos, implementando:

- ✅ API REST com 4 operações (POST, GET, PUT, DELETE)
- ✅ Produção de eventos para Apache Kafka
- ✅ Worker independente para processamento assíncrono
- ✅ Transformação de dados
- ✅ Persistência em memória e arquivo

### 1.2 Componentes Principais

```
┌─────────────────┐       ┌──────────────┐       ┌─────────────────┐
│   Cliente HTTP  │ ───── │  API REST    │ ───── │   H2 Database   │
└─────────────────┘       │  (Port 8080) │       └─────────────────┘
                          └──────┬───────┘
                                 │ Produce
                                 ▼
                          ┌──────────────┐
                          │ Kafka Broker │
                          │  (Port 9092) │
                          └──────┬───────┘
                                 │ Consume
                                 ▼
                          ┌──────────────┐
                          │Worker Kafka  │ ───── Files + Memory
                          └──────────────┘
```

---

## 2. Requisitos

### 2.1 Software Necessário

| Software | Versão Mínima | Versão Recomendada | Obrigatório |
|----------|---------------|-------------------|-------------|
| Java JDK | 23 | 23 | ✅ Sim |
| Maven | 3.8.0 | 3.9.x | ✅ Sim |
| Docker | 20.10 | Latest | ✅ Sim |
| Docker Compose | 2.0 | Latest | ✅ Sim |
| Git | 2.30 | Latest | ✅ Sim |

### 2.2 Hardware Recomendado

| Recurso | Desenvolvimento | Produção |
|---------|----------------|----------|
| RAM | 8GB | 16GB+ |
| CPU | 4 cores | 8 cores+ |
| Disco | 10GB livre | 50GB+ |
| Rede | 100Mbps | 1Gbps+ |

---

## 3. Instalação e Configuração

### 3.1 Clone do Repositório

```bash
git clone <repository-url>
cd "Cadastro de clientes"
```

### 3.2 Estrutura de Diretórios

```
Cadastro de clientes/
├── api-rest/                    # API REST Spring Boot
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/testetecnico/cliente/
│   │       │       ├── ClienteApiApplication.java
│   │       │       ├── config/
│   │       │       │   └── KafkaConfig.java
│   │       │       ├── controller/
│   │       │       │   └── ClienteController.java
│   │       │       ├── domain/
│   │       │       │   ├── dto/
│   │       │       │   │   ├── ClienteRequestDTO.java
│   │       │       │   │   ├── ClienteResponseDTO.java
│   │       │       │   │   └── ClienteEventDTO.java
│   │       │       │   └── entity/
│   │       │       │       ├── Cliente.java
│   │       │       │       └── StatusCliente.java
│   │       │       ├── exception/
│   │       │       │   ├── ClienteNotFoundException.java
│   │       │       │   ├── DuplicateClienteException.java
│   │       │       │   ├── ErrorResponse.java
│   │       │       │   └── GlobalExceptionHandler.java
│   │       │       ├── repository/
│   │       │       │   └── ClienteRepository.java
│   │       │       └── service/
│   │       │           ├── ClienteService.java
│   │       │           └── KafkaProducerService.java
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── kafka-worker/               # Worker Kafka independente
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/testetecnico/worker/
│   │       │       ├── ClienteWorkerApplication.java
│   │       │       ├── config/
│   │       │       │   └── KafkaConsumerConfig.java
│   │       │       ├── consumer/
│   │       │       │   └── ClienteEventConsumer.java
│   │       │       ├── dto/
│   │       │       │   ├── ClienteEventDTO.java
│   │       │       │   └── ProcessedEventDTO.java
│   │       │       └── service/
│   │       │           └── EventProcessorService.java
│   │       └── resources/
│   │           └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── docs/                       # Documentação
│   ├── C4-DIAGRAMAS.md
│   ├── INVENTARIO-TO-BE.md
│   └── IMPLEMENTACAO.md
│
├── docker-compose.yml
└── README.md
```

### 3.3 Configuração Inicial

#### 3.3.1 Configurar Java 23

**Windows:**
```powershell
# Verificar versão instalada
java -version

# Baixar Java 23 de: https://adoptium.net/
# Configurar JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-23", "Machine")
```

**Linux/Mac:**
```bash
# Verificar versão
java -version

# Instalar via SDKMAN (recomendado)
curl -s "https://get.sdkman.io" | bash
sdk install java 23-tem

# Ou baixar de: https://adoptium.net/
```

#### 3.3.2 Configurar Maven

```bash
# Verificar instalação
mvn -version

# Compilar projeto API
cd api-rest
mvn clean install

# Compilar projeto Worker
cd ../kafka-worker
mvn clean install
```

### 3.4 Executar com Docker Compose

```bash
# Voltar para raiz do projeto
cd ..

# Iniciar todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps

# Ver logs
docker-compose logs -f
```

### 3.5 Verificar Serviços

```bash
# API REST
curl http://localhost:8080/actuator/health

# Swagger UI
# Abrir no navegador: http://localhost:8080/swagger-ui.html

# Kafka UI
# Abrir no navegador: http://localhost:8090

# H2 Console
# Abrir no navegador: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:clientedb
# User: sa
# Password: (vazio)
```

---

## 4. Arquitetura Detalhada

### 4.1 Fluxo de Dados

#### 4.1.1 Criação de Cliente

```
1. Cliente HTTP -> POST /api/v1/clientes
   ↓
2. ClienteController.criarCliente()
   ↓ @Valid validação
3. ClienteService.criarCliente()
   ↓ verifica duplicados
4. ClienteRepository.save()
   ↓ persiste no H2
5. KafkaProducerService.enviarEvento()
   ↓ produz evento
6. Kafka Broker (tópico: cliente-events)
   ↓ armazena mensagem
7. ClienteEventConsumer.consumirEvento()
   ↓ consome mensagem
8. EventProcessorService.processarEvento()
   ↓ transforma dados
9. Salva em memória (HashMap) + Arquivo JSON
   ↓
10. Acknowledge da mensagem
```

#### 4.1.2 Transformação de Dados no Worker

| Campo Original | Transformação | Exemplo |
|---------------|---------------|---------|
| nome: "joão silva" | UPPERCASE | "JOÃO SILVA" |
| email: "TESTE@EMAIL.COM" | lowercase | "teste@email.com" |
| cpf: "123.456.789-01" | remove formato | "12345678901" |
| telefone: "(11) 98765-4321" | remove formato | "11987654321" |
| endereco: "rua exemplo" | Title Case | "Rua Exemplo" |

### 4.2 Configurações de Kafka

#### 4.2.1 Producer (API REST)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
      properties:
        spring.json.add.type.headers: false
```

#### 4.2.2 Consumer (Worker)

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: cliente-worker-group
      auto-offset-reset: earliest
      key-deserializer: StringDeserializer
      value-deserializer: JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    listener:
      ack-mode: manual
```

#### 4.2.3 Tópico

```java
@Bean
public NewTopic clienteEventsTopic() {
    return TopicBuilder.name("cliente-events")
            .partitions(3)        // 3 partições para paralelismo
            .replicas(1)          // 1 réplica (dev)
            .build();
}
```

### 4.3 Modelo de Dados

#### 4.3.1 Entidade Cliente (JPA)

```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nome;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;
    
    // ... outros campos
}
```

#### 4.3.2 DTO de Request

```java
public class ClienteRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email
    private String email;
    
    // ... outros campos com validações
}
```

#### 4.3.3 DTO de Evento Kafka

```java
public class ClienteEventDTO {
    private String eventType;      // CLIENTE_CRIADO, CLIENTE_ATUALIZADO, CLIENTE_DELETADO
    private Long clienteId;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String endereco;
    private String status;
    private String timestamp;
}
```

---

## 5. Guia de Desenvolvimento

### 5.1 Desenvolvimento Local (Sem Docker)

#### 5.1.1 Iniciar Kafka Local

```bash
# Iniciar apenas Kafka e Zookeeper
docker-compose up -d kafka zookeeper
```

#### 5.1.2 Executar API REST

```bash
cd api-rest
mvn spring-boot:run
```

#### 5.1.3 Executar Worker Kafka

```bash
cd kafka-worker
mvn spring-boot:run
```

### 5.2 Adicionar Nova Funcionalidade

#### Exemplo: Adicionar campo "dataNascimento"

**1. Atualizar Entidade**

```java
@Entity
public class Cliente {
    // ... campos existentes
    
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    
    // getter e setter
}
```

**2. Atualizar DTOs**

```java
public class ClienteRequestDTO {
    // ... campos existentes
    
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;
}

public class ClienteResponseDTO {
    // ... campos existentes
    private LocalDate dataNascimento;
}

public class ClienteEventDTO {
    // ... campos existentes
    private String dataNascimento;
}
```

**3. Atualizar Service**

```java
@Service
public class ClienteService {
    public ClienteResponseDTO criarCliente(ClienteRequestDTO request) {
        Cliente cliente = Cliente.builder()
                // ... campos existentes
                .dataNascimento(request.getDataNascimento())
                .build();
        // ... resto do código
    }
}
```

**4. Testar**

```bash
curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@email.com",
    "cpf": "12345678901",
    "dataNascimento": "1990-05-15"
  }'
```

### 5.3 Adicionar Nova Validação

```java
// Custom Validator para CPF
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CpfValidator.class)
public @interface ValidCPF {
    String message() default "CPF inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Implementação
public class CpfValidator implements ConstraintValidator<ValidCPF, String> {
    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        // Lógica de validação de CPF
        return cpf != null && cpf.matches("\\d{11}");
    }
}

// Uso no DTO
public class ClienteRequestDTO {
    @ValidCPF
    private String cpf;
}
```

### 5.4 Hot Reload (Spring Boot DevTools)

Adicionar ao pom.xml:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## 6. Deployment

### 6.1 Build de Imagens Docker

```bash
# Build API REST
cd api-rest
docker build -t cliente-api:1.0.0 .

# Build Worker
cd ../kafka-worker
docker build -t cliente-worker:1.0.0 .
```

### 6.2 Push para Registry

```bash
# Tag para registry
docker tag cliente-api:1.0.0 myregistry.com/cliente-api:1.0.0
docker tag cliente-worker:1.0.0 myregistry.com/cliente-worker:1.0.0

# Push
docker push myregistry.com/cliente-api:1.0.0
docker push myregistry.com/cliente-worker:1.0.0
```

### 6.3 Deploy em Kubernetes

#### 6.3.1 Deployment da API

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cliente-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cliente-api
  template:
    metadata:
      labels:
        app: cliente-api
    spec:
      containers:
      - name: cliente-api
        image: myregistry.com/cliente-api:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: cliente-api-service
spec:
  selector:
    app: cliente-api
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

#### 6.3.2 Deployment do Worker

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cliente-worker
spec:
  replicas: 2
  selector:
    matchLabels:
      app: cliente-worker
  template:
    metadata:
      labels:
        app: cliente-worker
    spec:
      containers:
      - name: cliente-worker
        image: myregistry.com/cliente-worker:1.0.0
        env:
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        volumeMounts:
        - name: worker-data
          mountPath: /app/data
      volumes:
      - name: worker-data
        persistentVolumeClaim:
          claimName: worker-data-pvc
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: worker-data-pvc
spec:
  accessModes:
  - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

### 6.4 Variáveis de Ambiente

| Variável | Descrição | Padrão | Obrigatória |
|----------|-----------|--------|-------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Endereço do Kafka | localhost:9092 | ✅ |
| `SPRING_PROFILES_ACTIVE` | Profile ativo | default | ❌ |
| `SERVER_PORT` | Porta da API | 8080 | ❌ |
| `JAVA_OPTS` | Opções da JVM | -Xms256m -Xmx512m | ❌ |

---

## 7. Testes

### 7.1 Testes Manuais

#### 7.1.1 Criar Cliente

```bash
curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "email": "maria.santos@email.com",
    "cpf": "98765432100",
    "telefone": "(11) 98765-4321",
    "endereco": "avenida paulista, 1000",
    "status": "ATIVO"
  }'
```

**Resposta Esperada:**
```json
{
  "id": 1,
  "nome": "Maria Santos",
  "email": "maria.santos@email.com",
  "cpf": "98765432100",
  "telefone": "(11) 98765-4321",
  "endereco": "avenida paulista, 1000",
  "status": "ATIVO",
  "dataCriacao": "2026-02-08T10:30:00",
  "dataAtualizacao": "2026-02-08T10:30:00"
}
```

#### 7.1.2 Listar Clientes

```bash
curl http://localhost:8080/api/v1/clientes
```

#### 7.1.3 Buscar por ID

```bash
curl http://localhost:8080/api/v1/clientes/1
```

#### 7.1.4 Atualizar Cliente

```bash
curl -X PUT http://localhost:8080/api/v1/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos Silva",
    "email": "maria.santos@email.com",
    "cpf": "98765432100",
    "telefone": "(11) 98765-4321",
    "endereco": "Rua Nova, 500",
    "status": "ATIVO"
  }'
```

#### 7.1.5 Deletar Cliente

```bash
curl -X DELETE http://localhost:8080/api/v1/clientes/1
```

### 7.2 Verificar Processamento do Worker

```bash
# Ver logs do worker
docker-compose logs -f cliente-worker

# Verificar arquivos gerados
docker exec cliente-worker ls -la /app/data/processed/

# Ver conteúdo de um arquivo
docker exec cliente-worker cat /app/data/processed/cliente_1_CLIENTE_CRIADO_20260208_103000.json
```

### 7.3 Testes Automatizados

#### 7.3.1 Teste Unitário de Service

```java
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    
    @Mock
    private ClienteRepository repository;
    
    @Mock
    private KafkaProducerService kafkaProducer;
    
    @InjectMocks
    private ClienteService service;
    
    @Test
    void deveCriarClienteComSucesso() {
        // Arrange
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nome("Test")
                .email("test@email.com")
                .cpf("12345678901")
                .build();
        
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nome("Test")
                .email("test@email.com")
                .cpf("12345678901")
                .build();
        
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(repository.save(any(Cliente.class))).thenReturn(cliente);
        
        // Act
        ClienteResponseDTO response = service.criarCliente(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(kafkaProducer).enviarEvento(any());
    }
}
```

#### 7.3.2 Teste de Integração

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ClienteControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void deveCriarClienteViaAPI() throws Exception {
        ClienteRequestDTO request = ClienteRequestDTO.builder()
                .nome("Integration Test")
                .email("integration@email.com")
                .cpf("11111111111")
                .build();
        
        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Integration Test"));
    }
}
```

### 7.4 Testes de Carga

```bash
# Usando Apache Bench
ab -n 1000 -c 10 -p payload.json -T application/json \
  http://localhost:8080/api/v1/clientes

# payload.json
{
  "nome": "Load Test",
  "email": "load@test.com",
  "cpf": "99999999999"
}
```

---

## 8. Monitoramento

### 8.1 Endpoints de Health

```bash
# Health check geral
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics

# Info da aplicação
curl http://localhost:8080/actuator/info
```

### 8.2 Logs

```bash
# API logs
docker-compose logs -f cliente-api

# Worker logs
docker-compose logs -f cliente-worker

# Kafka logs
docker-compose logs -f kafka

# Todos os logs
docker-compose logs -f
```

### 8.3 Monitorar Kafka

#### Via Kafka UI (http://localhost:8090)

1. Acessar "Topics"
2. Selecionar "cliente-events"
3. Ver mensagens, partições, consumer groups

#### Via linha de comando

```bash
# Listar tópicos
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Descrever tópico
docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic cliente-events

# Consumer group lag
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group cliente-worker-group
```

---

## 9. Troubleshooting

### 9.1 Problemas Comuns

#### 9.1.1 API não inicia

**Sintoma:** Container para ou não responde

**Soluções:**
```bash
# Ver logs
docker-compose logs cliente-api

# Problemas comuns:
# 1. Porta 8080 ocupada
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# 2. Kafka não disponível
docker-compose logs kafka

# 3. Memória insuficiente
docker stats
```

#### 9.1.2 Worker não consome mensagens

**Sintoma:** Mensagens ficam no tópico

**Soluções:**
```bash
# Verificar consumer group
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group cliente-worker-group

# Verificar logs do worker
docker-compose logs cliente-worker | grep ERROR

# Reiniciar worker
docker-compose restart cliente-worker
```

#### 9.1.3 Kafka não conecta

**Sintoma:** Connection refused

**Soluções:**
```bash
# Verificar se Kafka está rodando
docker-compose ps kafka

# Verificar logs do Kafka
docker-compose logs kafka | grep ERROR

# Reiniciar serviços na ordem
docker-compose down
docker-compose up -d zookeeper
sleep 10
docker-compose up -d kafka
sleep 20
docker-compose up -d cliente-api cliente-worker
```

### 9.2 Debug Remoto

#### 9.2.1 Configurar Debug na API

Adicionar ao docker-compose.yml:

```yaml
cliente-api:
  environment:
    JAVA_OPTS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
  ports:
    - "8080:8080"
    - "5005:5005"  # Debug port
```

#### 9.2.2 Conectar IDE

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Add New → Remote JVM Debug
3. Host: localhost, Port: 5005
4. Start debug

**VS Code:**
```json
{
  "type": "java",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```

---

## 10. Boas Práticas

### 10.1 Código

#### ✅ Recomendado

```java
// Use DTOs para separar camadas
public class ClienteController {
    public ClienteResponseDTO criar(@Valid @RequestBody ClienteRequestDTO request) {
        return service.criar(request);
    }
}

// Use Optional para evitar NullPointerException
public Optional<Cliente> buscar(Long id) {
    return repository.findById(id);
}

// Log apropriado
log.info("Cliente criado: id={}, email={}", cliente.getId(), cliente.getEmail());
```

#### ❌ Evitar

```java
// Não exponha entidades diretamente
public Cliente criar(Cliente cliente) {  // ERRADO
    return repository.save(cliente);
}

// Não ignore exceções
catch (Exception e) {  // ERRADO
    // nada
}

// Não use Strings concatenadas em logs
log.info("Cliente: " + cliente.toString());  // ERRADO
```

### 10.2 Kafka

#### ✅ Recomendado

```java
// Sempre use acknowledge manual
@KafkaListener(topics = "cliente-events")
public void consumir(ClienteEventDTO evento, Acknowledgment ack) {
    try {
        processar(evento);
        ack.acknowledge();
    } catch (Exception e) {
        log.error("Erro", e);
        // Implementar retry ou DLQ
    }
}

// Use keys para garantir ordem
kafkaTemplate.send(topic, clienteId.toString(), evento);
```

#### ❌ Evitar

```java
// Não bloqueie o consumer por muito tempo
@KafkaListener(topics = "cliente-events")
public void consumir(ClienteEventDTO evento) {
    Thread.sleep(60000);  // ERRADO - timeout
    processar(evento);
}
```

### 10.3 Performance

#### Otimizações

```yaml
# API REST - application.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

```java
// Use @Transactional apropriadamente
@Transactional(readOnly = true)
public List<Cliente> listar() {
    return repository.findAll();
}

@Transactional
public Cliente criar(ClienteRequestDTO dto) {
    return repository.save(toEntity(dto));
}
```

### 10.4 Segurança

```yaml
# Não commitar secrets
spring:
  kafka:
    properties:
      security.protocol: ${KAFKA_SECURITY_PROTOCOL:PLAINTEXT}
      sasl.mechanism: ${KAFKA_SASL_MECHANISM:PLAIN}
      sasl.jaas.config: ${KAFKA_JAAS_CONFIG}
```

```java
// Sanitize inputs
public class ClienteRequestDTO {
    @NotBlank
    @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$")
    private String nome;
    
    @Email
    @Pattern(regexp = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")
    private String email;
}
```

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Consultar documentação
2. Verificar logs
3. Abrir issue no repositório
4. Contatar equipe de desenvolvimento

---

**Versão:** 1.0.0  
**Última Atualização:** Fevereiro 2026  
**Autor:** Antonio Luiz

---

*Este documento deve ser mantido atualizado conforme a evolução do sistema.*

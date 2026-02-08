# Exemplos de Requisições - API de Clientes

## 📝 Variáveis de Ambiente

```bash
export API_URL=http://localhost:8080
export API_BASE_URL=$API_URL/api/v1/clientes
```

---

## 1. Criar Cliente (POST)

### 1.1 Cliente Completo

```bash
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao.silva@email.com",
    "cpf": "12345678901",
    "telefone": "11987654321",
    "endereco": "Rua Exemplo, 123 - São Paulo, SP",
    "status": "ATIVO"
  }'
```

### 1.2 Cliente Mínimo (Campos Obrigatórios)

```bash
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "email": "maria.santos@email.com",
    "cpf": "98765432100"
  }'
```

### 1.3 Múltiplos Clientes

```bash
# Cliente 1
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Carlos Oliveira", "email": "carlos@email.com", "cpf": "11111111111"}'

# Cliente 2
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Ana Costa", "email": "ana@email.com", "cpf": "22222222222"}'

# Cliente 3
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Pedro Alves", "email": "pedro@email.com", "cpf": "33333333333"}'
```

### 1.4 Diferentes Status

```bash
# ATIVO
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Cliente Ativo", "email": "ativo@email.com", "cpf": "44444444444", "status": "ATIVO"}'

# INATIVO
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Cliente Inativo", "email": "inativo@email.com", "cpf": "55555555555", "status": "INATIVO"}'

# BLOQUEADO
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Cliente Bloqueado", "email": "bloqueado@email.com", "cpf": "66666666666", "status": "BLOQUEADO"}'
```

---

## 2. Buscar Clientes (GET)

### 2.1 Listar Todos

```bash
curl -X GET $API_BASE_URL
```

### 2.2 Buscar por ID

```bash
curl -X GET $API_BASE_URL/1
```

### 2.3 Filtrar por Status

```bash
# Ativos
curl -X GET "$API_BASE_URL?status=ATIVO"

# Inativos
curl -X GET "$API_BASE_URL?status=INATIVO"

# Bloqueados
curl -X GET "$API_BASE_URL?status=BLOQUEADO"
```

### 2.4 Com Pretty Print (jq)

```bash
# Listar todos formatado
curl -X GET $API_BASE_URL | jq '.'

# Buscar específico formatado
curl -X GET $API_BASE_URL/1 | jq '.'

# Extrair apenas nomes
curl -X GET $API_BASE_URL | jq '.[].nome'

# Contar clientes
curl -X GET $API_BASE_URL | jq 'length'
```

---

## 3. Atualizar Cliente (PUT)

### 3.1 Atualização Completa

```bash
curl -X PUT $API_BASE_URL/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva Atualizado",
    "email": "joao.silva.novo@email.com",
    "cpf": "12345678901",
    "telefone": "11999999999",
    "endereco": "Rua Nova, 456 - São Paulo, SP",
    "status": "ATIVO"
  }'
```

### 3.2 Mudar Status

```bash
# Para INATIVO
curl -X PUT $API_BASE_URL/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao.silva@email.com",
    "cpf": "12345678901",
    "status": "INATIVO"
  }'

# Para BLOQUEADO
curl -X PUT $API_BASE_URL/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao.silva@email.com",
    "cpf": "12345678901",
    "status": "BLOQUEADO"
  }'
```

---

## 4. Deletar Cliente (DELETE)

### 4.1 Deletar por ID

```bash
curl -X DELETE $API_BASE_URL/1
```

### 4.2 Deletar Múltiplos

```bash
for id in 1 2 3 4 5; do
  curl -X DELETE $API_BASE_URL/$id
  echo "Deletado cliente ID: $id"
done
```

---

## 5. Testes de Validação (Erros Esperados)

### 5.1 Nome Inválido

```bash
# Nome muito curto (< 3 caracteres)
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Jo", "email": "test@email.com", "cpf": "12345678901"}'

# Resposta esperada: 400 Bad Request
```

### 5.2 Email Inválido

```bash
# Email sem @
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Test User", "email": "invalidemail", "cpf": "12345678901"}'

# Resposta esperada: 400 Bad Request
```

### 5.3 CPF Duplicado

```bash
# Criar primeiro cliente
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Primeiro", "email": "primeiro@email.com", "cpf": "99999999999"}'

# Tentar criar com mesmo CPF
curl -X POST $API_BASE_URL \
  -H "Content-Type: application/json" \
  -d '{"nome": "Segundo", "email": "segundo@email.com", "cpf": "99999999999"}'

# Resposta esperada: 409 Conflict
```

### 5.4 Cliente Não Encontrado

```bash
curl -X GET $API_BASE_URL/999999

# Resposta esperada: 404 Not Found
```

---

## 6. Testes de Carga

### 6.1 Criar 100 Clientes

```bash
for i in {1..100}; do
  curl -X POST $API_BASE_URL \
    -H "Content-Type: application/json" \
    -d "{\"nome\": \"Cliente $i\", \"email\": \"cliente$i@email.com\", \"cpf\": \"$(printf '%011d' $i)\"}" \
    -w "\nStatus: %{http_code}\n"
  sleep 0.1
done
```

### 6.2 Apache Bench (1000 requisições)

```bash
# Criar arquivo de payload
cat > payload.json <<EOF
{
  "nome": "Load Test User",
  "email": "loadtest@email.com",
  "cpf": "00000000001"
}
EOF

# Executar teste de carga
ab -n 1000 -c 50 -p payload.json -T application/json $API_BASE_URL/
```

---

## 7. Scripts de Utilidade

### 7.1 Script de População de Dados

```bash
#!/bin/bash
# populate_data.sh

API_URL="http://localhost:8080/api/v1/clientes"

NOMES=("João Silva" "Maria Santos" "Carlos Oliveira" "Ana Costa" "Pedro Alves")
EMAILS=("joao@email.com" "maria@email.com" "carlos@email.com" "ana@email.com" "pedro@email.com")
CPFS=("12345678901" "98765432100" "11111111111" "22222222222" "33333333333")

for i in {0..4}; do
  echo "Criando cliente ${NOMES[$i]}..."
  curl -X POST $API_URL \
    -H "Content-Type: application/json" \
    -d "{
      \"nome\": \"${NOMES[$i]}\",
      \"email\": \"${EMAILS[$i]}\",
      \"cpf\": \"${CPFS[$i]}\",
      \"telefone\": \"1198765432${i}\",
      \"endereco\": \"Rua Teste, ${i}00\",
      \"status\": \"ATIVO\"
    }" \
    -w "\n"
  sleep 0.5
done

echo "População de dados concluída!"
```

### 7.2 Script de Limpeza

```bash
#!/bin/bash
# cleanup_data.sh

API_URL="http://localhost:8080/api/v1/clientes"

echo "Obtendo lista de clientes..."
IDS=$(curl -s $API_URL | jq '.[].id')

echo "Deletando clientes..."
for id in $IDS; do
  curl -X DELETE $API_URL/$id
  echo "Deletado cliente ID: $id"
done

echo "Limpeza concluída!"
```

### 7.3 Script de Health Check

```bash
#!/bin/bash
# health_check.sh

SERVICES=(
  "API REST:http://localhost:8080/actuator/health"
  "Swagger UI:http://localhost:8080/swagger-ui.html"
  "Kafka UI:http://localhost:8090"
  "H2 Console:http://localhost:8080/h2-console"
)

echo "Verificando serviços..."
echo ""

for service in "${SERVICES[@]}"; do
  IFS=':' read -r name url <<< "$service"
  
  if curl -s -f -o /dev/null "$url"; then
    echo "✅ $name - OK"
  else
    echo "❌ $name - FALHA"
  fi
done
```

---

## 8. Postman Collection

### 8.1 Importar Collection

Crie um arquivo `cliente-api.postman_collection.json`:

```json
{
  "info": {
    "name": "Cliente API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Criar Cliente",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"nome\": \"João Silva\",\n  \"email\": \"joao@email.com\",\n  \"cpf\": \"12345678901\",\n  \"telefone\": \"11987654321\",\n  \"endereco\": \"Rua Teste, 123\",\n  \"status\": \"ATIVO\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/clientes",
          "host": ["{{base_url}}"],
          "path": ["clientes"]
        }
      }
    },
    {
      "name": "Listar Clientes",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{base_url}}/clientes",
          "host": ["{{base_url}}"],
          "path": ["clientes"]
        }
      }
    },
    {
      "name": "Buscar Cliente por ID",
      "request": {
        "method": "GET",
        "url": {
          "raw": "{{base_url}}/clientes/{{cliente_id}}",
          "host": ["{{base_url}}"],
          "path": ["clientes", "{{cliente_id}}"]
        }
      }
    },
    {
      "name": "Atualizar Cliente",
      "request": {
        "method": "PUT",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"nome\": \"João Silva Atualizado\",\n  \"email\": \"joao@email.com\",\n  \"cpf\": \"12345678901\",\n  \"status\": \"ATIVO\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/clientes/{{cliente_id}}",
          "host": ["{{base_url}}"],
          "path": ["clientes", "{{cliente_id}}"]
        }
      }
    },
    {
      "name": "Deletar Cliente",
      "request": {
        "method": "DELETE",
        "url": {
          "raw": "{{base_url}}/clientes/{{cliente_id}}",
          "host": ["{{base_url}}"],
          "path": ["clientes", "{{cliente_id}}"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080/api/v1"
    },
    {
      "key": "cliente_id",
      "value": "1"
    }
  ]
}
```

---

## 9. Exemplos com PowerShell (Windows)

### 9.1 Criar Cliente

```powershell
$body = @{
    nome = "João Silva"
    email = "joao@email.com"
    cpf = "12345678901"
    telefone = "11987654321"
    endereco = "Rua Teste, 123"
    status = "ATIVO"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clientes" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### 9.2 Listar Clientes

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clientes" `
    -Method GET | ConvertTo-Json -Depth 10
```

### 9.3 Buscar por ID

```powershell
$id = 1
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/clientes/$id" `
    -Method GET
```

---

## 10. Verificar Eventos no Kafka

### 10.1 Via Kafka UI

1. Acessar http://localhost:8090
2. Navegar para "Topics" → "cliente-events"
3. Ver mensagens produzidas

### 10.2 Via Console do Kafka

```bash
# Consumir mensagens do tópico
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic cliente-events \
  --from-beginning

# Descrever tópico
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic cliente-events
```

### 10.3 Verificar Worker Logs

```bash
# Ver logs em tempo real
docker-compose logs -f cliente-worker

# Buscar por processamento específico
docker-compose logs cliente-worker | grep "CLIENTE_CRIADO"

# Ver últimas 100 linhas
docker-compose logs --tail=100 cliente-worker
```

---

## 11. Exemplos com Python

### 11.1 Criar Cliente

```python
import requests
import json

url = "http://localhost:8080/api/v1/clientes"
payload = {
    "nome": "João Silva",
    "email": "joao@email.com",
    "cpf": "12345678901",
    "telefone": "11987654321",
    "endereco": "Rua Teste, 123",
    "status": "ATIVO"
}

response = requests.post(url, json=payload)
print(f"Status: {response.status_code}")
print(f"Response: {response.json()}")
```

### 11.2 Listar e Processar

```python
import requests

url = "http://localhost:8080/api/v1/clientes"
response = requests.get(url)

clientes = response.json()
print(f"Total de clientes: {len(clientes)}")

for cliente in clientes:
    print(f"ID: {cliente['id']}, Nome: {cliente['nome']}, Status: {cliente['status']}")
```

---

**Nota:** Todos os exemplos assumem que os serviços estão rodando localmente nas portas padrão.

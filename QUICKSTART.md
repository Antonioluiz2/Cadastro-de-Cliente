# 🚀 Guia Rápido de Início

## ⚡ Início Rápido (5 minutos)

### Windows

```powershell
# 1. Abrir PowerShell como Administrador

# 2. Navegar até a pasta do projeto
cd "d:\...\Cadastro de clientes"

# 3. Executar script de inicialização
.\start.ps1

# Ou manualmente:
docker-compose up -d
```

### Linux/Mac

```bash
# 1. Abrir Terminal

# 2. Navegar até a pasta do projeto
cd "/path/to/Cadastro de clientes"

# 3. Dar permissão de execução ao script
chmod +x start.sh

# 4. Executar script de inicialização
./start.sh

# Ou manualmente:
docker-compose up -d
```

## 📋 Pré-requisitos

- ✅ Docker Desktop instalado e rodando
- ✅ 8GB RAM disponível
- ✅ 10GB espaço em disco

## 🧪 Teste Rápido

### 1. Verificar se está funcionando

```bash
curl http://localhost:8080/actuator/health
```

### 2. Criar primeiro cliente

```bash
curl -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@email.com",
    "cpf": "12345678901"
  }'
```

### 3. Listar clientes

```bash
curl http://localhost:8080/api/v1/clientes
```

## 🌐 Acessar Interfaces

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **API REST** | http://localhost:8080/api/v1/clientes | Endpoints REST |
| **Swagger** | http://localhost:8080/swagger-ui.html | Documentação interativa |
| **Kafka UI** | http://localhost:8090 | Monitor de eventos |
| **H2 Console** | http://localhost:8080/h2-console | Banco de dados |

### Credenciais H2 Console

- **JDBC URL:** `jdbc:h2:mem:clientedb`
- **User:** `sa`
- **Password:** _(vazio)_

## 🛑 Parar os Serviços

```bash
docker-compose down
```

## 🔄 Reiniciar

```bash
docker-compose restart
```

## 📊 Ver Logs

```bash
# Todos os logs
docker-compose logs -f

# Apenas API
docker-compose logs -f cliente-api

# Apenas Worker
docker-compose logs -f cliente-worker
```

## ❓ Problemas Comuns

### Porta 8080 já em uso

```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Docker não está rodando

- Abra o Docker Desktop
- Aguarde aparecer "Docker is running"
- Execute novamente

### Serviços não iniciam

```bash
# Limpar tudo e reiniciar
docker-compose down -v
docker-compose up -d
```

## 📚 Documentação Completa

- [README.md](README.md) - Documentação geral
- [docs/IMPLEMENTACAO.md](docs/IMPLEMENTACAO.md) - Guia de implementação
- [docs/C4-DIAGRAMAS.md](docs/C4-DIAGRAMAS.md) - Diagramas arquiteturais
- [docs/INVENTARIO-TO-BE.md](docs/INVENTARIO-TO-BE.md) - Inventário TO-BE
- [docs/EXEMPLOS-REQUISICOES.md](docs/EXEMPLOS-REQUISICOES.md) - Exemplos de uso

## 🆘 Ajuda

Em caso de dúvidas:

1. Consulte a documentação completa
2. Verifique os logs com `docker-compose logs`
3. Abra uma issue no repositório

---

**Pronto para usar!** Acesse o Swagger em http://localhost:8080/swagger-ui.html para testar a API interativamente.

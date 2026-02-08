# Script de inicialização do Sistema de Cadastro de Clientes (Windows)
# Autor: Antonio Luiz da Silva Development Team
# Versão: 1.0.0

param(
    [switch]$Clean
)

# Configurar JAVA_HOME para Java 23
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
Write-Host "Configurando JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan

# Configurar cores
$Host.UI.RawUI.ForegroundColor = "White"

function Write-Banner {
    Write-Host ""
    Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Blue
    Write-Host "║                                                           ║" -ForegroundColor Blue
    Write-Host "║       Sistema de Cadastro de Clientes - Teste Antonio     ║" -ForegroundColor Blue
    Write-Host "║                                                           ║" -ForegroundColor Blue
    Write-Host "║       API REST + Apache Kafka + Worker                    ║" -ForegroundColor Blue
    Write-Host "║                                                           ║" -ForegroundColor Blue
    Write-Host "╔═══════════════════════════════════════════════════════════╝" -ForegroundColor Blue
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "[✓] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "[✗] $Message" -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[!] $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "[i] $Message" -ForegroundColor Blue
}

function Test-Dependencies {
    Write-Info "Verificando dependências..."
    
    # Verificar Docker
    try {
        $dockerVersion = docker --version
        Write-Success "Docker encontrado: $dockerVersion"
    }
    catch {
        Write-Error "Docker não encontrado. Por favor, instale o Docker Desktop."
        exit 1
    }
    
    # Verificar Docker Compose
    try {
        $composeVersion = docker-compose --version
        Write-Success "Docker Compose encontrado: $composeVersion"
    }
    catch {
        Write-Error "Docker Compose não encontrado. Por favor, instale o Docker Compose."
        exit 1
    }
    
    # Verificar se Docker está rodando
    try {
        docker info | Out-Null
        Write-Success "Docker daemon está rodando"
    }
    catch {
        Write-Error "Docker daemon não está rodando. Por favor, inicie o Docker Desktop."
        exit 1
    }
}

function Clear-OldContainers {
    Write-Info "Limpando containers antigos..."
    try {
        docker-compose down -v 2>$null
        Write-Success "Limpeza concluída"
    }
    catch {
        Write-Warning "Nenhum container para limpar"
    }
}

function Start-Services {
    Write-Info "Iniciando serviços..."
    
    # Zookeeper
    Write-Info "Iniciando Zookeeper..."
    docker-compose up -d zookeeper
    Start-Sleep -Seconds 5
    Write-Success "Zookeeper iniciado"
    
    # Kafka
    Write-Info "Iniciando Kafka..."
    docker-compose up -d kafka
    Start-Sleep -Seconds 15
    Write-Success "Kafka iniciado"
    
    # Kafka UI
    Write-Info "Iniciando Kafka UI..."
    docker-compose up -d kafka-ui
    Start-Sleep -Seconds 5
    Write-Success "Kafka UI iniciado"
    
    # API REST
    Write-Info "Iniciando API REST..."
    docker-compose up -d cliente-api
    Start-Sleep -Seconds 10
    Write-Success "API REST iniciada"
    
    # Worker
    Write-Info "Iniciando Worker Kafka..."
    docker-compose up -d cliente-worker
    Start-Sleep -Seconds 5
    Write-Success "Worker Kafka iniciado"
}

function Test-HealthCheck {
    Write-Info "Verificando saúde dos serviços..."
    
    $maxAttempts = 30
    $attempt = 0
    
    # Verificar API REST
    Write-Info "Aguardando API REST ficar pronta..."
    while ($attempt -lt $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200) {
                Write-Success "API REST está saudável"
                break
            }
        }
        catch {
            $attempt++
            Start-Sleep -Seconds 2
        }
    }
    
    if ($attempt -eq $maxAttempts) {
        Write-Error "API REST não respondeu em tempo hábil"
        docker-compose logs cliente-api
        exit 1
    }
    
    # Verificar outros serviços
    $containers = docker-compose ps
    
    if ($containers -match "kafka.*Up") {
        Write-Success "Kafka está rodando"
    }
    else {
        Write-Error "Kafka não está rodando"
        exit 1
    }
    
    if ($containers -match "zookeeper.*Up") {
        Write-Success "Zookeeper está rodando"
    }
    else {
        Write-Error "Zookeeper não está rodando"
        exit 1
    }
    
    if ($containers -match "cliente-worker.*Up") {
        Write-Success "Worker está rodando"
    }
    else {
        Write-Error "Worker não está rodando"
        exit 1
    }
}

function Show-Info {
    Write-Host ""
    Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║               ✅ Sistema Iniciado com Sucesso             ║" -ForegroundColor Green
    Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Green
    Write-Host ""
    Write-Host "📡 Endpoints Disponíveis:" -ForegroundColor Blue
    Write-Host ""
    Write-Host "  API REST:" -ForegroundColor Green
    Write-Host "    http://localhost:8080/api/v1/clientes"
    Write-Host ""
    Write-Host "  Swagger UI:" -ForegroundColor Green
    Write-Host "    http://localhost:8080/swagger-ui.html"
    Write-Host ""
    Write-Host "  Health Check:" -ForegroundColor Green
    Write-Host "    http://localhost:8080/actuator/health"
    Write-Host ""
    Write-Host "  Kafka UI:" -ForegroundColor Green
    Write-Host "    http://localhost:8090"
    Write-Host ""
    Write-Host "  H2 Console:" -ForegroundColor Green
    Write-Host "    http://localhost:8080/h2-console"
    Write-Host "    JDBC URL: jdbc:h2:mem:clientedb"
    Write-Host "    User: sa"
    Write-Host "    Password: (vazio)"
    Write-Host ""
    Write-Host "📊 Comandos Úteis:" -ForegroundColor Blue
    Write-Host ""
    Write-Host "  Ver logs:" -ForegroundColor Green
    Write-Host "    docker-compose logs -f"
    Write-Host ""
    Write-Host "  Parar serviços:" -ForegroundColor Green
    Write-Host "    docker-compose down"
    Write-Host ""
    Write-Host "  Reiniciar:" -ForegroundColor Green
    Write-Host "    docker-compose restart"
    Write-Host ""
    Write-Host "  Status dos containers:" -ForegroundColor Green
    Write-Host "    docker-compose ps"
    Write-Host ""
}

# Função principal
function Main {
    Write-Banner
    Test-Dependencies
    
    if ($Clean) {
        Write-Warning "Modo limpeza ativado. Todos os dados serão removidos."
        Clear-OldContainers
    }
    
    Start-Services
    Test-HealthCheck
    Show-Info
}

# Executar
Main

# Videos API - Development Environment

Este documento descreve como configurar e usar o ambiente de desenvolvimento local com Docker, Redis para cache e mocks para as integrações externas.

## 🏗️ Arquitetura de Desenvolvimento

O ambiente de desenvolvimento inclui:

- **PostgreSQL**: Banco de dados principal
- **Redis**: Cache distribuído
- **Kafka + Zookeeper**: Mensageria assíncrona
- **Azurite**: Mock do Azure Blob Storage
- **Kafka UI**: Interface web para monitoramento do Kafka

## 🚀 Setup Rápido

### 1. Pré-requisitos

- Docker e Docker Compose instalados
- Java 21
- Maven 3.8+

### 2. Configuração Inicial

```bash
# Clone o repositório (se necessário)
git clone <repository-url>
cd videos-api

# Configure o ambiente
./scripts/dev-setup.sh
```

### 3. Executar a Aplicação

**Opção 1: Via IDE (Recomendado para desenvolvimento)**
```bash
# Configure a variável de ambiente
export SPRING_PROFILES_ACTIVE=local

# Execute a classe VideosApiApplication na sua IDE
```

**Opção 2: Via Docker**
```bash
# Execute com todos os serviços
docker-compose --profile full-stack up
```

## 🔧 Configuração Detalhada

### Profiles de Aplicação

- **`local`**: Desenvolvimento local com mocks habilitados
- **`dev`**: Desenvolvimento com serviços reais
- **`prod`**: Produção

### Variáveis de Ambiente

Copie `.env.example` para `.env` e ajuste conforme necessário:

```bash
cp .env.example .env
```

### Serviços Disponíveis

| Serviço | URL/Porta | Descrição |
|---------|-----------|-----------|
| PostgreSQL | `localhost:5432` | Banco de dados |
| Redis | `localhost:6379` | Cache |
| Kafka | `localhost:9092` | Message broker |
| Kafka UI | `http://localhost:8081` | Interface do Kafka |
| Azurite | `localhost:10000` | Mock Azure Storage |
| API | `http://localhost:8080` | Aplicação principal |

## 🧪 Mocks e Simulações

### Azure Blob Storage Mock

O `MockAzureBlobStorageService` simula o Azure Blob Storage:

- **Habilitado quando**: `azure.storage.mock.enabled=true`
- **Armazenamento**: Arquivos salvos em `/tmp/mock-azure-storage/`
- **URLs**: Retorna URLs mock no formato `http://localhost:8080/mock-storage/{filename}`

### Kafka Mock

O `MockVideoEventProducer` simula o Kafka usando Redis:

- **Habilitado quando**: `kafka.mock.enabled=true`
- **Armazenamento**: Eventos salvos no Redis com TTL de 1 hora
- **Chaves**: `mock:video-upload-events:{videoId}`

### Cache Redis

Configurado para cache de dados com TTL de 10 minutos:

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutos
```

## 📝 Scripts Utilitários

### Gerenciamento do Ambiente

```bash
# Iniciar ambiente de desenvolvimento
./scripts/dev-setup.sh

# Parar serviços
./scripts/dev-stop.sh

# Limpar completamente (remove volumes)
./scripts/dev-clean.sh
```

### Comandos Docker Úteis

```bash
# Ver logs de um serviço específico
docker-compose logs -f kafka

# Executar apenas serviços de infraestrutura
docker-compose up -d postgres redis kafka zookeeper azurite

# Rebuild da aplicação
docker-compose build app

# Ver status dos serviços
docker-compose ps
```

## 🔍 Monitoramento e Debug

### Logs da Aplicação

```bash
# Logs em tempo real
docker-compose logs -f app

# Logs do Kafka
docker-compose logs -f kafka
```

### Kafka UI

Acesse `http://localhost:8081` para:
- Visualizar tópicos
- Monitorar mensagens
- Gerenciar consumers

### Redis CLI

```bash
# Conectar ao Redis
docker exec -it videos_redis redis-cli

# Ver eventos mock do Kafka
LRANGE mock:video-upload-events:list 0 -1
```

## 🧪 Testes

### Testes de Integração

```bash
# Executar testes com Testcontainers
mvn test -Dspring.profiles.active=test
```

### Teste Manual da API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Upload de vídeo (exemplo)
curl -X POST http://localhost:8080/api/videos/upload \
  -F "file=@test-video.mp4" \
  -H "Content-Type: multipart/form-data"
```

## 🔧 Troubleshooting

### Problemas Comuns

**Erro de conexão com PostgreSQL**
```bash
# Verificar se o container está rodando
docker-compose ps postgres

# Ver logs
docker-compose logs postgres
```

**Erro de conexão com Redis**
```bash
# Testar conexão
docker exec videos_redis redis-cli ping
```

**Kafka não está funcionando**
```bash
# Verificar Zookeeper primeiro
docker-compose logs zookeeper

# Depois o Kafka
docker-compose logs kafka
```

### Reset Completo

```bash
# Parar tudo e limpar volumes
./scripts/dev-clean.sh

# Recriar ambiente
./scripts/dev-setup.sh
```

## 📚 Estrutura do Projeto

```
src/main/java/br/com/fiap/videosapi/
├── core/
│   └── config/
│       ├── RedisConfig.java          # Configuração do Redis
│       └── DevelopmentConfig.java    # Beans para desenvolvimento
├── video/
│   └── infrastructure/
│       ├── azure/
│       │   ├── AzureBlobStorageService.java
│       │   └── MockAzureBlobStorageService.java
│       └── kafka/
│           ├── VideoEventProducer.java
│           ├── MockVideoEventProducer.java
│           └── VideoEventPublisher.java
```

## 🚀 Próximos Passos

1. **Cache Strategy**: Implementar cache em endpoints críticos
2. **Monitoring**: Adicionar Prometheus/Grafana
3. **Tracing**: Implementar distributed tracing
4. **Security**: Configurar autenticação/autorização

---

Para dúvidas ou sugestões, consulte a documentação da API em `http://localhost:8080/swagger-ui.html` quando a aplicação estiver rodando.

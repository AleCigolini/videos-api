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

### 4. Resolver Problemas de Compilação Lombok

Se encontrar erros de compilação relacionados ao Lombok, siga estes passos:

**Para IntelliJ IDEA:**
1. Instale o plugin Lombok: `File > Settings > Plugins > Lombok`
2. Habilite annotation processing: `File > Settings > Build > Compiler > Annotation Processors`
3. Marque "Enable annotation processing"
4. Rebuild o projeto: `Build > Rebuild Project`

**Para Eclipse:**
1. Baixe lombok.jar do site oficial
2. Execute: `java -jar lombok.jar`
3. Aponte para a instalação do Eclipse
4. Reinicie o Eclipse

**Via Maven (alternativa):**
```bash
mvn clean compile -Dmaven.compiler.annotationProcessorPaths=org.projectlombok:lombok:1.18.30
```

## 🔧 Configuração Detalhada

### Profiles de Aplicação

- **`local`**: Desenvolvimento local com mocks habilitados
- **`dev`**: Desenvolvimento com serviços reais
- **`prod`**: Produção

### Variáveis de Ambiente

```bash
# Configurações do banco de dados
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=techchallenge

# Configurações Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Configurações Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_VIDEO_UPLOAD=video-upload-events
KAFKA_TOPIC_VIDEO_STATUS_UPDATE=video-status-update-events
KAFKA_CONSUMER_GROUP_ID=video-api-consumer-group

# Azure Storage (Mock)
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;
AZURE_STORAGE_CONTAINER_NAME=videos
```

## 🎯 Testando os Endpoints

### 1. Upload de Vídeo

```bash
curl -X POST http://localhost:8080/api/v1/videos/upload \
  -F "file=@/path/to/video.mp4" \
  -H "Content-Type: multipart/form-data"
```

### 2. Listar Todos os Vídeos

```bash
curl -X GET http://localhost:8080/api/v1/videos
```

### 3. Listar Vídeos por Status

```bash
# Status disponíveis: UPLOADED, PROCESSING, PROCESSED, FAILED
curl -X GET http://localhost:8080/api/v1/videos/status/UPLOADED
```

### 4. Consultar Vídeo Específico

```bash
curl -X GET http://localhost:8080/api/v1/videos/1
```

### 5. Simular Atualização de Status via Kafka

Para testar o consumer, você pode publicar uma mensagem no tópico Kafka:

```bash
# Acesse o Kafka UI em http://localhost:8081
# Ou use o kafka-console-producer:

docker exec -it videos_kafka kafka-console-producer \
  --bootstrap-server localhost:29092 \
  --topic video-status-update-events

# Envie uma mensagem JSON:
{
  "videoId": 1,
  "previousStatus": "UPLOADED",
  "newStatus": "PROCESSING",
  "message": "Video processing started",
  "processedBy": "video-processor-service",
  "timestamp": "2025-09-06T10:44:00"
}
```

## 🔍 Monitoramento e Debug

### Acessar Serviços

- **Aplicação**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8081
- **Health Check**: http://localhost:8080/actuator/health

### Logs Importantes

```bash
# Logs da aplicação
docker-compose logs app

# Logs do Kafka
docker-compose logs kafka

# Logs do PostgreSQL
docker-compose logs postgres

# Logs do Redis
docker-compose logs redis
```

### Verificar Cache Redis

```bash
# Conectar ao Redis
docker exec -it videos_redis redis-cli

# Verificar chaves de cache
KEYS video:*
KEYS videos:*

# Ver status de um vídeo específico
GET video:status:1
```

## 🔄 Fluxo Completo de Teste

### 1. Teste de Upload e Processamento

```bash
# 1. Fazer upload de um vídeo
curl -X POST http://localhost:8080/api/v1/videos/upload \
  -F "file=@test-video.mp4"

# 2. Verificar se foi salvo
curl -X GET http://localhost:8080/api/v1/videos

# 3. Simular mudança de status para PROCESSING
# (via Kafka UI ou console producer)

# 4. Verificar atualização
curl -X GET http://localhost:8080/api/v1/videos/1

# 5. Simular conclusão do processamento
# Enviar status PROCESSED via Kafka

# 6. Verificar link de download disponível
curl -X GET http://localhost:8080/api/v1/videos/1
```

## 🛠️ Scripts de Gerenciamento

### Iniciar Ambiente

```bash
./scripts/dev-setup.sh
```

### Parar Serviços

```bash
./scripts/dev-stop.sh
```

### Limpar Ambiente (Remove volumes)

```bash
./scripts/dev-clean.sh
```

## 🐛 Troubleshooting

### Problema: Serviços não iniciam

```bash
# Verificar se as portas estão livres
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis
lsof -i :9092  # Kafka

# Limpar containers antigos
docker-compose down -v
docker system prune -f
```

### Problema: Kafka não conecta

```bash
# Verificar health do Kafka
docker-compose ps

# Verificar logs
docker-compose logs kafka

# Recriar apenas o Kafka
docker-compose up -d --force-recreate kafka
```

### Problema: Cache Redis não funciona

```bash
# Verificar conexão Redis
docker exec -it videos_redis redis-cli ping

# Verificar configuração da aplicação
curl http://localhost:8080/actuator/health
```

## 📋 Serviços Disponíveis

| Serviço | URL/Porta | Descrição |
|---------|-----------|-----------|
| API Principal | http://localhost:8080 | Aplicação Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentação interativa da API |
| Health Check | http://localhost:8080/actuator/health | Status da aplicação |
| PostgreSQL | localhost:5432 | Banco de dados principal |
| Redis | localhost:6379 | Cache distribuído |
| Kafka | localhost:9092 | Message broker |
| Kafka UI | http://localhost:8081 | Interface web do Kafka |
| Azurite | localhost:10000-10002 | Mock Azure Blob Storage |

## 🔄 Tópicos Kafka

| Tópico | Descrição | Uso |
|--------|-----------|-----|
| `video-upload-events` | Eventos de upload de vídeo | Publicado após upload bem-sucedido |
| `video-status-update-events` | Atualizações de status | Consumido para atualizar status no banco |

## 📊 Estrutura do Cache Redis

```
video:{id}              # Objeto completo do vídeo (TTL: 10min)
video:status:{id}       # Status do vídeo (TTL: 10min)
videos:all              # Lista de todos os vídeos (TTL: 10min)
videos:status_{status}  # Lista filtrada por status (TTL: 10min)
```

## 🧪 Dados de Teste

### Exemplo de Payload para Status Update

```json
{
  "videoId": 1,
  "previousStatus": "UPLOADED",
  "newStatus": "PROCESSING",
  "message": "Video processing started by external service",
  "processedBy": "video-processor-v1.0",
  "timestamp": "2025-09-06T10:44:00"
}
```

### Formatos de Vídeo Suportados

- MP4, AVI, MOV, WMV, FLV, WebM, MKV
- Tamanho máximo: 500MB
- Validação via Apache Tika

## 🚀 Próximos Passos

1. **Resolver Compilação Lombok**: Configure annotation processing na IDE
2. **Testar Upload**: Use um arquivo de vídeo pequeno para teste
3. **Verificar Consumer**: Publique mensagem no Kafka e verifique atualização
4. **Monitorar Cache**: Use Redis CLI para verificar chaves criadas
5. **Validar Fluxo Completo**: Teste todo o ciclo de upload → processamento → consulta

## 📝 Notas Importantes

- O perfil `local` usa mocks para Azure Storage e Kafka (via Redis)
- Logs detalhados estão habilitados para debug
- Health checks garantem que serviços estejam prontos antes da aplicação iniciar
- Migrations do Flyway são executadas automaticamente na inicialização

## 🧪 Mocks e Simulações

### Azure Blob Storage Mock

O Azurite simula o Azure Blob Storage localmente:

```bash
# Verificar containers
curl http://localhost:10000/devstoreaccount1?comp=list

# Arquivos são armazenados em: /tmp/mock-azure-storage/
```

### Kafka Mock (Redis)

No perfil `local`, eventos Kafka são simulados via Redis:

```bash
# Verificar eventos no Redis
docker exec -it videos_redis redis-cli
KEYS *events*
```

## 🔧 Configuração da IDE

### IntelliJ IDEA

1. **Importar Projeto**: `File > Open` → selecione o `pom.xml`
2. **Configurar JDK**: `File > Project Structure > Project > SDK` → Java 21
3. **Habilitar Lombok**: 
   - `File > Settings > Plugins` → instalar Lombok Plugin
   - `File > Settings > Build > Compiler > Annotation Processors` → Enable annotation processing
4. **Configurar Run Configuration**:
   - Main class: `br.com.fiap.videosapi.VideosApiApplication`
   - Environment variables: `SPRING_PROFILES_ACTIVE=local`

### VS Code

1. **Extensões necessárias**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Configurar settings.json**:
```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "spring-boot.ls.problem.application-properties.enabled": true
}
```

## 📈 Monitoramento de Performance

### Métricas Disponíveis

```bash
# Actuator endpoints
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/health/redis
curl http://localhost:8080/actuator/health/db
```

### Cache Hit Rate

```bash
# Verificar estatísticas do Redis
docker exec -it videos_redis redis-cli info stats
```

## 🔒 Segurança em Desenvolvimento

- Credenciais padrão apenas para desenvolvimento local
- Azure Storage usa chaves de desenvolvimento do Azurite
- Kafka sem autenticação (apenas desenvolvimento)
- PostgreSQL com usuário/senha padrão

**⚠️ NUNCA use essas configurações em produção!**

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

## 🔐 Escopo por Usuário (x-cliente-id)

- Todos os endpoints de negócio exigem o header `x-cliente-id`.
- As operações de upload, listagem e consulta são escopadas por esse usuário.
- O cabeçalho é refletido nas respostas para facilitar o tracing.

### Exemplos com curl

```bash
# Upload (múltiplos arquivos suportados)
curl -X POST http://localhost:8080/videos/api/v1/videos/upload \
  -H "x-cliente-id: user-123" \
  -F "files=@/path/to/video1.mp4" \
  -F "files=@/path/to/video2.mp4" \
  -H "Content-Type: multipart/form-data"

# Listar todos
curl -H "x-cliente-id: user-123" http://localhost:8080/videos/api/v1/videos

# Listar por status
curl -H "x-cliente-id: user-123" http://localhost:8080/videos/api/v1/videos/status/UPLOADED

# Buscar por ID
curl -H "x-cliente-id: user-123" http://localhost:8080/videos/api/v1/videos/1
```
# API de Vídeos

Uma aplicação Spring Boot robusta para upload e gerenciamento de vídeos com integração Azure Blob Storage, streaming de eventos Kafka e ambiente de desenvolvimento Docker completo.

## Funcionalidades

- **Upload de Vídeos**: Endpoint REST API para upload de arquivos de vídeo (até 500MB)
- **Listagem de Vídeos**: Endpoints para listar vídeos processados com filtros por status
- **Consulta Individual**: Endpoint para consultar informações específicas de um vídeo
- **Armazenamento Azure**: Armazenamento de vídeos usando Azure Blob Storage (Azurite em desenvolvimento)
- **Streaming de Eventos**: Integração Kafka para notificações de upload e atualizações de status
- **Consumer de Status**: Consumidor Kafka para processar atualizações de status de processamento
- **Desenvolvimento Docker**: Ambiente de desenvolvimento completamente containerizado
- **Validação de Arquivos**: Detecção de tipo MIME e validação de tamanho
- **Arquitetura Limpa**: Design orientado a domínio com clara separação de responsabilidades
- **Documentação da API**: Documentação OpenAPI/Swagger

## Formatos de Vídeo Suportados

- MP4, AVI, MOV, WMV, FLV, WebM, MKV

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3.3.5**
- **Spring Kafka** para streaming de eventos
- **Azure Blob Storage SDK** com mock Azurite
- **PostgreSQL** com migrações Flyway
- **Docker & Docker Compose** para ambiente de desenvolvimento
- **Apache Tika** para detecção de tipo MIME
- **Testcontainers** para testes de integração
- **JUnit 5** e Mockito para testes
- **OpenAPI 3** para documentação

## Arquitetura

A aplicação segue os princípios da Arquitetura Limpa com ambiente de desenvolvimento baseado em Docker:

```
src/main/java/br/com/fiap/videosapi/
├── core/
│   ├── config/             # Configurações (Kafka, ObjectMapper)
│   └── exception/          # Tratamento global de exceções
├── video/
    ├── application/         # Casos de uso e lógica de negócio
    ├── domain/             # Entidades e objetos de domínio
    ├── infrastructure/     # Integrações externas
    │   ├── azure/          # Azure Blob Storage (Azurite no dev)
    │   ├── kafka/          # Produtor/Consumer Kafka
    │   └── repository/     # Persistência de dados
    └── common/             # DTOs e eventos compartilhados
```

## 🚀 Início Rápido

### Pré-requisitos

- Docker e Docker Compose
- Java 21
- Maven 3.8+

### Configuração de Desenvolvimento

1. **Iniciar Ambiente de Desenvolvimento**
   ```bash
   # Configurar ambiente Docker completo
   ./scripts/dev-setup.sh
   ```

2. **Executar Aplicação**
   ```bash
   # Opção 1: Executar pela IDE (recomendado)
   export SPRING_PROFILES_ACTIVE=local
   # Depois execute a classe VideosApiApplication
   
   # Opção 2: Executar com Docker
   docker-compose --profile full-stack up
   ```

3. **Acessar Serviços**
   - API: http://localhost:8080/videos
   - Swagger UI: http://localhost:8080/videos/swagger-ui/index.html
   - Kafka UI: http://localhost:8081
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379

### Gerenciamento do Ambiente

```bash
# Parar serviços
./scripts/dev-stop.sh

# Limpar tudo (remove volumes)
./scripts/dev-clean.sh
```

## 🐳 Serviços Docker

O ambiente de desenvolvimento inclui:

- **PostgreSQL 16**: Banco de dados principal com health checks
- **Kafka + Zookeeper**: Plataforma de streaming de eventos
- **Azurite**: Emulador do Azure Blob Storage
- **Kafka UI**: Interface web para monitoramento do Kafka

## 🔧 Perfis de Configuração

- **`local`**: Desenvolvimento com serviços Docker locais (Kafka, PostgreSQL, Azurite)
- **`dev`**: Desenvolvimento com serviços externos reais
- **`prod`**: Configuração de produção

## 🔄 Fluxo de Processamento de Vídeos

### 1. Upload do Vídeo
1. Recebe arquivo de vídeo via endpoint REST
2. Valida tipo MIME e tamanho do arquivo
3. Faz upload para Azure Blob Storage (Azurite no dev)
4. Salva metadados no PostgreSQL
5. Publica evento no tópico Kafka `video-upload-events`

### 2. Processamento de Status
1. Consumer escuta o tópico `video-status-update-events`
2. Atualiza status no banco PostgreSQL
3. Registra timestamp de processamento quando aplicável

### 3. Consulta de Vídeos
1. Consulta banco PostgreSQL
2. Retorna informações com link de download (quando aplicável)

## 🧪 Testes

```bash
# Executar testes com Testcontainers
mvn test

# Testes de integração
mvn test -Dspring.profiles.active=test
```

## 📚 Documentação

- **Guia de Desenvolvimento**: Veja [DEVELOPMENT.md](DEVELOPMENT.md) para instruções detalhadas de configuração
- **Documentação da API**: Disponível em `/videos/swagger-ui/index.html` quando a aplicação estiver rodando
- **Health Checks**: Disponível em `/videos/actuator/health`

## 🎯 Endpoints da API

### Upload de Vídeo
```http
POST /videos/api/v1/videos/upload
Content-Type: multipart/form-data
x-cliente-id: <seu_user_id>

Parâmetros:
- files: Lista de arquivos de vídeo (máx 500MB cada)

Resposta:
- 201: Upload realizado com sucesso
- 400: Arquivo inválido ou parâmetros incorretos
- 500: Erro interno do servidor
```

### Listar Todos os Vídeos
```http
GET /videos/api/v1/videos
x-cliente-id: <seu_user_id>

Resposta:
- 200: Lista de vídeos com informações de status e processamento
```

### Listar Vídeos por Status
```http
GET /videos/api/v1/videos/status/{status}
x-cliente-id: <seu_user_id>

Parâmetros:
- status: UPLOADED, PROCESSING, PROCESSED, FAILED

Resposta:
- 200: Lista filtrada de vídeos
```

### Consultar Vídeo por ID
```http
GET /videos/api/v1/videos/{id}
x-cliente-id: <seu_user_id>

Parâmetros:
- id: ID do vídeo

Resposta:
- 200: Informações detalhadas do vídeo
- 404: Vídeo não encontrado
```

### Download do Vídeo e Frames (ZIP)
```http
GET /videos/api/v1/videos/{id}/download
x-cliente-id: <seu_user_id>

Parâmetros:
- id: ID do vídeo

Resposta:
- 200: Arquivo ZIP streamado com o conteúdo
- 404: Vídeo ou blobs associados não encontrados
- 500: Erro ao gerar o ZIP
```

### Health Check
```http
GET /videos/actuator/health
```

## 🤝 Contribuindo

1. Faça um fork do repositório
2. Crie uma branch para sua feature
3. Faça suas alterações
4. Execute os testes: `mvn test`
5. Submeta um pull request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo LICENSE para detalhes.
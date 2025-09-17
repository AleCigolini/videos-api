# API de Vídeos

Uma aplicação Spring Boot robusta para upload e gerenciamento de vídeos com integração Azure Blob Storage, streaming de eventos Kafka e ambiente de desenvolvimento Docker completo.

## Funcionalidades

- **Upload de Vídeos**: Endpoint REST API para upload de arquivos de vídeo (até 500MB)
- **Listagem de Vídeos**: Endpoints para listar vídeos processados com filtros por status
- **Consulta Individual**: Endpoint para consultar informações específicas de um vídeo
- **Armazenamento Azure**: Armazenamento seguro de vídeos usando Azure Blob Storage
- **Streaming de Eventos**: Integração Kafka para notificações de upload e atualizações de status
- **Consumer de Status**: Consumidor Kafka para processar atualizações de status de processamento
- **Cache Redis**: Cache distribuído para melhor performance com TTL configurável
- **Desenvolvimento Docker**: Ambiente de desenvolvimento completamente containerizado
- **Serviços Mock**: Desenvolvimento local com mocks do Azure Storage e Kafka
- **Validação de Arquivos**: Detecção de tipo MIME e validação de tamanho
- **Arquitetura Limpa**: Design orientado a domínio com clara separação de responsabilidades
- **Testes Abrangentes**: Testes unitários e de integração com Testcontainers
- **Documentação da API**: Documentação OpenAPI/Swagger

## Formatos de Vídeo Suportados

- MP4, AVI, MOV, WMV, FLV, WebM, MKV

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3.3.5**
- **Spring Kafka** para streaming de eventos
- **Redis** para cache distribuído
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
│   ├── config/             # Configurações Redis, Kafka e desenvolvimento
│   └── exception/          # Tratamento global de exceções
├── video/
    ├── application/         # Casos de uso e lógica de negócio
    ├── domain/             # Entidades e objetos de domínio
    ├── infrastructure/     # Integrações externas
    │   ├── azure/          # Azure Blob Storage (real + mock)
    │   ├── kafka/          # Produtores Kafka (real + mock)
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
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
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
- **Redis 7**: Cache distribuído com persistência
- **Kafka + Zookeeper**: Plataforma de streaming de eventos
- **Azurite**: Emulador do Azure Blob Storage
- **Kafka UI**: Interface web para monitoramento do Kafka

## 🔧 Perfis de Configuração

- **`local`**: Desenvolvimento com mocks habilitados (recomendado)
- **`dev`**: Desenvolvimento com serviços externos reais
- **`prod`**: Configuração de produção

## 📊 Serviços Mock

Para desenvolvimento local, a aplicação usa mocks inteligentes:

- **Mock Azure Storage**: Arquivos armazenados localmente em `/tmp/mock-azure-storage/`
- **Mock Kafka**: Eventos armazenados no Redis com TTL de 1 hora
- **Cache Redis**: TTL de 10 minutos para dados da aplicação

## 🔄 Fluxo de Processamento de Vídeos

### 1. Upload do Vídeo
1. Recebe arquivo de vídeo via endpoint REST
2. Valida tipo MIME e tamanho do arquivo
3. Faz upload para Azure Blob Storage (ou mock local)
4. Salva metadados no PostgreSQL
5. Armazena informações no Redis para consulta rápida
6. Publica evento no tópico Kafka `video-upload-events`

### 2. Processamento de Status
1. Consumer escuta o tópico `video-status-update-events`
2. Atualiza status no banco PostgreSQL
3. Atualiza cache Redis
4. Registra timestamp de processamento quando aplicável

### 3. Consulta de Vídeos
1. Verifica cache Redis primeiro
2. Consulta banco PostgreSQL se necessário
3. Retorna informações com link de download (se processado)

## 🧪 Testes

```bash
# Executar testes com Testcontainers
mvn test

# Testes de integração
mvn test -Dspring.profiles.active=test
```

## 📚 Documentação

- **Guia de Desenvolvimento**: Veja [DEVELOPMENT.md](DEVELOPMENT.md) para instruções detalhadas de configuração
- **Documentação da API**: Disponível em `/swagger-ui.html` quando a aplicação estiver rodando
- **Health Checks**: Disponível em `/actuator/health`

## 🎯 Endpoints da API

### Upload de Vídeo
```http
POST /api/v1/videos/upload
Content-Type: multipart/form-data

Parâmetros:
- file: Arquivo de vídeo (máx 500MB)

Resposta:
- 201: Upload realizado com sucesso
- 400: Arquivo inválido ou parâmetros incorretos
- 500: Erro interno do servidor
```

### Listar Todos os Vídeos
```http
GET /api/v1/videos

Resposta:
- 200: Lista de vídeos com informações de status e processamento
```

### Listar Vídeos por Status
```http
GET /api/v1/videos/status/{status}

Parâmetros:
- status: UPLOADED, PROCESSING, PROCESSED, FAILED

Resposta:
- 200: Lista filtrada de vídeos
```

### Consultar Vídeo por ID
```http
GET /api/v1/videos/{id}

Parâmetros:
- id: ID do vídeo

Resposta:
- 200: Informações detalhadas do vídeo
- 404: Vídeo não encontrado
```

### Health Check
```http
GET /actuator/health
```

## 🤝 Contribuindo

1. Faça um fork do repositório
2. Crie uma branch para sua feature
3. Faça suas alterações
4. Execute os testes: `mvn test`
5. Submeta um pull request

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo LICENSE para detalhes.
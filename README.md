💊 REDEMAISFARMA – API Java com Importação de Dados do Sistema Legado

API corporativa em Java 21 e Spring Boot 3.3.x usando Arquitetura Hexagonal (Ports & Adapters).
Integração nativa com Firebird (.FDB do Digifarma) para importação/sincronização de dados.
Pronta para ambientes DEV/TEST/PROD, com MapStruct, Lombok, Swagger, Actuator, Testcontainers, Jacoco, SpotBugs, Checkstyle.

✨ Visão Geral

Hexagonal (Domain ↔ Ports ↔ Adapters): regras de negócio desacopladas de infraestrutura.

Importação Firebird nativa via Jaybird (perfil firebird / prod), e MySQL para dev/test.

Qualidade & Observabilidade: Actuator, logs estruturados com X-Correlation-Id, Jacoco, SpotBugs, Checkstyle.

DX: Swagger/OpenAPI, perfis por ambiente, docker-compose para Firebird/MySQL.

🗂️ Estrutura do Projeto (resumo)
src/main/java/br/com/redemaisfarma/
├─ RedeMaisFarmaApiApplication.java
├─ config/                     # DataSources, Security, OpenAPI
├─ domain/                     # modelos/enums do domínio
├─ application/
│  ├─ controller/              # endpoints HTTP (REST)
│  ├─ dto/
│  │  ├─ request/              # *RequestDTO
│  │  └─ response/             # *ResponseDTO
│  ├─ mapper/                  # MapStruct
│  ├─ port/                    # ports inbound/outbound
│  ├─ service/                 # casos de uso/orquestração
│  ├─ session/                 # sessão/usuário atual
│  └─ validation/              # validators + annotations
└─ adapters/
   ├─ inbound/
   │  └─ web/                  # filtros/interceptors/security/advice/openapi
   └─ outbound/                # JPA/HTTP/legacy/mail/cache/storage/kafka-produtores


Controllers ficam em application/controller.
Inbound tem apenas infra de entrada (filtros, interceptors, JWT, exception translator, versionamento, OpenAPI customizers, schedulers/consumidores).
Outbound concentra persistência (JPA), Firebird legado, clientes HTTP, cache, e integrações externas.

🔧 Requisitos

Java 21 (JDK)

Maven 3.9+

Docker (opcional, para subir Firebird/MySQL localmente)

Arquivo do legado: digifarma6.FDB disponível localmente (ex.: C:\digifarma\database\digifarma6.FDB)

⚙️ Perfis de Execução

dev: MySQL local (ou container), Swagger habilitado, logs DEBUG.

test: Testcontainers (MySQL) em testes de integração.

firebird/prod: conexão nativa ao Firebird (Jaybird) + tunning de produção.

📦 Configuração (properties)
src/main/resources/application.yml (base)
spring:
  application:
    name: redemaisfarma-api
  jackson:
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

application-dev.properties (MySQL local)
spring.datasource.url=jdbc:mysql://localhost:3306/redemaisfarma?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
springdoc.swagger-ui.enabled=true
logging.level.org.hibernate.SQL=DEBUG

application-test.properties (Testcontainers)
spring.jpa.hibernate.ddl-auto=update
springdoc.swagger-ui.enabled=false

application-firebird.properties (legado Digifarma)
spring.datasource.url=jdbc:firebirdsql://localhost:3050/C:/digifarma/database/digifarma6.FDB?lc_ctype=UTF8
spring.datasource.username=sysdba
spring.datasource.password=masterkey
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.FirebirdDialect
logging.level.org.hibernate.SQL=INFO


Windows path no Firebird: use barra / ou escape \ duplo no JDBC.
Ajuste as credenciais conforme seu ambiente.

🐳 docker-compose (opcional p/ DEV)

docker/docker-compose.yml

version: "3.9"
services:
  firebird:
    image: jacobalberty/firebird:3.0
    container_name: firebird
    environment:
      ISC_PASSWORD: masterkey
      FIREBIRD_DATABASE: digifarma6.fdb
    ports:
      - "3050:3050"
    volumes:
      - ./firebird/data:/firebird/data   # coloque o .FDB aqui como digifarma6.fdb
    healthcheck:
      test: ["CMD-SHELL", "timeout 1 bash -c '< /dev/tcp/127.0.0.1/3050' || exit 1"]
      interval: 5s
      timeout: 2s
      retries: 30

  mysql:
    image: mysql:8.4
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: redemaisfarma
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/init:/docker-entrypoint-initdb.d


Copie seu digifarma6.FDB para docker/firebird/data/digifarma6.fdb.

🚀 Como subir
DEV (MySQL)
# (opcional) subir MySQL via docker-compose
docker compose -f docker/docker-compose.yml up -d mysql

# compilar e rodar com perfil dev
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev

Firebird (legado / importação)
# (opcional) subir Firebird via docker-compose
docker compose -f docker/docker-compose.yml up -d firebird

# executar apontando para seu .FDB
mvn clean spring-boot:run -Dspring-boot.run.profiles=firebird

Testes
# unit + integração (Testcontainers)
mvn -T 1C clean verify


Após verify, abra o Jacoco em target/site/jacoco/index.html.

🔄 Importação & Sincronização do Legado

Serviços-chave (exemplo):

application/service/sync/ProdutoLegacyService.java

application/service/sync/ProdutoSyncService.java

Uso típico:

Endpoint/Job que dispara a leitura no Firebird (via adapter adapters/outbound/legacy) → mapeia para DTO/Model → persiste no MySQL através dos ports/outbound JPA.

Opções de execução:

Endpoint (ex.: POST /api/legacy/produtos/sync)

Scheduler (adapters/inbound/scheduler) para sincronização periódica.

Os mappers (MapStruct) ficam em application/mapper e transformam Legacy → DTO/Domain → Entity.

🔐 Segurança (JWT)

Filtros JWT em adapters/inbound/web/security/* (ex.: JwtOncePerRequestFilter).

Configurações em config/SecurityConfig.java.

Refresh token via repositório em adapters/outbound/auth/jwt/*.

Fluxo típico:

POST /api/auth/login → retorna accessToken + refreshToken.

POST /api/auth/refresh → novo par de tokens.

Rotas protegidas com Authorization: Bearer <token>.

📚 Documentação & Saúde

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI JSON: /v3/api-docs

Actuator: /actuator/health, /actuator/metrics, /actuator/info

🔗 Endpoints principais (exemplos)

Health: GET /api/ping, GET /actuator/health

Produtos: GET /api/produtos, POST /api/produtos, GET /api/produtos/{id}

Pedidos: POST /api/pedidos, GET /api/pedidos/{id}, PUT /api/pedidos/{id}

Clientes: GET /api/clientes, POST /api/clientes

Config/Admin: GET /api/config, páginas de painel (se expostas)

Os nomes podem variar conforme seus controllers; ajuste aqui se necessário.

🧪 Testes (padrão)
src/test/java/br/com/redemaisfarma/
├─ unit/                # testes unitários (services/mappers)
├─ integration/         # @SpringBootTest + Testcontainers (MySQL)
│  ├─ BaseIntegrationTest.java
│  ├─ ProdutoFlowIT.java
│  ├─ PedidoFlowIT.java
│  └─ ClienteFlowIT.java
└─ support/containers/
   └─ MySqlContainerConfig.java

✅ Qualidade de Código

SpotBugs + Checkstyle: executados em mvn verify.

Jacoco: cobertura HTML em target/site/jacoco/index.html.

🧭 Convenções

Controllers: application/controller

DTOs: application/dto/request|response

Validators: application/validation(+/annotation)

Sessão: application/session

Inbound Infra: adapters/inbound/web/* (filters/interceptors/security/advice/openapi)

Outbound: adapters/outbound/* (JPA, legacy, http, cache, mail, storage, kafka-produtores)

🛠️ Scripts úteis (opcional)
scripts/
├─ dev-up.sh        # docker compose up (mysql/firebird)
├─ dev-down.sh
├─ clean-all.bat    # limpa Docker + Maven + cache
└─ verify-local.sh  # mvn -T 1C clean verify -Pdev

📄 Licença

Defina sua licença em LICENSE (ex.: MIT, Apache-2.0).

🤝 Contribuição

Crie branchs por feature (feat/…, fix/…).

Commits semânticos.

PR com descrição, screenshots (se front embutido), e checklist de testes.

💬 Dicas rápidas

Se estiver no Windows, confirme o caminho do .FDB no JDBC e permissões de leitura.

Em Firebird, mantenha lc_ctype=UTF8 para evitar problemas com acentuação.

Para diagnosticar importações, habilite logs dos adapters de legacy (logging.level.br.com.redemaisfarma.adapters.outbound.legacy=DEBUG).

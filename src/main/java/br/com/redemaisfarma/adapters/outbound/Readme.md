Outbound — Guia Rápido
Este README.md mapeia a estrutura do Outbound e explica como cada peça se encaixa. Use-o para navegar, criar novos adaptadores e manter as dependências limpas.

Visão geral
O Outbound é a borda da aplicação que conversa com “o mundo de fora”:

Banco de dados (JPA/Repositories)

Gateways HTTP (pagamento, frete, etc.)

Mensageria (Kafka/Rabbit)

E-mail

Cache (Redis)

Armazenamento (S3/Local)

Integrações legadas

Regra de dependência (Clean/Hexagonal)
core (domínio) não conhece outbound.

application orquestra portas (interfaces) e depende de implementações do outbound por injeção.

outbound implementa portas definidas em application/core.

Layout de pastas (alvo)
arduino
Copiar
Editar
src/main/java/br/com/redemaisfarma/adapters/outbound/
├─ auth/
│  ├─ adapter/
│  │  ├─ JwtTokenAdapter.java               // gera/valida JWT
│  │  ├─ PasswordEncoderAdapter.java        // BCrypt
│  │  └─ RefreshTokenStoreAdapter.java      // persiste/revoga refresh
│  ├─ config/
│  │  ├─ JwtProperties.java
│  │  └─ AuthProperties.java
│  ├─ repository/
│  │  └─ RefreshTokenJpaRepository.java
│  ├─ entity/
│  │  └─ RefreshTokenEntity.java
│  ├─ model/
│  │  └─ RefreshToken.java                  // value object
│  ├─ mapper/
│  │  └─ RefreshTokenMapper.java
│  └─ exception/
│     ├─ InvalidTokenException.java
│     └─ TokenExpiredException.java
│
├─ jobs/
│  └─ RefreshTokenCleanupJob.java           // limpeza de tokens expirados/revogados
│
├─ persistence/
│  ├─ jpa/
│  │  ├─ ProdutoRepository.java
│  │  ├─ UsuarioRepository.java
│  │  ├─ PedidoRepository.java
│  │  └─ ...
│  ├─ entity/
│  │  ├─ ProdutoEntity.java
│  │  ├─ UsuarioEntity.java
│  │  ├─ PedidoEntity.java
│  │  └─ ...
│  └─ mapper/
│     ├─ ProdutoMapper.java
│     ├─ UsuarioMapper.java
│     ├─ PedidoMapper.java
│     └─ ...
│
├─ http/
│  ├─ client/
│  │  ├─ PaymentGatewayClient.java
│  │  └─ ShippingClient.java
│  └─ config/
│     └─ WebClientConfig.java
│
├─ email/
│  ├─ adapter/
│  │  └─ MailSenderAdapter.java
│  └─ config/
│     └─ MailProperties.java
│
├─ messaging/
│  ├─ kafka/
│  │  ├─ config/KafkaProducerConfig.java
│  │  └─ producer/OrderEventProducer.java
│  └─ rabbit/
│     ├─ config/RabbitConfig.java
│     └─ producer/...
│
├─ cache/
│  ├─ config/RedisConfig.java
│  └─ TokenBlacklistRepository.java
│
├─ storage/
│  ├─ adapter/
│  │  ├─ S3StorageAdapter.java
│  │  └─ LocalStorageAdapter.java
│  └─ config/
│     └─ StorageProperties.java
│
└─ legacy/
   ├─ repository/
   ├─ mapper/
   ├─ entity/
   └─ client/
Papéis de cada módulo
auth
Objetivo: autenticação e autorização (JWT, refresh token, hashing de senha).

Depende de: spring-security-crypto, JPA (para refresh tokens).

Fornece: implementações para portas como TokenService, PasswordHashService, RefreshTokenService.

jobs
Objetivo: execução de tarefas agendadas, como limpeza de tokens expirados.

Exemplo atual: RefreshTokenCleanupJob remove tokens inválidos do banco.

persistence
Objetivo: persistência relacional (JPA/Hibernate).

Itens: entities, repositórios JPA, mapeadores.

http
Objetivo: comunicação HTTP com serviços externos.

Itens: clientes WebClient/RestTemplate.

email
Objetivo: envio de e-mails via SMTP/serviço externo.

messaging
Objetivo: publicar eventos em filas/streams.

cache
Objetivo: Redis/memória para tokens, throttling, lookups.

storage
Objetivo: armazenamento de arquivos.

legacy
Objetivo: integração com sistemas legados.

Convenções
Pacotes: br.com.redemaisfarma.adapters.outbound.<módulo>...

Nomes:

XxxAdapter → implementações de portas

XxxClient → HTTP

XxxRepository → Spring Data

XxxEntity → JPA

XxxMapper → MapStruct/manual

Configuração (exemplo)
yaml
Copiar
Editar
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: 30        # minutos
  refresh-token-expiration: 43200    # 30 dias
  issuer: redemaisfarma-api
Checklist de implementação
 Porta/interface definida em application/core

 Adapter implementa a porta em outbound/auth/adapter

 Config externa via @ConfigurationProperties

 Mapper pronto (entity ↔ domínio)

 Exceções externas traduzidas

 Testes unitários e de integração

 Documentado no README
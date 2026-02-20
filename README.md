# RedeMaisFarma API

API corporativa (Java 21 + Spring Boot 3.3) embalada em hexagonal: o módulo `boot-app` entrega REST, páginas
Thymeleaf e integrações com banco relacional, Firebird legado, storage e e-mail.

## Architecture snapshot
- Hexagonal layout dentro de `boot-app/src/main/java/br/com/redemaisfarma`: `domain` → `application` → `adapters`.
- `infra`, `mocks` e `infra` extras carregam helpers de infraestrutura, payloads simulados e scripts de apoio.
- O módulo principal cuida de filtros, segurança JWT/OAuth2/OpenAPI, Flyway, storage (S3/local), e-mail e jobs.

## Modules & layers
- `domain`: entidades/enum e serviços core (`EstoqueService`, sync, catálogos, finanças, IA de prompts).
- `application`: ports, services, mappers/DTOs, validações, sessões e views (admin/cliente).
- `adapters/inbound`: controllers REST/MVC, filtros, segurança, OpenAPI, templates, schedulers e páginas públicas/admin.
- `adapters/outbound`: JPA/Flyway, Firebird legacy (Jaybird), storage, HTTP clients, cache (Redis), e-mail, jobs/Kafka.
- `config`: beans globais, segurança, observabilidade, datasources e mensageria.
- `docs/`: guias de UI (`admin-frontend-refactor`, `client-flow-wireframes`), complementando `README_DEV.md`.

## Running the platform
### Build & verify
- `./mvnw -pl boot-app clean verify` compila, executa unitários/integração e dispara Jacoco, SpotBugs e Checkstyle.

### Executar localmente
- `./mvnw -pl boot-app spring-boot:run -Dspring-boot.run.profiles=dev` liga o serviço contra MySQL local (host/credenciais em `application-dev.yml` ou `DEV_DATASOURCE_*`).

### Docker-powered dev
- `docker compose -f docker-compose.dev.yml up -d mysql mailpit cliente-mock` levanta infra; suba o app com `./mvnw -pl boot-app spring-boot:run -Dspring-boot.run.profiles=docker` ou rode o container via compose para o host `18080`.

### Testes e legado
- `./mvnw -pl boot-app test -Dspring.profiles.active=test` usa Testcontainers (MySQL) e `application-test.yml`; adicione `-DskipITs` para ignorar integrações.
- Perfil `legacy` (`application-legacy.yml`) ativa o conector Firebird (`FIREBIRD_*`) para sincronização de catálogo via `ProdutoLegacyService`.

## Profiles & configuration
- `dev` (padrão): MySQL em `localhost:3306`, Swagger ativo, Mailpit opcional.
- `docker`: usa serviços definidos em `docker-compose.dev.yml`.
- `test`: Testcontainers com MySQL e perfis de teste.
- `legacy`/`firebird`: conectores Jaybird, esquema Firebird e variáveis `FIREBIRD_HOST/DB/USER/PASSWORD`.
- Alertas de estoque configurados por `app.estoque.alerta.*` (limite 10, percentual 10, cron padrão `0 */30 * * * *`, cooldown 60 min, e-mail opcional).

## Observabilidade & docs
- Swagger UI aparece em `/docs`, OpenAPI em `/v3/api-docs` e Actuator expõe `/actuator/health`, `/actuator/info` e `/actuator/metrics`.
- Logging via `logback-spring.xml`, filtros de correlação e auditoria (`HttpAuditFilter`), e métricas de jobs/Kafka.
- Templates e assets estão em `src/main/resources/templates/pages` e `static/`; JavaScript auxilia UI cliente/admin.
- **Campanhas por e-mail**: o worker registra Micrometer `email_campaign.worker.processed/sent/retry/failed` visível em `/actuator/metrics`; use esses counters para alertar quando `failed > 0` ou quando filas não avançam. A UI admin também exibe preview, cancelamento e agora suporta filtros por categoria, recência (dias) e ticket médio, mantendo os campos VIP/Inativos/Todos e agilizando pausa/retoma sem impactar a fila atual.
- **Automação de recompra**: o scheduler lê pedidos `ENTREGUE`, evita reenvios usando `email_campaign_queue` e dispara campanhas usando o template `mail/recompra`. Monitore `recompra.automation.processed`, `.skipped` e `.enqueued` via Actuator/Prometheus para confirmar que o volume e a deduplicação estão dentro do esperado; qualquer falha retorna log detalhado e a fila `email_campaign_queue` mostra os emails ligados.
- **Volta ao estoque**: clientes se inscrevem por produto (`product_stock_subscription`) via `POST /api/public/produtos/{produtoId}/stock/subscribe` (`email`, `nome`). O job `mail/back-in-stock` rastreia o estoque > 0 e dispara notificações únicas pelo `email_campaign_queue`. Observe `back_in_stock.automation.processed`, `.skipped` e `.enqueued` no Actuator/Prometheus e no painel `/admin/marketing/emails/campanhas/fila` para garantir entregas consistentes.

-## Produtos
-O domínio de produtos gira em torno de `ProdutoEntity`, que carrega nome, descrição, preço de custo/venda, código de barras, estoque, marca, categoria e timestamps (`dataCadastro`, `dataAtualizacao`, `PublicadoEm`). Os DTOs (`ProdutoResponseDTO`, `ProdutoRequestDTO`) expõem esses campos aos serviços públicos e ao admin.
-O catálogo público usa `PublicProdutoController` e `ProdutoExportController` para listar/filtrar/exportar produtos (`/api/public/produtos`, `/api/public/produtos/{id}`, exportação CSV/XLS) com buscas por categoria, tags e destaque em promoções. A API autenticada (`/api/v2/produtos`) mantém paginação com filtros predefinidos.
-O painel admin (`ProdutoAdminRestController`, `ProdutoAdminPageController`) dá suporte a CRUD completo, publicação, upload de imagens, sincronização com Firebird via `ProdutoSyncService` e repositórios JPA para disponibilidade global.
-A OpenAPI (`/v3/api-docs`) expõe `/api/admin/produtos` com seus parâmetros extras (validador, upload, imagem IA, validar/publicar) para consumidores Swagger/QA.
-Automatizações ligadas a produtos incluem `ProductBackInStockAutomationService` (assinaturas via `POST /api/public/produtos/{produtoId}/stock/subscribe`) e campanhas de e-mail (`mail/back-in-stock`, `mail/cart-abandon`, `mail/reengagement`) alimentadas pela fila `EmailCampaignQueue`.
-Produtos também são fonte de relatórios e dashboards (via `RelatorioResponseDTO`, `ReportService`, `AdminProductReportController`) e alimentam os feeds usados pelo frontend e mobile para mostrar estoque, promoções, destaques e alertas em tempo real.
-### Fluxo de imagem IA
-A página `/admin/imagens` e os endpoints `POST /api/admin/imagens/{produtoId}/queue` e `/regenerate` disparam eventos `ProductImageRequestedEvent` e forçam novos jobs via `ProductImageJobService`. `ProductImageAdminPageController` expõe também `/api/admin/imagens/jobs` para listar jobs (status QUEUED/PROCESSADO/ERRO) e retorna `EnqueueResponse` com metadados (status, última execução). Usem esses dados para atualizar a UI de edição/listagem (toasts, badges de fila) e encadear feedback imediato sempre que o job for enfileirado ou regenerado para o mesmo produto.
-### Adicionar/atualizar produtos
-1. `POST /api/admin/produtos` cria produtos a partir de `ProdutoRequestDTO` (nome, descrição, preçoVenda, estoque, categoria, códigoBarras e `imagem` opcional). O backend exige imagem quando `ativo=true`, valida nome único e marca o registro como `IMPORTADO`.
-2. Após criar, atualize a imagem com `POST /api/admin/produtos/{id}/imagem` (multipart/form-data com campo `file`), que salva o arquivo via `ImageStorageService` e atualiza o `ProdutoEntity`.
-3. Para liberar o item no catálogo público mova o status: `POST /api/admin/produtos/{id}/validar?validador={user}` registra `ProdutoStatus.VALIDADO` e `POST /api/admin/produtos/{id}/publicar?validador={user}` define `STATUS.PUBLICADO`, `publicadoEm` e `situacao`.
-4. O `PUT /api/admin/produtos/{id}` ajusta atributos (preço, estoque, texto); `DELETE /api/admin/produtos/{id}` remove o registro e as automações de marketing (carrinho, recompra, volta ao estoque) só consideram produtos ativos com `preco > 0`, `estoque > 0` e `disponivel=true`.
-5. Use o DTO `ProdutoResponseDTO` combinado com `ProdutoMapper` para visualizar os dados (incluindo `situacao`, `destaqueCarrossel` e `tags`). Filtre pelo painel admin antes de acionar o webhook da fila `EmailCampaignQueue` para evitar duplicatas.

## Key flows & integrations
- **Auth & conta**: `/api/auth/login`, refresh, OTP/email claim e reset; MVC para login, cadastro e alteração de senha (`AccountController`, `/auth/*`, `/cliente/senha`).
- **Site público**: landing, carrossel, catálogo/CSV de produtos (`PublicProdutoController`, `ProdutoExportController`, templates em `templates/pages/cliente`).
- **Cliente self-service**: carrinho REST (`/api/carrinho/itens`), checkout `/api/cliente/me/checkout`, CRUD de perfil/endereços/pedidos (`ClienteSelfController`), e `CurrentClienteProvider` para contexto autenticado.
- **Cliente self-service** (perfil público/cliente autenticado): além de `/api/cliente/me`/endereços/checkout/pedidos, os controladores `ClienteSelfApiController` e `AccountController` expõem troca de senha, OTP/resets (`/api/auth/otp/*`, `/api/auth/resetar-senha`, `/api/auth/password/reset-otp`), encerramento de sessões ativas e logout para limpar cookies/tokens.
- **Admin**: controllers e páginas para catálogo completo, clientes, pedidos, relatórios, marketing, configurações, estoque e rotas OSRM (e.g., `ProdutoAdminRestController`, `AdminEntregaRotaController`, templates em `templates/pages/admin`).
- **Clientes (Admin)**: `/api/admin/clientes/{id}/resumo`, `/api/admin/clientes/{id}` (GET/PATCH), `/ativar`, `/desativar`, `/pedidos`, `/enderecos` (GET/POST/PUT/DELETE); esses endpoints permitem consultar histórico, atualizar contatos, habilitar/desabilitar contas e listar pedidos detalhados.
- **Admin marketing**: `AdminMarketingEmailCampaignController` e a nova API `/api/admin/marketing/emails/campanhas` permitem CRUD/preview/segmentos, com o painel `/admin/marketing/emails/campanhas/fila` exibindo status da fila e logs do worker junto aos contadores `email_campaign.worker.*`.
- Essa API agora aceita/exibe os campos `segmentoDetalhado`, `agendarTimezone` e `validationStatus`, permitindo gravar filtros ricos e validar a campanha com a timezone correta antes de disparar os envios.
- O endpoint `POST /api/admin/marketing/emails/campanhas/{id}/validate` ajusta o `validationStatus` (PENDING/APPROVED/REJECTED/READY) e é recomendado para QA aprovar o conteúdo/público antes de liberar envios.
- Agora existem endpoints `POST /api/admin/marketing/emails/campanhas/{id}/pause` e `/resume` para desligar/retomar uma campanha agendada sem precisar recriá-la, mantendo os logs `/fila` e os counters `email_campaign.worker.*` em sincronia.
- **Integrações**: MySQL + Flyway, Firebird legado, storage `S3StorageAdapter`/`LocalStorageAdapter`, e-mail (Mailpit em dev), Kafka desligado por padrão, OSRM para rotas de entrega.
- **Jobs & alertas**: agendamentos em `adapters/inbound/scheduler`, alerta de estoque via log/e-mail, sincronização de catálogo, limpeza de tokens/OTP.

## Roadmap (em andamento)
### Email marketing (ideias)
- Campanha promocional segmentada (categorias, recencia, ticket medio).
- Carrinho abandonado (1h/24h com cupom).
- Recompra automatica (medicamentos de uso continuo).
- Aniversario do cliente (cupom).
- Lancamentos e novidades por categoria.
- Volta ao estoque (inscricao por produto).
- Status de pedido e entrega (confirmacao, separacao, envio, entregue).
- Cashback/beneficios (saldo, pontos, validade).
- Conteudo educativo e sazonal.
- Reengajamento de clientes inativos.

Em M3 estamos automatizando carrinho abandonado (envios para 1h e 24h) e reengajamento (30/60/90 dias) via `EmailCampaignQueue` para campanhas com cupons e mensagens específicas; o dashboard `/admin/marketing/emails/campanhas/fila` mostra o status das filas/ logs e as métricas `email_campaign.worker.*` acompanham cada job.

### Outras ideias em andamento
- Interface mobile cliente alinhada ao web.
- Pagina de produtos mais chamativa mesmo sem itens (empty state rico).
- Barra de busca com filtros (ex.: lupa de preco, seletor de categorias).
- Venda rapida com leitor de codigo de barras confiavel.
- Melhorias visuais no sidebar admin (recolher) e login (toggle senha).
- Pagamentos: metodos personalizados + PIX/dinheiro + retirada na loja.
- Ajustes de branding/hero e imagens iniciais do cliente.

### Backlog tecnico (prioridade + estimativa)
- P0 | Email marketing minimo viavel (segmentacao basica + fila + templates) | 5-7 dias
- P0 | Tela de campanhas (CRUD + agendamento) | 4-6 dias
- P0 | Envio transacional padronizado (pedido/entrega) | 3-4 dias
- P1 | Carrinho abandonado (regras + disparo) | 3-5 dias
- P1 | Recompra automatica (ciclos + opt-in) | 4-6 dias
- P1 | Reengajamento inativos (regra + lista) | 2-3 dias
- P1 | Volta ao estoque (subscribe + trigger) | 3-4 dias
- P1 | Integracoes com filtros na busca (preco/categoria) | 2-3 dias
- P2 | Conteudo educativo (templates + agenda) | 2-3 dias
- P2 | Cashback/beneficios (comunicacao por email) | 3-4 dias

### Ordem sugerida (milestones)
1) M1 - Fundacao email: fila + templates + envio transacional
2) M2 - Campanhas: CRUD + agendamento + segmentacao basica
3) M3 - Automacoes: carrinho abandonado + reengajamento + recompra
4) M4 - Engajamento: volta ao estoque + conteudo + cashback

### Detalhamento P0 (tarefas)
- Modelos e tabelas: campanha, publico, fila, log de envio
- Serviço de segmentacao: filtros simples (categoria, recencia, ticket)
- Worker de envio: throttling, retry, status (pendente/enviado/falha)
- Templates: base + promocao + pedido/entrega
- Admin UI: lista, criacao, agendamento, preview
- Observabilidade: logs e metrics por campanha

## Project health & analysis
- A estrutura hexagonal garante separação clara entre domínio, portos e adaptadores; `boot-app` reúne APIs REST, MVC e infraestrutura.
- Qualidade e observabilidade estão integradas: `clean verify` cobre Jacoco/SpotBugs/Checkstyle, Actuator e OpenAPI/Swagger rastreiam saúde e contratos.
- Admin/catalogo está estabilizado (`/admin/produtos`, `/api/admin/produtos`, upload + IA + export); o cliente vê templates e scripts dedicados para login, carrinho, checkout e dashboard.
- Legacy Firebird é ativado sob demanda (`firebird/legacy`), com sincronização via `ProdutoSyncService` e `LegacyProdutoJdbcAdapter`.
- Testes unitários e integrações existentes focam auth/produto; faltam cenários ponta a ponta para checkout/pagamento/estoque, marketing/agendamentos, storage/S3 e OSRM.

## Known gaps & next steps
- PRIORIDADE EXTREMA: adicionar foto de perfil em "Minha Conta" e "Meus Dados" (upload + exibicao).
- Implementar `AccountController.extrairUsuario` para liberar a alteração de senha do cliente.
- Documentar novos endpoints (carrinho/checkout, marketing/configurações, rotas de entrega) no OpenAPI com `@Tag`/`@Operation`.
- Ampliar testes integrando checkout/pagamento/estoque, cliente self-service, marketing/agendamentos, Firebird, storage e OSRM.
- Alinhar telas admin ao shell em `docs/admin-frontend-refactor.md` e experiência cliente aos wireframes de `docs/client-flow-wireframes.md`.

## Sinopse do plano de desenvolvimento da API
- **Progresso**: a fundação e os contratos principais (autenticação, carrinho, checkout, cliente) estão descritos nos passos 6-9, e as milestones M1-M4 estão registradas em `docs/backlog.md`.
- **Decisões chave**: priorizar o worker de e-mail e métricas Micrometer no M1, seguir o cronograma de campanhas do backlog e usar o plano de 50 passos para orientar front-end e mobile.
- **Qualidade e testes**: cada milestone exige `./mvnw -pl boot-app clean verify` ou `./mvnw -pl boot-app test -Dspring.profiles.active=test`, cobertura de OTP/reset e instrumentação dos jobs (`/actuator/metrics`, `email_campaign.worker.*`).
- **Próximos passos imediatos**: concluir upload de avatar/foto de perfil, ajustar `AccountController.extrairUsuario`, documentar perfis/env vars no README e alinhar QA com `README_DEV.md`, `docker-compose.dev.yml` e as instruções do backlog.
- **Restrições e dependências**: validar Firebird (`legacy`/`firebird`), storage (S3 vs local), SMTP/queue (`APP_MAIL_*`, `email_delivery`, `email_campaign_queue`) e manter os scripts de observabilidade descritos no backlog.



## API roadmap (prioridade)
### M1 - APIs principais do cliente
- Completar os endpoints P0 listados em  Backlog de endpoints para o app cliente (GET/PUT /api/cliente/me, avatar, carrinho CRUD, checkout resumo/finalizar e pedidos list/detalhe) antes de pular para automacoes de marketing.
- Garantir que os contratos usados por cada contrato referenciem o envelope de erro, paginacao e DTOs descritos nos passos 8-9 (ex: ClienteResponse, CartItemResponse, PedidoResumoResponse).
- Resolver os gaps imediatos (foto de perfil backend/storage e AccountController.extrairUsuario) para permitir atualizacao de senha e upload/exibicao de avatar dentro do mesmo ciclo.

### M2 - Testes e observabilidade
- Rodar ./mvnw clean verify ou ./mvnw test -Dspring.profiles.active=test (com Docker/Testcontainers ativos) para cobrir auth, produto, checkout/pagamento/estoque, marketing/agendamentos, Firebird, storage e OSRM antes de liberar cada milestone.
- Adotar o checklist do README_DEV.md (ambiente Docker, clean verify, QA de checkout) e criar testes de controlador para /api/admin/produtos e fluxos de alerta (estoque baixo, fila de e-mail, jobs/Kafka desativados) com logs/metrics observaveis.
- Monitorar storage/S3 e Firebird via os mesmos hooks usados hoje (Actuator, app.estoque.alerta.*, EmailDelivery) para validar entregas e dependencias externas.
- Cobrir `/api/admin/produtos` (listar, criar, atualizar, excluir, validar, publicar e upload de imagem) e o `EstoqueBaixoNotificacaoJob` com testes dedicados garante regressões do admin e dos alertas; acompanhe os indicadores pelos endpoints `/actuator/health`, `/actuator/metrics`, pelo status de Firebird/Storage/S3 e pelos flags `app.estoque.alerta.*` + a fila `email_delivery` do `EmailDeliveryWorker`.

### M3 - Documentacao e onboarding de endpoints
- Usar Tag/Operation para cada novo contrato (carrinho, marketing, rotas de entrega), atualizar Swagger/OpenAPI e expor no /docs e /v3/api-docs para que clientes e mobile vejam os recursos vivos.
- Registrar fluxos de OTP/email claim/reset no README (incluindo curl para /api/auth/otp/start, /api/auth/resetar-senha, env vars relevantes APP_MAIL_*, APP_WEB_BASE_URL, APP_MAIL_REPLY_TO), reforcando os passos 9 e a operacao do plano operacional descrito no final do README.
- Documentar a diferenca entre perfis (dev, docker, test, legacy/firebird) e quais variaveis devem ser populadas para SMTP, DB e storage (como exigido no plano operacional), incluindo os scripts/QA no README.
- #### Fluxos OTP e e-mail marketing
- 1. `POST /api/auth/otp/start` ({{APP_WEB_BASE_URL}}/api/auth/otp/start): envie `{ "canal": "email", "destino": "cliente@exemplo.com" }` e guarde `deliveryId`; `APP_MAIL_ENABLED=true`, `APP_MAIL_FROM`/`APP_MAIL_REPLY_TO` precisam estar configurados no perfil usado.
- 2. `POST /api/auth/otp/verify` com `{ "deliveryId": "...", "code": "123456" }` obtém o token temporário; confirme os logs `Hero available...` para garantir que o sender (`SettingsMailSenderAdapter`) carregou APP_MAIL_*.
- 3. Para reset de senha via token, pague `POST /api/auth/esqueci-senha`, aguarde o e-mail (`email_delivery`, `email_campaign_queue`), use `/api/auth/validar-token?token=...` e finalize com `POST /api/auth/resetar-senha`.
- 4. O `EmailDeliveryWorker` monitora `email_delivery` e respeita `APP_MAIL_*`, `APP_MAIL_REPLY_TO` e o status `email_delivery.status` (PENDING → SENT); use `curl -H "Authorization: Bearer $TOKEN" https://.../actuator/health` e `/actuator/metrics` para validar dependências (MySQL/Testcontainers, S3, Firebird).
- #### Perfis e variáveis
- - `dev` roda contra MySQL local/Swagger/Mailpit; ative `APP_MAIL_ENABLED=true` (Mailpit) e `APP_WEB_BASE_URL=http://localhost:18090`.
- - `docker` aponta para os serviços do `docker-compose.dev.yml`; mantenha `APP_MAIL_*` definidas (FROM, REPLY_TO, CSS_URL) e `APP_WEB_BASE_URL` no `config`.
- - `test` usa Testcontainers/MySQL e desativa Mailpit (APP_MAIL_ENABLED=false); a fila `email_delivery` é limpa a cada ciclo.
- - `legacy` habilita Firebird/Jaybird e os env vars `FIREBIRD_*` para sincronização; documente o fallback `StorageAdapter` (S3 vs Local) e os indicadores do `app.estoque.alerta.*`.

### M4 - Engajamento, marketing e integracoes legadas
- Avancar nos itens de email marketing (campanhas, automacoes, recompras, volta ao estoque, cashback e conteudo educativo) conforme o backlog P0/P1/P2 e os milestones M1-M4 descritos na secao de Roadmap atual.
- Ampliar integracoes com legacy Firebird, storage e OSRM usando as rotinas existentes de sincronizacao (ProdutoSyncService, LegacyProdutoJdbcAdapter, alertas de estoque) antes de ativar filtros de busca e integracoes adicionais.
- Documentar no README como o admin observa logs/QA (filtros, sidebar, hero responsive) e incluir checkpoints de QA a cada entrega (ex: email_delivery, email_campaign_queue, env vars Railway, scripts temp_*).

## Resources & references
- Guias de UI: `docs/admin-frontend-refactor.md`, `docs/client-flow-wireframes.md`.
- Desenvolvimento: `README_DEV.md` resume comandos, convenções e checklist de PR.
- Configuração: `docker-compose*.yml`, `infra/` helpers, `mocks/` payloads, `AGENTS.md` para instruções de agente.

## Branding & hero loop
- Atualize o hero público em `/admin/configuracoes/geral` → **Marca no site**: mantenha o upload/URL da imagem principal e use o novo campo **Loop em vídeo (URL)** para apontar um WebM/MP4 loopado (preferencialmente WebM VP9 com alfa) hospedado em CDN/S3/local acessível. O hero usa `absorventepronto.png` como fallback e permite que o texto de destaque continue sendo controlado por `branding.home_hero_texto`.
- O controller `BrandingModelAdvice` agrega `brandingHomeHeroVideoUrl`, `brandingHomeHeroVideoPoster` e `brandingHomeHeroVideoType`; a view renderiza `<video autoplay loop muted playsinline preload="auto">` com esse poster para clientes que suportam o vídeo e aplica o gradiente atualizado no CSS para garantir contraste (o vídeo fica em evidência e o produto continua destacado no card que já existia).
- Teste o hero em desktop, tablet e mobile para confirmar que o vídeo autoload funciona, que o fallback (imagem+texto) aparece quando o vídeo não carrega e que o log `Hero available publicly...` indica qual recurso foi usado no momento da requisição. Mantemos o mesmo card de produto principal (com CTA) ao lado da mídia animada para preservar a experiência de publicidade.

## Plano de 50 passos para o aplicativo (cliente/admin)
1) Alinhar escopo do app (cliente, admin, ambos) e definir metas de negocio.
2) Mapear personas, jornada e tarefas criticas do cliente final.
3) Consolidar requisitos funcionais e nao funcionais.
4) Definir arquitetura do app (web responsivo, PWA ou mobile nativo).
5) Definir estrategia de autenticacao (JWT/refresh/OTP) para o app.
6) Inventariar endpoints atuais e lacunas de API para o app.
7) Criar backlog de endpoints faltantes e priorizar.
8) Padronizar contratos de API (DTOs, erros, paginacao).
9) Documentar fluxo completo de login, cadastro e recuperacao de senha.
10) Definir layout base (header, nave, tabs) e grids responsivos.
11) Criar design system do app (tokens, cores, tipografia, icones).
12) Definir componentes UI base (buttons, inputs, cards, badges).
13) Especificar estados globais (loading, empty, error, offline).
14) Definir fluxo de catalogo e filtros (busca, categoria, preco).
15) Implementar listagem de produtos (publico e logado).
16) Implementar pagina de detalhe do produto (imagem, preco, estoque).
17) Adicionar produtos relacionados e recomendacoes basicas.
18) Definir regra de estoque (alerta, indisponivel, backorder).
19) Implementar carrinho (add/remove/quantidade).
20) Implementar checkout (endereco, entrega, pagamento, resumo).
21) Padronizar mascaras e validacoes de dados do cliente.
22) Implementar "Minha conta" e "Meus dados".
23) Implementar upload de foto de perfil (UI + backend + storage).
24) Implementar lista e detalhe de pedidos do cliente.
25) Implementar status de pedido e timeline de entrega.
26) Criar pagina de notificacoes do cliente.
27) Implementar central de avisos no dashboard admin.
28) Implementar configuracoes de alertas de estoque (limite e notificacao).
29) Implementar area de promocoes e ofertas.
30) Implementar favoritos / lista de desejos.
31) Implementar historico de buscas e sugestoes.
32) Implementar cupons e beneficios.
33) Implementar chatbot/ajuda e FAQ contextual.
34) Implementar pagina de contato e suporte (email/whatsapp).
35) Implementar politicas e termos no app.
36) Adicionar tracking basico (eventos-chave do funil).
37) Implementar dashboard de metricas basicas (admin).
38) Padronizar logs e observabilidade do app.
39) Implementar cache e performance (imagens, lazy load).
40) Adicionar fallback offline (PWA) ou modo resiliente.
41) Revisar seguranca (CORS, rate limit, headers).
42) Revisar acessibilidade (foco, contraste, labels).
43) Criar suite de testes unitarios e de integracao.
44) Criar testes E2E para fluxos criticos.
45) Configurar CI/CD e pipelines de QA.
46) Preparar ambiente de staging com dados mascarados.
47) Executar bateria de testes de regressao.
48) Criar plano de release e rollback.
49) Monitorar pos-deploy (erros, funil, performance).
50) Rodar ciclo de melhorias continuas e backlog trimestral.

## Levantamento de endpoints para app cliente (passo 6)
### Auth
- POST /api/auth/login
- POST /api/auth/register
- POST /api/auth/otp/start
- POST /api/auth/otp/verify
- POST /api/auth/register/complete-otp
- POST /api/auth/password/reset-otp
- POST /api/auth/esqueci-senha
- GET  /api/auth/validar-token
- POST /api/auth/resetar-senha
- POST /api/auth/email-claim/start
- POST /api/auth/email-claim/verify

### Catalogo e vitrine
- GET /produtos (MVC)
- GET /produtos?q= (MVC)
- GET /produto/{id} e /produtos/{id} (MVC)
- GET /api/public/vitrine/destaques
- GET /api/v2/produtos
- GET /api/v2/produtos/{id}

### Carrinho e checkout
- GET /carrinho (MVC)
- POST /carrinho/adicionar
- POST /carrinho/atualizar
- POST /carrinho/remover
- GET /checkout (MVC)

### Conta do cliente
- GET /cliente/conta (MVC)
- GET /cliente/dados (MVC)
- POST /cliente/dados (MVC)
- POST /cliente/avatar (MVC)
- GET /cliente/senha (MVC)
- POST /cliente/senha (MVC)

### Pedidos do cliente
- GET /cliente/pedidos (MVC)
- GET /cliente/pedidos/{id} (MVC)
- GET /api/v2/pedidos
- GET /api/v2/pedidos/{id}

### Lacunas para o app mobile
- API de carrinho (listar/add/update/remover) com auth
- API de checkout (resumo + finalizar pedido)
- API de perfil do cliente (meus dados, endereco, foto)
- API de pedidos do cliente (filtrado por usuario autenticado)
- API de notificacoes/avisos do cliente
- API de favoritos/lista de desejos (se entrar no MVP)

## Backlog de endpoints para o app cliente (passo 7)
### P0 (MVP 1 mes)
- GET /api/cliente/me (perfil do cliente autenticado)
- PUT /api/cliente/me (atualizar dados do cliente)
- POST /api/cliente/me/avatar (upload de foto)
- GET /api/cliente/me/pedidos (lista pedidos do cliente)
- GET /api/cliente/me/pedidos/{id} (detalhe pedido)
- GET /api/cliente/me/carrinho (listar itens)
- POST /api/cliente/me/carrinho (adicionar item)
- PUT /api/cliente/me/carrinho/{produtoId} (atualizar quantidade)
- DELETE /api/cliente/me/carrinho/{produtoId} (remover item)
- GET /api/cliente/me/checkout/resumo (resumo do pedido)
- POST /api/cliente/me/checkout/finalizar (criar pedido)

### P1 (pos-MVP)
- GET /api/cliente/me/notificacoes
- POST /api/cliente/me/notificacoes/lidas
- GET /api/cliente/me/favoritos
- POST /api/cliente/me/favoritos
- DELETE /api/cliente/me/favoritos/{produtoId}

## Padrao de contratos de API (passo 8)
### Envelope de erro (padrao)
{
  "timestamp": "2025-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descricao do erro",
  "path": "/api/cliente/me"
}

### Paginacao (padrao)
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 120,
  "totalPages": 6
}

### Exemplos de DTOs (MVP)
// ClienteResponse
{
  "id": 1,
  "nome": "Cliente",
  "email": "cliente@email.com",
  "cpf": "00000000000",
  "telefone": "",
  "avatarUrl": "https://...",
  "enderecos": []
}

// CartItemResponse
{
  "produtoId": 2,
  "nome": "Produto",
  "imagem": "https://...",
  "preco": 19.9,
  "quantidade": 2,
  "subtotal": 39.8,
  "invalido": false,
  "motivo": null,
  "estoque": 35
}

// PedidoResumoResponse
{
  "id": 1001,
  "data": "2025-01-01T10:00:00Z",
  "total": 89.9,
  "status": "CONFIRMADO",
  "metodoPagamento": "PIX"
}

// CheckoutResumoResponse
{
  "itens": [ ... ],
  "subtotal": 89.9,
  "frete": 5.0,
  "total": 94.9,
  "metodoPagamento": "PIX"
}

## Fluxos de autenticacao (passo 9)
### Login (email/senha)
1) POST /api/auth/login { usuario, senha }
2) Recebe accessToken + refreshToken + userId + roles
3) App salva tokens com expiracao e renova quando necessario

### Cadastro (email/senha + OTP opcional)
1) POST /api/auth/register { name, email, cpf, password }
2) (Opcional) POST /api/auth/otp/start { canal, destino }
3) POST /api/auth/otp/verify { deliveryId, code }
4) POST /api/auth/register/complete-otp { token, email, nome, senha }
5) App redireciona para login

### Recuperacao de senha (OTP)
1) POST /api/auth/otp/start { canal, destino }
2) POST /api/auth/otp/verify { deliveryId, code } => token
3) POST /api/auth/password/reset-otp { token, email, novaSenha }

### Reset por token (alternativo)
1) POST /api/auth/esqueci-senha { email }
2) GET /api/auth/validar-token?token=...
3) POST /api/auth/resetar-senha { token, novaSenha }

## Execucao do plano (passos 10-50)
10) Layout base: tabs inferiores (Home, Catalogo, Carrinho, Pedidos, Conta); header compacto com busca e badge do carrinho.
11) Design system: tipografia base (sans), escala de cores (primary/secondary/success/error), tokens de espaco e radius.
12) Componentes base: buttons (primary/ghost), inputs com estados, cards de produto, badges de estoque, chips de filtro.
13) Estados globais: loading (skeleton), empty state, error state, offline state.
14) Fluxo de catalogo: busca por nome/codigo, filtros por categoria e ordenacao por preco/novidade.
15) Listagem de produtos: pagina com grid 2 colunas, pagina��o infinita, usa /api/v2/produtos e /api/public/vitrine/destaques.
16) Detalhe do produto: imagem, preco, promo, estoque, botao adicionar ao carrinho.
17) Produtos relacionados: baseados em categoria + fallback por recentes.
18) Regra de estoque: disponivel se disponivel=true e estoque>0; mostrar aviso <=10.
19) Carrinho: estado local + sincronizacao com API /api/cliente/me/carrinho.
20) Checkout: resumo, endereco, pagamento, confirmacao; usar /api/cliente/me/checkout.
21) Mascaras/validacoes: CPF, telefone, email, senha forte.
22) Minha conta: dados pessoais, preferencias, atalhos de pedidos.
23) Foto de perfil: upload no app e API /api/cliente/me/avatar.
24) Pedidos (lista): status, total e data com filtros simples.
25) Pedido (detalhe): itens, status e timeline de entrega.
26) Notificacoes do cliente: tela dedicada com avisos e promo.
27) Avisos admin: central de alertas no dashboard (fora do app cliente).
28) Alertas de estoque: configuracao via admin e exibicao no app quando <=10.
29) Promocoes/ofertas: vitrine dedicada e campanhas sazonais.
30) Favoritos: wishlist simples por produto.
31) Historico de buscas: salvar ultimas 5 buscas locais.
32) Cupons/beneficios: aplicacao no checkout e exibicao no carrinho.
33) Chatbot/FAQ: pagina de ajuda com topicos e contato rapido.
34) Suporte: contato via email/whatsapp a partir do app.
35) Politicas/termos: paginas estaticas acessiveis no app.
36) Tracking: eventos chave (view produto, add cart, checkout, compra).
37) Metricas admin: painel basico (fora do app cliente).
38) Observabilidade: logs estruturados e alertas de erro.
39) Performance: cache de imagens, lazy load, compressao.
40) Offline: cache basico de home/catalogo (PWA futuro).
41) Seguranca: CORS, headers, rate limit, validacao de input.
42) Acessibilidade: contraste, labels, foco e leitor de tela.
43) Testes unitarios: services e validacoes.
44) Testes E2E: login, catalogo, carrinho, checkout.
45) CI/CD: pipeline com build, testes e deploy automatizado.
46) Staging: ambiente espelho com dados mascarados.
47) Regressao: suite de smoke antes de release.
48) Release/rollback: plano de reversao e comunicacao.
49) Monitoramento: erros, performance, funil.
50) Melhoria continua: ciclo mensal de ajustes e backlog.
# Mobile App Blueprint (Android)

## Stack and architecture
- Kotlin + Jetpack Compose
- MVVM + UseCases + Repositories
- Hilt for DI
- Retrofit/OkHttp for API
- Coil for images
- DataStore (Encrypted) for tokens

## Package
- br.com.redemaisfarma.mobile

## Modules
- app (UI)
- core (network, storage, common)
- domain (models, use cases)
- data (repositories, DTOs, mappers)

## Core dependencies
- androidx.compose.*
- androidx.navigation:navigation-compose
- androidx.lifecycle:lifecycle-viewmodel-compose
- com.google.dagger:hilt-android
- com.squareup.retrofit2:retrofit
- com.squareup.okhttp3:okhttp
- io.coil-kt:coil-compose
- androidx.datastore:datastore-preferences

## MVP screens
- Splash
- Login
- OTP (opcional)
- Cadastro
- Home/Vitrine
- Catalogo + busca
- Detalhe do produto
- Carrinho
- Checkout
- Conta
- Pedidos (lista/detalhe)

## API base
- Base URL: http://localhost:18090
- Auth: /api/auth/*
- Catalogo: /api/v2/produtos
- Vitrine: /api/public/vitrine/destaques

## Release
- Keystore + signingConfig
- versionCode/versionName
- App bundle (.aab)
- Play Console listing + privacy policy

## API contracts (detalhado)
### Auth
- POST /api/auth/login
  req: { "usuario": "email", "senha": "senha" }
  res: { "accessToken": "", "refreshToken": "", "userId": 1, "roles": ["ROLE_CLIENTE"], "expiresAt": "" }

- POST /api/auth/register
  req: { "name": "", "email": "", "cpf": "", "password": "" }
  res: { "accessToken": "", "refreshToken": "" }

- POST /api/auth/otp/start
  req: { "canal": "email", "destino": "email" }
  res: { "deliveryId": "", "maskedDestino": "", "cooldownSec": 60, "ttlSeconds": 300 }

- POST /api/auth/otp/verify
  req: { "deliveryId": "", "code": "" }
  res: { "token": "" }

- POST /api/auth/register/complete-otp
  req: { "token": "", "email": "", "nome": "", "senha": "" }
  res: { "message": "Conta criada" }

- POST /api/auth/password/reset-otp
  req: { "token": "", "email": "", "novaSenha": "" }
  res: { "message": "Senha redefinida" }

### Catalogo
- GET /api/v2/produtos?page=0&size=20&sort=dataCadastro,desc
  res: { "content": [Produto], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5 }

- GET /api/v2/produtos/{id}
  res: Produto

### Perfil
- GET /api/cliente/me
  res: ClienteResponse

- PUT /api/cliente/me
  req: { "nome": "", "telefone": "", "cpf": "" }
  res: ClienteResponse

- POST /api/cliente/me/avatar (multipart)
  res: { "avatarUrl": "" }

### Carrinho
- GET /api/cliente/me/carrinho
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- POST /api/cliente/me/carrinho
  req: { "produtoId": 1, "quantidade": 1 }
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- PUT /api/cliente/me/carrinho/{produtoId}
  req: { "quantidade": 2 }
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- DELETE /api/cliente/me/carrinho/{produtoId}
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

### Checkout
- GET /api/cliente/me/checkout/resumo
  res: CheckoutResumoResponse

- POST /api/cliente/me/checkout/finalizar
  req: { "enderecoId": 1, "metodoPagamento": "PIX" }
  res: { "pedidoId": 123 }

### Pedidos
- GET /api/cliente/me/pedidos
  res: { "content": [PedidoResumo], "page": 0, "size": 20, "totalElements": 10, "totalPages": 1 }

- GET /api/cliente/me/pedidos/{id}
  res: PedidoDetalhe

## Navigation map
- Splash -> Login
- Login -> Home
- Home -> Catalogo -> Detalhe
- Detalhe -> Carrinho -> Checkout -> Confirmacao
- Home -> Pedidos (lista) -> Pedido (detalhe)
- Home -> Conta -> Dados/Foto

## Auth strategy
- Access token + refresh token
- Refresh antes de expirar (ex.: 2 min)
- Tokens em Encrypted DataStore
- Interceptor para 401 e refresh

## Build and release
- versionCode/versionName por build
- Keystore em CI e local
- Build AAB para Play Store
- Flavors: dev, staging, prod

## Play Store checklist
- App icon, feature graphic, screenshots
- Descricao curta e longa
- Politica de privacidade publica
- Content rating
- Target SDK atualizado

## CI/CD and QA
- Pipeline: lint + tests + assemble
- Release interno para QA
- Crash reporting (Firebase)
- Analytics basico (eventos)


## API mobile (cliente) - endpoints implementados
### Perfil
- GET /api/cliente/me
- PUT /api/cliente/me
- POST /api/cliente/me/avatar

### Pedidos
- GET /api/cliente/me/pedidos
- GET /api/cliente/me/pedidos/{id}

### Carrinho
- GET /api/cliente/me/carrinho
- POST /api/cliente/me/carrinho
- PUT /api/cliente/me/carrinho/{produtoId}
- DELETE /api/cliente/me/carrinho/{produtoId}

### Checkout
- GET /api/cliente/me/checkout/resumo
- POST /api/cliente/me/checkout/finalizar

### Favoritos
- GET /api/cliente/me/favoritos
- POST /api/cliente/me/favoritos
- DELETE /api/cliente/me/favoritos/{produtoId}

### Notificacoes
- GET /api/cliente/me/notificacoes
- POST /api/cliente/me/notificacoes/lidas

### Admin notificacoes
- GET /admin/notificacoes
- POST /admin/notificacoes/api/enviar
- POST /admin/notificacoes/api/enviar/todos
- POST /admin/notificacoes/api/enviar/usuario/{id}
- POST /admin/notificacoes/api/enviar/estoque-baixo
- Job: Estoque baixo -> cria notificacoes para usuarios (cron: app.estoque.alerta.cron)
- Configuracoes de alerta de estoque: app.estoque.alerta.enabled/limite/cooldown-minutes/cron

## Mobile (Android) - estado atual
- Projeto em `mobile/` (Gradle Kotlin DSL, Compose, Java 17).
- Hilt + Retrofit/Moshi + OkHttp + DataStore configurados.
- Base URL: `BuildConfig.API_BASE_URL` (default `http://10.0.2.2:18090`).
- Navegacao Compose pronta (home, carrinho, avisos, conta, produto).
- Telas placeholder: Catalogo, Carrinho, Pedidos, Minha conta, Avisos, Produto.
- Cookie jar ativo para manter sessao web no mobile.

### Mobile - passo atual
- Tela Minha conta consome `GET /api/cliente/me` via ViewModel + Repository (Hilt).
- Estado de UI com loading/sucesso/erro e retry.

### Mobile - passo atual 2
- Tela Pedidos consome `GET /api/cliente/me/pedidos`.
- Tela Avisos consome `GET /api/cliente/me/notificacoes` + marca lidas.
- Tela Carrinho consome `GET /api/cliente/me/carrinho` e remove item.

### Mobile - passo atual 3
- Tela detalhe de pedido consome `GET /api/cliente/me/pedidos/{id}` e foi ligada na lista de pedidos.

### Mobile - passo atual 4
- Checkout ligado ao `GET /api/cliente/me/checkout/resumo` e `POST /api/cliente/me/checkout/finalizar` com inputs basicos no carrinho.

### Mobile - passo atual 5
- Tela Minha conta permite editar dados (PUT /api/cliente/me) e enviar avatar (POST /api/cliente/me/avatar) via seletor de imagem.

### Mobile - passo atual 6
- Catalogo mobile com favoritos (GET/POST/DELETE /api/cliente/me/favoritos) e toggle visual.

### Mobile - passo atual 7
- Catalogo mobile consome `GET /api/public/produtos` e detalhe do produto usa `GET /api/public/produtos/{id}` com adicionar ao carrinho.

### Mobile - passo atual 8
- Catalogo com busca (q) no endpoint `GET /api/public/produtos` e imagens renderizadas no card.

### Mobile - passo atual 9
- Catalogo com carregamento incremental (paginacao simples) e detalhe do produto com imagem e metadados basicos.

### Mobile - passo atual 10
- Checkout com validacao basica de CPF/email e feedback via snackbar.
- Detalhe do produto avisa estoque baixo (<= 10).

### Mobile - passo atual 11
- Catalogo com indicador de carregamento incremental.
- CPF com mascara simples e envio apenas de digitos no checkout.

### Mobile - passo atual 12
- Telefone com mascara no perfil e envio apenas de digitos no update.

### Mobile - passo atual 13
- Selecionar metodo de pagamento agora destaca o item ativo.
- Perfil valida telefone (10-11 digitos) antes de salvar.

### Mobile - passo atual 14
- Catalogo/detalhe com placeholder de imagem quando nao ha foto.
- Perfil valida email e CPF antes de salvar.

### Mobile - passo atual 15
- CPF do perfil usa mascara e envia apenas digitos no update.

### Mobile - passo atual 16
- Estados vazios com icone informativo no carrinho, pedidos e avisos.

### Mobile - passo atual 17
- Busca no catalogo com debounce (400ms) e disparo automatico a partir de 2 caracteres.

### Mobile - passo atual 18
- Mensagens de erro agora incluem detalhes HTTP e falha de conexao quando aplicavel.
- Catalogo mostra texto de carregamento incremental.

### Mobile - passo atual 19
- Acessibilidade: botoes com altura minima de toque (48dp) e icone de favorito com area maior.

### Mobile - passo atual 20
- Estados vazios reutilizados em um componente unico para padronizar UI.

### Mobile - passo atual 21
- Cards de listas padronizados com um composable unico para pedidos, avisos, itens do carrinho e itens do pedido.

### Mobile - passo atual 22
- Mensagens de erro padronizadas por contexto usando componente unico.

### Mobile - passo atual 23
- Componentes de UI extraidos para `ui/components` (EmptyState, ErrorState, SimpleCard).

### Mobile - passo atual 24
- Formatters (CPF/telefone) extraidos para util e cobertos por testes unitarios basicos.

### Mobile - passo atual 25
- Teste unitario para ErrorMapper (HTTP e IO).

### Mobile - passo atual 26
- Telas separadas em arquivos por feature (catalogo, carrinho, pedidos, perfil, avisos, detalhe do produto/pedido).

### Mobile - passo atual 27
- Catalogo agora usa ProductCard padronizado (imagem, favorito, preco e acao) no componente de UI.

### Mobile - passo atual 28
- Catalogo mostra estado vazio quando nao ha resultados.

### Mobile - passo atual 29
- Teste basico do CatalogViewModel com dispatcher de teste.

### Mobile - passo atual 30
- Teste basico do CheckoutViewModel (resumo e finalizar).

### Mobile - passo atual 31
- Detalhe do produto com linhas padronizadas e tags (marca/categoria/codigo/estoque).

### Mobile - passo atual 32
- ProductCard indica estoque baixo quando <= 10.
- Ajuste de label no detalhe do produto.

### APK
- Para gerar o APK debug: `cd mobile` + `./gradlew assembleDebug`.
- Saida: `mobile/app/build/outputs/apk/debug/app-debug.apk`.
## Plano operacional (50 etapas)
1. Validar o status atual da fila `email_delivery` e confirmar que está em `PENDING`.
2. Garantir que todas as variáveis Railway de SMTP estão populadas com os valores corretos e marcadas como sensíveis.
3. Redeployar o serviço após a revisão das variáveis para forçar leitura das configurações.
4. Disparar manualmente `/api/auth/email-claim/start` com curl e guardar `deliveryId/demoCode`.
5. Verificar na tabela `email_delivery` se o registro associado muda para `SENT`.
6. Se ficar em `PENDING`, habilitar logs `br.com.redemaisfarma=DEBUG` e `org.springframework.mail=DEBUG`.
7. Revisar o `SettingsMailSenderAdapter` para garantir fallback seguro para variáveis ambientais.
8. Confirmar que o fallback foi adotado no deploy (log `Hero available publicly...` ajuda a saber se settings carregaram).
9. Documentar no README cortes de variáveis e fallback em um subtítulo (feito com este plano).
10. Atualizar o fragmento `header.html` para manter o botão toggle e logo responsivos.
11. Ajustar a CSS do header para logos maiores no mobile e adicionar padding extra.
12. Revisar `sidebar.js` e garantir o overlay/escuro funciona para tocar fora.
13. Testar a abertura do drawer em um dispositivo Android WebView via DevTools remote (emulação).
14. Garantir que `nav-open` é adicionado ao `<html>` e que `main` não captura cliques quando o drawer está aberto.
15. Atualizar `.hero__bg` para usar dimensões menores e centralizadas com gradiente.
16. Adicionar query para ocultar `.hero__bg` em widths abaixo de 640px mantendo a imagem visível.
17. Incluir fallback fixo `absorventepronto.png` no hero e garantir o asset está empacotado.
18. Garantir que `BrandingModelAdvice` injeta `brandingHomeHeroImageUrl` limpando URLs inválidas (ex.: `/css/...`).
19. Logar no servidor qual fallback foi usado para ajudar debug.
20. Reduzir a opacidade do background hero e ajustar saturação para foco no conteúdo.
21. Configurar testes manuais para confirmar o hero aparece em desktop e mobile.
22. Atualizar `LoginRequest` e `cadastro-cliente.html` para aceitar qualquer caractere especial.
23. Manter a mensagem de ajuda consistente (`A senha precisa...`) nos campos e no JS.
24. Ajustar `cadastro-otp.js` para aceitar `demoCode` automático e submeter OTP imediatamente.
25. Inserir um usuário admin seed via script Flyway/SQL (caso necessário).
26. Garantir que o e-mail de testes dispara via admin e log registra `SENT`.
27. Criar script Python opcional para inspecionar `email_delivery` e disparar OTP manualmente.
28. Documentar esse script na sequência de README e /docs para futuras execuções.
29. Focar no layout mobile: alinhar header, hero e CTA sem sobreposição.
30. Criar uma lista de verificação de QA no README para testes pós-deploy (assets, login, sidebar).
31. Auditar `sidebar` para remover duplicação de itens e garantir overlay escuro com `click` closing.
32. Implementar botão fixo de "voltar ao topo" com delay e visibilidade condicional (já presente).
33. Documentar no README como configurar env de produção vs dev (SMTP, DB, base URL).
34. Garantir `app_settings` tem as chaves `branding.*` e `email.*` necessárias e mapear para README.
35. Escrever instruções para carregar arte hero via admin (upload, URL e loop em vídeo) e mencionar formatos recomendados.
36. Validar que a base `/images` serve o `absorventepronto.png` e não gera 404.
37. Verificar se `/css/pages/brandingHomeHeroUrl` parou de ser requisitado após ajustes.
38. Mapear no README como executar `./mvnw spotbugs:check` e `./mvnw checkstyle:check`.
39. Registrar no README o fluxo de deploy (variáveis e comandos Railway).
40. Confirmar que o login apresenta OTP form adequadamente após digitar a senha.
41. Documentar no README o `curl` para OTP start/verify para testes rápidos.
42. Adicionar no README instruções de reset de senha via `/auth/esqueci-senha`.
43. Listar no README as métricas observáveis via Railway (logs, fila, job schedules).
44. Registrar no README como expandir `sidebar` e a tecla Esc para fechar.
45. Criar uma seção no README para alertar sobre as mensagens técnicas que devem sumir (cards com config).
46. Acompanhar `email_delivery` e `email_campaign_queue` logs regularmente após deploy.
47. Atualizar README com o contato de suporte e WhatsApp corrigido.
48. Garantir README documenta o uso do `APP_MAIL_REPLY_TO`, `APP_WEB_BASE_URL` e `APP_MAIL_ENABLED`.
49. Colocar links no README para o `README_DEV.md`, `AGENTS.md` e doc de testes.
50. Finalizar com a confirmação no README que o plano de 50 etapas está completo e revisá-lo para clareza.

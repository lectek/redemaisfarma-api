# Guia Rápido de Desenvolvimento

## Visão geral
- Stack: Java 21, Spring Boot 3.3, Maven Wrapper, MapStruct, Lombok.
- Módulo principal: `boot-app` (API e templates admin). Hexagonal: `domain`, `application`, `adapters` (web/JPA), `config`.
- Perfis: `dev` (MySQL local), `test` (Testcontainers MySQL), `firebird` (legado). Configs em `src/main/resources/application*.yml`.

## Estado atual da API
- Catálogo admin funcional: páginas `/admin/produtos`, `/admin/produtos/novo`, `/admin/produtos/{id}/editar` consumindo `/api/admin/produtos` (CRUD, validação/publicação, upload de imagem, ações IA).
- Importação legado ativa via perfil `firebird` (conector Firebird Jaybird).
- Observabilidade e qualidade ativas: Actuator, Jacoco, SpotBugs, Checkstyle.
- Testes de controlador (ProdutoAdminRestControllerTest) e do job de alerta (EstoqueBaixoNotificacaoJobTest) cobrem /api/admin/produtos e o alerta de estoque; monitore as flags app.estoque.alerta.*, a fila email_delivery/EmailDeliveryWorker e os endpoints /actuator/health e /actuator/metrics para validar dependencias externas (storage/S3, Firebird).
- Estoque: reservas no checkout/pagamento via `EstoqueService`, baixa em venda rápida, job de alerta de estoque baixo (log/e-mail) configurável em `app.estoque.alerta.*`.

## Fluxos de produto (admin)
- Páginas: `/admin/produtos` (lista), `/admin/produtos/novo` (criar), `/admin/produtos/{id}/editar` (editar). Templates em `templates/pages/admin/produtos/`; JS em `static/js/pages/admin/produto-editar.js`.
- APIs admin: `ProdutoAdminRestController` em `/api/admin/produtos` (listar, obter, criar, atualizar, excluir, validar/publicar, upload de imagem).
- Uso em tela: criação/edição envia `tenantId` padrão (`rede-mais-farma`), gera SKU se não informado e redireciona para edição após criar.

## Comandos úteis
- Build + testes completos: `./mvnw clean verify`
- Somente testes (perfil test): `./mvnw test -Dspring.profiles.active=test`
- Pular integrações (sem Docker): `./mvnw verify -DskipITs`
- Rodar local (dev): `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- MySQL dev via Docker: `docker-compose -f docker-compose.dev.yml up -d mysql`

## Perfis e configuração
- `dev` (padrão): MySQL local `localhost:3306`, Swagger ligado, Mailpit opcional. Ver `application-dev.yml`.
- `docker`: aponta para os serviços do `docker-compose.dev.yml`.
- `test`: Testcontainers (MySQL) em `application-test.yml`.
- `legacy`: conector Jaybird para Firebird; ajuste `FIREBIRD_HOST`, `FIREBIRD_DB`, `FIREBIRD_USER/PASSWORD`.
- Alerta de estoque: habilite com `app.estoque.alerta.enabled=true`; defaults `limite=10`, `percentual=10`, `cron=0 */30 * * * *`, `cooldown-minutos=60`, `email=` opcional.

## Convenções de código
- Injeção por construtor; evitar field injection.
- Indentação 4 espaços, linhas ~120 colunas. Classes: PascalCase; fields/métodos: camelCase.
- Controllers terminam em `Controller`, DTOs em `RequestDTO`/`ResponseDTO`, mappers em `Mapper`.
- Segredos fora do repo; usar perfis/YAML e variáveis de ambiente.

## Testes
- JUnit 5, Mockito, Testcontainers (MySQL). Unitários em `src/test/java`, integração espelha o main.
- JaCoCo roda em `clean verify`; cobrir sucesso e falha.
- Integrações atuais: Auth/Produto; faltam cenários end-to-end checkout/pagamento/estoque.

## Checklist de PR
- Rodar `./mvnw clean verify` (ou `./mvnw test -DskipITs` se sem Docker).
- Descrever motivação, abordagem, endpoints alterados, novos env vars/portas/perfis.
- Atualizar README/Swagger quando mudar contrato de API.

## Notas rápidas sobre produtos
- Repositórios: `ProdutoJpaRepository` e `ProdutoRepository` (busca/paginação/categorias).
- Entidade: `ProdutoEntity` (status, estoque, preços, timestamps).
- Mapper: `ProdutoMapper` converte domain↔entity/DTO.
- Imagem IA: ações em lista/edição chamam `/api/admin/imagens/{id}/queue|regenerate`.

## Blocos de Desenvolvimento (módulos)
- **Cliente (Self-service)**: `/api/cliente/me` (GET/PUT perfil), `/api/cliente/me/enderecos` (CRUD), `/api/cliente/me/pedidos` (paginado). Usa CurrentClienteProvider para extrair cliente autenticado do token.
- **Legado/Importação**: conectores Firebird (perfil `firebird`), serviços de sincronização; monitorar logs em `adapters.outbound.legacy`.
- **Qualidade/Observabilidade**: Actuator, Jacoco, SpotBugs, Checkstyle já integrados; manter `clean verify` no PR.

## Próximos passos (cliente self-service)
- Expor troca de senha/OTP e encerrar sessões em `/api/cliente/me`.
- UI "Minha Conta" (cliente) consumindo `/api/cliente/me` e `/me/enderecos`.

## Pr�ximos 25 passos para conclus�o web
11. Confirmar com o time de produto os crit�rios de aceita��o restantes: checkout completo, pagamentos, notifica��es e fluxos de erro esperados.
12. Completar o invent�rio de telas/classes front-end (carrinho, checkout, pagamento, confirma��o) e identificar gaps em componentes reutiliz�veis.
13. Mapear APIs/backlog necess�rias para cada tela pendente, incluindo contratos de frete, promo��es, cupom e estoque.
14. Priorizar integra��es cr�ticas de backend com base no valor percebido pelo comprador e depend�ncias t�cnicas.
15. Checar o estado atual dos mocks/testes existentes para os endpoints de checkout e criar vers�es atualizadas, se necess�rio.
16. Gerar ou atualizar componentes UI responsivos para carrinho/checkout com estados de loading, sucesso e erro.
17. Implementar valida��es de front-end (dados do cliente, n.� de cart�o, CPF, CEP) alinhadas ao backend e ao UX.
18. Garantir que o cat�logo esteja atualizado antes do carrinho: cache, atualiza��o ass�ncrona ou fallback.
19. Conectar o front ao backend oficial para frete/pagamento, cuidando da autentica��o e do token CSRF.
20. Sincronizar estoque/pre�o ao abrir o carrinho e antes do pagamento, mostrando avisos de indisponibilidade.
21. Implementar logs e m�tricas para o fluxo de checkout (eventos, tempo m�dio, falhas) visando monitoramento.
22. Validar o fluxo com testes automatizados (unit�rios e integrados) e documentar os cen�rios cobertos.
23. Realizar smoke tests manuais no ambiente dev com vers�es completas de banco e integra��es (MySQL/Testcontainers).
24. Ajustar performance (lazy load, debounce, minifica��o) e garantir acessibilidade b�sica (contrast, tab order).
25. Preparar release notes e atualizar README/OpenAPI com os novos fluxos e endpoints expostos.
26. Coordenar com QA e suporte o plano de testes de regress�o, registrando bugs e fechando bloqueios.
27. Fazer deploy em homologa��o, executar testes finais de ponta a ponta e coletar logs de erro/sucesso.
28. Receber feedback de QA/PO, corrigir falhas cr�ticas e reorganizar prioridades residuais.
29. Atualizar scripts de implanta��o e configura��o (env vars, secrets) caso novos servi�os sejam utilizados.
30. Acompanhar m�tricas p�s-deploy, monitorar integra��es externas e validar rollback/alertas.
31. Comunicar o time e stakeholders da entrega com checklist, teste executado e pr�ximos passos.
32. Reavaliar o backlog para adicionar ajustes finos de UX/performance detectados durante testes.
33. Garantir documenta��o interna atualizada (diagramas, contratos, endpoints) antes do fechamento.
34. Treinar suporte/operations sobre o novo fluxo e preparar runbooks para incidentes.
35. Planejar itera��o seguinte com foco em novos requisitos ou otimiza��es ap�s estabilizar a entrega.

## Prioridade extrema
- foto de perfil em 'Minha Conta' e 'Meus Dados' (upload + exibicao).




## Observabilidade das campanhas
- A fila de campanhas gera email_campaign.worker.processed/sent/retry/failed e pode ser monitorada via /actuator/metrics; configure alertas quando ailed > 0 ou processados pararem.
- O formul�rio admin agora aceita filtros adicionais (categoria comprada, rec�ncia em dias, ticket m�dio) antes de enfileirar, al�m de preview/cancelamento/pause para ajustar antes do envio.

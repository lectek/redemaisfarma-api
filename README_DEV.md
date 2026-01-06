# Guia R√°pido de Desenvolvimento

## Vis√£o geral
- Stack: Java 21, Spring Boot 3.3, Maven Wrapper, MapStruct, Lombok.
- M√≥dulo principal: `boot-app` (API e templates admin). Hexagonal: `domain`, `application`, `adapters` (web/JPA), `config`.
- Perfis: `dev` (MySQL local), `test` (Testcontainers MySQL), `firebird` (legado). Configs em `src/main/resources/application*.yml`.

## Estado atual da API
- Cat√°logo admin funcional: p√°ginas `/admin/produtos`, `/admin/produtos/novo`, `/admin/produtos/{id}/editar` consumindo `/api/admin/produtos` (CRUD, valida√ß√£o/publica√ß√£o, upload de imagem, a√ß√µes IA).
- Importa√ß√£o legado ativa via perfil `firebird` (conector Firebird Jaybird).
- Observabilidade e qualidade ativas: Actuator, Jacoco, SpotBugs, Checkstyle.
- Estoque: reservas no checkout/pagamento via `EstoqueService`, baixa em venda r√°pida, job de alerta de estoque baixo (log/e-mail) configur√°vel em `app.estoque.alerta.*`.

## Fluxos de produto (admin)
- P√°ginas: `/admin/produtos` (lista), `/admin/produtos/novo` (criar), `/admin/produtos/{id}/editar` (editar). Templates em `templates/pages/admin/produtos/`; JS em `static/js/pages/admin/produto-editar.js`.
- APIs admin: `ProdutoAdminRestController` em `/api/admin/produtos` (listar, obter, criar, atualizar, excluir, validar/publicar, upload de imagem).
- Uso em tela: cria√ß√£o/edi√ß√£o envia `tenantId` padr√£o (`rede-mais-farma`), gera SKU se n√£o informado e redireciona para edi√ß√£o ap√≥s criar.

## Comandos √∫teis
- Build + testes completos: `./mvnw clean verify`
- Somente testes (perfil test): `./mvnw test -Dspring.profiles.active=test`
- Pular integra√ß√µes (sem Docker): `./mvnw verify -DskipITs`
- Rodar local (dev): `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- MySQL dev via Docker: `docker-compose -f docker-compose.dev.yml up -d mysql`

## Perfis e configura√ß√£o
- `dev` (padr√£o): MySQL local `localhost:3306`, Swagger ligado, Mailpit opcional. Ver `application-dev.yml`.
- `docker`: aponta para os servi√ßos do `docker-compose.dev.yml`.
- `test`: Testcontainers (MySQL) em `application-test.yml`.
- `legacy`: conector Jaybird para Firebird; ajuste `FIREBIRD_HOST`, `FIREBIRD_DB`, `FIREBIRD_USER/PASSWORD`.
- Alerta de estoque: habilite com `app.estoque.alerta.enabled=true`; defaults `limite=10`, `percentual=10`, `cron=0 */30 * * * *`, `cooldown-minutos=60`, `email=` opcional.

## Conven√ß√µes de c√≥digo
- Inje√ß√£o por construtor; evitar field injection.
- Indenta√ß√£o 4 espa√ßos, linhas ~120 colunas. Classes: PascalCase; fields/m√©todos: camelCase.
- Controllers terminam em `Controller`, DTOs em `RequestDTO`/`ResponseDTO`, mappers em `Mapper`.
- Segredos fora do repo; usar perfis/YAML e vari√°veis de ambiente.

## Testes
- JUnit 5, Mockito, Testcontainers (MySQL). Unit√°rios em `src/test/java`, integra√ß√£o espelha o main.
- JaCoCo roda em `clean verify`; cobrir sucesso e falha.
- Integra√ß√µes atuais: Auth/Produto; faltam cen√°rios end-to-end checkout/pagamento/estoque.

## Checklist de PR
- Rodar `./mvnw clean verify` (ou `./mvnw test -DskipITs` se sem Docker).
- Descrever motiva√ß√£o, abordagem, endpoints alterados, novos env vars/portas/perfis.
- Atualizar README/Swagger quando mudar contrato de API.

## Notas r√°pidas sobre produtos
- Reposit√≥rios: `ProdutoJpaRepository` e `ProdutoRepository` (busca/pagina√ß√£o/categorias).
- Entidade: `ProdutoEntity` (status, estoque, pre√ßos, timestamps).
- Mapper: `ProdutoMapper` converte domain‚Üîentity/DTO.
- Imagem IA: a√ß√µes em lista/edi√ß√£o chamam `/api/admin/imagens/{id}/queue|regenerate`.

## Pr√≥ximos passos sugeridos
- Garantir ambiente de teste com Docker ativo ou MySQL real para rodar ITs com Testcontainers.
- Adicionar testes de controlador para `/api/admin/produtos` (CRUD feliz + erros) e cobrir principais valida√ß√µes; novos ITs para checkout/pagamento/estoque e alerta.
- Atualizar Swagger/OpenAPI com rotas admin de produtos e par√¢metros (validador, upload, imagem IA) e com fluxo de estoque/pagamento/checkout.
- Revisar fluxo de imagem IA (queue/regenerate) e adicionar feedback na UI lista/edi√ß√£o quando conclu√≠da.
- Clientes (Admin) pr√≥ximos passos: CRUD de contatos/endere√ßo, ativar/desativar conta, reset de senha/OTP, encerrar sess√µes, listar pedidos detalhados.

## Blocos de Desenvolvimento (m√≥dulos)
- **Produtos (Admin)**: CRUD completo via `/api/admin/produtos`, p√°ginas `/admin/produtos`, `/novo`, `/editar`, a√ß√µes de imagem IA, export CSV.
- **Clientes (Admin)**: resumo em `/api/admin/clientes/{id}/resumo`; detalhe/atualiza√ß√£o parcial em `/api/admin/clientes/{id}` (PATCH), ativar/desativar (`/ativar`/`/desativar`), pedidos do cliente em `/api/admin/clientes/{id}/pedidos`, endere√ßos em `/api/admin/clientes/{id}/enderecos` (GET/POST/PUT/DELETE). Pr√≥ximos passos: reset de senha/OTP, encerrar sess√µes.
- **Cliente (Self-service)**: `/api/cliente/me` (GET/PUT perfil), `/api/cliente/me/enderecos` (CRUD), `/api/cliente/me/pedidos` (paginado). Usa CurrentClienteProvider para extrair cliente autenticado do token.
- **Legado/Importa√ß√£o**: conectores Firebird (perfil `firebird`), servi√ßos de sincroniza√ß√£o; monitorar logs em `adapters.outbound.legacy`.
- **Qualidade/Observabilidade**: Actuator, Jacoco, SpotBugs, Checkstyle j√° integrados; manter `clean verify` no PR.

## Pr√≥ximos passos (cliente self-service)
- Expor troca de senha/OTP e encerrar sess√µes em `/api/cliente/me`.
- UI "Minha Conta" (cliente) consumindo `/api/cliente/me` e `/me/enderecos`.

## PrÛximos 25 passos para conclus„o web
11. Confirmar com o time de produto os critÈrios de aceitaÁ„o restantes: checkout completo, pagamentos, notificaÁıes e fluxos de erro esperados.
12. Completar o invent·rio de telas/classes front-end (carrinho, checkout, pagamento, confirmaÁ„o) e identificar gaps em componentes reutiliz·veis.
13. Mapear APIs/backlog necess·rias para cada tela pendente, incluindo contratos de frete, promoÁıes, cupom e estoque.
14. Priorizar integraÁıes crÌticas de backend com base no valor percebido pelo comprador e dependÍncias tÈcnicas.
15. Checar o estado atual dos mocks/testes existentes para os endpoints de checkout e criar versıes atualizadas, se necess·rio.
16. Gerar ou atualizar componentes UI responsivos para carrinho/checkout com estados de loading, sucesso e erro.
17. Implementar validaÁıes de front-end (dados do cliente, n.∫ de cart„o, CPF, CEP) alinhadas ao backend e ao UX.
18. Garantir que o cat·logo esteja atualizado antes do carrinho: cache, atualizaÁ„o assÌncrona ou fallback.
19. Conectar o front ao backend oficial para frete/pagamento, cuidando da autenticaÁ„o e do token CSRF.
20. Sincronizar estoque/preÁo ao abrir o carrinho e antes do pagamento, mostrando avisos de indisponibilidade.
21. Implementar logs e mÈtricas para o fluxo de checkout (eventos, tempo mÈdio, falhas) visando monitoramento.
22. Validar o fluxo com testes automatizados (unit·rios e integrados) e documentar os cen·rios cobertos.
23. Realizar smoke tests manuais no ambiente dev com versıes completas de banco e integraÁıes (MySQL/Testcontainers).
24. Ajustar performance (lazy load, debounce, minificaÁ„o) e garantir acessibilidade b·sica (contrast, tab order).
25. Preparar release notes e atualizar README/OpenAPI com os novos fluxos e endpoints expostos.
26. Coordenar com QA e suporte o plano de testes de regress„o, registrando bugs e fechando bloqueios.
27. Fazer deploy em homologaÁ„o, executar testes finais de ponta a ponta e coletar logs de erro/sucesso.
28. Receber feedback de QA/PO, corrigir falhas crÌticas e reorganizar prioridades residuais.
29. Atualizar scripts de implantaÁ„o e configuraÁ„o (env vars, secrets) caso novos serviÁos sejam utilizados.
30. Acompanhar mÈtricas pÛs-deploy, monitorar integraÁıes externas e validar rollback/alertas.
31. Comunicar o time e stakeholders da entrega com checklist, teste executado e prÛximos passos.
32. Reavaliar o backlog para adicionar ajustes finos de UX/performance detectados durante testes.
33. Garantir documentaÁ„o interna atualizada (diagramas, contratos, endpoints) antes do fechamento.
34. Treinar suporte/operations sobre o novo fluxo e preparar runbooks para incidentes.
35. Planejar iteraÁ„o seguinte com foco em novos requisitos ou otimizaÁıes apÛs estabilizar a entrega.

## Prioridade extrema
- foto de perfil em 'Minha Conta' e 'Meus Dados' (upload + exibicao).

# Backlog tecnico (email marketing e configuracoes)

## Milestones
1) M1 - Fundacao email: fila + templates + envio transacional
2) M2 - Campanhas: CRUD + agendamento + segmentacao basica
3) M3 - Automacoes: carrinho abandonado + reengajamento + recompra
4) M4 - Engajamento: volta ao estoque + conteudo + cashback

## Epics e tarefas

### M1 - Fundacao email (P0)
- DB: tabelas `email_campaign`, `email_recipient`, `email_queue`, `email_delivery_log`
- Serviço: fila de envio (pending/sending/sent/failed), retries e throttling
- Templates: base + promocional + transacional (pedido/entrega)
- Admin UI: lista de envios + detalhes do log
- Observabilidade: logs por campanha e metricas basicas

### M2 - Campanhas (P0)
- CRUD campanhas (nome, assunto, template, publico, agendamento)
- Segmentacao basica: categoria comprada, recencia, ticket medio
- Preview antes de enviar
- Cancelamento/pausa de campanha

### M3 - Automacoes (P1)
- Carrinho abandonado (1h/24h com cupom)
- Reengajamento inativos (30/60/90 dias)
- Recompra automatica (medicamentos uso continuo)

### M4 - Engajamento (P2)
- Volta ao estoque (subscribe + trigger)
- Conteudo educativo (agenda)
- Cashback/beneficios (alertas e validade)

## Tickets detalhados (inicio)

### T1 - Criar entidades e migrations do email marketing (P0, M1)
- Criar tabelas de campanha, fila e log
- Definir indices por status e created_at
- Adicionar status enum (PENDING/SENDING/SENT/FAILED)

### T2 - Serviço de fila de envio (P0, M1)
- Buscar lote de emails pendentes
- Enviar com throttling
- Persistir resultado em log
- Marcar falhas com retry

### T3 - Templates base + transacional (P0, M1)
- Base HTML com header/footer
- Template de pedido (confirmacao e status)
- Template de promocao simples

### T4 - UI de monitoramento de envios (P0, M1)
- Lista de envios recentes
- Detalhe por campanha
- Filtros por status

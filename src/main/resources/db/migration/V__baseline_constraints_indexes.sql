/* ======================================================================
   V1 — Baseline de constraints, índices, defaults e tipos (MySQL 8+)
   Projeto: RedeMaisFarma API
   Objetivo: Integridade, performance e precisão monetária
   ====================================================================== */

/* ---------- Ambiente (opcional, caso use outro charset/collation) ----- */
-- ALTER DATABASE `seu_schema` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

/* =========================== USUARIO ================================== */

-- Unicidade
ALTER TABLE usuario
  ADD CONSTRAINT uk_usuario_email UNIQUE (email),
  ADD CONSTRAINT uk_usuario_cpf   UNIQUE (cpf);

-- Defaults
ALTER TABLE usuario
  MODIFY COLUMN tentativas_falhas INT DEFAULT 0,
  MODIFY COLUMN cliente_vip       TINYINT(1) DEFAULT 0;

-- Índices úteis
CREATE INDEX idx_usuario_ultimo_acesso ON usuario (ultimo_acesso);

-- Tabela de roles (ElementCollection) — garante PK e FK
ALTER TABLE usuario_roles
  ADD CONSTRAINT pk_usuario_roles PRIMARY KEY (usuario_id, role),
  ADD CONSTRAINT fk_usuario_roles_usuario
      FOREIGN KEY (usuario_id) REFERENCES usuario(id)
      ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX idx_usuario_roles_role ON usuario_roles (role);

/* =========================== CLIENTE ================================== */

-- Unicidade
ALTER TABLE cliente
  ADD CONSTRAINT uk_cliente_email UNIQUE (email),
  ADD CONSTRAINT uk_cliente_cpf   UNIQUE (cpf);

-- Índices úteis (buscas por nome/telefone)
CREATE INDEX idx_cliente_nome     ON cliente (nome);
CREATE INDEX idx_cliente_telefone ON cliente (telefone);

/* =========================== PRODUTO ================================== */

-- Precisão monetária e padrões
ALTER TABLE produto
  MODIFY COLUMN preco_venda DECIMAL(19,2),
  MODIFY COLUMN preco_custo DECIMAL(19,2),
  MODIFY COLUMN estoque     INT DEFAULT 0,
  MODIFY COLUMN disponivel  TINYINT(1) DEFAULT 1;

-- Unicidade de código de barras
ALTER TABLE produto
  ADD CONSTRAINT uk_produto_cod_barras UNIQUE (codigo_barras);

-- Índices de catálogo
CREATE INDEX idx_produto_categoria_disponivel ON produto (categoria, disponivel);
CREATE INDEX idx_produto_estoque              ON produto (estoque);
CREATE INDEX idx_produto_nome                 ON produto (nome);
CREATE INDEX idx_produto_fabricante           ON produto (fabricante);

/* ============================ PEDIDO ================================== */

-- Precisão monetária
ALTER TABLE pedido
  MODIFY COLUMN total DECIMAL(19,2);

-- Índices para relatórios
CREATE INDEX idx_pedido_cliente ON pedido (cliente_id);
CREATE INDEX idx_pedido_status  ON pedido (status);
CREATE INDEX idx_pedido_data    ON pedido (data);

-- Integridade referencial
ALTER TABLE pedido
  ADD CONSTRAINT fk_pedido_cliente
      FOREIGN KEY (cliente_id) REFERENCES cliente(id)
      ON DELETE RESTRICT ON UPDATE CASCADE;

/* ========================= ITEM_PEDIDO ================================ */

-- Precisão monetária
ALTER TABLE item_pedido
  MODIFY COLUMN subtotal DECIMAL(19,2);

-- Integridade referencial
ALTER TABLE item_pedido
  ADD CONSTRAINT fk_item_pedido__pedido
      FOREIGN KEY (pedido_id)  REFERENCES pedido(id)
      ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT fk_item_pedido__produto
      FOREIGN KEY (produto_id) REFERENCES produto(id)
      ON DELETE RESTRICT ON UPDATE CASCADE;

-- Evita item repetido do mesmo produto no mesmo pedido
ALTER TABLE item_pedido
  ADD CONSTRAINT uk_item_pedido__pedido_produto UNIQUE (pedido_id, produto_id);

-- Índices
CREATE INDEX idx_item_pedido_pedido  ON item_pedido (pedido_id);
CREATE INDEX idx_item_pedido_produto ON item_pedido (produto_id);

/* ============================ NOTAS ===================================

- Este script assume nomes de tabelas/colunas conforme suas Entities:
  usuario(id, email, cpf, ultimo_acesso, cliente_vip, tentativas_falhas)
  usuario_roles(usuario_id, role)
  cliente(id, nome, email, telefone, cpf, senha)
  produto(id_produto? -> id), nome, descricao, preco_venda, imagem, categoria,
          codigo_barras, preco_custo, estoque, disponivel, fabricante,
          codigo_original, unidade, data_cadastro
  pedido(id, cliente_id, data, total, status, tipo_pagamento)
  item_pedido(id, produto_id, pedido_id, quantidade, subtotal)

- Se sua coluna PK de produto chama "id_produto", ajuste as FKs/índices:
    - Trocar "produto(id)" por "produto(id_produto)" onde aparecer.

- MySQL 8+ valida CHECKs; não incluímos porque seus enums vêm do código
  (StatusPedido/TipoPagamento). Se quiser, adicione conforme seus valores.

- Rode Flyway depois de conferir nomes: 
  mvn -Dflyway.cleanDisabled=true flyway:migrate

======================================================================= */

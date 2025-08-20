/* ================================================================
   V2 — Criação das tabelas pedido e item_pedido
   Projeto: RedeMaisFarma API
   Objetivo: Persistência das entidades Pedido e ItemPedido
   ================================================================= */

-- ========================= TABELA PEDIDO =========================
CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    data DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    tipo_pagamento VARCHAR(20) NOT NULL,

    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Índices úteis
CREATE INDEX idx_pedido_cliente_id ON pedido (cliente_id);
CREATE INDEX idx_pedido_status     ON pedido (status);
CREATE INDEX idx_pedido_data       ON pedido (data);

-- ====================== TABELA ITEM_PEDIDO =======================
CREATE TABLE item_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(19,2) NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL,

    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    -- 🔒 evita duplicar o mesmo produto em um pedido
    CONSTRAINT uk_item_pedido_pedido_produto UNIQUE (pedido_id, produto_id)
);

-- Índices úteis
CREATE INDEX idx_item_pedido_pedido   ON item_pedido (pedido_id);
CREATE INDEX idx_item_pedido_produto  ON item_pedido (produto_id);


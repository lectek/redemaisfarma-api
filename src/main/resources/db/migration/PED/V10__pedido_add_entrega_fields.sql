ALTER TABLE pedido
    ADD COLUMN IF NOT EXISTS endereco_entrega VARCHAR(255) NULL AFTER metodo_pagamento,
    ADD COLUMN IF NOT EXISTS codigo_entrega VARCHAR(6) NULL AFTER endereco_entrega,
    ADD COLUMN IF NOT EXISTS codigo_entrega_gerado_em DATETIME NULL AFTER codigo_entrega,
    ADD COLUMN IF NOT EXISTS codigo_entrega_confirmado_em DATETIME NULL AFTER codigo_entrega_gerado_em;


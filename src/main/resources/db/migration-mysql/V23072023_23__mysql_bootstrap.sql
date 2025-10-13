-- 1) Tabela de checkpoint do sync (se não existir)
CREATE TABLE IF NOT EXISTS sync_checkpoint (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source VARCHAR(100) NOT NULL UNIQUE,
  last_since DATETIME NULL,
  updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2) Campos extras em produto (se ainda não existirem)
ALTER TABLE produto
  ADD COLUMN IF NOT EXISTS preco_promocional DECIMAL(10,2) NULL,
  ADD COLUMN IF NOT EXISTS imagem_webp       VARCHAR(255)  NULL,
  ADD COLUMN IF NOT EXISTS desconto_percentual INT         NULL,
  ADD COLUMN IF NOT EXISTS destaque_carrossel TINYINT(1)   NULL DEFAULT 0;

-- 3) (Opcional) Índice único por código de barras.
--    Se você já criou manualmente, pode deixar como comentário.
--    Caso queira criar via migration, use este bloco simples e rode UMA vez:
-- ALTER TABLE produto ADD UNIQUE KEY uk_produto_cod_barras (codigo_barras);

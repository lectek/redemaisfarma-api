-- Garante que a tabela exista antes (opcional, só pra segurança extra)
-- Se tiver certeza que ela existe, pode remover esse bloco.
CREATE TABLE IF NOT EXISTS sync_checkpoint (
    id BIGINT NOT NULL PRIMARY KEY,
    source VARCHAR(100) NOT NULL
    -- não repita colunas que já existem aqui, isso é só um exemplo
);

-- Adiciona a coluna só se ainda não existir
ALTER TABLE sync_checkpoint
    ADD COLUMN IF NOT EXISTS last_since datetime(6) NULL AFTER source;

-- Adiciona coluna last_since para controle de checkpoint do import
ALTER TABLE sync_checkpoint
    ADD COLUMN last_since datetime(6) NULL AFTER updated_at;

-- Normaliza email/cpf em INSERT/UPDATE (minúsculo, sem espaços; cpf só dígitos)

DROP TRIGGER IF EXISTS cliente_bi;
DROP TRIGGER IF EXISTS cliente_bu;

CREATE TRIGGER cliente_bi
BEFORE INSERT ON cliente
FOR EACH ROW
  SET NEW.email = LOWER(TRIM(NEW.email)),
      NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf),'.',''),'-',''),' ','');  -- só dígitos

CREATE TRIGGER cliente_bu
BEFORE UPDATE ON cliente
FOR EACH ROW
  SET NEW.email = LOWER(TRIM(NEW.email)),
      NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf),'.',''),'-',''),' ','');

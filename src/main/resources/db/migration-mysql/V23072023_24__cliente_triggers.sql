-- Gatilhos para normalizar email/cpf sempre que inserir/atualizar

DROP TRIGGER IF EXISTS cliente_bi;
DROP TRIGGER IF EXISTS cliente_bu;

DELIMITER $$

CREATE TRIGGER cliente_bi
BEFORE INSERT ON cliente
FOR EACH ROW
BEGIN
  SET NEW.email = LOWER(TRIM(NEW.email));
  SET NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf),'.',''),'-',''),' ');
END$$

CREATE TRIGGER cliente_bu
BEFORE UPDATE ON cliente
FOR EACH ROW
BEGIN
  SET NEW.email = LOWER(TRIM(NEW.email));
  SET NEW.cpf   = REPLACE(REPLACE(REPLACE(TRIM(NEW.cpf),'.',''),'-',''),' ');
END$$

DELIMITER ;

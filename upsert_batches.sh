#!/bin/sh
set -e

MIN=$(mysql -N -uapp_user -p'Rede_Mais_Farma25@' -D redemaisfarma -e "SELECT COALESCE(MIN(produto_id),0) FROM produtos")
MAX=$(mysql -N -uapp_user -p'Rede_Mais_Farma25@' -D redemaisfarma -e "SELECT COALESCE(MAX(produto_id),0) FROM produtos")
STEP=250

echo "Faixa de produto_id: $MIN..$MAX (step=$STEP)"
A=$MIN
while [ "$A" -le "$MAX" ]; do
  B=$((A+STEP-1))
  echo ">> Batch $A..$B"

mysql --default-character-set=utf8mb4 -uapp_user -p'Rede_Mais_Farma25@' -D redemaisfarma <<SQL
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
SET SESSION innodb_lock_wait_timeout = 60;

INSERT INTO produto (
  nome, descricao, preco_venda, preco_custo, codigo_barras, estoque, unidade,
  data_cadastro, created_at, updated_at, version, hash_legado, legacy_id, status, disponivel
)
SELECT
  p.produto,
  p.apresentacao,
  COALESCE(p.prod_prpromocao, p.prod_prvenda, 0),
  COALESCE(p.prod_prvenda, 0),
  CASE
    WHEN p.cod_barras IS NULL OR p.cod_barras = '' THEN NULL
    WHEN p.produto_id = kb.keep_id THEN p.cod_barras
    ELSE NULL
  END AS codigo_barras,
  COALESCE(p.prod_saldo, 0),
  'UN',
  NOW(), NOW(), NOW(),
  0,
  CASE
    WHEN p.produto_id IS NOT NULL AND p.produto_id > 0
      THEN SHA1(CONCAT('FIREBIRD:ID:', p.produto_id))
    WHEN NULLIF(p.cod_barras,'') IS NOT NULL
      THEN SHA1(CONCAT('FIREBIRD:EAN:', p.cod_barras))
    ELSE
      SHA1(CONCAT('FIREBIRD:NOME:', TRIM(p.produto)))
  END AS hash_legado,
  p.produto_id,
  'ATIVO',
  1
FROM produtos p
LEFT JOIN (
  SELECT cod_barras, MIN(produto_id) AS keep_id
  FROM produtos
  WHERE cod_barras IS NOT NULL AND cod_barras <> ''
  GROUP BY cod_barras
) kb ON kb.cod_barras = p.cod_barras
WHERE p.produto_id BETWEEN ${A} AND ${B}
ORDER BY COALESCE(p.cod_barras,''), p.produto_id
ON DUPLICATE KEY UPDATE
  preco_venda = VALUES(preco_venda),
  preco_custo = VALUES(preco_custo),
  estoque     = VALUES(estoque),
  updated_at  = NOW();

SELECT ROW_COUNT() AS afetadas;
SQL

  A=$((B+1))
done
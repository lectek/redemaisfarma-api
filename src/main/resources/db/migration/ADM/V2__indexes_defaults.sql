/* ---------- USUARIO ---------- */
CREATE INDEX idx_usuario_ultimo_acesso ON usuario (ultimo_acesso);

/* ---------- USUARIO_ROLES ---------- */
CREATE INDEX idx_usuario_roles_role ON usuario_roles (role);

/* ---------- CLIENTE ---------- */
CREATE INDEX idx_cliente_nome     ON cliente (nome);
CREATE INDEX idx_cliente_telefone ON cliente (telefone);

/* ---------- PRODUTO ---------- */
CREATE INDEX idx_produto_categoria_disponivel ON produto (categoria, disponivel);
CREATE INDEX idx_produto_estoque              ON produto (estoque);
CREATE INDEX idx_produto_nome                 ON produto (nome);
CREATE INDEX idx_produto_fabricante           ON produto (fabricante);

/* ---------- PEDIDO ---------- */
CREATE INDEX idx_pedido_cliente ON pedido (cliente_id);
CREATE INDEX idx_pedido_status  ON pedido (status);
CREATE INDEX idx_pedido_data    ON pedido (data);

/* ---------- ITEM_PEDIDO ---------- */
CREATE INDEX idx_item_pedido_pedido  ON item_pedido (pedido_id);
CREATE INDEX idx_item_pedido_produto ON item_pedido (produto_id);

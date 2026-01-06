(() => {
  const $ = (id) => document.getElementById(id);
  const termInput = $("pdv-term");
  const qtyInput = $("pdv-qty");
  const resultsEl = $("pdv-results");
  const cartEl = $("pdv-cart");
  const emptyEl = $("pdv-empty");
  const totalEl = $("pdv-total");
  const totalItemsEl = $("pdv-total-items");
  const statusEl = $("pdv-status");
  const scanStatusEl = $("pdv-scan-status");
  const cpfInput = $("pdv-cliente-cpf");
  const nomeInput = $("pdv-cliente-nome");
  const emailInput = $("pdv-cliente-email");
  const notaEmailRadio = document.querySelector("input[name='nota'][value='EMAIL']");
  let criarCliente = false;
  const finalizarBtn = $("pdv-finalizar");

  const cart = new Map();

  function formatMoney(value) {
    const num = Number(value || 0);
    return num.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  }

  function getCsrfToken() {
    const meta = document.querySelector("meta[name='_csrf']");
    return meta ? meta.getAttribute("content") : "";
  }

  function setStatus(message, isError = false) {
    statusEl.textContent = message || "";
    statusEl.style.color = isError ? "#b91c1c" : "var(--layout-muted)";
  }

  function setScanStatus(message, isError = false) {
    if (!scanStatusEl) return;
    scanStatusEl.textContent = message || "";
    scanStatusEl.style.color = isError ? "#b91c1c" : "var(--layout-muted)";
  }

  function normalizeBarcode(value) {
    if (!value) return "";
    let cleaned = value.trim();
    if (cleaned.startsWith("]C1") || cleaned.startsWith("]C2") || cleaned.startsWith("]C0")) {
      cleaned = cleaned.slice(3);
    }
    cleaned = cleaned.replace(/\D+/g, "");
    return cleaned;
  }

  function toggleEmailOption() {
    const hasEmail = !!emailInput.value.trim();
    if (notaEmailRadio) {
      notaEmailRadio.disabled = !hasEmail;
      if (!hasEmail && notaEmailRadio.checked) {
        document.querySelector("input[name='nota'][value='IMPRESSAO']").checked = true;
      }
    }
  }

  function renderCart() {
    cartEl.innerHTML = "";
    let total = 0;
    let totalItems = 0;
    cart.forEach((item) => {
      const subtotal = item.preco * item.quantidade;
      total += subtotal;
      totalItems += item.quantidade;
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>
          <div>${item.nome}</div>
          <small>${item.codigoBarras || ""}</small>
        </td>
        <td>
          <input class="input pdv-cart-qty" type="number" min="1" value="${item.quantidade}" data-id="${item.id}"/>
        </td>
        <td>${formatMoney(item.preco)}</td>
        <td>${formatMoney(subtotal)}</td>
        <td><button class="btn btn--ghost" data-remove="${item.id}">Remover</button></td>
      `;
      cartEl.appendChild(row);
    });
    emptyEl.style.display = cart.size ? "none" : "block";
    totalEl.textContent = formatMoney(total);
    totalItemsEl.textContent = totalItems.toString();
  }

  function addToCart(produto, quantidade) {
    const qty = Number(quantidade) || 1;
    const existing = cart.get(produto.id);
    if (existing) {
      existing.quantidade += qty;
    } else {
      cart.set(produto.id, {
        id: produto.id,
        nome: produto.nome,
        codigoBarras: produto.codigoBarras,
        preco: Number(produto.preco || 0),
        quantidade: qty,
      });
    }
    renderCart();
  }

  async function buscarProdutos(termo) {
    const resp = await fetch(`/admin/vendas/rapida/produtos?q=${encodeURIComponent(termo)}`);
    if (!resp.ok) {
      throw new Error("Falha ao buscar produtos.");
    }
    return resp.json();
  }

  function renderResultados(lista) {
    resultsEl.innerHTML = "";
    if (!lista || !lista.length) {
      resultsEl.innerHTML = "<div class='muted'>Nenhum produto encontrado.</div>";
      return;
    }
    lista.forEach((p) => {
      const div = document.createElement("div");
      div.className = "pdv-result";
      div.innerHTML = `
        <div>
          <strong>${p.nome}</strong>
          <small>${p.codigoBarras || "Sem código"}</small>
        </div>
        <div>${formatMoney(p.preco)}</div>
        <button class="btn btn--sm" data-add="${p.id}">Adicionar</button>
      `;
      div.querySelector("[data-add]").addEventListener("click", () => {
        addToCart(p, qtyInput.value);
        termInput.value = "";
        resultsEl.innerHTML = "";
        termInput.focus();
      });
      resultsEl.appendChild(div);
    });
  }

  document.getElementById("pdv-search").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const raw = termInput.value.trim();
    if (!raw) return;
    const termo = normalizeBarcode(raw) || raw;
    if (termo !== raw) {
      termInput.value = termo;
      setScanStatus(`Leitura normalizada: ${termo}`);
    } else {
      setScanStatus("Leitor pronto. Foque o campo e escaneie o código.");
    }
    setStatus("");
    try {
      const lista = await buscarProdutos(termo);
      if (lista.length === 1) {
        addToCart(lista[0], qtyInput.value);
        termInput.value = "";
        resultsEl.innerHTML = "";
      } else {
        renderResultados(lista);
      }
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  async function buscarClientePorCpf(cpf) {
    const resp = await fetch(`/admin/vendas/rapida/cliente?cpf=${encodeURIComponent(cpf)}`);
    if (!resp.ok) {
      throw new Error("Falha ao buscar cliente.");
    }
    return resp.json();
  }

  cpfInput?.addEventListener("blur", async () => {
    const cpf = cpfInput.value.trim();
    if (!cpf) {
      criarCliente = false;
      return;
    }
    try {
      const data = await buscarClientePorCpf(cpf);
      if (data.existe) {
        criarCliente = false;
        if (data.nome && !nomeInput.value.trim()) nomeInput.value = data.nome;
        if (data.email && !emailInput.value.trim()) emailInput.value = data.email;
        setStatus("Cliente encontrado. Dados carregados.");
      } else {
        const aceita = window.confirm("CPF não encontrado. Deseja criar cadastro?");
        criarCliente = aceita;
        if (!aceita) {
          setStatus("Sem cadastro. Informe e-mail se desejar envio por e-mail.");
        } else {
          setStatus("Cadastro será criado ao finalizar.");
        }
      }
      toggleEmailOption();
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  emailInput?.addEventListener("input", toggleEmailOption);

  cartEl.addEventListener("input", (ev) => {
    const target = ev.target;
    if (!target.matches("[data-id]")) return;
    const id = Number(target.getAttribute("data-id"));
    const item = cart.get(id);
    if (!item) return;
    const qty = Math.max(1, Number(target.value || 1));
    item.quantidade = qty;
    renderCart();
  });

  cartEl.addEventListener("click", (ev) => {
    const target = ev.target;
    const id = target.getAttribute("data-remove");
    if (!id) return;
    cart.delete(Number(id));
    renderCart();
  });

  function getPagamento() {
    const radio = document.querySelector("input[name='pagamento']:checked");
    return radio ? radio.value : "PIX";
  }

  function getNota() {
    const radio = document.querySelector("input[name='nota']:checked");
    return radio ? radio.value : "IMPRESSAO";
  }

  finalizarBtn.addEventListener("click", async () => {
    setStatus("");
    if (!cart.size) {
      setStatus("Adicione ao menos um item.", true);
      return;
    }
    const pagamentoTipo = getPagamento();
    const notaOpcao = getNota();
    const trocoPara = Number($("pdv-troco").value || 0);
    const total = Array.from(cart.values()).reduce((acc, item) => acc + item.preco * item.quantidade, 0);
    if (pagamentoTipo === "DINHEIRO" && trocoPara > 0 && trocoPara < total) {
      setStatus("Troco para deve ser maior que o total.", true);
      return;
    }
    const email = $("pdv-cliente-email").value.trim();
    if (notaOpcao === "EMAIL" && !email) {
      setStatus("Informe o e-mail para envio da nota.", true);
      return;
    }
    const payload = {
      itens: Array.from(cart.values()).map((item) => ({
        produtoId: item.id,
        quantidade: item.quantidade
      })),
      clienteCpf: cpfInput.value.trim(),
      clienteNome: nomeInput.value.trim(),
      clienteEmail: email,
      criarCliente,
      pagamentoTipo,
      trocoPara: pagamentoTipo === "DINHEIRO" ? trocoPara : null,
      notaOpcao
    };
    try {
      const resp = await fetch("/admin/vendas/rapida/finalizar", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": getCsrfToken()
        },
        body: JSON.stringify(payload)
      });
      const data = await resp.json();
      if (!resp.ok || !data.ok) {
        throw new Error(data.message || "Falha ao finalizar venda.");
      }
      setStatus(data.message || "Venda finalizada.");
      cart.clear();
      renderCart();
      if (data.reciboUrl && notaOpcao === "IMPRESSAO") {
        window.open(data.reciboUrl, "_blank", "width=480,height=640");
      }
    } catch (err) {
      setStatus(err.message, true);
    }
  });

  renderCart();
  toggleEmailOption();
})();

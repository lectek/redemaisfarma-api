(() => {
  const isAdmin = !!(window.CATALOGO_CTX && window.CATALOGO_CTX.isAdmin);
  const grid = document.getElementById("catalogo-grid");
  const formBusca = document.getElementById("form-busca");
  const inputQ = document.getElementById("q");
  const btnLimpar = document.getElementById("btn-limpar");
  const btnPrev = document.getElementById("btn-prev");
  const btnNext = document.getElementById("btn-next");
  const pagInfo = document.getElementById("paginacao-info");

  // paginação simples (server-side)
  let page = 0;
  let size = 24;
  let hasNext = false;

  document.addEventListener("DOMContentLoaded", () => {
    carregar();
    wireBusca();
    wirePaginacao();
    wireModal();
  });

  function wireBusca() {
    formBusca?.addEventListener("submit", (e) => {
      e.preventDefault();
      page = 0;
      carregar();
    });
    btnLimpar?.addEventListener("click", () => {
      inputQ.value = "";
      page = 0;
      carregar();
    });
  }

  function wirePaginacao() {
    btnPrev?.addEventListener("click", () => {
      if (page > 0) {
        page--;
        carregar();
      }
    });
    btnNext?.addEventListener("click", () => {
      if (hasNext) {
        page++;
        carregar();
      }
    });
  }

  async function carregar() {
    if (!grid) return;
    grid.innerHTML = `<div class="card p-4">Carregando…</div>`;
    const q = inputQ?.value?.trim() || "";

    // >>> Admin API
    const url = new URL("/api/admin/produtos", window.location.origin);
    url.searchParams.set("page", page);
    url.searchParams.set("size", size);
    if (q) url.searchParams.set("q", q);

    try {
      const resp = await fetch(url, { headers: { "Accept": "application/json" } });
      if (!resp.ok) {
        grid.innerHTML = `<div class="card p-4">Erro ao carregar (${resp.status}).</div>`;
        return;
      }

      const data = await resp.json();
      const content = Array.isArray(data) ? data : (data.content || []);
      hasNext = Array.isArray(data) ? false : !data.last;
      atualizarPaginacao();

      if (!content.length) {
        grid.innerHTML = `<div class="card p-4">Nenhum produto encontrado.</div>`;
        return;
      }

      grid.innerHTML = content.map(p => renderCard(p)).join("");
      bindCardActions(content);
    } catch (err) {
      grid.innerHTML = `<div class="card p-4">Falha de rede ao consultar catálogo.</div>`;
    }
  }

  function atualizarPaginacao() {
    if (!btnPrev || !btnNext || !pagInfo) return;
    btnPrev.disabled = page <= 0;
    btnNext.disabled = !hasNext;
    pagInfo.textContent = `Página ${page + 1}`;
  }

  function renderCard(p) {
    // ProdutoResponseDTO -> preco / estoqueAtual / situacao
    const preco = (p.preco != null) ? Number(p.preco) :
                  (p.precoVenda != null ? Number(p.precoVenda) : 0.01); // fallback
    const precoFmt = preco.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

    const estoqueNum = Number.isFinite(p.estoqueAtual) ? p.estoqueAtual :
                       (Number.isFinite(p.estoque) ? p.estoque : undefined); // fallback
    const estoque = Number.isFinite(estoqueNum) ? `<small class="text-muted">Estoque: ${estoqueNum}</small>` : "";

    const disponivelCalc =
      (typeof p.disponivel === "boolean" ? p.disponivel :
        (p.situacao ? String(p.situacao).toUpperCase() === "ATIVO" : (preco > 0 && (estoqueNum ?? 0) > 0)));

    const disponibilidade = disponivelCalc ? "" : `<span class="badge danger ml-2">Indisponível</span>`;

    const acoesCliente = `
      <div class="form-row gap-2 mt-2">
        <button class="btn" data-add-carrinho data-id="${p.id}" ${disponivelCalc ? "" : "disabled"}>Adicionar ao carrinho</button>
      </div>`;

    const acoesAdmin = `
      <div class="form-row gap-2 mt-2">
        <button class="btn" data-editar data-id="${p.id}">Editar</button>
        <button class="btn btn-ghost" data-trocar-foto data-id="${p.id}">Trocar foto</button>
      </div>`;

    const nome = p.nome || "Produto sem nome";
    const desc = p.descricao || "";
    const imagemUrl = (p.imagem && String(p.imagem).trim() !== "") ? p.imagem : null;

    const imagemHtml = imagemUrl
      ? `<img src="${escapeHtml(imagemUrl)}" alt="${escapeHtml(nome)}" class="card-img-top" style="height:180px;object-fit:cover;border-radius: var(--radius-lg);"/>`
      : (isAdmin
          ? `<div class="card-img-top center muted" style="height:180px;border:1px dashed var(--border);border-radius: var(--radius-lg);display:flex;align-items:center;justify-content:center">Sem imagem</div>`
          : `<div class="card-img-top center muted" style="height:180px;border:1px solid var(--border);border-radius: var(--radius-lg);display:flex;align-items:center;justify-content:center">sem mídia no momento</div>`);

    return `
      <article class="card p-3 product-card" data-card data-id="${p.id}">
        ${imagemHtml}
        <div class="stack mt-2">
          <h3 class="h5 m-0 line-clamp-2">${escapeHtml(nome)}${disponibilidade}</h3>
          <p class="text-muted m-0 line-clamp-2">${escapeHtml(desc)}</p>
          <div class="between mt-1">
            <strong>${precoFmt}</strong>
            ${estoque}
          </div>
          ${isAdmin ? acoesAdmin : acoesCliente}
        </div>
      </article>
    `;
  }

  function bindCardActions(produtos) {
    // Cliente: adicionar ao carrinho
    document.querySelectorAll("[data-add-carrinho]").forEach(btn => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-id");
        btn.disabled = true;
        try {
          const ok = await addToCart(id);
          toast(ok ? "Adicionado ao carrinho!" : "Não foi possível adicionar.");
        } finally {
          btn.disabled = false;
        }
      });
    });

    if (!isAdmin) return;

    // Admin: editar
    document.querySelectorAll("[data-editar]").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = String(btn.getAttribute("data-id"));
        const p = produtos.find(x => String(x.id) === id);
        if (p) abrirModalEdicao(p);
      });
    });

    // Admin: trocar foto
    document.querySelectorAll("[data-trocar-foto]").forEach(btn => {
      btn.addEventListener("click", () => {
        const id = String(btn.getAttribute("data-id"));
        const p = produtos.find(x => String(x.id) === id);
        if (p) abrirModalEdicao(p, /*focoImagem*/ true);
      });
    });
  }

  async function addToCart(produtoId) {
    try {
      const resp = await fetch("/api/carrinho/itens", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ produtoId, quantidade: 1 })
      });
      return resp.ok;
    } catch {
      return false;
    }
  }

  // ===== Modal de edição =====

  const modal = document.getElementById("modal-editar");
  const formEditar = document.getElementById("form-editar");
  const closeBtns = modal?.querySelectorAll(".modal-close, [data-close]");

  function wireModal() {
    closeBtns?.forEach(b => b.addEventListener("click", fecharModal));
    modal?.addEventListener("click", (e) => {
      if (e.target === modal) fecharModal();
    });

    formEditar?.addEventListener("submit", async (e) => {
      e.preventDefault();
      await salvarEdicao();
    });
  }

  function abrirModalEdicao(p, focoImagem = false) {
    if (!modal) return;
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");

    byId("edit-id").value = p.id;
    byId("edit-nome").value = p.nome || "";
    byId("edit-desc").value = p.descricao || "";
    const preco = (p.preco != null ? Number(p.preco) :
                   (p.precoVenda != null ? Number(p.precoVenda) : 0.01));
    byId("edit-preco").value = preco.toFixed(2);

    const estoque = Number.isFinite(p.estoqueAtual) ? p.estoqueAtual :
                    (Number.isFinite(p.estoque) ? p.estoque : 0);
    byId("edit-estoque").value = estoque;

    const disponivel =
      (typeof p.disponivel === "boolean" ? p.disponivel :
        (p.situacao ? String(p.situacao).toUpperCase() === "ATIVO" : (preco > 0 && estoque > 0)));
    byId("edit-disponivel").value = String(!!disponivel);

    byId("edit-categoria").value = p.categoria || "";
    byId("edit-imagem-url").value = p.imagem || "";
    byId("edit-imagem-file").value = "";
    byId("edit-ean").textContent = p.codigoBarras || "—";

    if (focoImagem) {
      byId("edit-imagem-file").focus();
    } else {
      byId("edit-nome").focus();
    }
  }

  function fecharModal() {
    if (!modal) return;
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
    formEditar?.reset();
  }

  async function salvarEdicao() {
    const id = byId("edit-id")?.value;
    if (!id) return;

    // ProdutoRequestDTO esperado pelo Admin REST:
    const body = {
      nome: byId("edit-nome").value.trim(),
      descricao: byId("edit-desc").value.trim(),
      preco: parseFloat(byId("edit-preco").value.replace(",", ".")),
      estoque: parseInt(byId("edit-estoque").value, 10) || 0,
      ativo: byId("edit-disponivel").value === "true",
      categoria: byId("edit-categoria").value.trim(),
      imagem: byId("edit-imagem-url").value.trim() || null
      // se o DTO exigir sku/tenantId/codigoBarras obrigatórios, incluir aqui conforme sua regra
    };

    // 1) Atualiza dados principais
    const resp = await fetch(`/api/admin/produtos/${encodeURIComponent(id)}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    if (!resp.ok) {
      try {
        const err = await resp.json();
        toast(err?.message || "Erro ao salvar dados.", true);
      } catch {
        toast("Erro ao salvar dados.", true);
      }
      return;
    }

    // 2) Upload de imagem, se houver
    const file = byId("edit-imagem-file").files?.[0];
    if (file) {
      const fd = new FormData();
      fd.append("file", file);
      const up = await fetch(`/api/admin/produtos/${encodeURIComponent(id)}/imagem`, {
        method: "POST",
        body: fd
      });
      if (!up.ok) {
        try {
          const err = await up.json();
          toast(err?.message || "Dados salvos, mas falhou ao enviar a imagem.", true);
        } catch {
          toast("Dados salvos, mas falhou ao enviar a imagem.", true);
        }
        fecharModal();
        carregar();
        return;
      }
    }

    toast("Produto atualizado!");
    fecharModal();
    carregar();
  }

  // ===== Utils =====
  function byId(id) { return document.getElementById(id); }
  function toast(msg, err) {
    const el = document.createElement("div");
    el.className = `toast ${err ? "danger" : "success"}`;
    el.textContent = msg;
    Object.assign(el.style, {
      position: "fixed", right: "1rem", bottom: "1rem",
      padding: "0.75rem 1rem", background: err ? "#ef4444" : "#10b981",
      color: "#fff", borderRadius: "8px", zIndex: 9999, boxShadow: "0 8px 30px rgba(0,0,0,.2)"
    });
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2500);
  }
  function escapeHtml(s) {
    return String(s ?? "").replace(/[&<>"']/g, m => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[m]));
  }
})();

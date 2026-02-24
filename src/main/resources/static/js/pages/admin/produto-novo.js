(() => {
  const input = document.getElementById("quickProdutoTermo");
  const clearBtn = document.getElementById("quickProdutoClear");
  const hint = document.getElementById("quickProdutoHint");
  const results = document.getElementById("quickProdutoResultados");

  if (!input || !results || !hint) return;

  const fields = {
    nome: document.getElementById("nome"),
    descricao: document.getElementById("descricao"),
    precoVenda: document.getElementById("precoVenda"),
    precoPromocional: document.getElementById("precoPromocional"),
    estoque: document.getElementById("estoque"),
    categoria: document.getElementById("categoria"),
    codigoBarras: document.getElementById("codigoBarras"),
    fabricante: document.getElementById("fabricante"),
    unidade: document.getElementById("unidade"),
    disponivel: document.getElementById("disponivel")
  };

  let debounceId = null;
  let requestId = 0;

  const setHint = (message) => {
    hint.textContent = message;
  };

  const money = (value) => {
    if (value == null || value === "") return "-";
    const n = Number(value);
    if (!Number.isFinite(n)) return "-";
    return n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
  };

  const escapeHtml = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (char) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    })[char]);

  const clearResults = () => {
    results.innerHTML = "";
  };

  const hasValue = (el) => {
    if (!el) return false;
    return String(el.value ?? "").trim().length > 0;
  };

  const setText = (el, value) => {
    if (!el) return;
    el.value = value == null ? "" : String(value);
  };

  const setNumber = (el, value) => {
    if (!el) return;
    if (value == null || value === "") {
      el.value = "";
      return;
    }
    const n = Number(value);
    el.value = Number.isFinite(n) ? String(n) : "";
  };

  const setSelect = (el, value) => {
    if (!el || !value) return;
    const optionExists = Array.from(el.options || []).some((opt) => opt.value === value);
    if (!optionExists) {
      const opt = new Option(value, value, true, true);
      el.add(opt);
    }
    el.value = value;
  };

  const applySuggestion = (item) => {
    const formHasData = [
      fields.nome,
      fields.descricao,
      fields.precoVenda,
      fields.precoPromocional,
      fields.codigoBarras
    ].some(hasValue);

    if (formHasData) {
      const ok = window.confirm(
        "Deseja substituir os campos atuais pelos dados do produto selecionado?"
      );
      if (!ok) return;
    }

    setText(fields.nome, item.nome);
    setText(fields.descricao, item.descricao);
    setNumber(fields.precoVenda, item.precoVenda);
    setNumber(fields.precoPromocional, item.precoPromocional);
    setNumber(fields.estoque, item.estoque);
    setSelect(fields.categoria, item.categoria);
    setText(fields.codigoBarras, item.codigoBarras);
    setText(fields.fabricante, item.fabricante);
    setText(fields.unidade, item.unidade);
    if (fields.disponivel && item.estoque != null) {
      fields.disponivel.checked = Number(item.estoque) > 0;
    }

    setHint("Formulario preenchido. Revise os dados e clique em Salvar.");
    fields.precoVenda?.focus();
  };

  const renderResults = (items) => {
    clearResults();

    if (!Array.isArray(items) || items.length === 0) {
      setHint("Nenhum produto encontrado. Continue com cadastro manual.");
      return;
    }

    setHint(`Encontrados ${items.length} produto(s).`);

    const html = items.map((item) => {
      const titulo = escapeHtml(item.nome || "Produto sem nome");
      const descricao = escapeHtml(item.descricao || "Sem descricao");
      const categoria = escapeHtml(item.categoria || "Sem categoria");
      const codigo = escapeHtml(item.codigoBarras || "-");
      const estoque = item.estoque == null ? "-" : escapeHtml(item.estoque);
      const preco = escapeHtml(money(item.precoVenda));
      const id = encodeURIComponent(item.id);

      return `
        <article class="card p-3">
          <div class="between">
            <strong>${titulo}</strong>
            <small class="text-muted">${preco}</small>
          </div>
          <p class="text-muted m-0">${descricao}</p>
          <small class="text-muted">Categoria: ${categoria} | EAN: ${codigo} | Estoque: ${estoque}</small>
          <div class="form-actions mt-2">
            <button type="button" class="btn btn-primary" data-apply-id="${id}">Usar dados</button>
            <a class="btn btn-ghost" href="/admin/produtos/${id}/editar">Abrir edicao</a>
          </div>
        </article>
      `;
    }).join("");

    results.innerHTML = html;

    results.querySelectorAll("[data-apply-id]").forEach((button) => {
      button.addEventListener("click", () => {
        const id = decodeURIComponent(button.getAttribute("data-apply-id"));
        const selected = items.find((item) => String(item.id) === String(id));
        if (selected) applySuggestion(selected);
      });
    });
  };

  const search = async (query) => {
    const currentRequest = ++requestId;

    if (query.length < 2) {
      clearResults();
      setHint("Digite pelo menos 2 caracteres para pesquisar.");
      return;
    }

    setHint("Buscando produtos...");

    try {
      const resp = await fetch(
        `/admin/produtos/busca-rapida?q=${encodeURIComponent(query)}&limit=8`,
        {
          method: "GET",
          headers: { Accept: "application/json" },
          credentials: "same-origin"
        }
      );

      if (currentRequest !== requestId) return;

      if (!resp.ok) {
        clearResults();
        setHint("Nao foi possivel pesquisar agora. Tente novamente.");
        return;
      }

      const items = await resp.json();
      if (currentRequest !== requestId) return;
      renderResults(items);
    } catch (_error) {
      if (currentRequest !== requestId) return;
      clearResults();
      setHint("Falha de rede ao pesquisar produtos.");
    }
  };

  input.addEventListener("input", () => {
    clearTimeout(debounceId);
    const query = String(input.value || "").trim();
    debounceId = setTimeout(() => search(query), 260);
  });

  input.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    clearTimeout(debounceId);
    search(String(input.value || "").trim());
  });

  clearBtn?.addEventListener("click", () => {
    input.value = "";
    clearTimeout(debounceId);
    clearResults();
    setHint("Digite pelo menos 2 caracteres para pesquisar.");
    input.focus();
  });
})();

/* =========================================================
   src/main/resources/static/js/index.js
   Garante que a Home não fica vazia quando não houver
   "destaques" renderizados no servidor.
   - Mostra skeletons
   - Tenta carregar via API JSON (se houver)
   - Fallback parseando /produtos (HTML) e importando cards
   - Mostra mensagem/CTA se nada for encontrado
   ========================================================= */

(() => {
  const FALLBACK_LIMIT = 12;

  // URLs configuráveis (pode sobrescrever via window.APP_ROUTES)
  const URLS = {
    destaquesApi:
      (window.APP_ROUTES && window.APP_ROUTES.destaquesUrl) ||
      "/api/public/produtos/destaques?limit=" + FALLBACK_LIMIT,
    produtosPage:
      (window.APP_ROUTES && window.APP_ROUTES.produtosListUrl) ||
      "/produtos",
  };

  document.addEventListener("DOMContentLoaded", bootstrap);

  async function bootstrap() {
    const section = findDestaquesSection();
    if (!section) return; // não força nada se a home não tiver "Destaques"

    const container = ensureContainer(section);
    if (hasAnyProductCards(section)) return; // já veio renderizado pelo servidor

    const cleanup = showSkeletons(container, FALLBACK_LIMIT);

    // 1) tenta via API JSON (se existir)
    let filled = false;
    try {
      filled = await tryFillFromApi(container);
    } catch (_) { /* silencia */ }

    // 2) se não deu, tenta parsear /produtos e importar cards
    if (!filled) {
      try {
        filled = await tryFillFromProdutosPage(container);
      } catch (_) { /* silencia */ }
    }

    // 3) remove skeletons e, se ainda vazio, mostra fallback amigável
    cleanup();
    if (!filled && !hasAnyProductCards(section)) {
      showEmptyState(container);
    }
  }

  /* =============================
     Localiza/garante área destino
     ============================= */
  function findDestaquesSection() {
    // Procura uma <section> que contenha o título "Destaques"
    const allSections = document.querySelectorAll("main section, section");
    for (const s of allSections) {
      const h = s.querySelector("h2, h3, .page-title, .section-title");
      const text = (h?.textContent || "").trim().toLowerCase();
      if (text === "destaques") return s;
    }
    // Fallback: usa o <main> inteiro
    return document.getElementById("main-content") || document.querySelector("main");
  }

  function ensureContainer(section) {
    // Tenta achar contêineres comuns (fragmentos existentes)
    const existing =
      section.querySelector("[data-destaques], .carousel, .card-grid, .cards, .vitrine, .lista-produtos");
    if (existing) return existing;

    // Se não existir, cria um grid padrão
    const div = document.createElement("div");
    div.className = "card-grid";
    div.setAttribute("data-destaques", "true");
    section.appendChild(div);
    return div;
  }

  function hasAnyProductCards(scope) {
    return !!scope.querySelector(
      "[data-card='produto'], .product-card, .produto-card, .card.card-produto"
    );
  }

  /* =================
     Skeleton handling
     ================= */
  function showSkeletons(container, n = 8) {
    const frag = document.createDocumentFragment();
    for (let i = 0; i < n; i++) {
      const sk = document.createElement("div");
      sk.className = "card product-card skeleton";
      sk.innerHTML = `
        <div class="card-media skeleton-block" style="aspect-ratio:1/1;"></div>
        <div class="card-body">
          <div class="skeleton-line" style="height:14px;width:80%;margin:6px 0;"></div>
          <div class="skeleton-line" style="height:14px;width:60%;margin:6px 0;"></div>
          <div class="skeleton-line" style="height:20px;width:40%;margin-top:10px;"></div>
        </div>
      `;
      frag.appendChild(sk);
    }
    container.appendChild(frag);

    return () => {
      container.querySelectorAll(".skeleton").forEach(el => el.remove());
    };
  }

  /* ===============================
     1) Tentar via API JSON (se houver)
     =============================== */
  async function tryFillFromApi(container) {
    try {
      const res = await fetch(URLS.destaquesApi, { headers: { "Accept": "application/json" } });
      if (!res.ok) return false;
      const data = await res.json();
      if (!Array.isArray(data) || data.length === 0) return false;
      renderProducts(container, data.slice(0, FALLBACK_LIMIT));
      return true;
    } catch {
      return false;
    }
  }

  /* ======================================================
     2) Fallback: puxar /produtos e importar primeiros cards
     (funciona mesmo que não exista API pública de destaques)
     ====================================================== */
  async function tryFillFromProdutosPage(container) {
    const res = await fetch(URLS.produtosPage, { headers: { "Accept": "text/html" } });
    if (!res.ok) return false;

    const html = await res.text();
    const doc = new DOMParser().parseFromString(html, "text/html");

    // tenta pegar cards com seletores comuns
    const cards = doc.querySelectorAll(
      "[data-card='produto'], .product-card, .produto-card, .card.card-produto"
    );
    if (!cards.length) return false;

    const frag = document.createDocumentFragment();
    for (let i = 0; i < Math.min(cards.length, FALLBACK_LIMIT); i++) {
      // clona o card para não perder estilos
      frag.appendChild(cards[i].cloneNode(true));
    }
    container.appendChild(frag);
    return true;
  }

  /* =====================
     Render com dados JSON
     ===================== */
  function renderProducts(container, items) {
    const frag = document.createDocumentFragment();
    items.forEach((p) => {
      const href = p.slug ? `/produtos/${p.slug}` : (p.id ? `/produtos/${p.id}` : URLS.produtosPage);
      const nome = p.nome || p.titulo || "Produto";
      const img = p.imagemUrl || p.foto || "/images/placeholder.png";
      const preco =
        p.precoPor ?? p.preco ?? p.valor ?? null;
      const precoDe =
        p.precoDe ?? p.de ?? null;

      const el = document.createElement("a");
      el.className = "card product-card";
      el.href = href;
      el.setAttribute("data-card", "produto");
      el.innerHTML = `
        <div class="card-media">
          <img src="${escapeHtml(img)}" alt="${escapeHtmlAttr(nome)}" loading="lazy">
        </div>
        <div class="card-body">
          <h3 class="card-title">${escapeHtml(nome)}</h3>
          <div class="card-price">
            ${priceHtml(preco, precoDe)}
          </div>
          <div class="card-actions">
            <span class="btn btn-sm">Ver detalhes</span>
          </div>
        </div>
      `;
      frag.appendChild(el);
    });
    container.appendChild(frag);
  }

  function priceHtml(preco, precoDe) {
    const fmt = (v) =>
      typeof v === "number"
        ? v.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })
        : (v || "");
    if (preco == null && precoDe == null) return `<span class="text-muted">Consulte</span>`;
    if (precoDe != null && preco != null) {
      return `
        <span class="price-old">${escapeHtml(fmt(precoDe))}</span>
        <span class="price">${escapeHtml(fmt(preco))}</span>
      `;
    }
    return `<span class="price">${escapeHtml(fmt(preco ?? precoDe))}</span>`;
  }

  /* ===========================
     Estado vazio (mensagem/CTA)
     =========================== */
  function showEmptyState(container) {
    const wrap = document.createElement("div");
    wrap.className = "empty-state text-center";
    wrap.innerHTML = `
      <p class="text-muted">Nenhum destaque disponível no momento.</p>
      <a class="btn" href="${URLS.produtosPage}">Ver todos os produtos</a>
    `;
    container.appendChild(wrap);
  }

  /* =========
     Helpers
     ========= */
  function escapeHtml(s) {
    return String(s)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;");
  }
  function escapeHtmlAttr(s) {
    return escapeHtml(s).replaceAll('"', "&quot;");
  }
})();

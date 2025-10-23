// home-fallback.js (v2) — compatível com #listStatus/#listMessage/#loadMoreBtn
(() => {
  const grid = document.querySelector('#grid');
  const statusWrap = document.querySelector('#listStatus');
  const statusMsg = document.querySelector('#listMessage');
  const loadMoreBtn = document.querySelector('#loadMoreBtn');

  if (!grid) return;

  const fmtBRL = (v) =>
    Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
      .format(Number(v || 0));

  const normalize = (item) => {
    const title = item.titulo ?? item.nome ?? item.descricao ?? 'Produto';
    const img   = item.imagemUrl ?? item.imagem ?? '/img/placeholder.png';
    const preco = item.precoPromocional ?? item.precoVenda ?? item.preco ?? 0;
    const id    = item.id ?? item.codigo ?? title;
    const link  = item.link ?? (item.id ? `/produtos/${item.id}` : '#');
    return { id, title, img, preco, link };
  };

  const renderCard = (p) => `
    <li class="product-card">
      <a href="${p.link}" aria-label="${p.title}">
        <img src="${p.img}" alt="${p.title}">
        <h3 class="product-card__title">${p.title}</h3>
      </a>
      <div class="product-card__price">${fmtBRL(p.preco)}</div>
      <button class="btn--add" data-add="${p.id}">Adicionar</button>
    </li>
  `;

  const mount = (items = []) => {
    if (!items.length) {
      grid.innerHTML = '';
      if (statusWrap) {
        statusWrap.hidden = false;
        statusMsg.textContent = 'Sem itens para exibir no momento.';
        loadMoreBtn.hidden = true;
      }
      return;
    }

    grid.innerHTML = `<ul class="product-grid">${items.map(renderCard).join('')}</ul>`;
    if (statusWrap) statusWrap.hidden = true;

    // ação "Adicionar"
    grid.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-add]');
      if (!btn) return;
      const id = btn.getAttribute('data-add');
      const ev = new CustomEvent('carrinho:add', { detail: { id }, bubbles: true });
      btn.dispatchEvent(ev);
      btn.textContent = 'Adicionado!';
      setTimeout(() => (btn.textContent = 'Adicionar'), 1200);
    }, { once: true });
  };

  const tryFetch = async (page = 1, limit = 12) => {
    try {
      const res = await fetch(`/api/home/destaques?page=${page}&limit=${limit}`, {
        headers: { 'Accept': 'application/json' },
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const json = await res.json();
      // aceita array puro ou {data, page, hasMore}
      const raw = Array.isArray(json) ? json : (json?.data ?? []);
      const items = raw.map(normalize);
      const hasMore = Array.isArray(json) ? false : Boolean(json?.hasMore);
      return { items, hasMore };
    } catch {
      return null;
    }
  };

  const mock = () => ({
    items: [
      { id: 1, titulo: 'Vitamina C 1g', imagemUrl: '/img/mock/vitc.png',   preco: 19.9, link: '/produtos/1' },
      { id: 2, titulo: 'Máscara Cirúrgica', imagemUrl: '/img/mock/mask.png', preco: 9.9,  link: '/produtos/2' },
      { id: 3, titulo: 'Álcool 70% 500ml', imagemUrl: '/img/mock/alcool.png',preco: 12.5, link: '/produtos/3' },
    ].map(normalize),
    hasMore: false
  });

  let currentPage = 1;
  const pageSize = 12;
  let acc = [];

  const load = async (append = false) => {
    if (statusWrap) {
      statusWrap.hidden = false;
      statusMsg.textContent = 'Carregando…';
      loadMoreBtn.hidden = true;
    }

    const result = await tryFetch(currentPage, pageSize) ?? mock();

    if (append) {
      acc = acc.concat(result.items);
    } else {
      acc = result.items;
    }

    mount(acc);

    if (statusWrap) {
      if (result.hasMore) {
        loadMoreBtn.hidden = false;
        statusMsg.textContent = '';
      } else {
        loadMoreBtn.hidden = true;
        statusMsg.textContent = acc.length ? '' : 'Nada mais por aqui.';
      }
    }
  };

  // paginação (se o backend expuser hasMore)
  if (loadMoreBtn) {
    loadMoreBtn.addEventListener('click', () => {
      currentPage += 1;
      load(true);
    });
  }

  // inicial
  load(false);
})();

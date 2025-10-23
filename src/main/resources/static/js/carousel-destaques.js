// carousel-destaques.js
(() => {
  const dataSSR = (typeof window.__DESTAQUES !== 'undefined') ? window.__DESTAQUES : null;
  const root = document.querySelector('[data-carousel]') || document.querySelector('.carousel');
  if (!root) return;

  const vp   = root.querySelector('#carousel-viewport');
  const prev = root.querySelector('#carousel-prev');
  const next = root.querySelector('#carousel-next');
  const dots = root.querySelector('#carousel-dots');

  // Normaliza item (suporta chaves diferentes que podem vir do backend)
  const normalize = (item) => {
    const title = item.titulo ?? item.nome ?? item.descricao ?? 'Produto';
    const img   = item.imagemUrl ?? item.imagem ?? item.fotoUrl ?? '/img/placeholder.png';
    const preco = item.precoPromocional ?? item.precoPromocao ?? item.precoVenda ?? item.preco ?? null;
    const precoAnt = item.precoAnterior ?? item.precoDe ?? null;
    const id   = item.id ?? item.codigo ?? title;
    const link = item.link ?? (item.id ? `/produtos/${item.id}` : '#');
    return { id, title, img, preco, precoAnt, link };
  };

  const render = (items) => {
    if (!items?.length) {
      vp.innerHTML = `<div class="carousel__item"><div class="carousel__info"><h3 class="carousel__title">Sem destaques no momento</h3><p>Volte mais tarde para ver novas ofertas.</p></div></div>`;
      dots.innerHTML = '';
      prev.disabled = next.disabled = true;
      return { api: [], idx: 0 };
    }

    const api = items.map(normalize);
    vp.innerHTML = api.map((p, i) => `
      <article class="carousel__item" data-idx="${i}" aria-roledescription="slide">
        <img class="carousel__img" src="${p.img}" alt="${p.title}">
        <div class="carousel__info">
          <h3 class="carousel__title">${p.title}</h3>
          ${
            p.preco ? `
              <div class="price">
                <span class="new">${Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(p.preco))}</span>
                ${p.precoAnt ? `<s class="old">${Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(p.precoAnt))}</s>`:''}
              </div>` : ''
          }
          <a class="carousel__cta" href="${p.link}">Ver produto</a>
        </div>
      </article>
    `).join('');

    dots.innerHTML = api.map((_, i) => `<button class="dot" type="button" data-dot="${i}" aria-label="Ir para o slide ${i+1}"></button>`).join('');
    return { api, idx: 0 };
  };

  const state = render(dataSSR);

  const update = (idx) => {
    const count = vp.children.length;
    if (!count) return;
    const newIdx = (idx + count) % count;

    // mostra apenas o slide atual (CSS simples sem overflow-x)
    Array.from(vp.children).forEach((el, i) => {
      el.style.display = i === newIdx ? 'grid' : 'none';
    });

    Array.from(dots.children).forEach((d, i) => {
      d.classList.toggle('active', i === newIdx);
      d.setAttribute('aria-current', i === newIdx ? 'true' : 'false');
    });

    state.idx = newIdx;
  };

  // evento dots
  dots.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-dot]');
    if (!btn) return;
    update(Number(btn.dataset.dot));
  });

  // setas
  prev?.addEventListener('click', () => update(state.idx - 1));
  next?.addEventListener('click', () => update(state.idx + 1));

  // primeira pintura
  update(0);

  // (opcional) auto-play com foco seguro
  let timer;
  const start = () => { timer = setInterval(() => update(state.idx + 1), 5000); };
  const stop  = () => { clearInterval(timer); };
  root.addEventListener('mouseenter', stop);
  root.addEventListener('mouseleave', start);
  start();
})();

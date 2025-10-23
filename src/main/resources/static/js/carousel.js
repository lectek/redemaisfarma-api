// src/main/resources/static/js/components/carousel-destaques.js
const API = '/api/public/produtos/destaques?limit=10';

const viewport = document.querySelector('#carousel-viewport');
const prevBtn  = document.querySelector('#carousel-prev');
const nextBtn  = document.querySelector('#carousel-next');
const dots     = document.querySelector('#carousel-dots');

let items = [];
let idx = 0;
let timer = null;
const AUTOPLAY_MS = 4000;

// -------- helpers --------
const fmtPreco = (p) => {
  const v = p?.preco ?? p?.precoVenda ?? 0;
  return Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
};

const imgOrPlaceholder = (url) => {
  const u = (url || '').trim();
  if (!u) return '/images/placeholders/product.png';
  if (/^https?:\/\/|^\/\//i.test(u)) return u;
  return `/media/products/${u.replace(/^[/\\]+/, '')}`;
};

// id público: nossa API retorna UUID; manter fallback se vier numérico
const produtoHref = (p) => `/produtos/${encodeURIComponent(p.id ?? p.entityId ?? '')}`;

function cardTemplate(p) {
  const preco = fmtPreco(p);
  const nome  = p?.nome || 'Produto';
  const img   = imgOrPlaceholder(p?.imagem);

  return `
  <article class="carousel__item" role="group" aria-roledescription="slide"
           aria-label="${escapeHtml(nome)}">
    <img loading="lazy" decoding="async" fetchpriority="low"
         src="${img}" alt="${escapeHtml(nome)}" class="carousel__img"/>
    <div class="carousel__info">
      <h3 class="carousel__title">${escapeHtml(nome)}</h3>
      <div class="price"><span class="new">${preco}</span></div>
      <a class="carousel__cta" href="${produtoHref(p)}"
         aria-label="Ver ${escapeHtml(nome)}">Ver produto</a>
    </div>
  </article>`;
}

function render() {
  if (!viewport) return;

  if (!items.length) {
    viewport.innerHTML = '<p class="muted">Sem destaques no momento.</p>';
    dots && (dots.innerHTML = '');
    return;
  }

  viewport.innerHTML = cardTemplate(items[idx]);

  if (!dots) return;
  dots.innerHTML = items.map((_, i) =>
    `<button type="button" role="tab"
             class="dot ${i === idx ? 'active' : ''}"
             aria-selected="${i === idx}"
             aria-label="Ir para item ${i + 1} de ${items.length}"></button>`
  ).join('');

  dots.querySelectorAll('.dot').forEach((d, i) =>
    d.addEventListener('click', () => go(i), { once: true })
  );
}

function go(newIdx) {
  if (!items.length) return;
  idx = (newIdx + items.length) % items.length;
  render();
  restart();
}
function next(){ go(idx + 1); }
function prev(){ go(idx - 1); }

function start(){
  if (timer || items.length <= 1 || prefersReducedMotion()) return;
  timer = setInterval(next, AUTOPLAY_MS);
}
function stop(){
  if (timer) { clearInterval(timer); timer = null; }
}
function restart(){ stop(); start(); }

function prefersReducedMotion(){
  return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

// -------- swipe (touch) --------
let touchStartX = null;
viewport?.addEventListener('touchstart', (e) => {
  touchStartX = e.touches[0].clientX;
  stop();
}, { passive: true });

viewport?.addEventListener('touchend', (e) => {
  if (touchStartX == null) return;
  const dx = e.changedTouches[0].clientX - touchStartX;
  touchStartX = null;
  if (Math.abs(dx) > 30) (dx < 0 ? next() : prev());
  start();
}, { passive: true });

// -------- hover / focus / keyboard --------
viewport?.addEventListener('mouseenter', stop);
viewport?.addEventListener('mouseleave', start);
viewport?.addEventListener('focusin', stop);
viewport?.addEventListener('focusout', start);

prevBtn?.addEventListener('click', prev);
nextBtn?.addEventListener('click', next);

document.addEventListener('keydown', (e)=> {
  if (!viewport || !viewport.closest(':hover')) return; // só quando focado/hover
  if (e.key === 'ArrowRight') next();
  if (e.key === 'ArrowLeft')  prev();
});

// pause em aba oculta
document.addEventListener('visibilitychange', () => {
  if (document.hidden) stop(); else start();
});

// -------- init --------
(async function init(){
  try {
    const res = await fetch(API, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();

    // API pode retornar lista direta ou Page-like {content:[]}
    items = Array.isArray(data) ? data : (Array.isArray(data.content) ? data.content : []);
  } catch (e) {
    console.error('Falha ao carregar destaques', e);
    items = [];
  }
  render();
  start();
})();

// -------- utils --------
function escapeHtml(s){
  return String(s ?? '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
  }[m]));
}

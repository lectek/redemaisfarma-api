/* ===========================================
   Sidebar – FarmaVida / RedeMaisFarma (LekTeC)
   - Off-canvas acessível no mobile
   - Trap de foco, ESC, clique fora, scroll-lock
   - Compacto no desktop (persistente)
   - Auto-mount do botão no header (.sidebar-toggle)
   - Toggle com animação hamburger → chevron (via .is-open)
   =========================================== */

document.addEventListener("DOMContentLoaded", () => {
  initSidebar({ autoMountToggle: true });
});

function initSidebar(opts = {}) {
  const { autoMountToggle = false } = opts;

  const sidebar = document.querySelector(".sidebar");
  if (!sidebar) return;

  // Auto-injetar botão no header (mobile), se não existir
  if (autoMountToggle) autoMountSidebarToggle();

  // Seletores de botões
  const toggleBtns = document.querySelectorAll(
    "[data-sidebar-toggle], .sidebar-toggle, .js-sidebar-toggle"
  );
  const compactBtns = document.querySelectorAll(
    "[data-sidebar-compact-toggle], .sidebar-compact-toggle"
  );

  // ARIA base
  const SIDEBAR_ID = sidebar.id || "app-sidebar";
  sidebar.id = SIDEBAR_ID;
  sidebar.setAttribute("aria-hidden", "true");
  sidebar.setAttribute("tabindex", "-1");
  toggleBtns.forEach((btn) => {
    btn.setAttribute("aria-controls", SIDEBAR_ID);
    btn.setAttribute("aria-expanded", "false");
    btn.setAttribute("aria-label", btn.getAttribute("aria-label") || "Abrir menu");
  });

  // Overlay
  let overlay = null;
  function ensureOverlay() {
    if (overlay) return overlay;
    overlay = document.createElement("div");
    overlay.className = "sidebar-overlay";
    Object.assign(overlay.style, {
      position: "fixed",
      inset: "0",
      background: "rgba(15,23,42,.45)",
      backdropFilter: "blur(2px)",
      zIndex: "59",
      display: "none",
    });
    document.body.appendChild(overlay);
    overlay.addEventListener("click", close);
    return overlay;
  }

  // Trap de foco
  const focusableSel =
    'a[href],button:not([disabled]),input,select,textarea,[tabindex]:not([tabindex="-1"])';
  let lastFocused = null;

  function trapKeydown(e) {
    if (!isOpen()) return;
    if (e.key === "Escape") {
      e.preventDefault();
      close();
      return;
    }
    if (e.key !== "Tab") return;
    const items = Array.from(sidebar.querySelectorAll(focusableSel)).filter(
      (el) => el.offsetParent !== null || getComputedStyle(el).position === "fixed"
    );
    if (!items.length) return;
    const first = items[0];
    const last = items[items.length - 1];
    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault(); last.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault(); first.focus();
    }
  }

  const mqDesktop = window.matchMedia("(min-width: 769px)");

  function isOpen() { return sidebar.classList.contains("is-open"); }

  function open() {
    if (isOpen()) return;
    lastFocused = document.activeElement;
    sidebar.classList.add("is-open");
    sidebar.setAttribute("aria-hidden", "false");
    toggleBtns.forEach((b) => {
      b.classList.add("is-open");                 // <-- ativa animação chevron
      b.setAttribute("aria-expanded", "true");
      b.setAttribute("aria-label", "Fechar menu");
    });
    document.body.classList.add("no-scroll");
    if (!mqDesktop.matches) ensureOverlay().style.display = "block";
    const first = sidebar.querySelector(focusableSel);
    (first || sidebar).focus();
    document.addEventListener("keydown", trapKeydown);
  }

  function close() {
    if (!isOpen()) return;
    sidebar.classList.remove("is-open");
    sidebar.setAttribute("aria-hidden", "true");
    toggleBtns.forEach((b) => {
      b.classList.remove("is-open");              // <-- volta ao hamburger
      b.setAttribute("aria-expanded", "false");
      b.setAttribute("aria-label", "Abrir menu");
    });
    document.body.classList.remove("no-scroll");
    if (overlay) overlay.style.display = "none";
    document.removeEventListener("keydown", trapKeydown);
    if (lastFocused) try { lastFocused.focus(); } catch {}
  }

  function toggle() { isOpen() ? close() : open(); }

  // Eventos
  toggleBtns.forEach((btn) => btn.addEventListener("click", toggle));
  sidebar.addEventListener("click", (e) => {
    const a = e.target.closest("a[href]");
    if (a && !mqDesktop.matches) close();
  });
  mqDesktop.addEventListener("change", (e) => { if (e.matches) close(); });

  // Fallback de link ativo
  highlightActiveLink(sidebar);

  // Modo compacto persistente (desktop)
  const COMPACT_KEY = "sidebar_compact_v1";
  if (localStorage.getItem(COMPACT_KEY) === "1") sidebar.classList.add("sidebar--compact");
  function toggleCompact() {
    sidebar.classList.toggle("sidebar--compact");
    localStorage.setItem(COMPACT_KEY, sidebar.classList.contains("sidebar--compact") ? "1" : "0");
  }
  compactBtns.forEach((btn) => btn.addEventListener("click", toggleCompact));

  ensureOverlay(); // cria cedo
}

/* ---> Auto-montagem do botão no header (se não existir) */
function autoMountSidebarToggle() {
  if (document.querySelector("[data-sidebar-toggle], .sidebar-toggle, .js-sidebar-toggle")) return;

  const host =
    document.querySelector(".header-inner") ||
    document.querySelector(".site-header .container") ||
    document.querySelector(".navbar .container") ||
    document.querySelector(".site-header, header, .navbar");

  if (!host) return;

  const btn = document.createElement("button");
  btn.className = "sidebar-toggle";
  btn.type = "button";
  btn.setAttribute("data-sidebar-toggle", "");
  btn.setAttribute("aria-label", "Abrir menu");
  btn.setAttribute("aria-expanded", "false");
  btn.innerHTML = `<span class="menu-icon" aria-hidden="true"></span>`;
  host.firstChild ? host.insertBefore(btn, host.firstChild) : host.appendChild(btn);
}

/* ---------- utilidades ---------- */
function highlightActiveLink(sidebar) {
  try {
    const path = (location.pathname || "").replace(/\/+$/, "") || "/";
    const links = sidebar.querySelectorAll(".sidebar__link[href]");
    links.forEach((a) => {
      const href = a.getAttribute("href") || a.getAttribute("th:href") || "";
      if (!href) return;
      const clean = href.replace(/^https?:\/\/[^/]+/i, "").replace(/\/+$/, "") || "/";
      if (clean === path || (clean !== "/" && path.startsWith(clean))) {
        a.classList.add("is-active");
        a.setAttribute("aria-current", "page");
      } else {
        a.classList.remove("is-active");
        a.removeAttribute("aria-current");
      }
    });
  } catch {}
}

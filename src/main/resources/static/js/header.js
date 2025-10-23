// src/main/resources/static/js/header.js
document.addEventListener("DOMContentLoaded", () => {
  // Seletores tolerantes (suporta #primary-nav, #primary-navigation e .primary-nav)
  const toggleBtn = document.querySelector(".nav-toggle, .menu-button[data-role='nav-toggle']");
  const nav =
    document.getElementById("primary-nav") ||
    document.getElementById("primary-navigation") ||
    document.querySelector(".primary-nav");

  if (!toggleBtn || !nav) return;

  // ARIA e roles básicos
  const navId = nav.id || "primary-nav";
  if (!nav.id) nav.id = navId;
  toggleBtn.setAttribute("aria-controls", navId);
  toggleBtn.setAttribute("aria-expanded", "false");
  nav.setAttribute("role", "navigation");
  if (!nav.getAttribute("aria-label")) nav.setAttribute("aria-label", "Menu principal");

  // Injeta backdrop se não existir
  let backdrop = document.querySelector(".nav-backdrop");
  if (!backdrop) {
    backdrop = document.createElement("div");
    backdrop.className = "nav-backdrop";
    backdrop.setAttribute("aria-hidden", "true");
    document.body.appendChild(backdrop);
  }

  const root = document.documentElement;
  const body = document.body;
  let lastFocused = null;

  const isOpen = () => root.classList.contains("nav-open") || nav.classList.contains("open");
  const setExpanded = (v) => toggleBtn.setAttribute("aria-expanded", String(v));
  const focusables = () =>
    nav.querySelectorAll(
      'a[href],button:not([disabled]),input,select,textarea,[tabindex]:not([tabindex="-1"])'
    );

  function openNav() {
    lastFocused = document.activeElement;
    root.classList.add("nav-open");
    nav.classList.add("open");
    body.classList.add("no-scroll");
    setExpanded(true);
    const f = focusables();
    if (f.length) f[0].focus();
  }

  function closeNav() {
    root.classList.remove("nav-open");
    nav.classList.remove("open");
    body.classList.remove("no-scroll");
    setExpanded(false);
    if (lastFocused) lastFocused.focus();
  }

  function toggleNav() {
    isOpen() ? closeNav() : openNav();
    toggleBtn.classList.toggle("is-open", isOpen());
  }

  // Toggle pelo botão e backdrop
  toggleBtn.addEventListener("click", toggleNav);
  backdrop.addEventListener("click", closeNav);

  // Fecha ao clicar em um link (UX mobile)
  nav.querySelectorAll("a[href]").forEach((a) =>
    a.addEventListener("click", () => {
      closeNav();
      toggleBtn.classList.remove("is-open");
    })
  );

  // Teclado: ESC fecha, TAB faz trap do foco
  document.addEventListener("keydown", (e) => {
    if (!isOpen()) return;

    if (e.key === "Escape") {
      e.preventDefault();
      closeNav();
      return;
    }

    if (e.key === "Tab") {
      const f = Array.from(focusables());
      if (!f.length) return;
      const first = f[0];
      const last = f[f.length - 1];

      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    }
  });

  // Segurança em resize: volta ao desktop => fecha
  window.addEventListener("resize", () => {
    if (window.innerWidth >= 1024 && isOpen()) closeNav();
  });

  // Sombra ao rolar no header
  const header = document.querySelector("header.site-header, .navbar, header");
  if (header) {
    const onScroll = () => header.classList.toggle("scrolled", window.scrollY > 20);
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
  }

  // Destaque do link ativo (normaliza / e sufixos)
  const path = (window.location.pathname || "/").replace(/\/+$/, "") || "/";
  document.querySelectorAll(".primary-nav a[href]").forEach((link) => {
    try {
      const href = new URL(link.getAttribute("href"), window.location.origin)
        .pathname.replace(/\/+$/, "") || "/";
      if (href && (path === href || (href !== "/" && path.startsWith(href)))) {
        link.classList.add("ativo");
      }
    } catch {}
  });
});

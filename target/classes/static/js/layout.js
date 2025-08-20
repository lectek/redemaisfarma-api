document.addEventListener("DOMContentLoaded", () => {
  initNavigationToggle();
  initSkipLinks();
  initDarkModeDetection();
  initScrollShadow();
  initNewsletterValidation();
  initModalEvents();
  initPainelNavigation();
});

// 📱 Menu responsivo
function initNavigationToggle() {
  const toggleButton = document.querySelector(".nav-toggle");
  const navMenu = document.querySelector("#primary-nav");

  if (toggleButton && navMenu) {
    toggleButton.addEventListener("click", () => {
      const expanded = toggleButton.getAttribute("aria-expanded") === "true";
      toggleButton.setAttribute("aria-expanded", !expanded);
      navMenu.classList.toggle("open");
    });
  }
}

// ⌨️ Skip links
function initSkipLinks() {
  const skipLinks = document.querySelectorAll(".skip-link, .skip-footer");
  skipLinks.forEach(link => {
    link.addEventListener("click", (e) => {
      const targetId = link.getAttribute("href").replace("#", "");
      const target = document.getElementById(targetId);
      if (target) target.focus();
    });
  });
}

// 🌙 Dark Mode automático
function initDarkModeDetection() {
  if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
    document.documentElement.setAttribute("data-theme", "dark");
  }
}

// 🧭 Header com sombra ao rolar
function initScrollShadow() {
  const header = document.querySelector(".site-header, header");
  if (!header) return;

  window.addEventListener("scroll", () => {
    if (window.scrollY > 8) {
      header.classList.add("scrolled");
    } else {
      header.classList.remove("scrolled");
    }
  });
}

// 📧 Newsletter (rodapé)
function initNewsletterValidation() {
  const form = document.getElementById("newsletter-form");
  const emailInput = document.getElementById("newsletter-email");
  const errorMsg = document.querySelector(".error-message");
  const status = document.getElementById("newsletter-status");

  if (!form || !emailInput) return;

  form.addEventListener("submit", (e) => {
    if (!emailInput.value || !emailInput.value.includes("@")) {
      e.preventDefault();
      errorMsg.textContent = "Digite um e-mail válido.";
    } else {
      errorMsg.textContent = "";
      status.textContent = "Inscrição enviada!";
    }
  });
}

// 🧩 Modal genérico
function abrirModal(id = "modal-padrao") {
  const modal = document.getElementById(id);
  if (modal) {
    modal.style.display = "flex";
    modal.setAttribute("aria-hidden", "false");
  }
}

function fecharModal(id = "modal-padrao") {
  const modal = document.getElementById(id);
  if (modal) {
    modal.style.display = "none";
    modal.setAttribute("aria-hidden", "true");
  }
}

function confirmarAcao(callback) {
  fecharModal();
  if (typeof callback === "function") callback();
}

function initModalEvents() {
  const fecharBtns = document.querySelectorAll(".modal-close");
  fecharBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      const modal = btn.closest(".modal");
      if (modal) fecharModal(modal.id);
    });
  });
}

// 🛠️ Painel administrativo: destaque do botão ativo
function initPainelNavigation() {
  const botoes = document.querySelectorAll(".menu-button");
  const urlAtual = window.location.pathname;

  botoes.forEach(btn => {
    const texto = btn.textContent?.trim().toLowerCase();

    if (
      (texto === "painel" && urlAtual.includes("/admin")) ||
      (texto === "produtos" && urlAtual.includes("/produtos")) ||
      (texto === "pedidos" && urlAtual.includes("/pedidos")) ||
      (texto === "clientes" && urlAtual.includes("/clientes"))
    ) {
      btn.classList.add("ativo");
    }

    // Navegação manual (se quiser tornar os botões clicáveis)
    if (!btn.getAttribute("href")) {
      btn.addEventListener("click", () => {
        switch (texto) {
          case "painel":
            window.location.href = "/admin/painel";
            break;
          case "produtos":
            window.location.href = "/admin/produtos";
            break;
          case "pedidos":
            window.location.href = "/admin/pedidos";
            break;
          case "clientes":
            window.location.href = "/admin/clientes";
            break;
        }
      });
    }
  });
}

// header.js
document.addEventListener("DOMContentLoaded", () => {
  // Alterna visibilidade do menu mobile
  const toggleBtn = document.querySelector(".nav-toggle");
  const nav = document.querySelector(".nav-menu");

  if (toggleBtn && nav) {
    toggleBtn.addEventListener("click", () => {
      nav.classList.toggle("open");
      toggleBtn.setAttribute("aria-expanded", nav.classList.contains("open"));
    });
  }

  // Sombra ao rolar
  const header = document.querySelector("header");
  window.addEventListener("scroll", () => {
    if (window.scrollY > 20) {
      header.classList.add("scrolled");
    } else {
      header.classList.remove("scrolled");
    }
  });

  // Destaque no botão de navegação ativo
  const path = window.location.pathname;
  const links = document.querySelectorAll(".barra-inferior a");

  links.forEach(link => {
    if (path.includes(link.getAttribute("href"))) {
      link.classList.add("ativo");
    }
  });
});

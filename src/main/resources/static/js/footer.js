
// footer.js
document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("footer form");
  const emailInput = form?.querySelector('input[type="email"]');
  const statusDiv = document.getElementById("newsletter-status");

  if (form && emailInput && statusDiv) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();

      const email = emailInput.value.trim();

      if (!validateEmail(email)) {
        showMessage("Digite um e-mail válido.", "error");
        return;
      }

      // Simulação de envio (poderia ser um POST real futuramente)
      setTimeout(() => {
        showMessage("Inscrição realizada com sucesso! 🎉", "success");
        form.reset();
      }, 800);
    });
  }

  function validateEmail(email) {
    // Regex simples e eficaz para validar e-mails
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  function showMessage(message, type) {
    statusDiv.textContent = message;
    statusDiv.setAttribute("role", "alert");
    statusDiv.style.color = type === "error" ? "#ef4444" : "#14b8a6"; // vermelho ou verde
    statusDiv.style.fontWeight = "600";
    statusDiv.style.marginTop = "0.5rem";
    statusDiv.style.fontSize = "0.95rem";
    statusDiv.style.transition = "opacity 0.3s ease";
  }
});

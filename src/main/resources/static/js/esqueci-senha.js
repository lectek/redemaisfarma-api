// /static/js/esqueci-senha.js
const form = document.querySelector('form');
const emailInput = document.getElementById('emailOuCpf') || document.getElementById('email');
const alertBox = document.getElementById('alert');
const enviarBtn = document.getElementById('enviarBtn') || form?.querySelector('button[type="submit"]');

// OTP modal (reutilizado do cadastro)
const otpDialog = document.getElementById('otpDialog');
const otpInputs = () => Array.from(otpDialog?.querySelectorAll('.otp') || []);
const otpConfirmBtn = document.getElementById('otpConfirmBtn');
const otpResendBtn  = document.getElementById('otpResendBtn');
const otpDestinoEl  = document.getElementById('otpDestino');
const otpError      = document.getElementById('otpError');
const otpTimer      = document.getElementById('otpTimer');

let currentDeliveryId = null;
let verifiedToken = null;
let userExists = null;
let resendCooldown = 60;
let cooldownTimer = null;

const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(v).trim());
const pick = (obj, ...keys) => {
  if (!obj || typeof obj !== 'object') return undefined;
  const found = keys.find((k) => Object.prototype.hasOwnProperty.call(obj, k));
  return found ? obj[found] : undefined;
};

function setAlert(msg, type='error') {
  if (!alertBox) return;
  alertBox.textContent = msg || '';
  alertBox.className = msg ? `alert ${type}` : 'alert';
}
function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  return { header, token };
}
function startCooldown() {
  clearInterval(cooldownTimer);
  let t = resendCooldown;
  otpResendBtn.disabled = true;
  otpTimer.textContent = t>0 ? `Reenviar em ${t}s` : '';
  cooldownTimer = setInterval(() => {
    t -= 1;
    if (t <= 0) {
      clearInterval(cooldownTimer);
      otpResendBtn.disabled = false;
      otpTimer.textContent = '';
    } else {
      otpTimer.textContent = `Reenviar em ${t}s`;
    }
  }, 1000);
}

// 1) Start
form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const email = (emailInput?.value || '').trim();
  if (!isEmail(email)) {
    setAlert('Informe um e-mail vÃ¡lido.');
    emailInput?.focus();
    return;
  }
  setAlert('');
  try {
    enviarBtn.disabled = true;
    enviarBtn.textContent = 'Enviando cÃ³digoâ€¦';
    const { header, token } = getCsrf();

    const resp = await fetch('/api/auth/email-claim/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [header]: token || '' },
      body: JSON.stringify({ email })
    });
    const data = await resp.json().catch(()=> ({}));
    if (!resp.ok) {
      setAlert(data?.message || 'NÃ£o foi possÃ­vel enviar o cÃ³digo.', 'error');
      return;
    }
    currentDeliveryId = pick(data, 'deliveryId', 'delivery_id') || null;
    userExists = !!pick(data, 'userExists', 'user_exists');
    resendCooldown = Number(pick(data, 'cooldownSec', 'cooldown_sec')) || 60;

    if (!currentDeliveryId) {
      setAlert('NÃ£o foi possÃ­vel iniciar a validaÃ§Ã£o. Reenvie o cÃ³digo.', 'error');
      return;
    }

    otpInputs().forEach(i => i.value = '');
    otpError.textContent = '';
    otpDestinoEl.textContent = pick(data, 'maskedDestino', 'masked_destino') || email;
    otpDialog?.showModal();
    otpInputs()[0]?.focus();
    startCooldown();
    setAlert('CÃ³digo enviado. Verifique seu e-mail.', 'info');
  } catch {
    setAlert('Falha de rede. Tente novamente.', 'error');
  } finally {
    enviarBtn.disabled = false;
    enviarBtn.textContent = 'Enviar link';
  }
});

// UX inputs OTP
otpDialog?.addEventListener('input', (e) => {
  const el = e.target;
  if (!(el instanceof HTMLInputElement)) return;
  if (el.classList.contains('otp')) {
    el.value = el.value.replace(/\D/g,'').slice(0,1);
    if (el.value && el.nextElementSibling) el.nextElementSibling.focus();
  }
});
document.addEventListener('keydown', (e) => {
  if (!otpDialog?.open) return;
  const target = e.target;
  if (!(target instanceof Element) || !otpDialog.contains(target)) return;
  if (e.key === 'Backspace' &&
      target instanceof HTMLInputElement &&
      target.classList.contains('otp') &&
      !target.value &&
      target.previousElementSibling) {
    target.previousElementSibling.focus();
  }
});

// 2) Verify
otpConfirmBtn?.addEventListener('click', async (e) => {
  e.preventDefault();
  const code = otpInputs().map(i => i.value).join('');
  const email = (emailInput?.value || '').trim();
  if (!/^\d{6}$/.test(code)) { otpError.textContent = 'Digite os 6 dÃ­gitos.'; return; }
  if (!currentDeliveryId) { otpError.textContent = 'SessÃ£o OTP expirada. Reenvie o cÃ³digo.'; return; }
  try {
    otpConfirmBtn.disabled = true;
    otpError.textContent = '';
    const { header, token } = getCsrf();

    const resp = await fetch('/api/auth/email-claim/verify', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [header]: token || '' },
      body: JSON.stringify({ delivery_id: currentDeliveryId, code, email })
    });
    const data = await resp.json().catch(()=> ({}));
    if (!resp.ok) {
      const r = data?.reason;
      if (r === 'expired') {
        otpError.textContent = 'CÃ³digo expirado.';
      } else if (r === 'invalid' || r === 'too_many_attempts') {
        otpError.textContent = 'CÃ³digo incorreto.';
      } else {
        otpError.textContent = data?.message || 'NÃ£o foi possÃ­vel validar o cÃ³digo.';
      }
      return;
    }

    verifiedToken = data.token;
    userExists = !!pick(data, 'userExists', 'user_exists');
    otpDialog.close();

    // Branch:
    if (userExists) {
      const nova = prompt('E-mail confirmado. Digite a NOVA SENHA (mÃ­n 8 chars):');
      if (!nova || nova.length < 8) { setAlert('Senha muito curta.', 'error'); return; }
      const resp2 = await fetch('/api/auth/password/reset-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [header]: token || '' },
        body: JSON.stringify({ token: verifiedToken, email, novaSenha: nova })
      });
      const d2 = await resp2.json().catch(()=> ({}));
      if (!resp2.ok) { setAlert(d2?.message || 'Falha ao redefinir senha.', 'error'); return; }
      setAlert('Senha redefinida! VocÃª jÃ¡ pode entrar.', 'success');
      setTimeout(()=> window.location.assign('/login'), 800);
    } else {
      const nome = prompt('E-mail confirmado. Informe seu NOME:') || '';
      const senha = prompt('Crie uma SENHA (mÃ­n 8 chars):') || '';
      if (nome.trim().length < 2 || senha.length < 8) { setAlert('Dados insuficientes.', 'error'); return; }
      const { header, token } = getCsrf();
      const resp3 = await fetch('/api/auth/register/complete-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', [header]: token || '' },
        body: JSON.stringify({ token: verifiedToken, email, nome, senha })
      });
      const d3 = await resp3.json().catch(()=> ({}));
      if (!resp3.ok) { setAlert(d3?.message || 'Falha ao criar conta.', 'error'); return; }
      setAlert('Conta criada com sucesso! FaÃ§a login.', 'success');
      setTimeout(()=> window.location.assign('/login?novo=1'), 800);
    }
  } catch {
    otpError.textContent = 'Falha de rede. Tente novamente.';
  } finally {
    otpConfirmBtn.disabled = false;
  }
});

// 3) Reenviar
otpResendBtn?.addEventListener('click', async () => {
  const email = (emailInput?.value || '').trim();
  if (!isEmail(email)) { otpError.textContent = 'E-mail invÃ¡lido.'; return; }
  try {
    otpResendBtn.disabled = true;
    const { header, token } = getCsrf();
    const resp = await fetch('/api/auth/email-claim/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [header]: token || '' },
      body: JSON.stringify({ email, previous_delivery_id: currentDeliveryId })
    });
    const data = await resp.json().catch(()=> ({}));
    if (resp.ok) {
      currentDeliveryId = pick(data, 'deliveryId', 'delivery_id') || currentDeliveryId;
      otpDestinoEl.textContent = pick(data, 'maskedDestino', 'masked_destino') || email;
      resendCooldown = Number(pick(data, 'cooldownSec', 'cooldown_sec')) || 60;
      startCooldown();
      otpError.textContent = '';
    } else {
      otpError.textContent = data?.message || 'NÃ£o foi possÃ­vel reenviar.';
    }
  } catch {
    otpError.textContent = 'Falha de rede.';
  }
});

// pages/produto-editar.js
function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  return { token, header };
}

function withCsrf(init = {}) {
  const { token, header } = getCsrf();
  init.headers = Object.assign({}, init.headers || {}, token ? { [header]: token } : {});
  return init;
}

function toast(msg, type = 'ok') {
  let box = document.getElementById('toast');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast';
    document.body.appendChild(box);
  }
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  box.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function normalizeErrorMessage(raw, fallback) {
  const text = String(raw || '')
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
  return text || fallback;
}

function parseJsonSafely(raw) {
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function resolveProdutoId(raw) {
  const id = String(raw ?? '').trim();
  return /^\d+$/.test(id) ? id : null;
}

const $ = (s) => document.querySelector(s);

const $id = $('#id');
const $nome = $('#nome');
const $descricao = $('#descricao');
const $preco = $('#preco');
const $imagem = $('#imagem');
const $imagemPreview = $('#imagem-preview');
const $imagemArquivo = $('#imagemArquivo');
const $categoria = $('#categoria');
const $tarjaMedicacao = $('#tarjaMedicacao');
const $exigeReceita = $('#exigeReceita');
const $estoque = $('#estoque');
const $alertaEstoqueLimite = $('#alertaEstoqueLimite');
const $perfilVenda = $('#perfilVenda');
const $perfilVendaHint = $('#perfilVendaHint');
const $codigoBarras = $('#codigoBarras');
const $disponivel = $('#disponivel');
const $validador = $('#validador');

const $status = $('#status');
const $btnSalvar = $('#btn-salvar');
const $btnGerarIA = $('#btn-gerar-ia');
const $btnUploadImagem = $('#btn-upload-imagem');
const $btnValidar = $('#btn-validar');
const $btnPublicar = $('#btn-publicar');
let imageUploadInProgress = false;

const ALERTA_ESTOQUE_MIN = 1;
const ALERTA_ESTOQUE_MAX = 100000;
const ALERTA_ESTOQUE_PADRAO = 10;
const PERFIL_VENDA_MULTIPLIERS = Object.freeze({
  LENTA: 0.6,
  NORMAL: 1,
  RAPIDA: 1.8
});

async function carregarProdutoSeNecessario() {
  // Se já veio do SSR, não precisa. Mantemos apenas como fallback
  const id = $id?.value;
  if (!id) return;
  if ($nome?.value) return; // já tem dados do SSR

  try {
    const r = await fetch(`/api/admin/produtos/${id}`);
    if (!r.ok) return;
    const p = await r.json();
    fill(p);
  } catch (e) {
    console.warn('Falha ao carregar produto (fallback):', e);
  }
}

function fill(p) {
  if (!$nome.value) $nome.value = p.nome ?? '';
  if (!$descricao.value) $descricao.value = p.descricao ?? '';
  if (!$preco.value) $preco.value = p.preco ?? p.precoVenda ?? '';
  if (!$imagem.value) $imagem.value = p.imagem ?? p.imagemUrl ?? '';
  if ($alertaEstoqueLimite && !$alertaEstoqueLimite.value) {
    $alertaEstoqueLimite.value = p.alertaEstoqueLimite ?? '';
  }
  if (!$categoria.value) $categoria.value = p.categoria ?? '';
  if ($tarjaMedicacao && !$tarjaMedicacao.value) {
    $tarjaMedicacao.value = p.tarjaMedicacao ?? '';
  }
  if ($exigeReceita && !$exigeReceita.checked) {
    $exigeReceita.checked = !!p.exigeReceita;
  }
  if (!$estoque.value) $estoque.value = p.estoque ?? p.estoqueAtual ?? 0;
  if (!$codigoBarras.value) $codigoBarras.value = p.codigoBarras ?? '';
  if ($disponivel) $disponivel.checked = (p.situacao ? p.situacao === 'ATIVO' : !!p.disponivel);
  updateImagePreview($imagem.value);
  syncTarjaReceitaRule();
  syncPerfilVendaFromLimite();
}

function imagePlaceholderUrl() {
  return '/img/produtos/placeholder-generico.png';
}

function updateImagePreview(url) {
  if (!$imagemPreview) return;
  const value = String(url || '').trim();
  $imagemPreview.src = value || imagePlaceholderUrl();
}

function normalizeCategoria(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}

function syncTarjaReceitaRule() {
  if (!$tarjaMedicacao || !$exigeReceita) return;
  const categoria = normalizeCategoria($categoria?.value);
  const tarja = ($tarjaMedicacao.value || '').trim();
  const categoriaMedicacoes = categoria === 'medicacoes';

  if (!categoriaMedicacoes) {
    $tarjaMedicacao.value = '';
    $tarjaMedicacao.disabled = true;
    $exigeReceita.checked = false;
    $exigeReceita.disabled = true;
    return;
  }

  $tarjaMedicacao.disabled = false;
  if (!tarja) {
    $exigeReceita.disabled = false;
    return;
  }

  if (tarja === 'TARJA_VERMELHA' || tarja === 'TARJA_PRETA') {
    $exigeReceita.checked = true;
    $exigeReceita.disabled = true;
    return;
  }

  if (tarja === 'SEM_TARJA') {
    $exigeReceita.checked = false;
  }
  $exigeReceita.disabled = false;
}

function parsePositiveInt(raw) {
  const parsed = parseInt(String(raw ?? '').trim(), 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function clampAlertaLimite(value) {
  if (!Number.isFinite(value)) return ALERTA_ESTOQUE_PADRAO;
  return Math.min(ALERTA_ESTOQUE_MAX, Math.max(ALERTA_ESTOQUE_MIN, value));
}

function resolveLimiteGlobal() {
  const fromDataAttr = parsePositiveInt($alertaEstoqueLimite?.dataset?.globalLimite);
  return clampAlertaLimite(fromDataAttr ?? ALERTA_ESTOQUE_PADRAO);
}

function resolveLimitePorPerfil(perfil) {
  const multiplier = PERFIL_VENDA_MULTIPLIERS[perfil];
  if (!multiplier) return null;
  return clampAlertaLimite(Math.round(resolveLimiteGlobal() * multiplier));
}

function resolvePerfilPeloLimite(limiteAtual) {
  if (limiteAtual == null) return 'NORMAL';
  for (const perfil of Object.keys(PERFIL_VENDA_MULTIPLIERS)) {
    if (limiteAtual === resolveLimitePorPerfil(perfil)) {
      return perfil;
    }
  }
  return 'PERSONALIZADO';
}

function resolvePerfilHint(perfil, limiteAtual) {
  const limiteGlobal = resolveLimiteGlobal();
  switch (perfil) {
    case 'LENTA':
      return `Venda lenta: limite sugerido ${resolveLimitePorPerfil('LENTA')} (global ${limiteGlobal}).`;
    case 'NORMAL':
      return `Venda normal: limite sugerido ${resolveLimitePorPerfil('NORMAL')} (global ${limiteGlobal}).`;
    case 'RAPIDA':
      return `Venda rapida: limite sugerido ${resolveLimitePorPerfil('RAPIDA')} (global ${limiteGlobal}).`;
    default:
      if (limiteAtual == null) {
        return `Personalizado: em branco usa o limite global (${limiteGlobal}).`;
      }
      return `Personalizado: limite manual atual ${limiteAtual}.`;
  }
}

function applyPerfilHint(perfil, limiteAtual) {
  if (!$perfilVendaHint) return;
  $perfilVendaHint.textContent = resolvePerfilHint(perfil, limiteAtual);
}

function syncPerfilVendaFromLimite() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const limiteAtual = parsePositiveInt($alertaEstoqueLimite.value);
  const perfil = resolvePerfilPeloLimite(limiteAtual);
  $perfilVenda.value = perfil;
  applyPerfilHint(perfil, limiteAtual);
}

function onPerfilVendaChange() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const perfil = String($perfilVenda.value || 'PERSONALIZADO').toUpperCase();
  if (perfil === 'PERSONALIZADO') {
    applyPerfilHint(perfil, parsePositiveInt($alertaEstoqueLimite.value));
    return;
  }

  const limite = resolveLimitePorPerfil(perfil);
  if (limite != null) {
    $alertaEstoqueLimite.value = String(limite);
  }
  applyPerfilHint(perfil, limite);
}

function onAlertaLimiteInput() {
  if (!$perfilVenda || !$alertaEstoqueLimite) return;
  const limiteAtual = parsePositiveInt($alertaEstoqueLimite.value);
  const perfil = resolvePerfilPeloLimite(limiteAtual);
  $perfilVenda.value = perfil;
  applyPerfilHint(perfil, limiteAtual);
}

function normalizeAlertaLimite() {
  if (!$alertaEstoqueLimite) return;
  const parsed = parsePositiveInt($alertaEstoqueLimite.value);
  if (parsed == null) {
    $alertaEstoqueLimite.value = '';
  } else {
    $alertaEstoqueLimite.value = String(clampAlertaLimite(parsed));
  }
  onAlertaLimiteInput();
}

async function salvar() {
  const id = $id?.value;
  if (!id) return toast('ID do produto ausente.', 'err');

  const dto = {
    nome: $nome.value || null,
    descricao: $descricao.value || null,
    preco: parseFloat($preco.value || '0'),
    imagem: $imagem.value || null,
    categoria: $categoria.value || null,
    tarjaMedicacao: ($tarjaMedicacao?.value || '').trim() || null,
    exigeReceita: !!$exigeReceita?.checked,
    estoque: parseInt($estoque.value || '0', 10),
    alertaEstoqueLimite: (() => {
      if (!$alertaEstoqueLimite) return null;
      const parsed = parsePositiveInt($alertaEstoqueLimite.value);
      return parsed == null ? null : clampAlertaLimite(parsed);
    })(),
    codigoBarras: $codigoBarras.value || null,
    ativo: !!$disponivel.checked
  };

  $status.textContent = 'Salvando…';
  $btnSalvar.disabled = true;

  try {
    const r = await fetch(`/api/admin/produtos/${id}`, withCsrf({
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto),
      credentials: 'same-origin'
    }));

    if (!r.ok) {
      const raw = await r.text();
      throw new Error(normalizeErrorMessage(raw, `Falha ao salvar (HTTP ${r.status})`));
    }
    $status.textContent = 'Salvo com sucesso.';
    toast('Produto salvo!', 'ok');
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao salvar.';
    toast(e?.message || 'Erro ao salvar produto', 'err');
  } finally {
    $btnSalvar.disabled = false;
  }
}

async function uploadImagem() {
  if (imageUploadInProgress) return;
  const id = $id?.value;
  if (!id) return toast('Produto ainda nao salvo.', 'err');

  const file = $imagemArquivo?.files?.[0];
  if (!file) {
    $imagemArquivo?.click();
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  $status.textContent = 'Enviando imagem...';
  $btnUploadImagem.disabled = true;
  imageUploadInProgress = true;

  try {
    const r = await fetch(`/api/admin/produtos/${id}/imagem`, withCsrf({
      method: 'POST',
      body: formData,
      credentials: 'same-origin'
    }));

    if (!r.ok) {
      const raw = await r.text();
      throw new Error(normalizeErrorMessage(raw, `Falha no upload (HTTP ${r.status})`));
    }
    const url = (await r.text())?.trim();
    if (url) {
      $imagem.value = url;
      updateImagePreview(url);
    }
    $status.textContent = 'Imagem enviada.';
    toast('Imagem atualizada!', 'ok');
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao enviar imagem.';
    toast(e?.message || 'Falha ao enviar imagem', 'err');
  } finally {
    $btnUploadImagem.disabled = false;
    imageUploadInProgress = false;
  }
}

async function gerarIA() {
  const id = resolveProdutoId($id?.value);
  if (!id) return toast('ID do produto invalido.', 'err');

  const hasCurrentImage = String($imagem?.value || '').trim().length > 0;
  const endpoint = hasCurrentImage
    ? `/admin/imagens/${id}/regenerate`
    : `/admin/imagens/${id}/queue`;
  $btnGerarIA.disabled = true;
  $status.textContent = hasCurrentImage
    ? 'Solicitando regeneracao de imagem...'
    : 'Solicitando geracao de imagem...';

  try {
    const r = await fetch(endpoint, withCsrf({ method: 'POST', credentials: 'same-origin' }));
    const raw = await r.text();
    const payload = parseJsonSafely(raw);

    if (!r.ok) {
      throw new Error(
        normalizeErrorMessage(
          payload?.lastJob?.errorMsg || payload?.message || raw,
          `Falha na solicitacao de imagem (HTTP ${r.status})`
        )
      );
    }

    const lastJobStatus = String(payload?.lastJob?.status || '');
    const lastJobError = payload?.lastJob?.errorMsg || '';
    if (lastJobStatus === 'ERROR') {
      throw new Error(
        normalizeErrorMessage(lastJobError, 'A IA nao conseguiu gerar a imagem para esse produto.')
      );
    }

    const resultUrl = String(payload?.lastJob?.resultUrl || '').trim();
    if (resultUrl) {
      $imagem.value = resultUrl;
      updateImagePreview(resultUrl);
    }

    const result = String(payload?.result || '');
    if (
      lastJobStatus === 'DONE'
      || result.startsWith('PROCESSADO_SYNC')
      || result === 'REGERADO'
      || result === 'PROCESSADO_SYNC_IMAGEM_EXISTENTE'
    ) {
      toast('Imagem gerada e salva!', 'ok');
      $status.textContent = 'Imagem gerada e salva.';
    } else {
      toast(hasCurrentImage ? 'Regeneracao solicitada!' : 'Enfileirado com sucesso!', 'ok');
      $status.textContent = hasCurrentImage
        ? 'Regeneracao enfileirada...'
        : 'Geracao enfileirada...';
    }
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao solicitar imagem.';
    toast(e?.message || 'Falha na solicitacao de imagem', 'err');
  } finally {
    $btnGerarIA.disabled = false;
  }
}

function validadorAtual() {
  const nome = ($validador?.value || '').trim();
  return nome || 'admin-web';
}

function setFluxoButtonsDisabled(disabled) {
  if ($btnValidar) $btnValidar.disabled = disabled;
  if ($btnPublicar) $btnPublicar.disabled = disabled;
}

async function moverFluxo(acao) {
  const id = $id?.value;
  if (!id) return toast('Produto ainda nao salvo.', 'err');

  const validador = encodeURIComponent(validadorAtual());
  const endpoint = `/admin/produtos/${id}/${acao}?validador=${validador}`;
  const acaoLabel = acao === 'validar' ? 'validando' : 'publicando';

  setFluxoButtonsDisabled(true);
  $status.textContent = `Fluxo: ${acaoLabel}...`;

  try {
    const r = await fetch(endpoint, withCsrf({ method: 'POST', credentials: 'same-origin' }));
    if (!r.ok) {
      const raw = await r.text();
      throw new Error(normalizeErrorMessage(raw, `Falha no fluxo (HTTP ${r.status})`));
    }

    if (acao === 'validar') {
      $status.textContent = 'Fluxo: produto VALIDADO.';
      toast('Produto validado!', 'ok');
    } else {
      $status.textContent = 'Fluxo: produto PUBLICADO.';
      toast('Produto publicado!', 'ok');
    }
  } catch (e) {
    console.error(e);
    $status.textContent = `Erro ao ${acao}.`;
    toast(e?.message || `Falha ao ${acao} produto`, 'err');
  } finally {
    setFluxoButtonsDisabled(false);
  }
}

async function validar() {
  await moverFluxo('validar');
}

async function publicar() {
  await moverFluxo('publicar');
}

// Listeners
$btnSalvar?.addEventListener('click', salvar);
$btnUploadImagem?.addEventListener('click', uploadImagem);
$btnGerarIA?.addEventListener('click', gerarIA);
$btnValidar?.addEventListener('click', validar);
$btnPublicar?.addEventListener('click', publicar);
$categoria?.addEventListener('change', syncTarjaReceitaRule);
$tarjaMedicacao?.addEventListener('change', syncTarjaReceitaRule);
$perfilVenda?.addEventListener('change', onPerfilVendaChange);
$alertaEstoqueLimite?.addEventListener('input', onAlertaLimiteInput);
$alertaEstoqueLimite?.addEventListener('blur', normalizeAlertaLimite);
$imagem?.addEventListener('input', () => updateImagePreview($imagem.value));
$imagemArquivo?.addEventListener('change', () => {
  if ($imagemArquivo?.files?.length) {
    uploadImagem();
  }
});

// bootstrap
syncTarjaReceitaRule();
syncPerfilVendaFromLimite();
carregarProdutoSeNecessario();
updateImagePreview($imagem?.value);


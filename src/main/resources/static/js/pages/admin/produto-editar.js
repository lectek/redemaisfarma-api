// pages/produto-editar.js
import { withCsrf, toast } from '/js/lib/ui.js';

const $ = (s) => document.querySelector(s);

const $id = $('#id');
const $nome = $('#nome');
const $descricao = $('#descricao');
const $preco = $('#preco');
const $imagem = $('#imagem');
const $categoria = $('#categoria');
const $estoque = $('#estoque');
const $codigoBarras = $('#codigoBarras');
const $disponivel = $('#disponivel');

const $status = $('#status');
const $btnSalvar = $('#btn-salvar');
const $btnGerarIA = $('#btn-gerar-ia');
const $btnRegerarIA = $('#btn-regerar-ia');

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
  if (!$categoria.value) $categoria.value = p.categoria ?? '';
  if (!$estoque.value) $estoque.value = p.estoque ?? p.estoqueAtual ?? 0;
  if (!$codigoBarras.value) $codigoBarras.value = p.codigoBarras ?? '';
  if ($disponivel) $disponivel.checked = (p.situacao ? p.situacao === 'ATIVO' : !!p.disponivel);
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
    estoque: parseInt($estoque.value || '0', 10),
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

    if (!r.ok) throw new Error(await r.text());
    $status.textContent = 'Salvo com sucesso.';
    toast('Produto salvo!', 'ok');
  } catch (e) {
    console.error(e);
    $status.textContent = 'Erro ao salvar.';
    toast('Erro ao salvar produto', 'err');
  } finally {
    $btnSalvar.disabled = false;
  }
}

async function gerarIA(action /* 'queue' | 'regenerate' */) {
  const id = $id?.value;
  if (!id) return toast('Produto ainda não salvo.', 'err');

  const endpoint = action === 'regenerate'
    ? `/api/admin/imagens/${id}/regenerate`
    : `/api/admin/imagens/${id}/queue`;

  const btn = action === 'regenerate' ? $btnRegerarIA : $btnGerarIA;
  btn.disabled = true;

  try {
    const r = await fetch(endpoint, withCsrf({ method: 'POST', credentials: 'same-origin' }));
    if (!r.ok) throw new Error(`HTTP ${r.status}`);

    toast(action === 'regenerate' ? 'Regeneração solicitada!' : 'Enfileirado com sucesso!', 'ok');
    $status.textContent = action === 'regenerate' ? 'Regeneração solicitada…' : 'Geração enfileirada…';
  } catch (e) {
    console.error(e);
    toast('Falha na solicitação de imagem', 'err');
  } finally {
    btn.disabled = false;
  }
}

// Listeners
$btnSalvar?.addEventListener('click', salvar);
$btnGerarIA?.addEventListener('click', () => gerarIA('queue'));
$btnRegerarIA?.addEventListener('click', () => gerarIA('regenerate'));

// bootstrap
carregarProdutoSeNecessario();

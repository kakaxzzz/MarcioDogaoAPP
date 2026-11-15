/* App de atendimento Mdogão: telas Pedido, Mesas e Dashboard em cima da API REST (/api). */
'use strict';

// ---------------------------------------------------------------- estado
const S = {
  token: lerLocal('mdogao.token'),
  usuario: null,
  info: { demo: false, turnoAtual: 'MANHA', inicioNoite: '17:00' },
  aba: 'pedido',
  subaba: 'geral',
  modoLogin: 'entrar',
  mesas: [],
  cardapio: [],
  mesaSel: lerLocal('mdogao.mesa'),   // número da mesa em que o pedido está sendo lançado
  carrinho: new Map(),                // codCar -> { qtd, variacao }
  observacao: '',
  turnoVisto: null,                   // cardápio do dia ou da noite na tela
  busca: '',
  dash: null,
  caixa: null,
  caixas: [],
  historico: [],
  editando: null,                     // produto em edição no cadastro do cardápio
  folha: null,                        // janela aberta por cima da tela
  bancoFora: false,
  carregando: true,
};

const FORMAS = {
  DINHEIRO: ['Dinheiro', 'nota'],
  PIX: ['Pix', 'pix'],
  CARTAO_DEBITO: ['Cartão de débito', 'cartao'],
  CARTAO_CREDITO: ['Cartão de crédito', 'cartao'],
};

// ---------------------------------------------------------------- utilidades
function lerLocal(chave) { try { return localStorage.getItem(chave); } catch (e) { return null; } }
function gravarLocal(chave, valor) {
  try { valor == null ? localStorage.removeItem(chave) : localStorage.setItem(chave, valor); } catch (e) { /* sem armazenamento */ }
}
function esc(v) {
  return String(v == null ? '' : v).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
const fmt = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
function rs(v) { return fmt.format(Number(v || 0)); }
function numero(texto) {
  const t = String(texto == null ? '' : texto).trim();
  if (!t) return null;
  const n = Number(t.includes(',') ? t.replace(/\./g, '').replace(',', '.') : t);
  return Number.isFinite(n) ? n : NaN;
}
function hora(h) { return h ? String(h).slice(0, 5) : ''; }
function nomeTurno(t) { return t === 'NOITE' ? 'noite' : 'dia'; }

const ICONES = {
  pedido: '<rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4h6v3H9zM9 12h6M9 16h4"/>',
  mesas: '<path d="M4 11V7a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v4"/><path d="M2 13a2 2 0 0 1 4 0v3h12v-3a2 2 0 0 1 4 0v6H2zM6 19v2M18 19v2"/>',
  painel: '<path d="M4 20V10M10 20V4M16 20v-8M21 20H3"/>',
  usuario: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  sol: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
  lua: '<path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z"/>',
  busca: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
  mais: '<path d="M12 5v14M5 12h14"/>',
  seta: '<path d="M5 12h14M13 6l6 6-6 6"/>',
  sacola: '<path d="M6 8h12l1 12H5zM9 8V6a3 3 0 0 1 6 0v2"/>',
  x: '<path d="M6 6l12 12M18 6 6 18"/>',
  lixo: '<path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/>',
  relogio: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  cifrao: '<path d="M12 3v18M16.5 7.5c0-1.7-2-3-4.5-3s-4.5 1.3-4.5 3 1.5 2.6 4.5 3.2 4.5 1.5 4.5 3.3-2 3-4.5 3-4.5-1.3-4.5-3"/>',
  recibo: '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2zM9 8h6M9 12h6"/>',
  trofeu: '<path d="M8 4h8v5a4 4 0 0 1-8 0zM8 6H4a4 4 0 0 0 4 4M16 6h4a4 4 0 0 1-4 4M12 13v4M8 21h8M10 17h4"/>',
  cartao: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18M7 15h3"/>',
  nota: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2.5"/><path d="M6 10v4M18 10v4"/>',
  pix: '<path d="M12 3 21 12l-9 9-9-9zM8 8l4 4 4-4M8 16l4-4 4 4"/>',
  escudo: '<path d="M12 3 4 6v6c0 4.5 3.2 7.8 8 9 4.8-1.2 8-4.5 8-9V6z"/><path d="m9 12 2 2 4-4"/>',
  lapis: '<path d="M4 20h4L19 9l-4-4L4 16zM13 7l4 4"/>',
  sair: '<path d="M9 4H5v16h4M16 8l4 4-4 4M20 12H9"/>',
  caixa: '<rect x="3" y="10" width="18" height="10" rx="2"/><path d="M6 10V5h12v5M10 15h4"/>',
  livro: '<path d="M5 4h11a3 3 0 0 1 3 3v13H8a3 3 0 0 1-3-3zM5 17a3 3 0 0 1 3-3h11"/>',
  pessoas: '<circle cx="9" cy="8" r="3.5"/><path d="M2 20a7 7 0 0 1 14 0M16 5a3.5 3.5 0 0 1 0 6.5M18 14a7 7 0 0 1 4 6"/>',
};
function ic(nome) { return `<svg viewBox="0 0 24 24" aria-hidden="true">${ICONES[nome] || ''}</svg>`; }

function avisar(texto, tipo) {
  const el = document.createElement('div');
  el.className = 'aviso ' + (tipo || '');
  el.textContent = texto;
  document.getElementById('avisos').appendChild(el);
  setTimeout(() => el.remove(), tipo === 'erro' ? 6000 : 3000);
}

// ---------------------------------------------------------------- API
async function api(metodo, caminho, corpo) {
  const cab = { 'Content-Type': 'application/json' };
  if (S.token) cab.Authorization = 'Bearer ' + S.token;
  let resp;
  try {
    resp = await fetch('/api' + caminho, { method: metodo, headers: cab, body: corpo === undefined ? undefined : JSON.stringify(corpo) });
  } catch (e) {
    throw new Error('Não consegui falar com a API. Ela está rodando?');
  }
  if (resp.status === 401 && S.token && !caminho.startsWith('/auth/login')) {
    sair();
    throw new Error('Sua sessão terminou. Entre de novo.');
  }
  if (resp.status === 204) return null;
  const texto = await resp.text();
  let dados = null;
  try { dados = texto ? JSON.parse(texto) : null; } catch (e) { /* resposta sem JSON */ }
  if (!resp.ok) {
    S.bancoFora = resp.status === 503;
    let msg = (dados && dados.detail) || 'Erro ' + resp.status;
    if (dados && dados.campos) msg = Object.values(dados.campos).join(' ');
    const erro = new Error(msg);
    erro.status = resp.status;
    throw erro;
  }
  S.bancoFora = false;
  return dados;
}

/** Executa uma ação da tela mostrando o erro da API (regras do restaurante) como aviso. */
async function tentar(fn) {
  try { return await fn(); } catch (e) { avisar(e.message, 'erro'); desenhar(); return undefined; }
}

// ---------------------------------------------------------------- carga de dados
async function carregar() {
  try {
    const [mesas, cardapio] = await Promise.all([api('GET', '/mesas'), api('GET', '/cardapio')]);
    S.mesas = mesas;
    S.cardapio = cardapio;
    if (S.mesaSel && !S.mesas.some(m => m.numero === S.mesaSel)) S.mesaSel = null;
    if (S.aba === 'dashboard') await carregarPainel();
  } catch (e) {
    if (S.usuario) avisar(e.message, 'erro');
  }
  S.carregando = false;
  desenhar();
}

async function carregarPainel() {
  const [dash, caixas, historico] = await Promise.all([api('GET', '/dashboard'), api('GET', '/caixa'), api('GET', '/dashboard/historico')]);
  S.dash = dash;
  S.caixas = caixas;
  S.historico = historico;
  S.caixa = dash.caixaAberto ? await api('GET', '/caixa/atual') : null;
}

async function iniciar() {
  const guardado = S.token;
  S.token = null;   // /info é público: sem token, um login vencido não atrapalha
  try { S.info = await api('GET', '/info'); } catch (e) { /* segue com o padrão */ }
  S.token = guardado;
  if (S.token) {
    try { S.usuario = await api('GET', '/auth/me'); } catch (e) { S.token = null; gravarLocal('mdogao.token', null); }
  }
  if (S.usuario) await carregar(); else { S.carregando = false; desenhar(); }
}

function sair() {
  S.token = null; S.usuario = null; S.folha = null;
  gravarLocal('mdogao.token', null);
  desenhar();
}

// ---------------------------------------------------------------- regras de tela
function mesaAtual() { return S.mesas.find(m => m.numero === S.mesaSel) || null; }
/** Turno que vale para a cobrança: o da comanda aberta da mesa, ou o de agora se a mesa está livre. */
function turnoCobranca() {
  const m = mesaAtual();
  return m && m.comanda ? m.comanda.turno : S.info.turnoAtual;
}
function precoDe(p, turno) { return turno === 'NOITE' && p.precoNoite != null ? p.precoNoite : p.precoManha; }
function itensCarrinho() {
  const itens = [];
  S.carrinho.forEach((v, codCar) => {
    const p = S.cardapio.find(x => x.codCar === codCar);
    if (p) itens.push({ p, qtd: v.qtd, variacao: v.variacao });
  });
  return itens;
}
function totalCarrinho() {
  const turno = turnoCobranca();
  return itensCarrinho().reduce((s, i) => s + precoDe(i.p, turno) * i.qtd, 0);
}
function qtdCarrinho() { let q = 0; S.carrinho.forEach(v => q += v.qtd); return q; }

// ---------------------------------------------------------------- desenho
function desenhar() {
  const app = document.getElementById('app');
  if (S.carregando) { app.innerHTML = '<div class="vazio">Carregando…</div>'; return; }
  if (!S.usuario) { app.innerHTML = telaLogin(); desenharFolha(); return; }
  const corpo = S.aba === 'pedido' ? telaPedido() : S.aba === 'mesas' ? telaMesas() : telaDashboard();
  app.innerHTML = `
    ${S.info.demo ? '<div class="faixa-demo">Modo demonstração: dados de exemplo, que somem quando a API é desligada.</div>' : ''}
    ${S.bancoFora ? '<div class="faixa-erro">Banco de dados indisponível. Verifique se o SQL Server está rodando com TCP/IP na porta 1433.</div>' : ''}
    <header class="topo"><div class="topo-dentro">
      <nav class="abas">
        ${aba('pedido', 'Pedido', 'pedido')}${aba('mesas', 'Mesas', 'mesas')}${aba('dashboard', 'Dashboard', 'painel')}
      </nav>
      <button class="usuario" data-acao="conta" title="${esc(S.usuario.email)}" aria-label="Conta">${ic('usuario')}</button>
    </div></header>
    <main class="pagina">${corpo}</main>
    ${S.aba === 'pedido' ? barraPedido() : ''}`;
  desenharFolha();
}
function aba(id, nome, icone) {
  return `<button class="aba ${S.aba === id ? 'ativa' : ''}" data-acao="aba" data-id="${id}">${ic(icone)}${nome}</button>`;
}

// ---- login
function telaLogin() {
  const criar = S.modoLogin === 'criar';
  return `<div class="login"><div class="cartao">
    <div class="marca"><div class="logo">M</div><div><b>Mdogão</b><small>App de atendimento</small></div></div>
    ${S.bancoFora ? '<div class="faixa-erro" style="border-radius:12px;margin-bottom:14px">Banco de dados indisponível. Ligue o TCP/IP do SQL Server ou rode a API no modo demonstração.</div>' : ''}
    <form data-form="login">
      <label class="campo"><span>E-mail</span><input name="email" type="email" autocomplete="username" required></label>
      <label class="campo"><span>Senha${criar ? ' (mínimo 6 caracteres)' : ''}</span><input name="senha" type="password" autocomplete="${criar ? 'new-password' : 'current-password'}" minlength="${criar ? 6 : 1}" required></label>
      <button class="botao bloco" type="submit">${criar ? 'Criar conta e entrar' : 'Entrar'}</button>
    </form>
    <p class="troca">${criar
      ? 'Já tem conta? <button class="link" data-acao="modo-login" data-id="entrar">Entrar</button>'
      : 'Primeiro acesso? <button class="link" data-acao="modo-login" data-id="criar">Criar o primeiro funcionário</button>'}</p>
    ${S.info.demo ? `<div class="caixa-demo">Modo demonstração: já existe um garçom de exemplo cadastrado.
      <button class="botao bloco" data-acao="entrar-demo">Entrar na demonstração</button></div>` : ''}
  </div></div>`;
}

// ---- pedido
function telaPedido() {
  const m = mesaAtual();
  const cobranca = turnoCobranca();
  if (!S.turnoVisto) S.turnoVisto = cobranca;
  const hoje = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' });
  return `
    <div class="cabecalho">
      <div><small>${esc(hoje.charAt(0).toUpperCase() + hoje.slice(1))}</small><h1>Novo pedido</h1></div>
      <button class="botao claro" data-acao="trocar-mesa">${ic('mesas')} ${m ? 'Mesa <b>' + esc(m.numero) + '</b>' : 'Escolher mesa'}</button>
    </div>
    <div class="cartao mesa-atual">
      <span class="icone-mesa">${ic('mesas')}</span>
      <div>
        <div class="rotulo">${m ? 'Pedido sendo lançado na' : 'Antes de começar'}</div>
        <strong>${m ? 'Mesa ' + esc(m.numero) : 'Escolha a mesa'}</strong>
        <div class="detalhe">${m
          ? (m.comanda ? `Comanda aberta às ${hora(m.comanda.horarioPedido)} · conta atual ${rs(m.comanda.valorTotal)} · preço da ${nomeTurno(cobranca)}`
                       : `Mesa livre · a comanda abre ao enviar o pedido · preço da ${nomeTurno(cobranca)}`)
          : 'Toque em "Escolher mesa" para lançar o pedido.'}</div>
      </div>
      <span class="espaco"></span>
      ${m ? '<span class="pilula conta">Confira a mesa antes de enviar</span>' : ''}
    </div>
    <div class="barra-filtros">
      <div class="segmentos">
        <button class="segmento ${S.turnoVisto === 'MANHA' ? 'ativo' : ''}" data-acao="turno" data-id="MANHA">${ic('sol')}Cardápio do dia</button>
        <button class="segmento ${S.turnoVisto === 'NOITE' ? 'ativo' : ''}" data-acao="turno" data-id="NOITE">${ic('lua')}Cardápio da noite</button>
      </div>
      <label class="busca">${ic('busca')}<input id="busca" type="search" placeholder="Buscar no cardápio" value="${esc(S.busca)}" aria-label="Buscar no cardápio"></label>
    </div>
    <div id="lista-produtos">${listaProdutos()}</div>`;
}
function listaProdutos() {
  const turno = S.turnoVisto || turnoCobranca();
  const cobranca = turnoCobranca();
  const termo = S.busca.trim().toLowerCase();
  const itens = S.cardapio.filter(p => !termo || p.produto.toLowerCase().includes(termo));
  const titulo = `<div class="titulo-lista ${turno === 'NOITE' ? 'noite' : ''}">
      <div><div class="sobre">${turno === 'NOITE' ? 'Servido a partir das ' + esc(S.info.inicioNoite.replace(':00', 'h')) : 'Servido até às ' + esc(S.info.inicioNoite.replace(':00', 'h'))}</div>
      <h2>Cardápio ${turno === 'NOITE' ? 'da noite' : 'do dia'}</h2></div><span>${itens.length} ${itens.length === 1 ? 'opção' : 'opções'}</span></div>
      ${turno !== cobranca ? `<p class="legenda" style="margin:-4px 0 12px;color:var(--texto-suave)">Você está vendo os preços da ${nomeTurno(turno)}. Este pedido será cobrado com o preço da ${nomeTurno(cobranca)}.</p>` : ''}`;
  if (!S.cardapio.length) {
    return titulo + `<div class="cartao vazio">${ic('livro')}<p>O cardápio ainda está vazio.</p>
      <button class="botao" data-acao="ir-cardapio">Cadastrar produtos</button></div>`;
  }
  if (!itens.length) return titulo + '<div class="cartao vazio">Nenhum produto encontrado para essa busca.</div>';
  return titulo + `<div class="grade g3">${itens.map(p => {
    const q = (S.carrinho.get(p.codCar) || {}).qtd || 0;
    return `<article class="cartao produto">
      <h3>${esc(p.produto)}</h3>
      <div class="precos">Dia ${rs(p.precoManha)}${p.precoNoite != null ? ' · Noite ' + rs(p.precoNoite) : ''}</div>
      <div class="rodape">
        <div class="preco num">${rs(precoDe(p, turno))}<small>Preço ${turno === 'NOITE' ? 'da noite' : 'do dia'}</small></div>
        ${q ? `<div class="qtd"><button data-acao="menos" data-id="${p.codCar}" aria-label="Tirar um">−</button><b>${q}</b><button data-acao="mais" data-id="${p.codCar}" aria-label="Mais um">+</button></div>`
            : `<button class="mais" data-acao="mais" data-id="${p.codCar}" aria-label="Adicionar ${esc(p.produto)}">${ic('mais')}</button>`}
      </div></article>`;
  }).join('')}</div>`;
}
function barraPedido() {
  const q = qtdCarrinho();
  return `<div class="barra-pedido"><button class="botao largo" data-acao="ver-pedido" ${q ? '' : 'disabled'}>
    <span style="display:inline-flex;align-items:center;gap:8px">${ic('sacola')}<span class="contador">${q}</span></span>
    <span>Ver pedido</span><span class="num">${rs(totalCarrinho())}</span></button></div>`;
}

// ---- mesas
function telaMesas() {
  const livres = S.mesas.filter(m => m.livre).length;
  const ocupadas = S.mesas.length - livres;
  const emAberto = S.mesas.reduce((s, m) => s + (m.comanda ? Number(m.comanda.valorPendente) : 0), 0);
  const cab = `<div class="cabecalho">
      <div><small>Salão</small><h1>Mesas</h1>
        <div class="resumo-mesas" style="margin-top:10px">
          <span class="pilula livre">${livres} ${livres === 1 ? 'livre' : 'livres'}</span>
          <span class="pilula ocupada">${ocupadas} em atendimento</span>
          <span class="pilula">${rs(emAberto)} em aberto</span>
        </div></div>
      <button class="botao claro" data-acao="nova-mesa">${ic('mais')} Nova mesa</button>
    </div>`;
  if (!S.mesas.length) {
    return cab + `<div class="cartao vazio">${ic('mesas')}<p>Nenhuma mesa cadastrada ainda.</p>
      <button class="botao" data-acao="criar-mesas">Criar mesas 01 a 12</button></div>`;
  }
  return cab + `<div class="grade g3">${S.mesas.map(cartaoMesa).join('')}</div>`;
}
function cartaoMesa(m) {
  if (m.livre) {
    return `<article class="cartao mesa livre">
      <div class="linha-topo"><div><div class="rotulo">MESA</div><div class="numero">${esc(m.numero)}</div></div><span class="pilula livre">Livre</span></div>
      <div class="pronta">${ic('mesas')}Pronta para receber clientes</div>
      <button class="botao verde largo" data-acao="abrir-mesa" data-id="${esc(m.numero)}">Abrir mesa ${ic('seta')}</button>
    </article>`;
  }
  const c = m.comanda;
  const itens = c.pedidos.reduce((s, p) => s + p.itens.length, 0);
  const parcial = Number(c.valorPago) > 0;
  return `<article class="cartao mesa">
    <div class="linha-topo"><div><div class="rotulo">MESA</div><div class="numero">${esc(m.numero)}</div></div>
      <span class="pilula ${parcial ? 'conta' : 'ocupada'}">${ic('relogio')}${parcial ? 'Pago em parte' : itens ? 'Em atendimento' : 'Aguardando pedido'}</span></div>
    <div class="nome">${esc(c.nomeFuncionario || 'Sem garçom')}</div>
    <div class="meta"><span>${ic('relogio')}Aberta às ${hora(c.horarioPedido)}</span><span>${ic('recibo')}${itens} ${itens === 1 ? 'item' : 'itens'}</span></div>
    <div class="conta"><small>Conta atual</small><b class="num">${rs(c.valorPendente)}</b></div>
    <button class="botao largo" data-acao="ver-mesa" data-id="${esc(m.numero)}">Ver pedido ${ic('seta')}</button>
  </article>`;
}

// ---- dashboard
function telaDashboard() {
  const hoje = new Date().toLocaleDateString('pt-BR', { weekday: 'short', day: '2-digit', month: 'short' });
  return `
    <div class="cabecalho">
      <div><span class="selo">${ic('escudo')}Área exclusiva da gerência</span><h1>Visão geral das vendas</h1>
        <p class="subtitulo">Acompanhe o desempenho do Mdogão por turno.</p></div>
      <div class="cartao" style="padding:12px 16px;text-align:right"><div class="legenda" style="font-size:10px;font-weight:700;letter-spacing:.08em">HOJE</div><b style="font-size:18px;color:var(--vinho)">${esc(hoje)}</b></div>
    </div>
    <div class="segmentos fortes">
      ${seg('geral', 'Visão geral', 'painel')}${seg('caixa', 'Caixa', 'caixa')}${seg('cardapio', 'Cardápio', 'livro')}
    </div>
    <div class="secao">${S.subaba === 'geral' ? painelGeral() : S.subaba === 'caixa' ? painelCaixa() : painelCardapio()}</div>`;
}
function seg(id, nome, icone) {
  return `<button class="segmento ${S.subaba === id ? 'ativo' : ''}" data-acao="subaba" data-id="${id}">${ic(icone)}${nome}</button>`;
}
function painelGeral() {
  const d = S.dash;
  if (!d) return '<div class="cartao vazio">Carregando…</div>';
  if (!d.caixaAberto) {
    return `<div class="cartao vazio">${ic('caixa')}<p>O caixa está fechado. Abra o caixa para começar a receber e ver as vendas aqui.</p>
      <button class="botao" data-acao="subaba" data-id="caixa">Abrir o caixa</button></div>${historicoFechamentos()}`;
  }
  const dia = Number(d.faturamentoPorTurno.MANHA || 0), noite = Number(d.faturamentoPorTurno.NOITE || 0);
  const total = dia + noite;
  const pc = v => total ? Math.round(v * 100 / total) : 0;
  const formas = Object.entries(d.faturamentoPorFormaPagamento);
  return `
    <div class="grade g4">
      ${indicador('cifrao', 'Faturamento do caixa', rs(d.faturamento), 'Caixa nº ' + d.codCaix + ' aberto', true)}
      ${indicador('recibo', 'Comandas pagas', d.comandasAtendidas, 'neste caixa')}
      ${indicador('painel', 'Ticket médio', rs(d.ticketMedio), 'por comanda paga')}
      ${indicador('mesas', 'Mesas em atendimento', d.comandasAbertas, 'comandas abertas agora')}
    </div>
    <div class="grade g2 secao">
      <div class="cartao turno"><div class="cab"><span class="icone-quadro">${ic('sol')}</span><div><small>Turno do dia</small><b>até ${esc(S.info.inicioNoite.replace(':00', 'h'))}</b></div></div>
        <div class="legenda">VENDAS</div><div class="valor num">${rs(dia)}</div>
        <div class="barra"><i style="width:${pc(dia)}%"></i></div><div class="parte">${pc(dia)}% do faturamento</div></div>
      <div class="cartao turno noite"><div class="cab"><span class="icone-quadro">${ic('lua')}</span><div><small>Turno da noite</small><b>a partir das ${esc(S.info.inicioNoite.replace(':00', 'h'))}</b></div></div>
        <div class="legenda">VENDAS</div><div class="valor num">${rs(noite)}</div>
        <div class="barra"><i style="width:${pc(noite)}%"></i></div><div class="parte">${pc(noite)}% do faturamento</div></div>
    </div>
    <div class="grade g2 secao">
      <div class="cartao"><div class="cab-cartao"><div><h3>Mais vendidos</h3><span class="legenda">Produtos com maior saída neste caixa</span></div>${ic('trofeu')}</div>
        <div class="lista">${d.maisVendidos.length ? d.maisVendidos.slice(0, 5).map((p, i) => `
          <div class="item-lista"><span class="posicao">${i + 1}</span><div class="corpo"><b>${esc(p.produto)}</b><small>${p.quantidade} ${p.quantidade === 1 ? 'vendido' : 'vendidos'}</small></div></div>`).join('')
          : '<p class="legenda">Nenhuma venda paga ainda.</p>'}</div></div>
      <div class="cartao"><div class="cab-cartao"><div><h3>Recebimentos por forma de pagamento</h3><span class="legenda">Valores registrados no sistema</span></div>${ic('cartao')}</div>
        <div class="lista">${formas.map(([f, v]) => `
          <div class="item-lista"><span class="icone-forma">${ic((FORMAS[f] || [f, 'cartao'])[1])}</span><div class="corpo"><b>${esc((FORMAS[f] || [f])[0])}</b></div><span class="direita num">${rs(v)}</span></div>`).join('')}</div></div>
    </div>`;
}
function indicador(icone, legenda, valor, nota, destaque) {
  return `<div class="cartao indicador ${destaque ? 'destaque' : ''}"><span class="icone-quadro">${ic(icone)}</span>
    <div class="legenda">${legenda}</div><div class="valor num">${esc(valor)}</div><div class="nota">${esc(nota)}</div></div>`;
}
function painelCaixa() {
  const c = S.caixa;
  if (!c) {
    return `<div class="cartao faixa-caixa"><div><small>Caixa</small><h2>Caixa fechado</h2></div><span class="situacao">Sem caixa aberto</span></div>
      <div class="cartao secao"><h3>Abrir o caixa</h3><p class="legenda" style="margin-bottom:12px">Informe o troco que está na gaveta para começar o dia.</p>
        <form data-form="abrir-caixa" class="linha-form">
          <label class="campo"><span>Valor de abertura (R$)</span><input name="valor" inputmode="decimal" placeholder="100,00" required></label>
          <button class="botao" type="submit">Abrir caixa</button>
        </form></div>${historicoFechamentos()}`;
  }
  const formas = Object.entries(c.recebidoPorFormaPagamento);
  return `
    <div class="cartao faixa-caixa"><div><small>Fechamento diário</small><h2>Conferência do caixa nº ${c.codCaix}</h2></div><span class="situacao">${ic('relogio')} Caixa aberto</span></div>
    <div class="cartao secao"><div class="conta-caixa">
      <div><small>Abertura (troco)</small><b class="num">${rs(c.valorAbertura)}</b></div>
      <div><small>+ Entradas</small><b class="num">${rs(c.totalEntradas)}</b></div>
      <div><small>− Saídas</small><b class="num">${rs(c.totalSaidas)}</b></div>
      <div class="total"><small>= Saldo no caixa</small><b class="num">${rs(c.saldo)}</b></div>
    </div></div>
    <div class="grade g2 secao">
      <div class="cartao"><div class="cab-cartao"><div><h3>Recebimentos por forma de pagamento</h3><span class="legenda">Dia ${rs(c.recebidoPorTurno.MANHA)} · Noite ${rs(c.recebidoPorTurno.NOITE)}</span></div>${ic('cartao')}</div>
        <div class="lista">${formas.map(([f, v]) => `
          <div class="item-lista"><span class="icone-forma">${ic((FORMAS[f] || [f, 'cartao'])[1])}</span><div class="corpo"><b>${esc((FORMAS[f] || [f])[0])}</b></div><span class="direita num">${rs(v)}</span></div>`).join('')}</div></div>
      <div class="cartao"><h3>Suprimento ou sangria</h3><p class="legenda" style="margin-bottom:12px">Entrada de troco ou retirada de dinheiro do caixa.</p>
        <form data-form="movimentar" class="linha-form">
          <label class="campo"><span>Tipo</span><select name="tipo"><option value="SAIDA">Saída (sangria)</option><option value="ENTRADA">Entrada (suprimento)</option></select></label>
          <label class="campo"><span>Valor (R$)</span><input name="valor" inputmode="decimal" placeholder="0,00" required></label>
          <button class="botao claro" type="submit">Lançar</button>
        </form>
        <div class="lista" style="margin-top:14px">${c.movimentacoes.slice(-5).reverse().map(m => Number(m.saida) > 0
          ? `<div class="item-lista"><div class="corpo"><b>Saída</b><small>movimento nº ${m.codMov}</small></div><span class="direita num">− ${rs(m.saida)}</span></div>`
          : `<div class="item-lista"><div class="corpo"><b>Entrada</b><small>movimento nº ${m.codMov}</small></div><span class="direita num" style="color:var(--verde)">+ ${rs(m.entrada)}</span></div>`).join('') || ''}</div>
      </div>
    </div>
    <div class="secao"><button class="botao bloco" data-acao="fechar-caixa">Fechar o caixa</button></div>
    ${historicoFechamentos()}`;
}
function historicoFechamentos() {
  const fechados = S.caixas.filter(c => !c.aberto);
  if (!fechados.length) return '';
  return `<div class="cartao secao"><h3>Caixas fechados</h3><div class="lista" style="margin-top:12px">${fechados.slice(0, 8).map(c => `
    <div class="item-lista"><span class="posicao">${c.codCaix}</span><div class="corpo"><b>Fechou com ${rs(c.valorFechamento)}</b><small>${esc(c.relacoesPagamento || '')}</small></div></div>`).join('')}</div></div>`;
}
function painelCardapio() {
  const e = S.editando;
  return `
    <div class="cartao"><h3>${e ? 'Editar produto' : 'Novo produto'}</h3>
      <p class="legenda" style="margin-bottom:12px">O preço da noite é opcional: sem ele, vale o preço do dia.</p>
      <form data-form="produto" class="linha-form">
        <label class="campo" style="grid-column:span 2"><span>Produto</span><input name="produto" maxlength="100" value="${esc(e ? e.produto : '')}" required></label>
        <label class="campo"><span>Preço do dia (R$)</span><input name="dia" inputmode="decimal" value="${e ? String(e.precoManha).replace('.', ',') : ''}" required></label>
        <label class="campo"><span>Preço da noite (R$)</span><input name="noite" inputmode="decimal" value="${e && e.precoNoite != null ? String(e.precoNoite).replace('.', ',') : ''}"></label>
        <button class="botao" type="submit">${e ? 'Salvar' : 'Adicionar'}</button>
        ${e ? '<button class="botao claro" type="button" data-acao="cancelar-edicao">Cancelar</button>' : ''}
      </form></div>
    <div class="cartao secao"><h3>Produtos do cardápio</h3><div class="lista" style="margin-top:12px">${S.cardapio.length ? S.cardapio.map(p => `
      <div class="item-lista"><div class="corpo"><b>${esc(p.produto)}</b><small>Dia ${rs(p.precoManha)}${p.precoNoite != null ? ' · Noite ' + rs(p.precoNoite) : ''}</small></div>
        <div class="acoes"><button class="botao claro pequeno" data-acao="editar-produto" data-id="${p.codCar}" aria-label="Editar ${esc(p.produto)}">${ic('lapis')}</button>
        <button class="botao claro pequeno" data-acao="excluir-produto" data-id="${p.codCar}" aria-label="Excluir ${esc(p.produto)}">${ic('lixo')}</button></div></div>`).join('')
      : '<p class="legenda">Nenhum produto cadastrado.</p>'}</div></div>`;
}

// ---------------------------------------------------------------- folhas (janelas)
function desenharFolha() {
  const camada = document.getElementById('camada');
  const f = S.folha;
  if (!f) { camada.innerHTML = ''; return; }
  const conteudo = f.tipo === 'carrinho' ? folhaCarrinho() : f.tipo === 'mesa' ? folhaMesa(f.numero)
    : f.tipo === 'pagar' ? folhaPagar(f.numero) : f.tipo === 'trocar' ? folhaTrocarMesa() : folhaConta();
  camada.innerHTML = `<div class="fundo-folha" data-acao="fechar-folha"><section class="folha" role="dialog" aria-modal="true">${conteudo}</section></div>`;
}
function cabFolha(sobre, titulo) {
  return `<div class="cab-folha"><div><div class="legenda" style="color:var(--texto-suave);font-size:13px">${sobre}</div><h2>${titulo}</h2></div>
    <button class="fechar" data-acao="fechar-folha" data-forcar="1" aria-label="Fechar">${ic('x')}</button></div>`;
}
function folhaCarrinho() {
  const m = mesaAtual();
  const turno = turnoCobranca();
  const itens = itensCarrinho();
  return `${cabFolha(m ? 'Mesa ' + esc(m.numero) + ' · preço da ' + nomeTurno(turno) : 'Nenhuma mesa escolhida', 'Seu pedido')}
    ${itens.map(i => `<div class="linha-pedido">
      <div class="corpo"><b>${esc(i.p.produto)}</b><small class="num">${rs(precoDe(i.p, turno))} cada</small>
        <input data-campo="variacao" data-id="${i.p.codCar}" maxlength="100" placeholder="Observação do item (ex.: sem cebola)" value="${esc(i.variacao || '')}"></div>
      <div class="qtd"><button data-acao="menos" data-id="${i.p.codCar}" aria-label="Tirar um">−</button><b>${i.qtd}</b><button data-acao="mais" data-id="${i.p.codCar}" aria-label="Mais um">+</button></div>
      <div class="valor num">${rs(precoDe(i.p, turno) * i.qtd)}</div></div>`).join('')}
    <label class="campo" style="margin-top:14px"><span>Observação do pedido</span><input data-campo="observacao" maxlength="70" placeholder="Ex.: para viagem" value="${esc(S.observacao)}"></label>
    <div class="totais"><div class="grande"><span>Total do pedido</span><b class="num">${rs(totalCarrinho())}</b></div></div>
    <div class="acoes-folha">
      ${m ? `<button class="botao bloco" data-acao="enviar-pedido">Enviar pedido para a mesa ${esc(m.numero)}</button>`
          : '<button class="botao bloco" data-acao="trocar-mesa">Escolher a mesa</button>'}
      <button class="botao claro bloco" data-acao="limpar-carrinho">Esvaziar pedido</button>
    </div>`;
}
function folhaMesa(numeroMesa) {
  const m = S.mesas.find(x => x.numero === numeroMesa);
  if (!m || !m.comanda) return cabFolha('Mesa ' + esc(numeroMesa), 'Mesa livre') + '<p class="legenda">Esta mesa não tem comanda aberta.</p>';
  const c = m.comanda;
  return `${cabFolha(`Aberta às ${hora(c.horarioPedido)} · ${esc(c.nomeFuncionario || '')} · preço da ${nomeTurno(c.turno)}`, 'Mesa ' + esc(m.numero))}
    ${c.pedidos.length ? c.pedidos.map((p, i) => `<div class="grupo-pedido">
      <div class="cab-grupo"><span>Pedido ${i + 1}${p.observacao ? ' · ' + esc(p.observacao) : ''}</span>
        <span class="pilula ${p.pago ? 'livre' : 'ocupada'}">${p.pago ? 'Pago · ' + esc((FORMAS[p.formaPagamento] || [p.formaPagamento])[0]) : 'A pagar'}</span></div>
      ${p.itens.map(it => `<div class="linha-pedido"><div class="corpo"><b>${esc(it.produto)}</b>${it.variacao && it.variacao !== 'Padrão' ? `<small>${esc(it.variacao)}</small>` : ''}</div>
        <div class="valor num">${rs(it.precoUnitario)}</div>
        ${p.pago ? '' : `<button class="remover" data-acao="remover-item" data-comanda="${c.codComan}" data-pedido="${p.codPed}" data-id="${it.codPedFei}" aria-label="Remover ${esc(it.produto)}">${ic('lixo')}</button>`}</div>`).join('')}
    </div>`).join('') : '<p class="legenda" style="padding:12px 0">Nenhum pedido lançado ainda.</p>'}
    <div class="totais">
      <div><span>Total da comanda</span><span class="num">${rs(c.valorTotal)}</span></div>
      <div><span>Já pago</span><span class="num">${rs(c.valorPago)}</span></div>
      <div class="grande"><span>Falta pagar</span><b class="num">${rs(c.valorPendente)}</b></div>
    </div>
    <div class="acoes-folha duas">
      <button class="botao claro" data-acao="abrir-mesa" data-id="${esc(m.numero)}">${ic('mais')} Adicionar itens</button>
      <button class="botao" data-acao="receber" data-id="${esc(m.numero)}" ${Number(c.valorPendente) > 0 ? '' : 'disabled'}>${ic('cifrao')} Receber</button>
    </div>
    ${Number(c.valorPago) > 0 ? '' : `<div class="acoes-folha"><button class="botao claro bloco" data-acao="cancelar-comanda" data-id="${c.codComan}">Cancelar comanda</button></div>`}`;
}
function folhaPagar(numeroMesa) {
  const m = S.mesas.find(x => x.numero === numeroMesa);
  const c = m && m.comanda;
  if (!c) return cabFolha('Mesa ' + esc(numeroMesa), 'Nada a receber');
  return `${cabFolha('Mesa ' + esc(m.numero) + ' · receber tudo o que falta', 'Receber ' + rs(c.valorPendente))}
    <p class="legenda" style="margin-bottom:12px;color:var(--texto-suave)">Escolha a forma de pagamento. O valor entra no caixa aberto.</p>
    <div class="formas">${Object.entries(FORMAS).map(([f, [nome, icone]]) =>
      `<button class="forma" data-acao="pagar" data-comanda="${c.codComan}" data-id="${f}">${ic(icone)}${nome}</button>`).join('')}</div>
    <div class="acoes-folha"><button class="botao claro bloco" data-acao="ver-mesa" data-id="${esc(m.numero)}">Voltar</button></div>`;
}
function folhaTrocarMesa() {
  return `${cabFolha('Em qual mesa é o pedido?', 'Escolher mesa')}
    ${S.mesas.length ? `<div class="grade-mesas-mini">${S.mesas.map(m => `
      <button class="mesa-mini ${m.livre ? '' : 'ocupada'} ${m.numero === S.mesaSel ? 'atual' : ''}" data-acao="escolher-mesa" data-id="${esc(m.numero)}">
        <b>${esc(m.numero)}</b><small>${m.livre ? 'Livre' : rs(m.comanda.valorPendente)}</small></button>`).join('')}</div>`
      : '<p class="legenda">Nenhuma mesa cadastrada. Crie as mesas na aba Mesas.</p>'}`;
}
function folhaConta() {
  return `${cabFolha('Conectado como', esc(S.usuario.email))}
    <p class="legenda" style="color:var(--texto-suave)">Turno agora: <b>${S.info.turnoAtual === 'NOITE' ? 'noite' : 'dia'}</b> (a noite começa às ${esc(S.info.inicioNoite)}).</p>
    <form data-form="novo-funcionario" style="margin-top:16px">
      <h3 style="font-size:16px;margin-bottom:10px">Cadastrar outro funcionário</h3>
      <label class="campo"><span>E-mail</span><input name="email" type="email" required></label>
      <label class="campo"><span>Senha (mínimo 6 caracteres)</span><input name="senha" type="password" minlength="6" autocomplete="new-password" required></label>
      <button class="botao claro bloco" type="submit">Cadastrar funcionário</button>
    </form>
    <div class="acoes-folha"><button class="botao bloco" data-acao="sair">${ic('sair')} Sair</button></div>`;
}

// ---------------------------------------------------------------- ações
const ACOES = {
  aba(el) {
    S.aba = el.dataset.id; S.folha = null;
    desenhar(); carregar();
    window.scrollTo(0, 0);
  },
  subaba(el) { S.subaba = el.dataset.id; S.editando = null; desenhar(); },
  'modo-login'(el) { S.modoLogin = el.dataset.id; desenhar(); },
  'entrar-demo'() { return entrar(S.info.demoEmail, S.info.demoSenha); },
  conta() { S.folha = { tipo: 'conta' }; desenharFolha(); },
  sair,
  turno(el) { S.turnoVisto = el.dataset.id; desenhar(); },
  mais(el) {
    const id = Number(el.dataset.id);
    const atual = S.carrinho.get(id) || { qtd: 0, variacao: '' };
    if (atual.qtd >= 50) return avisar('Quantidade máxima por item é 50.', 'erro');
    S.carrinho.set(id, { qtd: atual.qtd + 1, variacao: atual.variacao });
    desenhar();
  },
  menos(el) {
    const id = Number(el.dataset.id);
    const atual = S.carrinho.get(id);
    if (!atual) return;
    if (atual.qtd <= 1) S.carrinho.delete(id); else S.carrinho.set(id, { qtd: atual.qtd - 1, variacao: atual.variacao });
    if (!S.carrinho.size && S.folha && S.folha.tipo === 'carrinho') S.folha = null;
    desenhar();
  },
  'ver-pedido'() { S.folha = { tipo: 'carrinho' }; desenharFolha(); },
  'limpar-carrinho'() { S.carrinho.clear(); S.observacao = ''; S.folha = null; desenhar(); },
  'trocar-mesa'() { S.folha = { tipo: 'trocar', voltar: S.folha && S.folha.tipo === 'carrinho' }; desenharFolha(); },
  'escolher-mesa'(el) {
    const voltar = S.folha && S.folha.voltar;
    escolherMesa(el.dataset.id);
    S.folha = voltar ? { tipo: 'carrinho' } : null;
    desenhar();
  },
  'abrir-mesa'(el) { escolherMesa(el.dataset.id); S.aba = 'pedido'; S.folha = null; desenhar(); window.scrollTo(0, 0); },
  'ver-mesa'(el) { S.folha = { tipo: 'mesa', numero: el.dataset.id }; desenharFolha(); },
  receber(el) { S.folha = { tipo: 'pagar', numero: el.dataset.id }; desenharFolha(); },
  'fechar-folha'(el, ev) {
    if (el.dataset.forcar || ev.target === el) { S.folha = null; desenharFolha(); }
  },
  'ir-cardapio'() { S.aba = 'dashboard'; S.subaba = 'cardapio'; desenhar(); carregar(); },
  'cancelar-edicao'() { S.editando = null; desenhar(); },
  'editar-produto'(el) { S.editando = S.cardapio.find(p => p.codCar === Number(el.dataset.id)) || null; desenhar(); window.scrollTo(0, 0); },

  'enviar-pedido'() {
    return tentar(async () => {
      const m = mesaAtual();
      if (!m) throw new Error('Escolha a mesa antes de enviar.');
      const itens = itensCarrinho().map(i => ({ codCar: i.p.codCar, quantidade: i.qtd, variacao: i.variacao || null }));
      if (!itens.length) throw new Error('O pedido está vazio.');
      let comanda = m.comanda;
      if (!comanda) comanda = await api('POST', '/comandas', { numeroComanda: m.numero, mesa: m.numero });
      await api('POST', `/comandas/${comanda.codComan}/pedidos`, { observacao: S.observacao || null, itens });
      S.carrinho.clear(); S.observacao = ''; S.folha = null;
      avisar('Pedido enviado para a mesa ' + m.numero + '.', 'ok');
      S.aba = 'mesas';
      await carregar();
    });
  },
  'remover-item'(el) {
    return tentar(async () => {
      await api('DELETE', `/comandas/${el.dataset.comanda}/pedidos/${el.dataset.pedido}/itens/${el.dataset.id}`);
      await carregar();
    });
  },
  pagar(el) {
    return tentar(async () => {
      const r = await api('POST', `/comandas/${el.dataset.comanda}/pagamentos`, { formaPagamento: el.dataset.id });
      S.folha = null;
      avisar('Recebido ' + rs(r.valorPago) + ' em ' + FORMAS[el.dataset.id][0] + '. Mesa liberada.', 'ok');
      await carregar();
    });
  },
  'cancelar-comanda'(el) {
    if (!confirm('Cancelar esta comanda e apagar os pedidos dela?')) return undefined;
    return tentar(async () => {
      await api('DELETE', '/comandas/' + el.dataset.id);
      S.folha = null;
      avisar('Comanda cancelada.', 'ok');
      await carregar();
    });
  },
  'nova-mesa'() {
    const n = prompt('Número da nova mesa (até 2 caracteres, ex.: 13):');
    if (!n) return undefined;
    return tentar(async () => { await api('POST', '/mesas', { numero: n.trim() }); await carregar(); });
  },
  'criar-mesas'() {
    return tentar(async () => {
      for (let i = 1; i <= 12; i++) await api('POST', '/mesas', { numero: String(i).padStart(2, '0') });
      avisar('Mesas 01 a 12 criadas.', 'ok');
      await carregar();
    });
  },
  'fechar-caixa'() {
    if (!confirm('Fechar o caixa agora? Depois de fechado ele não pode ser reaberto.')) return undefined;
    return tentar(async () => {
      const c = await api('POST', '/caixa/fechar');
      avisar('Caixa fechado com ' + rs(c.valorFechamento) + '.', 'ok');
      await carregar();
    });
  },
  'excluir-produto'(el) {
    const p = S.cardapio.find(x => x.codCar === Number(el.dataset.id));
    if (!p || !confirm('Excluir "' + p.produto + '" do cardápio?')) return undefined;
    return tentar(async () => { await api('DELETE', '/cardapio/' + p.codCar); S.carrinho.delete(p.codCar); await carregar(); });
  },
};

function escolherMesa(numeroMesa) {
  S.mesaSel = numeroMesa;
  S.turnoVisto = null;   // volta a mostrar o cardápio do turno que vale para essa mesa
  gravarLocal('mdogao.mesa', numeroMesa);
}

async function entrar(email, senha) {
  return tentar(async () => {
    const r = await api('POST', '/auth/login', { email, senha });
    S.token = r.token;
    gravarLocal('mdogao.token', r.token);
    S.usuario = { codLog: r.codLog, email: r.email };
    S.carregando = true; desenhar();
    await carregar();
  });
}

const FORMULARIOS = {
  async login(d) {
    if (S.modoLogin === 'criar') {
      await tentar(async () => { await api('POST', '/auth/registrar', { email: d.email, senha: d.senha }); await entrar(d.email, d.senha); });
    } else {
      await entrar(d.email, d.senha);
    }
  },
  'novo-funcionario'(d) {
    return tentar(async () => {
      const u = await api('POST', '/auth/registrar', { email: d.email, senha: d.senha });
      S.folha = null; desenharFolha();
      avisar('Funcionário ' + u.email + ' cadastrado.', 'ok');
    });
  },
  'abrir-caixa'(d) {
    return tentar(async () => {
      const v = numero(d.valor);
      if (v == null || Number.isNaN(v)) throw new Error('Informe um valor válido, por exemplo 100,00.');
      await api('POST', '/caixa/abrir', { valorAbertura: v });
      avisar('Caixa aberto.', 'ok');
      await carregar();
    });
  },
  movimentar(d) {
    return tentar(async () => {
      const v = numero(d.valor);
      if (v == null || Number.isNaN(v)) throw new Error('Informe um valor válido, por exemplo 20,00.');
      await api('POST', '/caixa/movimentacoes', { tipo: d.tipo, valor: v });
      avisar(d.tipo === 'SAIDA' ? 'Saída lançada.' : 'Entrada lançada.', 'ok');
      await carregar();
    });
  },
  produto(d) {
    return tentar(async () => {
      const dia = numero(d.dia), noite = numero(d.noite);
      if (dia == null || Number.isNaN(dia) || Number.isNaN(noite)) throw new Error('Informe preços válidos, por exemplo 24,90.');
      const corpo = { produto: d.produto, precoManha: dia, precoNoite: noite };
      if (S.editando) await api('PUT', '/cardapio/' + S.editando.codCar, corpo); else await api('POST', '/cardapio', corpo);
      avisar(S.editando ? 'Produto atualizado.' : 'Produto adicionado.', 'ok');
      S.editando = null;
      await carregar();
    });
  },
};

// ---------------------------------------------------------------- eventos
document.addEventListener('click', ev => {
  const el = ev.target.closest('[data-acao]');
  if (!el || el.disabled) return;
  const acao = ACOES[el.dataset.acao];
  if (!acao) return;
  if (el.tagName === 'BUTTON' && el.type !== 'submit') ev.preventDefault();
  // clique dentro da folha não deve fechar a folha
  if (el.dataset.acao === 'fechar-folha' && !el.dataset.forcar && ev.target !== el) return;
  acao(el, ev);
});
document.addEventListener('submit', ev => {
  const form = ev.target.closest('[data-form]');
  if (!form) return;
  ev.preventDefault();
  const dados = Object.fromEntries(new FormData(form).entries());
  FORMULARIOS[form.dataset.form](dados);
});
document.addEventListener('input', ev => {
  const el = ev.target;
  if (el.id === 'busca') {
    S.busca = el.value;
    document.getElementById('lista-produtos').innerHTML = listaProdutos();
  } else if (el.dataset.campo === 'variacao') {
    const item = S.carrinho.get(Number(el.dataset.id));
    if (item) item.variacao = el.value;
  } else if (el.dataset.campo === 'observacao') {
    S.observacao = el.value;
  }
});
document.addEventListener('keydown', ev => {
  if (ev.key === 'Escape' && S.folha) { S.folha = null; desenharFolha(); }
});

// Atualiza mesas e painel sozinho (outro garçom pode ter lançado algo), sem atrapalhar quem está digitando.
setInterval(() => {
  const digitando = document.activeElement && ['INPUT', 'SELECT'].includes(document.activeElement.tagName);
  if (S.usuario && !S.folha && !digitando && !document.hidden && S.aba !== 'pedido') carregar();
}, 20000);

iniciar();

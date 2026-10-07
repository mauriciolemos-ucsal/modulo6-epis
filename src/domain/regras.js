// Regras de negócio do Módulo 6 — Segurança do Trabalho.
// Funções puras (sem React, sem estado global) para facilitar testes e a
// migração para um backend real na Unidade 2.

export const TIPOS_OCORRENCIA = {
  RISCO: 'Risco Identificado',
  QUASE_ACIDENTE: 'Quase-Acidente',
  ACIDENTE: 'Acidente',
}

export const PAPEIS_EPI = ['faltou', 'falhou', 'ajudou']

// RN07 — só Quase-Acidente e Acidente admitem associação com o papel do EPI.
export function permiteAssociarEpi(tipo) {
  return tipo === 'QUASE_ACIDENTE' || tipo === 'ACIDENTE'
}

// RN05 — EPIs exigidos = básicos da obra ∪ obrigatórios de todas as funções.
export function episExigidos(trabalhador, obra, funcoes) {
  const exigidos = new Set(obra?.episBasicos ?? [])
  for (const funcaoId of trabalhador.funcoes) {
    const funcao = funcoes.find((f) => f.id === funcaoId)
    for (const epiId of funcao?.episObrigatorios ?? []) exigidos.add(epiId)
  }
  return [...exigidos]
}

export function calcularConformidade(trabalhador, obra, funcoes) {
  const exigidos = episExigidos(trabalhador, obra, funcoes)
  const emUso = new Set(trabalhador.episEmUso ?? [])
  const itens = exigidos.map((epiId) => ({ epiId, ok: emUso.has(epiId) }))
  const faltando = itens.filter((i) => !i.ok).map((i) => i.epiId)
  const percentual = exigidos.length
    ? Math.round(((exigidos.length - faltando.length) / exigidos.length) * 100)
    : 100
  return { itens, faltando, percentual, conforme: faltando.length === 0 }
}

// RN09 — painel exclusivo do Fiscal.
export function podeVerPainel(usuario) {
  return Boolean(usuario?.fiscal)
}

// Fiscal pode registrar em nome de outro trabalhador, desde que da mesma obra.
export function podeRegistrarEmNomeDe(usuario, autor) {
  if (!usuario || !autor) return false
  if (usuario.id === autor.id) return true
  return Boolean(usuario.fiscal) && usuario.obraId === autor.obraId
}

// RN10 — o usuário só enxerga dados da sua obra.
export function filtrarPorObra(itens, usuario) {
  return itens.filter((i) => i.obraId === usuario.obraId)
}

export function ordenarRecentesPrimeiro(ocorrencias) {
  return [...ocorrencias].sort((a, b) => {
    const diff = new Date(b.dataHora) - new Date(a.dataHora)
    return diff !== 0 ? diff : b.id - a.id
  })
}

// Períodos do filtro do feed: `dias` = janela até agora (hoje = desde 00:00).
export const PERIODOS_FEED = {
  TODOS: { rotulo: 'Todo o período' },
  HOJE: { rotulo: 'Hoje', dias: 0 },
  SETE_DIAS: { rotulo: '7 dias', dias: 7 },
  TRINTA_DIAS: { rotulo: '30 dias', dias: 30 },
}

// Filtra ocorrências por período e/ou tipo. `tipo` vazio = todos.
export function filtrarOcorrencias(ocorrencias, { periodo = 'TODOS', tipo = '' } = {}, agora = new Date()) {
  const dias = PERIODOS_FEED[periodo]?.dias
  let inicio = null
  if (dias !== undefined) {
    inicio = new Date(agora)
    inicio.setHours(0, 0, 0, 0)
    inicio.setDate(inicio.getDate() - dias)
  }
  return ocorrencias.filter((o) => {
    if (tipo && o.tipo !== tipo) return false
    return !inicio || new Date(o.dataHora) >= inicio
  })
}

// Valida um rascunho de ocorrência. Retorna lista de mensagens de erro (vazia = ok).
export function validarOcorrencia(rascunho, { usuario, autor }) {
  const erros = []
  // RN06 — classificação obrigatória.
  if (!TIPOS_OCORRENCIA[rascunho.tipo]) erros.push('Selecione a classificação da ocorrência.')
  if (!rascunho.foto) erros.push('Anexe uma foto da ocorrência.')
  if (!podeRegistrarEmNomeDe(usuario, autor)) {
    erros.push('Você não tem permissão para registrar em nome deste trabalhador.')
  }
  if (permiteAssociarEpi(rascunho.tipo)) {
    const semPapel = (rascunho.epis ?? []).filter((e) => !PAPEIS_EPI.includes(e.papel))
    if (semPapel.length) erros.push('Informe o papel (faltou / falhou / ajudou) de cada EPI marcado.')
  }
  return erros
}

// Monta a ocorrência final a partir do rascunho validado.
export function criarOcorrencia(rascunho, { id, usuario, autor, agora = new Date() }) {
  return {
    id,
    obraId: autor.obraId,
    tipo: rascunho.tipo,
    autorId: autor.id,
    registradoPorId: usuario.id,
    dataHora: agora.toISOString(),
    descricao: rascunho.descricao?.trim() ?? '',
    foto: rascunho.foto,
    // RN07 — EPIs descartados quando o tipo é Risco Identificado.
    epis: permiteAssociarEpi(rascunho.tipo) ? rascunho.epis ?? [] : [],
  }
}

// RN08 — uma notificação por trabalhador da obra da ocorrência.
export function gerarNotificacoes(ocorrencia, trabalhadores, proximoId) {
  return trabalhadores
    .filter((t) => t.obraId === ocorrencia.obraId)
    .map((t, i) => ({
      id: proximoId + i,
      destinatarioId: t.id,
      ocorrenciaId: ocorrencia.id,
      lida: false,
    }))
}

export function indicadoresDaObra(ocorrencias, trabalhadores, obra, funcoes) {
  const contagem = { RISCO: 0, QUASE_ACIDENTE: 0, ACIDENTE: 0 }
  const papeis = { faltou: 0, falhou: 0, ajudou: 0 }
  for (const o of ocorrencias) {
    contagem[o.tipo] = (contagem[o.tipo] ?? 0) + 1
    for (const e of o.epis) papeis[e.papel] = (papeis[e.papel] ?? 0) + 1
  }
  const conformidades = trabalhadores.map((t) => ({
    trabalhador: t,
    ...calcularConformidade(t, obra, funcoes),
  }))
  const conformes = conformidades.filter((c) => c.conforme).length
  return {
    total: ocorrencias.length,
    contagem,
    papeis,
    percentualConformes: trabalhadores.length
      ? Math.round((conformes / trabalhadores.length) * 100)
      : 100,
    naoConformes: conformidades.filter((c) => !c.conforme),
  }
}

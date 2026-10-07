// Cliente HTTP do backend (Spring Boot). Em desenvolvimento o Vite encaminha /api para :8080.
let token = null
let aoExpirar = () => {}

export function definirAoExpirar(fn) {
  aoExpirar = fn
}

export class ErroApi extends Error {
  constructor(status, erros) {
    super(erros.join(' '))
    this.status = status
    this.erros = erros
  }
}

async function requisitar(metodo, url, corpo) {
  const cabecalhos = {}
  if (token) cabecalhos.Authorization = `Bearer ${token}`
  let body
  if (corpo instanceof FormData) {
    body = corpo
  } else if (corpo !== undefined) {
    cabecalhos['Content-Type'] = 'application/json'
    body = JSON.stringify(corpo)
  }

  let resposta
  try {
    resposta = await fetch(url, { method: metodo, headers: cabecalhos, body })
  } catch {
    throw new ErroApi(0, ['Não foi possível conectar ao servidor. Verifique se o backend está rodando.'])
  }

  if (resposta.status === 204) return null
  const dados = await resposta.json().catch(() => null)
  if (!resposta.ok) {
    // 401 com sessão aberta = token expirado (ex.: backend reiniciado).
    if (resposta.status === 401 && token) {
      token = null
      aoExpirar()
    }
    throw new ErroApi(resposta.status, dados?.erros ?? [`Erro ${resposta.status} no servidor.`])
  }
  return dados
}

export const api = {
  opcoesLogin: () => requisitar('GET', '/api/login/opcoes'),

  async login(matricula) {
    const r = await requisitar('POST', '/api/login', { matricula })
    token = r.token
    return r.usuarioId
  },

  async logout() {
    try {
      await requisitar('POST', '/api/logout')
    } finally {
      token = null
    }
  },

  dados: () => requisitar('GET', '/api/dados'),

  registrarOcorrencia({ tipo, descricao, epis, arquivoFoto, autorId }) {
    const form = new FormData()
    form.append('tipo', tipo)
    form.append('descricao', descricao ?? '')
    form.append('autorId', autorId)
    form.append('epis', JSON.stringify(epis ?? []))
    if (arquivoFoto) form.append('foto', arquivoFoto)
    return requisitar('POST', '/api/ocorrencias', form)
  },

  marcarNotificacoesLidas: () => requisitar('POST', '/api/notificacoes/lidas'),

  definirEpiDaObra: (obraId, epiId, ativo) =>
    requisitar('PUT', `/api/obras/${encodeURIComponent(obraId)}/epis/${encodeURIComponent(epiId)}`, { ativo }),

  definirEpiDaFuncao: (funcaoId, epiId, ativo) =>
    requisitar('PUT', `/api/funcoes/${encodeURIComponent(funcaoId)}/epis/${encodeURIComponent(epiId)}`, { ativo }),
}

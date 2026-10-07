import { createContext, useContext, useEffect, useMemo, useReducer } from 'react'
import { api, definirAoExpirar } from '../api.js'
import { podeVerPainel } from '../domain/regras.js'

// Os dados vêm do backend (MySQL); aqui ficam só a cópia para renderização, o usuário
// logado e a tela atual. As regras de negócio são reaplicadas e garantidas no servidor.
const dadosVazios = {
  epis: [],
  funcoes: [],
  obras: [],
  trabalhadores: [],
  ocorrencias: [],
  notificacoes: [],
}

const estadoInicial = {
  usuarioId: null,
  tela: { nome: 'login' },
  ...dadosVazios,
}

const TELAS_FISCAL = ['painel', 'registrar_terceiro']
const INTERVALO_ATUALIZACAO_MS = 30_000

function reducer(state, action) {
  switch (action.type) {
    case 'login':
      return { ...state, ...action.dados, usuarioId: action.usuarioId, tela: { nome: 'feed' } }

    case 'logout':
      return { ...estadoInicial }

    // Substitui os dados pelos do servidor; `tela` opcional para navegar junto.
    case 'carregar':
      return { ...state, ...action.dados, tela: action.tela ?? state.tela }

    case 'navegar': {
      const usuario = state.trabalhadores.find((t) => t.id === state.usuarioId)
      if (TELAS_FISCAL.includes(action.tela.nome) && !podeVerPainel(usuario)) return state
      return { ...state, tela: action.tela }
    }

    case 'marcarNotificacoesLidas':
      return {
        ...state,
        notificacoes: state.notificacoes.map((n) =>
          n.destinatarioId === state.usuarioId ? { ...n, lida: true } : n,
        ),
      }

    default:
      throw new Error(`Ação desconhecida: ${action.type}`)
  }
}

const AppContext = createContext(null)

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, estadoInicial)

  // Operações assíncronas contra o backend. Lançam ErroApi (com `erros`) para a tela exibir.
  const acoes = useMemo(() => {
    const recarregar = async (tela) => dispatch({ type: 'carregar', dados: await api.dados(), tela })
    return {
      async entrar(matricula) {
        const usuarioId = await api.login(matricula)
        dispatch({ type: 'login', usuarioId, dados: await api.dados() })
      },

      sair() {
        dispatch({ type: 'logout' })
        api.logout().catch(() => {})
      },

      async registrarOcorrencia({ rascunho, autorId }) {
        const { id } = await api.registrarOcorrencia({
          tipo: rascunho.tipo,
          descricao: rascunho.descricao,
          epis: rascunho.epis,
          arquivoFoto: rascunho.arquivoFoto,
          autorId,
        })
        await recarregar({ nome: 'detalhe', ocorrenciaId: id })
      },

      marcarNotificacoesLidas() {
        dispatch({ type: 'marcarNotificacoesLidas' })
        api.marcarNotificacoesLidas().catch(() => {})
      },

      async definirEpiDaObra(obraId, epiId, ativo) {
        await api.definirEpiDaObra(obraId, epiId, ativo)
        await recarregar()
      },

      async definirEpiDaFuncao(funcaoId, epiId, ativo) {
        await api.definirEpiDaFuncao(funcaoId, epiId, ativo)
        await recarregar()
      },
    }
  }, [])

  // Sessão expirada (ex.: backend reiniciado) volta para o login.
  useEffect(() => {
    definirAoExpirar(() => dispatch({ type: 'logout' }))
  }, [])

  // Atualização periódica: traz ocorrências e notificações (RN08) criadas por outros usuários.
  useEffect(() => {
    if (!state.usuarioId) return undefined
    const id = setInterval(() => {
      api.dados().then((dados) => dispatch({ type: 'carregar', dados })).catch(() => {})
    }, INTERVALO_ATUALIZACAO_MS)
    return () => clearInterval(id)
  }, [state.usuarioId])

  const valor = useMemo(() => {
    const usuario = state.trabalhadores.find((t) => t.id === state.usuarioId) ?? null
    const obra = usuario ? state.obras.find((o) => o.id === usuario.obraId) ?? null : null
    return { state, dispatch, acoes, usuario, obra }
  }, [state, acoes])

  return <AppContext.Provider value={valor}>{children}</AppContext.Provider>
}

export function useApp() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useApp deve ser usado dentro de <AppProvider>')
  return ctx
}

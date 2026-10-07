import { useApp } from '../state/AppContext.jsx'
import { podeVerPainel } from '../domain/regras.js'
import { nomesFuncoes } from '../utils.js'
import Notificacoes from './Notificacoes.jsx'

export const TELAS = [
  { id: 'feed', rotulo: 'Feed de Ocorrências' },
  { id: 'registrar', rotulo: 'Registrar Ocorrência' },
  { id: 'conformidade', rotulo: 'Conformidade de EPI' },
  { id: 'obra', rotulo: 'Cadastro de Obra' },
  { id: 'funcao', rotulo: 'Cadastro de Função' },
  { id: 'painel', rotulo: 'Painel de Indicadores', fiscal: true },
  { id: 'registrar_terceiro', rotulo: 'Registrar em nome de outro', fiscal: true },
]

const TITULOS = { ...Object.fromEntries(TELAS.map((t) => [t.id, t.rotulo])), detalhe: 'Detalhe da Ocorrência' }

export default function Layout({ children }) {
  const { state, dispatch, acoes, usuario, obra } = useApp()
  const ehFiscal = podeVerPainel(usuario)
  const telaAtual = state.tela.nome

  return (
    <div className="app">
      <aside className="app__lateral">
        <div>
          <div className="marca">SGF</div>
          <div className="texto-suave">Módulo 6 · Segurança do Trabalho</div>
        </div>

        <nav className="nav">
          <div className="rotulo">Navegação</div>
          {TELAS.filter((t) => !t.fiscal || ehFiscal).map((t) => (
            <button
              type="button"
              key={t.id}
              className={`nav__item${telaAtual === t.id ? ' nav__item--ativo' : ''}`}
              onClick={() => dispatch({ type: 'navegar', tela: { nome: t.id } })}
            >
              <span>{t.rotulo}</span>
              {t.fiscal && <span className="tag">Fiscal</span>}
            </button>
          ))}
        </nav>

        <div className="app__usuario">
          <div className="forte">{usuario.nome}{ehFiscal && <span className="tag">Fiscal</span>}</div>
          <div>{nomesFuncoes(usuario, state.funcoes)}</div>
          <div>Obra: {obra.nome}</div>
          <button type="button" className="link" onClick={acoes.sair}>
            Sair / trocar usuário
          </button>
        </div>
      </aside>

      <div className="app__principal">
        <header className="app__topo">
          <h1 className="app__titulo">{TITULOS[telaAtual]}</h1>
          <Notificacoes />
        </header>
        <main className="app__conteudo">{children}</main>
      </div>
    </div>
  )
}

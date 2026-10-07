import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import { TIPOS_OCORRENCIA } from '../domain/regras.js'
import { formatarDataHora } from '../utils.js'

// "Sino" de notificações internas (RN08). Geradas no servidor; o front as atualiza periodicamente.
export default function Notificacoes() {
  const { state, dispatch, acoes, usuario } = useApp()
  const [aberto, setAberto] = useState(false)

  const minhas = state.notificacoes
    .filter((n) => n.destinatarioId === usuario.id)
    .map((n) => ({ ...n, ocorrencia: state.ocorrencias.find((o) => o.id === n.ocorrenciaId) }))
    .filter((n) => n.ocorrencia)
    .sort((a, b) => b.id - a.id)
  const naoLidas = minhas.filter((n) => !n.lida).length

  function alternar() {
    if (aberto) acoes.marcarNotificacoesLidas()
    setAberto(!aberto)
  }

  function abrir(ocorrenciaId) {
    acoes.marcarNotificacoesLidas()
    dispatch({ type: 'navegar', tela: { nome: 'detalhe', ocorrenciaId } })
    setAberto(false)
  }

  function nomeAutor(id) {
    return state.trabalhadores.find((t) => t.id === id)?.nome ?? '—'
  }

  return (
    <div className="notif">
      <button type="button" className="botao botao--contorno" onClick={alternar} aria-expanded={aberto}>
        🔔 Notificações
        {naoLidas > 0 && <span className="notif__contador">{naoLidas}</span>}
      </button>
      {aberto && (
        <div className="notif__painel">
          <div className="notif__titulo">Notificações da obra</div>
          {minhas.length === 0 && <div className="notif__item vazio">Nenhuma notificação.</div>}
          {minhas.slice(0, 15).map((n) => (
            <button
              type="button"
              key={n.id}
              className={`notif__item${n.lida ? '' : ' notif__item--nova'}`}
              onClick={() => abrir(n.ocorrenciaId)}
            >
              <div>
                Nova ocorrência ({TIPOS_OCORRENCIA[n.ocorrencia.tipo]}) registrada por{' '}
                {nomeAutor(n.ocorrencia.autorId)}
              </div>
              <div className="texto-suave">{formatarDataHora(n.ocorrencia.dataHora)}</div>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

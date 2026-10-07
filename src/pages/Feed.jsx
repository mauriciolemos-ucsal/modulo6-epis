import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import {
  filtrarPorObra, filtrarOcorrencias, ordenarRecentesPrimeiro,
  PERIODOS_FEED, TIPOS_OCORRENCIA,
} from '../domain/regras.js'
import { formatarDataHora } from '../utils.js'
import TipoBadge from '../components/TipoBadge.jsx'
import Foto from '../components/Foto.jsx'
import Chip from '../components/Chip.jsx'

export default function Feed() {
  const { state, dispatch, usuario, obra } = useApp()
  const [periodo, setPeriodo] = useState('TODOS')
  const [tipo, setTipo] = useState('')
  // RN10 — só ocorrências da obra do usuário, mais recentes primeiro.
  const daObra = filtrarPorObra(state.ocorrencias, usuario)
  const ocorrencias = ordenarRecentesPrimeiro(filtrarOcorrencias(daObra, { periodo, tipo }))
  const nome = (id) => state.trabalhadores.find((t) => t.id === id)?.nome ?? '—'

  return (
    <div className="coluna coluna--media">
      <div className="cabecalho-secao">
        <div className="texto-suave">Feed de Ocorrências · {obra.nome}</div>
        <button
          type="button"
          className="botao botao--primario"
          onClick={() => dispatch({ type: 'navegar', tela: { nome: 'registrar' } })}
        >
          + Registrar Ocorrência
        </button>
      </div>

      <div className="chips" role="group" aria-label="Filtrar por período">
        {Object.entries(PERIODOS_FEED).map(([id, p]) => (
          <Chip key={id} pequeno selecionado={periodo === id} onClick={() => setPeriodo(id)}>{p.rotulo}</Chip>
        ))}
      </div>
      <div className="chips" role="group" aria-label="Filtrar por tipo">
        <Chip pequeno selecionado={tipo === ''} onClick={() => setTipo('')}>Todos os tipos</Chip>
        {Object.entries(TIPOS_OCORRENCIA).map(([id, rotulo]) => (
          <Chip key={id} pequeno selecionado={tipo === id} onClick={() => setTipo(id)}>{rotulo}</Chip>
        ))}
      </div>

      {ocorrencias.length === 0 && (
        <div className="cartao vazio">
          {daObra.length === 0
            ? 'Nenhuma ocorrência registrada nesta obra.'
            : 'Nenhuma ocorrência para os filtros selecionados.'}
        </div>
      )}

      {ocorrencias.map((o) => (
        <button
          type="button"
          key={o.id}
          className="cartao feed-card"
          onClick={() => dispatch({ type: 'navegar', tela: { nome: 'detalhe', ocorrenciaId: o.id } })}
        >
          <Foto src={o.foto} alt="" className="feed-card__foto" />
          <div className="feed-card__corpo">
            <div className="linha-entre">
              <TipoBadge tipo={o.tipo} />
              <span className="texto-suave">{formatarDataHora(o.dataHora)}</span>
            </div>
            <div className="feed-card__desc">{o.descricao || '(sem descrição)'}</div>
            <div className="texto-suave">Registrado por {nome(o.autorId)}</div>
          </div>
        </button>
      ))}
    </div>
  )
}

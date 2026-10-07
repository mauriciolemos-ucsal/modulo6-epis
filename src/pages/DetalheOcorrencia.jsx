import { useApp } from '../state/AppContext.jsx'
import { formatarDataHora, nomeEpi } from '../utils.js'
import TipoBadge from '../components/TipoBadge.jsx'
import Foto from '../components/Foto.jsx'

export default function DetalheOcorrencia({ ocorrenciaId }) {
  const { state, dispatch, usuario, obra } = useApp()
  const o = state.ocorrencias.find((x) => x.id === ocorrenciaId)
  const voltar = () => dispatch({ type: 'navegar', tela: { nome: 'feed' } })
  const nome = (id) => state.trabalhadores.find((t) => t.id === id)?.nome ?? '—'

  // RN10 — ocorrência de outra obra é tratada como inexistente.
  if (!o || o.obraId !== usuario.obraId) {
    return (
      <div className="coluna coluna--estreita">
        <button type="button" className="link" onClick={voltar}>← Voltar ao feed</button>
        <div className="cartao vazio">Ocorrência não encontrada.</div>
      </div>
    )
  }

  return (
    <div className="coluna coluna--estreita">
      <button type="button" className="link" onClick={voltar}>← Voltar ao feed</button>
      <div className="cartao">
        <div className="linha-entre">
          <TipoBadge tipo={o.tipo} />
          <span className="texto-suave">{formatarDataHora(o.dataHora)}</span>
        </div>
        <Foto src={o.foto} alt="Foto da ocorrência" className="foto--grande" />
        <p>{o.descricao || '(sem descrição)'}</p>
        <div className="texto-suave">
          Registrado por {nome(o.autorId)} · Obra {obra.nome}
          {o.registradoPorId !== o.autorId && <> · lançado pelo Fiscal {nome(o.registradoPorId)}</>}
        </div>

        {o.epis.length > 0 && (
          <div className="secao">
            <div className="rotulo">EPIs envolvidos</div>
            {o.epis.map((e) => (
              <div key={e.epiId} className="linha-lista">
                <span>{nomeEpi(state.epis, e.epiId)}</span>
                <span className={`papel papel--${e.papel}`}>{e.papel}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

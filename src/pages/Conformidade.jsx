import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import { calcularConformidade, podeVerPainel } from '../domain/regras.js'
import { nomeEpi, nomesFuncoes } from '../utils.js'

// Trabalhador vê a própria conformidade; Fiscal pode consultar colegas da obra.
export default function Conformidade() {
  const { state, usuario, obra } = useApp()
  const [trabalhadorId, setTrabalhadorId] = useState(usuario.id)
  const ehFiscal = podeVerPainel(usuario)
  const colegas = state.trabalhadores.filter((t) => t.obraId === usuario.obraId)
  const trabalhador = colegas.find((t) => t.id === trabalhadorId) ?? usuario
  const c = calcularConformidade(trabalhador, obra, state.funcoes)

  return (
    <div className="coluna coluna--estreita">
      {ehFiscal && (
        <label className="campo">
          <span className="rotulo">Consultar trabalhador</span>
          <select value={trabalhador.id} onChange={(e) => setTrabalhadorId(e.target.value)}>
            {colegas.map((t) => <option key={t.id} value={t.id}>{t.nome}</option>)}
          </select>
        </label>
      )}

      <div className="cartao">
        <div className="forte">{trabalhador.nome}</div>
        <div className="texto-suave">{nomesFuncoes(trabalhador, state.funcoes)} · Obra {obra.nome}</div>

        <div className="barra" role="progressbar" aria-valuenow={c.percentual} aria-valuemin={0} aria-valuemax={100}>
          <div className="barra__preenchida" style={{ width: `${c.percentual}%` }} />
        </div>
        <div className="texto-suave">{c.percentual}% de conformidade de EPI</div>

        <div className="lista-epis">
          {c.itens.map((i) => (
            <div key={i.epiId} className="linha-lista linha-lista--caixa">
              <span>{nomeEpi(state.epis, i.epiId)}</span>
              <span className={i.ok ? 'status-ok' : 'status-falta'}>{i.ok ? 'ok' : 'faltando'}</span>
            </div>
          ))}
        </div>
        <p className="texto-suave nota">
          EPIs exigidos = básicos da obra + obrigatórios de cada função (RN05).
        </p>
      </div>
    </div>
  )
}

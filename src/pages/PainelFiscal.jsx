import { useApp } from '../state/AppContext.jsx'
import { filtrarPorObra, indicadoresDaObra, podeVerPainel } from '../domain/regras.js'
import { nomeEpi } from '../utils.js'

export default function PainelFiscal() {
  const { state, usuario, obra } = useApp()

  // RN09 — defesa extra além do filtro de navegação.
  if (!podeVerPainel(usuario)) {
    return <div className="cartao vazio">Acesso restrito a usuários com permissão de Fiscal.</div>
  }

  // RN10 — indicadores apenas da obra do Fiscal.
  const ind = indicadoresDaObra(
    filtrarPorObra(state.ocorrencias, usuario),
    filtrarPorObra(state.trabalhadores, usuario),
    obra,
    state.funcoes,
  )

  const cards = [
    { rotulo: 'Total de ocorrências', valor: ind.total },
    { rotulo: 'Riscos identificados', valor: ind.contagem.RISCO },
    { rotulo: 'Quase-acidentes', valor: ind.contagem.QUASE_ACIDENTE },
    { rotulo: 'Acidentes', valor: ind.contagem.ACIDENTE },
    { rotulo: 'Trabalhadores conformes', valor: `${ind.percentualConformes}%` },
    { rotulo: 'EPI faltou', valor: ind.papeis.faltou },
    { rotulo: 'EPI falhou', valor: ind.papeis.falhou },
    { rotulo: 'EPI ajudou', valor: ind.papeis.ajudou },
  ]

  return (
    <div className="coluna coluna--larga">
      <div className="texto-suave cabecalho-secao">Painel de Indicadores · {obra.nome} (exclusivo Fiscal)</div>
      <div className="painel-grid">
        {cards.map((c) => (
          <div key={c.rotulo} className="cartao indicador">
            <div className="indicador__valor">{c.valor}</div>
            <div className="texto-suave">{c.rotulo}</div>
          </div>
        ))}
      </div>

      <div className="cartao">
        <div className="rotulo">Trabalhadores não conformes</div>
        {ind.naoConformes.length === 0 && <div className="texto-suave">Todos os trabalhadores estão conformes.</div>}
        {ind.naoConformes.map((c) => (
          <div key={c.trabalhador.id} className="linha-lista">
            <span>{c.trabalhador.nome}</span>
            <span className="texto-suave">falta: {c.faltando.map((id) => nomeEpi(state.epis, id)).join(', ')}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

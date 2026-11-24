import { useEffect, useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import { api } from '../api.js'
import { podeVerPainel } from '../domain/regras.js'
import { nomeEpi } from '../utils.js'

export default function PainelFiscal() {
  const { state, usuario, obra } = useApp()
  const ehFiscal = podeVerPainel(usuario)
  const [ind, setInd] = useState(null)
  const [erro, setErro] = useState('')

  // RN09/RN10 — os indicadores vêm do servidor, que só responde ao Fiscal e só com a obra dele.
  // Recarrega quando a lista de ocorrências muda (atualização a cada 30 s do AppContext).
  useEffect(() => {
    if (!ehFiscal) return
    let ativo = true
    api.painel()
      .then((dados) => { if (ativo) { setInd(dados); setErro('') } })
      .catch((e) => { if (ativo) setErro(e.message) })
    return () => { ativo = false }
  }, [ehFiscal, state.ocorrencias])

  if (!ehFiscal) {
    return <div className="cartao vazio">Acesso restrito a usuários com permissão de Fiscal.</div>
  }
  if (erro && !ind) return <div className="cartao vazio">{erro}</div>
  if (!ind) return <div className="cartao vazio">Carregando indicadores…</div>

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
          <div key={c.trabalhadorId} className="linha-lista">
            <span>{c.nome}</span>
            <span className="texto-suave">falta: {c.faltando.map((id) => nomeEpi(state.epis, id)).join(', ')}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import Checkbox from '../components/Checkbox.jsx'

export default function CadastroObra() {
  const { state, acoes, obra } = useApp()
  const [erro, setErro] = useState('')

  async function alternar(epiId) {
    setErro('')
    try {
      await acoes.definirEpiDaObra(obra.id, epiId, !obra.episBasicos.includes(epiId))
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <div className="coluna coluna--estreita">
      <div className="cartao">
        <div className="campo">
          <span className="rotulo">Nome da Obra</span>
          <div className="campo__leitura">{obra.nome}</div>
        </div>
        <div className="rotulo">EPIs básicos da obra (aplicam-se a todos os trabalhadores)</div>
        {state.epis.map((epi) => (
          <div key={epi.id} className="linha-lista">
            <Checkbox
              marcado={obra.episBasicos.includes(epi.id)}
              onChange={() => alternar(epi.id)}
            >
              {epi.nome}
            </Checkbox>
          </div>
        ))}
        {erro && <ul className="erros" role="alert"><li>{erro}</li></ul>}
      </div>
    </div>
  )
}

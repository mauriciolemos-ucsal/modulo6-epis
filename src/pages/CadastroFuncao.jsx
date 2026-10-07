import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import Chip from '../components/Chip.jsx'
import Checkbox from '../components/Checkbox.jsx'

export default function CadastroFuncao() {
  const { state, acoes } = useApp()
  const [erro, setErro] = useState('')
  const [funcaoId, setFuncaoId] = useState(state.funcoes[0].id)
  const funcao = state.funcoes.find((f) => f.id === funcaoId)

  async function alternar(epiId) {
    setErro('')
    try {
      await acoes.definirEpiDaFuncao(funcaoId, epiId, !funcao.episObrigatorios.includes(epiId))
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <div className="coluna coluna--estreita">
      <div className="cartao">
        <div className="campo">
          <span className="rotulo">Função</span>
          <div className="chips">
            {state.funcoes.map((f) => (
              <Chip key={f.id} selecionado={f.id === funcaoId} onClick={() => setFuncaoId(f.id)}>
                {f.nome}
              </Chip>
            ))}
          </div>
        </div>
        <div className="rotulo">EPIs obrigatórios para {funcao.nome}</div>
        {state.epis.map((epi) => (
          <div key={epi.id} className="linha-lista">
            <Checkbox
              marcado={funcao.episObrigatorios.includes(epi.id)}
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

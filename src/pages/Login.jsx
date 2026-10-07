import { useEffect, useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import { api } from '../api.js'

// Autenticação central está fora do escopo do módulo: aqui apenas simulamos a
// escolha de um trabalhador já cadastrado (RN01 — a obra vem do cadastro dele).
export default function Login() {
  const { acoes } = useApp()
  const [opcoes, setOpcoes] = useState(null)
  const [matricula, setMatricula] = useState('')
  const [erro, setErro] = useState('')
  const [entrando, setEntrando] = useState(false)

  useEffect(() => {
    api.opcoesLogin()
      .then((lista) => {
        setOpcoes(lista)
        setMatricula(lista[0]?.matricula ?? '')
      })
      .catch((e) => setErro(e.message))
  }, [])

  const selecionado = opcoes?.find((o) => o.matricula === matricula)

  async function entrar(e) {
    e.preventDefault()
    setErro('')
    setEntrando(true)
    try {
      await acoes.entrar(matricula)
    } catch (err) {
      setErro(err.message)
      setEntrando(false)
    }
  }

  return (
    <div className="login">
      <form className="cartao login__cartao" onSubmit={entrar}>
        <h1 className="login__titulo">Acessar obra</h1>
        <div className="texto-suave centro">SGF · Segurança do Trabalho</div>

        <label className="campo">
          <span className="rotulo">Matrícula (mock)</span>
          <select value={matricula} onChange={(e) => setMatricula(e.target.value)} disabled={!opcoes}>
            {(opcoes ?? []).map((o) => (
              <option key={o.matricula} value={o.matricula}>
                {o.matricula} — {o.nome}{o.fiscal ? ' (Fiscal)' : ''}
              </option>
            ))}
          </select>
        </label>

        <div className="campo">
          <span className="rotulo">Obra</span>
          <div className="campo__leitura">{selecionado?.obraNome ?? '—'}</div>
        </div>

        {erro && <ul className="erros" role="alert"><li>{erro}</li></ul>}

        <button type="submit" className="botao botao--primario" disabled={!selecionado || entrando}>
          {entrando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </div>
  )
}

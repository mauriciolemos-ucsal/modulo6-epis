import { useState } from 'react'
import { useApp } from '../state/AppContext.jsx'
import {
  PAPEIS_EPI, TIPOS_OCORRENCIA, permiteAssociarEpi, validarOcorrencia,
} from '../domain/regras.js'
import Chip from '../components/Chip.jsx'
import Checkbox from '../components/Checkbox.jsx'

// `foto` é a URL de pré-visualização; `arquivoFoto` é o arquivo enviado ao servidor.
const RASCUNHO_VAZIO = { tipo: 'RISCO', descricao: '', foto: null, arquivoFoto: null, epis: [] }

// `emNomeDeOutro` habilita a escolha do autor (exclusivo do Fiscal).
export default function RegistrarOcorrencia({ emNomeDeOutro = false }) {
  const { state, acoes, usuario } = useApp()
  const [rascunho, setRascunho] = useState(RASCUNHO_VAZIO)
  const [autorId, setAutorId] = useState(usuario.id)
  const [erros, setErros] = useState([])
  const [enviando, setEnviando] = useState(false)

  const colegas = state.trabalhadores.filter((t) => t.obraId === usuario.obraId)
  const autor = state.trabalhadores.find((t) => t.id === (emNomeDeOutro ? autorId : usuario.id))
  const mostrarEpis = permiteAssociarEpi(rascunho.tipo)

  const atualizar = (campos) => setRascunho((r) => ({ ...r, ...campos }))

  function escolherTipo(tipo) {
    // RN07 — ao voltar para Risco Identificado, a associação de EPIs é descartada.
    atualizar({ tipo, epis: permiteAssociarEpi(tipo) ? rascunho.epis : [] })
  }

  function alternarEpi(epiId) {
    const marcado = rascunho.epis.some((e) => e.epiId === epiId)
    atualizar({
      epis: marcado
        ? rascunho.epis.filter((e) => e.epiId !== epiId)
        : [...rascunho.epis, { epiId, papel: null }],
    })
  }

  function escolherPapel(epiId, papel) {
    atualizar({ epis: rascunho.epis.map((e) => (e.epiId === epiId ? { ...e, papel } : e)) })
  }

  function escolherFoto(e) {
    const arquivo = e.target.files?.[0]
    if (!arquivo) return
    if (rascunho.foto) URL.revokeObjectURL(rascunho.foto)
    atualizar({ foto: URL.createObjectURL(arquivo), arquivoFoto: arquivo })
  }

  async function enviar(e) {
    e.preventDefault()
    const problemas = validarOcorrencia(rascunho, { usuario, autor })
    setErros(problemas)
    if (problemas.length) return
    setEnviando(true)
    try {
      await acoes.registrarOcorrencia({ rascunho, autorId: autor.id })
    } catch (err) {
      // O servidor revalida tudo (RN06, RN07, RN09, RN10) e devolve as mensagens.
      setErros(err.erros ?? [err.message])
      setEnviando(false)
    }
  }

  return (
    <div className="coluna coluna--estreita">
      <form className="cartao formulario" onSubmit={enviar} noValidate>
        {emNomeDeOutro && (
          <fieldset className="campo">
            <legend className="rotulo">Registrar em nome de</legend>
            <div className="chips">
              {colegas.map((t) => (
                <Chip key={t.id} selecionado={autorId === t.id} onClick={() => setAutorId(t.id)}>
                  {t.nome}
                </Chip>
              ))}
            </div>
          </fieldset>
        )}

        <fieldset className="campo">
          <legend className="rotulo">Classificação *</legend>
          <div className="chips chips--iguais">
            {Object.entries(TIPOS_OCORRENCIA).map(([id, rotulo]) => (
              <Chip key={id} selecionado={rascunho.tipo === id} onClick={() => escolherTipo(id)}>
                {rotulo}
              </Chip>
            ))}
          </div>
        </fieldset>

        <div className="campo">
          <span className="rotulo">Foto *</span>
          <label className="upload">
            {rascunho.foto
              ? <img src={rascunho.foto} alt="Pré-visualização da foto" />
              : <span>📷 Tirar / anexar foto</span>}
            <input type="file" accept="image/*" capture="environment" onChange={escolherFoto} />
          </label>
        </div>

        <label className="campo">
          <span className="rotulo">Descrição</span>
          <textarea
            value={rascunho.descricao}
            onChange={(e) => atualizar({ descricao: e.target.value })}
            placeholder="Descreva o que aconteceu…"
            rows={3}
          />
        </label>

        {mostrarEpis && (
          <fieldset className="campo">
            <legend className="rotulo">EPI(s) envolvido(s) — opcional</legend>
            {state.epis.map((epi) => {
              const sel = rascunho.epis.find((e) => e.epiId === epi.id)
              return (
                <div key={epi.id} className="epi-linha">
                  <Checkbox marcado={Boolean(sel)} onChange={() => alternarEpi(epi.id)}>
                    {epi.nome}
                  </Checkbox>
                  {sel && (
                    <div className="chips epi-linha__papeis">
                      {PAPEIS_EPI.map((p) => (
                        <Chip key={p} pequeno selecionado={sel.papel === p} onClick={() => escolherPapel(epi.id, p)}>
                          {p}
                        </Chip>
                      ))}
                    </div>
                  )}
                </div>
              )
            })}
          </fieldset>
        )}

        {erros.length > 0 && (
          <ul className="erros" role="alert">
            {erros.map((m) => <li key={m}>{m}</li>)}
          </ul>
        )}

        <button type="submit" className="botao botao--primario" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Registrar Ocorrência'}
        </button>
      </form>
    </div>
  )
}

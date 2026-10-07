import { TIPOS_OCORRENCIA } from '../domain/regras.js'

export default function TipoBadge({ tipo }) {
  return <span className={`badge badge--${tipo.toLowerCase()}`}>{TIPOS_OCORRENCIA[tipo]}</span>
}

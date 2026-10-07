// Botão de seleção (classificação, papel do EPI, função, trabalhador).
export default function Chip({ selecionado, onClick, children, pequeno = false }) {
  return (
    <button
      type="button"
      className={`chip${selecionado ? ' chip--ativo' : ''}${pequeno ? ' chip--pequeno' : ''}`}
      aria-pressed={selecionado}
      onClick={onClick}
    >
      {children}
    </button>
  )
}

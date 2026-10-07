// Exibe a foto da ocorrência ou um placeholder (ocorrências mockadas não têm foto).
export default function Foto({ src, alt, className = '' }) {
  if (!src) return <div className={`foto foto--vazia ${className}`}>sem foto</div>
  return <img className={`foto ${className}`} src={src} alt={alt} />
}

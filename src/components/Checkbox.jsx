export default function Checkbox({ marcado, onChange, children }) {
  return (
    <label className="checkbox">
      <input type="checkbox" checked={marcado} onChange={onChange} />
      <span>{children}</span>
    </label>
  )
}

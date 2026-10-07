export function formatarDataHora(iso) {
  const d = new Date(iso)
  return d.toLocaleString('pt-BR', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

export function nomeEpi(epis, id) {
  return epis.find((e) => e.id === id)?.nome ?? id
}

export function nomesFuncoes(trabalhador, funcoes) {
  return trabalhador.funcoes
    .map((id) => funcoes.find((f) => f.id === id)?.nome ?? id)
    .join(', ')
}

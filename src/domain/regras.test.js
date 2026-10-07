import { describe, it, expect } from 'vitest'
import {
  permiteAssociarEpi, episExigidos, calcularConformidade, podeVerPainel,
  podeRegistrarEmNomeDe, filtrarPorObra, ordenarRecentesPrimeiro, filtrarOcorrencias,
  validarOcorrencia, criarOcorrencia, gerarNotificacoes, indicadoresDaObra,
} from './regras.js'

const funcoes = [
  { id: 'pedreiro', episObrigatorios: ['luva', 'oculos'] },
  { id: 'soldador', episObrigatorios: ['mascara', 'oculos'] },
]
const obra = { id: 'alfa', episBasicos: ['capacete', 'bota'] }
const carlos = { id: 't1', obraId: 'alfa', funcoes: ['pedreiro'], fiscal: false, episEmUso: ['capacete', 'bota', 'luva'] }
const maria = { id: 't2', obraId: 'alfa', funcoes: ['pedreiro', 'soldador'], fiscal: true, episEmUso: ['capacete', 'bota', 'luva', 'oculos', 'mascara'] }
const paulo = { id: 't3', obraId: 'beta', funcoes: [], fiscal: false, episEmUso: [] }

describe('RN05 — EPIs exigidos', () => {
  it('une EPIs básicos da obra com os de todas as funções, sem duplicar', () => {
    expect(episExigidos(maria, obra, funcoes).sort())
      .toEqual(['bota', 'capacete', 'luva', 'mascara', 'oculos'])
  })

  it('calcula percentual e EPIs faltando', () => {
    const c = calcularConformidade(carlos, obra, funcoes)
    expect(c.faltando).toEqual(['oculos'])
    expect(c.percentual).toBe(75)
    expect(c.conforme).toBe(false)
    expect(calcularConformidade(maria, obra, funcoes).conforme).toBe(true)
  })
})

describe('RN07 — papel do EPI', () => {
  it('só Quase-Acidente e Acidente permitem associação', () => {
    expect(permiteAssociarEpi('RISCO')).toBe(false)
    expect(permiteAssociarEpi('QUASE_ACIDENTE')).toBe(true)
    expect(permiteAssociarEpi('ACIDENTE')).toBe(true)
  })

  it('descarta EPIs ao criar um Risco Identificado', () => {
    const o = criarOcorrencia(
      { tipo: 'RISCO', foto: 'x', epis: [{ epiId: 'luva', papel: 'faltou' }] },
      { id: 1, usuario: carlos, autor: carlos },
    )
    expect(o.epis).toEqual([])
  })
})

describe('Permissões de Fiscal (RN09, registro em nome de outro)', () => {
  it('só Fiscal vê o painel', () => {
    expect(podeVerPainel(carlos)).toBe(false)
    expect(podeVerPainel(maria)).toBe(true)
  })

  it('Fiscal registra em nome de colega da mesma obra, não de outra', () => {
    expect(podeRegistrarEmNomeDe(maria, carlos)).toBe(true)
    expect(podeRegistrarEmNomeDe(maria, paulo)).toBe(false)
    expect(podeRegistrarEmNomeDe(carlos, maria)).toBe(false)
    expect(podeRegistrarEmNomeDe(carlos, carlos)).toBe(true)
  })
})

describe('RN10 — isolamento por obra', () => {
  it('filtra itens pela obra do usuário', () => {
    const itens = [{ obraId: 'alfa' }, { obraId: 'beta' }]
    expect(filtrarPorObra(itens, maria)).toEqual([{ obraId: 'alfa' }])
  })
})

describe('Validação da ocorrência', () => {
  it('RN06 — exige classificação e foto', () => {
    const erros = validarOcorrencia({ tipo: '', foto: null }, { usuario: carlos, autor: carlos })
    expect(erros).toHaveLength(2)
  })

  it('exige papel para cada EPI marcado', () => {
    const erros = validarOcorrencia(
      { tipo: 'ACIDENTE', foto: 'x', epis: [{ epiId: 'luva', papel: null }] },
      { usuario: carlos, autor: carlos },
    )
    expect(erros).toHaveLength(1)
  })

  it('aceita rascunho válido', () => {
    expect(validarOcorrencia(
      { tipo: 'ACIDENTE', foto: 'x', epis: [{ epiId: 'luva', papel: 'faltou' }] },
      { usuario: maria, autor: carlos },
    )).toEqual([])
  })
})

describe('Feed e notificações', () => {
  it('ordena mais recentes primeiro', () => {
    const ordenadas = ordenarRecentesPrimeiro([
      { id: 1, dataHora: '2026-09-01T10:00:00' },
      { id: 2, dataHora: '2026-09-03T10:00:00' },
      { id: 3, dataHora: '2026-09-02T10:00:00' },
    ])
    expect(ordenadas.map((o) => o.id)).toEqual([2, 3, 1])
  })

  it('filtra por período e por tipo', () => {
    const agora = new Date('2026-09-10T12:00:00')
    const itens = [
      { id: 1, tipo: 'RISCO', dataHora: '2026-09-10T08:00:00' },
      { id: 2, tipo: 'ACIDENTE', dataHora: '2026-09-05T08:00:00' },
      { id: 3, tipo: 'ACIDENTE', dataHora: '2026-08-01T08:00:00' },
    ]
    const ids = (f) => filtrarOcorrencias(itens, f, agora).map((o) => o.id)
    expect(ids({})).toEqual([1, 2, 3])
    expect(ids({ periodo: 'HOJE' })).toEqual([1])
    expect(ids({ periodo: 'SETE_DIAS' })).toEqual([1, 2])
    expect(ids({ periodo: 'TRINTA_DIAS', tipo: 'ACIDENTE' })).toEqual([2])
    expect(ids({ tipo: 'ACIDENTE' })).toEqual([2, 3])
  })

  it('RN08 — notifica todos os trabalhadores da obra, e só eles', () => {
    const notifs = gerarNotificacoes({ id: 9, obraId: 'alfa' }, [carlos, maria, paulo], 100)
    expect(notifs.map((n) => n.destinatarioId)).toEqual(['t1', 't2'])
    expect(notifs.map((n) => n.id)).toEqual([100, 101])
  })
})

describe('Indicadores do painel', () => {
  it('conta ocorrências por tipo e lista não conformes', () => {
    const ind = indicadoresDaObra(
      [
        { tipo: 'RISCO', epis: [] },
        { tipo: 'ACIDENTE', epis: [{ epiId: 'luva', papel: 'faltou' }] },
      ],
      [carlos, maria], obra, funcoes,
    )
    expect(ind.total).toBe(2)
    expect(ind.contagem).toEqual({ RISCO: 1, QUASE_ACIDENTE: 0, ACIDENTE: 1 })
    expect(ind.papeis.faltou).toBe(1)
    expect(ind.percentualConformes).toBe(50)
    expect(ind.naoConformes.map((c) => c.trabalhador.id)).toEqual(['t1'])
  })
})

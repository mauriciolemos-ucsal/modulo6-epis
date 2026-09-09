# SGF — Módulo Segurança do Trabalho

Módulo vertical do Sistema de Gestão e Fiscalização de Obras (SGF), focado em registro de riscos, quase-acidentes e acidentes em canteiros de obra, com controle de conformidade de EPIs.

## Contexto Acadêmico

- **Disciplina:** Engenharia de Processos de Software
- **Semestre:** 2026.2
- **Professor:** Eng. Me. Rafael Bispo
- **Caso Integrador:** SGF (Sistema de Gestão e Fiscalização de Obras)
- **Equipe:** 6 — Segurança do Trabalho

## Sobre o Módulo

Este módulo permite que trabalhadores de uma obra registrem, via celular, ocorrências de segurança (riscos identificados, quase-acidentes e acidentes) com foto, associando-as opcionalmente a falhas ou acertos no uso de EPIs. O sistema notifica os demais trabalhadores da obra a cada nova ocorrência e oferece, a usuários com permissão de Fiscal, um painel de indicadores agregados sobre a segurança do canteiro.

Detalhes completos de escopo, regras de negócio e personas estão em [`VISAO_ESCOPO.md`](./VISAO_ESCOPO.md).

## Status Atual

**Unidade 1 — Regime Agile-Driven** (em andamento)

Fase de validação de interface e fluxos com dados mockados, sem persistência real. A reconstrução com backend relacional (MER/DER, normalização, banco real) está planejada para a Unidade 2, conforme as diretrizes da disciplina.

## Stack Tecnológica

**Unidade 1 (atual):**
- React + Vite
- Dados mockados (sem backend / sem banco de dados)

**Unidade 2 (planejado):**
- Backend relacional a definir (banco SQL, normalização até 3FN)

## Como Rodar o Projeto

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd <pasta-do-projeto>

# Instalar dependências
npm install

# Rodar em modo desenvolvimento
npm run dev
```

## Estrutura de Pastas

```
/src
  /components   # componentes reutilizáveis de UI
  /pages        # telas (Feed, Registro de Ocorrência, Painel do Fiscal)
  /data         # dados mockados (obras, funções, EPIs, ocorrências)
  /assets       # imagens e ícones
README.md
VISAO_ESCOPO.md
```

> Estrutura sujeita a ajustes conforme o desenvolvimento avança.

## Gestão do Projeto

O acompanhamento das tarefas é feito via GitHub Projects (Kanban), com colunas Backlog, Todo, In Progress e Done. Toda funcionalidade é rastreada por uma Issue correspondente.

- [Board Kanban](#) <!-- inserir link do Project -->
- [Issues](#) <!-- inserir link das Issues -->

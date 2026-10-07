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

Detalhes completos de escopo, regras de negócio e personas estão em [`docs/VISAO_ESCOPO.md`](./docs/VISAO_ESCOPO.md).

## Status Atual

**Concluído** — versão final do módulo

O módulo roda com **backend Java (Spring Boot) + MySQL**, tudo local, e as fotos são gravadas em disco. O esquema relacional está em `backend/src/main/resources/db/migration/`.

## Stack Tecnológica

- **Front-end:** React + Vite
- **Backend:** Java (bytecode 21), Spring Boot 3.5, Spring JDBC, Flyway
- **Banco:** MySQL 8.4 local (schema e dados iniciais versionados com Flyway)
- **Fotos:** pasta `C:\Users\mauri\OneDrive\Documentos\fotos sfg-seguranca` (configurável, ver abaixo)

## Como Rodar o Projeto

São três processos, cada um em seu terminal (PowerShell), a partir da raiz do projeto:

```powershell
# 1) MySQL local (na 1ª vez cria o diretório de dados, o banco `sgf` e o usuário `sgf`)
powershell -ExecutionPolicy Bypass -File scripts\mysql-iniciar.ps1

# 2) Backend em http://localhost:8080 (as migrations do Flyway rodam sozinhas)
powershell -ExecutionPolicy Bypass -File scripts\backend-iniciar.ps1

# 3) Front-end em http://localhost:5173 (o Vite encaminha /api para o backend)
npm install
npm run dev
```

Dados de demonstração (opcional, rode uma vez): `.\mysql.exe -usgf -psgf sgf --default-character-set=utf8mb4 < scripts\dados-demo.sql`, a partir da pasta `bin` do MySQL. Adiciona a obra Condomínio Gama, 20 trabalhadores, mais funções e EPIs e 84 ocorrências dos últimos dois meses.

Outros comandos: `npm test` (regras no front), `npm run build` e, na pasta `backend`, `mvn test` (regras no servidor).

O MySQL portátil fica em `%USERPROFILE%\tools\mysql-8.4.9-winx64` (dados em `%USERPROFILE%\tools\mysql-dados-sgf`) e o Maven em `%USERPROFILE%\tools\apache-maven-3.9.16`. Configurações podem ser trocadas por variáveis de ambiente: `SGF_DB_URL`, `SGF_DB_USUARIO`, `SGF_DB_SENHA` e `SGF_FOTOS_DIR`.

Na tela de login, escolha um trabalhador cadastrado (login por matrícula, sem senha). Usuários com permissão de **Fiscal**: Maria Lima (Residencial Alfa) e Ana Ribeiro (Edifício Beta). Reiniciar o backend encerra as sessões.

## Estrutura de Pastas

```
/src
  /components   # componentes reutilizáveis de UI (Layout, Notificações, Chip…)
  /pages        # telas (Login, Feed, Registrar, Detalhe, Conformidade, Obra, Função, Painel)
  /domain       # regras de negócio (RN01–RN10) em funções puras + testes
  /state        # estado global (Context + useReducer) alimentado pela API
  api.js        # cliente HTTP do backend
  /assets       # imagens e ícones
/backend        # Spring Boot em camadas + migrations do Flyway (resources/db/migration)
  /model        #   entidades (records) e regras de negócio puras (Regras)
  /repository   #   acesso ao MySQL (JDBC) e às fotos em disco
  /service      #   fluxos, validações, permissões e transações
  /controller   #   API REST (entrada/saída HTTP, autenticação e tratamento de erros)
/scripts        # inicialização do MySQL e do backend
/docs           # Visão de Escopo (MD e PDF)
/mock_prototipo # protótipo de interface usado como referência
```

## Rastreabilidade das Regras de Negócio

| Regra | Onde está implementada |
|---|---|
| RN01, RN02 | tabelas `trabalhador` e `trabalhador_funcao` (`obra_id` obrigatório) |
| RN03, RN04 | tabelas `obra_epi` e `funcao_epi`; telas Cadastro de Obra / Função |
| RN05 | `episExigidos` / `calcularConformidade` em `src/domain/regras.js` |
| RN06 | `validarOcorrencia` no front e em `backend/.../model/Regras.java` (garantida no servidor) |
| RN07 | `permiteAssociarEpi`; no servidor, EPIs de Risco Identificado são descartados |
| RN08 | `Repositorio.gerarNotificacoes` (tabela `notificacao`); sino no topo, atualizado a cada 30 s |
| RN09 | `podeVerPainel`; menu e tela do Painel |
| RN10 | o servidor só devolve dados da obra do usuário (`GET /api/dados`) |

## Decisões e Suposições

Pontos não definidos explicitamente na Visão de Escopo e as decisões tomadas:

- **EPIs em uso por trabalhador:** para calcular conformidade, cada trabalhador tem EPIs em uso na tabela `trabalhador_epi_uso`, preenchida pela carga inicial. A origem real desse dado não está definida no escopo.
- **Foto obrigatória:** a seção 5 lista a foto junto com a classificação; a descrição é opcional.
- **Papel do EPI obrigatório quando o EPI é marcado:** marcar um EPI sem escolher faltou/falhou/ajudou bloqueia o envio.
- **Notificações:** seguindo a RN08 literalmente, o próprio autor também recebe a notificação.
- **Cadastro de Obra/Função:** acessível a qualquer usuário, como no protótipo (o escopo não restringe).
- **Login:** apenas por matrícula, sem senha (autenticação central está fora do escopo); sessão em memória no backend.
- **Fotos:** gravadas com nome aleatório (UUID) na pasta configurada e servidas por `/api/fotos/{nome}` sem exigir login, pois a tag `<img>` não envia o cabeçalho de autenticação. Só JPG, PNG, GIF e WebP até 10 MB.
- **Painel (RN09):** os indicadores ainda são calculados no front; o servidor não tem endpoint próprio de painel, então a restrição ao Fiscal é só de interface.

## Gestão do Projeto

O acompanhamento das tarefas é feito via GitHub Projects (Kanban), com colunas Backlog, Todo, In Progress e Done. Toda funcionalidade é rastreada por uma Issue correspondente.

- [Board Kanban](#) <!-- inserir link do Project -->
- [Issues](#) <!-- inserir link das Issues -->

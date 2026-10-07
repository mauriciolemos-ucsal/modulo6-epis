# Visão de Escopo — Módulo de Segurança do Trabalho

**Sistema:** SGF — Sistema de Gestão e Fiscalização de Obras
**Módulo:** 6 — Segurança do Trabalho
**Disciplina:** Engenharia de Processos de Software — Semestre 2026.2
**Professor:** Eng. Me. Rafael Bispo
**Status:** Concluído — versão final com backend (Java / Spring Boot) e banco relacional (MySQL)

---

## 1. Introdução e Objetivo do Módulo

O módulo de Segurança do Trabalho é uma das 20 verticais de negócio que compõem o SGF, sistema de gestão e fiscalização voltado a canteiros de obra da construção civil. Este módulo tem como objetivo permitir que os próprios trabalhadores de uma obra registrem, em tempo real e diretamente do celular, situações de risco, quase-acidentes e acidentes, associando esses registros ao uso (ou ausência) de Equipamentos de Proteção Individual (EPIs). O sistema deve, a partir desses registros, alertar os demais trabalhadores da obra e fornecer, aos fiscais, uma visão consolidada da situação de segurança do canteiro.

## 2. Problema que Resolve

Em canteiros de obra, o registro de riscos e incidentes normalmente é informal, verbal ou tardio (papel, memória, comunicação de boca em boca), o que dificulta a rastreabilidade de causas, a resposta rápida a perigos ativos e a fiscalização da conformidade no uso de EPIs. O módulo propõe digitalizar esse fluxo, tornando o registro rápido (feito por qualquer trabalhador, com foto) e dando visibilidade imediata tanto para os colegas de obra quanto para a fiscalização.

## 3. Escopo

### 3.1 Dentro do escopo (entregue)
- Registro de Ocorrência (Risco Identificado, Quase-Acidente ou Acidente) com foto real, gravada em disco no servidor.
- Associação opcional da Ocorrência a um ou mais EPIs, indicando o papel do EPI no evento (faltou / falhou / ajudou).
- Definição de EPIs básicos por Obra (configurados na criação da obra).
- Definição de EPIs obrigatórios por Função (ex.: soldador, eletricista).
- Cálculo de conformidade de EPI por trabalhador (EPIs básicos da obra + EPIs obrigatórios da(s) função(ões) do trabalhador).
- Feed cronológico de Ocorrências, visível a todos os trabalhadores da obra, mais recentes primeiro, com filtros por período (hoje, 7 dias, 30 dias) e por tipo de ocorrência.
- Painel de indicadores agregados (contagem de ocorrências, % de conformidade, etc.), visível apenas ao Fiscal.
- Notificações internas (estilo "sino") a cada nova Ocorrência registrada, para todos os trabalhadores da obra.
- Papel de Fiscal: mesmo tipo de usuário que o Trabalhador comum, com permissão adicional de registrar Ocorrência em nome de outro trabalhador da mesma obra.
- Persistência real em banco relacional MySQL, acessada por um backend em Java (Spring Boot) com API REST; as regras de negócio são validadas no servidor.

### 3.2 Fora do escopo (neste módulo)
- Controle de estoque/quantidade de EPIs no almoxarifado (requisição, entrada/saída) — pertence ao Módulo 2 (Almoxarifado e Canteiro).
- Autenticação central / gestão de usuários do sistema como um todo (assume-se que já existe fora deste módulo).

## 4. Personas / Usuários

| Persona | Descrição | Permissões |
|---|---|---|
| Trabalhador | Qualquer funcionário alocado a uma obra e a uma ou mais funções | Registra ocorrência própria; visualiza feed e notificações da obra |
| Fiscal | Trabalhador com permissão adicional de fiscalização | Tudo do Trabalhador + registra ocorrência em nome de colegas da mesma obra + acessa painel de indicadores agregados |

*Observação: Fiscal não é uma entidade separada — é o mesmo cadastro de Trabalhador com uma permissão adicional.*

## 5. Principais Funcionalidades

1. Registrar Ocorrência (com classificação, foto e, opcionalmente, EPI(s) envolvido(s) e seu papel no evento).
2. Visualizar feed de Ocorrências da obra.
3. Receber notificação a cada nova Ocorrência.
4. Configurar EPIs básicos de uma Obra (no cadastro da obra).
5. Configurar EPIs obrigatórios de uma Função.
6. Consultar conformidade de EPI de um trabalhador (básicos da obra + obrigatórios da função).
7. Visualizar painel de indicadores agregados (exclusivo do Fiscal).
8. Registrar Ocorrência em nome de outro trabalhador (exclusivo do Fiscal).

## 6. Regras de Negócio

- RN01 — Todo trabalhador está vinculado a exatamente uma Obra.
- RN02 — Um trabalhador pode acumular mais de uma Função simultaneamente.
- RN03 — Toda Obra possui um conjunto de EPIs básicos, definidos em sua criação.
- RN04 — Toda Função possui um conjunto de EPIs obrigatórios específicos.
- RN05 — A conformidade de EPI de um trabalhador é dada pela união dos EPIs básicos da sua Obra com os EPIs obrigatórios de todas as suas Funções.
- RN06 — Toda Ocorrência possui uma classificação obrigatória: Risco Identificado, Quase-Acidente ou Acidente.
- RN07 — Apenas Ocorrências do tipo Quase-Acidente ou Acidente permitem (fazem sentido) a associação com o papel do EPI (faltou/falhou/ajudou); Risco Identificado não exige essa associação.
- RN08 — Toda nova Ocorrência gera uma notificação para todos os trabalhadores da obra correspondente.
- RN09 — O painel de indicadores agregados é visível apenas para usuários com permissão de Fiscal.
- RN10 — Um Fiscal só visualiza dados (feed, painel, ocorrências) da(s) obra(s) à(s) qual(is) está alocado.

## 7. Arquitetura e Restrições Técnicas

A solução é composta por três partes, todas executadas localmente:

- **Front-end:** React + Vite, que consome a API do backend.
- **Backend:** Java (Spring Boot, Spring JDBC), com API REST. Valida no servidor as regras RN06 (classificação e foto obrigatórias), RN07 (papel do EPI só em Quase-Acidente e Acidente), RN08 (geração das notificações), RN09 e RN10 (permissão do Fiscal e isolamento por obra para registro e consulta de ocorrências).
- **Banco de dados:** MySQL 8.4, com esquema relacional e carga inicial versionados por migrations (Flyway) em `backend/src/main/resources/db/migration/`. As tabelas usam chaves estrangeiras para garantir a integridade referencial.
- **Fotos:** gravadas em uma pasta do disco local (configurável), com nome aleatório; apenas o nome do arquivo é guardado no banco. São aceitos JPG, PNG, GIF e WebP de até 10 MB.

Decisões e limitações conhecidas:
- **Autenticação:** a gestão central de usuários está fora do escopo; o acesso é por matrícula, sem senha, com sessão em memória no backend.
- **Acesso às fotos:** as fotos são servidas sem exigir login, e o endereço de cada uma é um identificador aleatório.
- **Painel de indicadores:** os indicadores são calculados no front-end; a restrição da RN09 ao Fiscal é aplicada na interface, e o servidor ainda não possui um endpoint próprio para o painel.
- **EPIs em uso por trabalhador:** a origem real desse dado, necessário ao cálculo de conformidade, não está definida neste escopo e vem da carga inicial do banco.
- **Notificações:** são registradas no banco e exibidas no sino da interface, que se atualiza periodicamente; não há envio por push, e-mail ou outro canal externo.

## 8. Glossário

| Termo | Definição |
|---|---|
| EPI | Equipamento de Proteção Individual (ex.: capacete, luva, jaqueta refletiva) |
| Ocorrência | Registro de um evento de segurança: Risco Identificado, Quase-Acidente ou Acidente |
| Quase-Acidente | Evento que teve potencial de causar dano, mas não causou |
| Conformidade de EPI | Situação em que um trabalhador possui/usa todos os EPIs exigidos para sua obra e função(ões) |
| Fiscal | Trabalhador com permissão adicional de supervisão e registro em nome de terceiros |

---

*Documento atualizado para a versão final do módulo (Equipe 6).*

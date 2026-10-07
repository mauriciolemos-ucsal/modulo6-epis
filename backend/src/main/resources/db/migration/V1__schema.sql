CREATE TABLE epi (
  id   VARCHAR(40)  NOT NULL PRIMARY KEY,
  nome VARCHAR(100) NOT NULL
);

CREATE TABLE funcao (
  id   VARCHAR(40)  NOT NULL PRIMARY KEY,
  nome VARCHAR(100) NOT NULL
);

-- RN04 — EPIs obrigatórios de cada função.
CREATE TABLE funcao_epi (
  funcao_id VARCHAR(40) NOT NULL,
  epi_id    VARCHAR(40) NOT NULL,
  PRIMARY KEY (funcao_id, epi_id),
  FOREIGN KEY (funcao_id) REFERENCES funcao (id),
  FOREIGN KEY (epi_id)    REFERENCES epi (id)
);

CREATE TABLE obra (
  id   VARCHAR(40)  NOT NULL PRIMARY KEY,
  nome VARCHAR(100) NOT NULL
);

-- RN03 — EPIs básicos de cada obra.
CREATE TABLE obra_epi (
  obra_id VARCHAR(40) NOT NULL,
  epi_id  VARCHAR(40) NOT NULL,
  PRIMARY KEY (obra_id, epi_id),
  FOREIGN KEY (obra_id) REFERENCES obra (id),
  FOREIGN KEY (epi_id)  REFERENCES epi (id)
);

-- RN01 — um trabalhador pertence a exatamente uma obra. Fiscal = permissão do mesmo cadastro.
CREATE TABLE trabalhador (
  id        VARCHAR(40)  NOT NULL PRIMARY KEY,
  matricula VARCHAR(20)  NOT NULL UNIQUE,
  nome      VARCHAR(100) NOT NULL,
  obra_id   VARCHAR(40)  NOT NULL,
  fiscal    BOOLEAN      NOT NULL DEFAULT FALSE,
  FOREIGN KEY (obra_id) REFERENCES obra (id)
);

-- RN02 — um trabalhador pode acumular funções.
CREATE TABLE trabalhador_funcao (
  trabalhador_id VARCHAR(40) NOT NULL,
  funcao_id      VARCHAR(40) NOT NULL,
  PRIMARY KEY (trabalhador_id, funcao_id),
  FOREIGN KEY (trabalhador_id) REFERENCES trabalhador (id),
  FOREIGN KEY (funcao_id)      REFERENCES funcao (id)
);

-- EPIs que o trabalhador possui/usa hoje (origem real ainda indefinida na Visão de Escopo).
CREATE TABLE trabalhador_epi_uso (
  trabalhador_id VARCHAR(40) NOT NULL,
  epi_id         VARCHAR(40) NOT NULL,
  PRIMARY KEY (trabalhador_id, epi_id),
  FOREIGN KEY (trabalhador_id) REFERENCES trabalhador (id),
  FOREIGN KEY (epi_id)         REFERENCES epi (id)
);

CREATE TABLE ocorrencia (
  id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  obra_id           VARCHAR(40)  NOT NULL,
  tipo              ENUM('RISCO', 'QUASE_ACIDENTE', 'ACIDENTE') NOT NULL,
  autor_id          VARCHAR(40)  NOT NULL,
  registrado_por_id VARCHAR(40)  NOT NULL,
  data_hora         DATETIME     NOT NULL,
  descricao         TEXT         NOT NULL,
  foto              VARCHAR(100) NULL,  -- nome do arquivo na pasta de fotos
  FOREIGN KEY (obra_id)           REFERENCES obra (id),
  FOREIGN KEY (autor_id)          REFERENCES trabalhador (id),
  FOREIGN KEY (registrado_por_id) REFERENCES trabalhador (id),
  INDEX idx_ocorrencia_obra_data (obra_id, data_hora)
);

-- RN07 — papel do EPI (só em Quase-Acidente e Acidente; garantido na aplicação).
CREATE TABLE ocorrencia_epi (
  ocorrencia_id BIGINT      NOT NULL,
  epi_id        VARCHAR(40) NOT NULL,
  papel         ENUM('faltou', 'falhou', 'ajudou') NOT NULL,
  PRIMARY KEY (ocorrencia_id, epi_id),
  FOREIGN KEY (ocorrencia_id) REFERENCES ocorrencia (id) ON DELETE CASCADE,
  FOREIGN KEY (epi_id)        REFERENCES epi (id)
);

-- RN08 — notificação interna, uma por trabalhador da obra.
CREATE TABLE notificacao (
  id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  destinatario_id VARCHAR(40) NOT NULL,
  ocorrencia_id   BIGINT      NOT NULL,
  lida            BOOLEAN     NOT NULL DEFAULT FALSE,
  FOREIGN KEY (destinatario_id) REFERENCES trabalhador (id),
  FOREIGN KEY (ocorrencia_id)   REFERENCES ocorrencia (id) ON DELETE CASCADE,
  INDEX idx_notificacao_destinatario (destinatario_id)
);

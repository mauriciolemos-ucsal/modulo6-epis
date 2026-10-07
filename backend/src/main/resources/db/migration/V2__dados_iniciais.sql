-- Dados iniciais (antes mockados no front-end).
INSERT INTO epi (id, nome) VALUES
  ('capacete', 'Capacete'),
  ('luva', 'Luva'),
  ('bota', 'Bota de segurança'),
  ('oculos', 'Óculos de proteção'),
  ('cinto', 'Cinto de segurança'),
  ('mascara', 'Máscara respiratória'),
  ('protetor_auricular', 'Protetor auricular'),
  ('jaqueta', 'Jaqueta refletiva');

INSERT INTO funcao (id, nome) VALUES
  ('pedreiro', 'Pedreiro'),
  ('eletricista', 'Eletricista'),
  ('soldador', 'Soldador');

INSERT INTO funcao_epi (funcao_id, epi_id) VALUES
  ('pedreiro', 'luva'), ('pedreiro', 'oculos'),
  ('eletricista', 'luva'), ('eletricista', 'cinto'), ('eletricista', 'oculos'),
  ('soldador', 'mascara'), ('soldador', 'oculos'), ('soldador', 'jaqueta');

INSERT INTO obra (id, nome) VALUES
  ('alfa', 'Residencial Alfa'),
  ('beta', 'Edifício Beta');

INSERT INTO obra_epi (obra_id, epi_id) VALUES
  ('alfa', 'capacete'), ('alfa', 'bota'), ('alfa', 'jaqueta'),
  ('beta', 'capacete'), ('beta', 'bota'), ('beta', 'protetor_auricular');

INSERT INTO trabalhador (id, matricula, nome, obra_id, fiscal) VALUES
  ('t1', '000123', 'Carlos Souza', 'alfa', FALSE),
  ('t2', '000124', 'João Pedro', 'alfa', FALSE),
  ('t3', '000125', 'Maria Lima', 'alfa', TRUE),
  ('t4', '000201', 'Ana Ribeiro', 'beta', TRUE),
  ('t5', '000202', 'Paulo Mendes', 'beta', FALSE);

INSERT INTO trabalhador_funcao (trabalhador_id, funcao_id) VALUES
  ('t1', 'pedreiro'), ('t2', 'pedreiro'),
  ('t3', 'eletricista'), ('t3', 'soldador'),
  ('t4', 'eletricista'), ('t5', 'soldador');

INSERT INTO trabalhador_epi_uso (trabalhador_id, epi_id) VALUES
  ('t1', 'capacete'), ('t1', 'bota'), ('t1', 'jaqueta'), ('t1', 'luva'),
  ('t2', 'capacete'), ('t2', 'bota'), ('t2', 'jaqueta'), ('t2', 'luva'), ('t2', 'oculos'),
  ('t3', 'capacete'), ('t3', 'bota'), ('t3', 'jaqueta'), ('t3', 'luva'), ('t3', 'cinto'), ('t3', 'oculos'),
  ('t4', 'capacete'), ('t4', 'bota'), ('t4', 'protetor_auricular'), ('t4', 'luva'), ('t4', 'cinto'), ('t4', 'oculos'),
  ('t5', 'capacete'), ('t5', 'bota'), ('t5', 'mascara'), ('t5', 'oculos');

INSERT INTO ocorrencia (id, obra_id, tipo, autor_id, registrado_por_id, data_hora, descricao, foto) VALUES
  (1, 'alfa', 'RISCO', 't1', 't1', '2026-09-08 16:45:00', 'Fiação exposta próxima ao almoxarifado.', NULL),
  (2, 'alfa', 'QUASE_ACIDENTE', 't2', 't2', '2026-09-09 09:10:00', 'Queda de material de andaime; ninguém foi atingido.', NULL),
  (3, 'alfa', 'ACIDENTE', 't3', 't3', '2026-09-09 14:20:00', 'Corte na mão ao manusear chapa metálica.', NULL),
  (4, 'beta', 'RISCO', 't5', 't5', '2026-09-10 08:30:00', 'Escada sem corrimão no 3º pavimento.', NULL);

INSERT INTO ocorrencia_epi (ocorrencia_id, epi_id, papel) VALUES
  (2, 'capacete', 'ajudou'),
  (3, 'luva', 'faltou');

-- Notificações históricas já lidas, uma por trabalhador da obra de cada ocorrência.
INSERT INTO notificacao (destinatario_id, ocorrencia_id, lida)
  SELECT t.id, o.id, TRUE FROM ocorrencia o JOIN trabalhador t ON t.obra_id = o.obra_id;

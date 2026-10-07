package br.com.sgf.seguranca.model;

import java.util.List;

/** RN01 — pertence a exatamente uma obra. RN02 — pode acumular funções. Fiscal é uma permissão do mesmo cadastro. */
public record Trabalhador(String id, String matricula, String nome, String obraId, List<String> funcoes,
                          boolean fiscal, List<String> episEmUso) {}

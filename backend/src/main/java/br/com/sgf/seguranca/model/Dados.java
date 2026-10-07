package br.com.sgf.seguranca.model;

import java.util.List;

/** Tudo que o front precisa, restrito à obra do usuário (RN10). */
public record Dados(List<Epi> epis, List<Funcao> funcoes, List<Obra> obras, List<Trabalhador> trabalhadores,
                    List<Ocorrencia> ocorrencias, List<Notificacao> notificacoes) {}

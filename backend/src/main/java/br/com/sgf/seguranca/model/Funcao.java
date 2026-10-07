package br.com.sgf.seguranca.model;

import java.util.List;

/** RN04 — toda função possui EPIs obrigatórios específicos. */
public record Funcao(String id, String nome, List<String> episObrigatorios) {}

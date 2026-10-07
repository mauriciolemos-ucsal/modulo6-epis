package br.com.sgf.seguranca.model;

import java.util.List;

/** RN03 — toda obra possui um conjunto de EPIs básicos. */
public record Obra(String id, String nome, List<String> episBasicos) {}

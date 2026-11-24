package br.com.sgf.seguranca.model;

import java.util.List;

/** RN05 — situação de um trabalhador frente aos EPIs exigidos. */
public record Conformidade(List<String> exigidos, List<String> faltando, int percentual, boolean conforme) {}

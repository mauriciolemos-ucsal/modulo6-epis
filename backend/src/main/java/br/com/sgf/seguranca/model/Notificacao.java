package br.com.sgf.seguranca.model;

/** RN08 — uma notificação por trabalhador da obra a cada nova ocorrência. */
public record Notificacao(long id, String destinatarioId, long ocorrenciaId, boolean lida) {}

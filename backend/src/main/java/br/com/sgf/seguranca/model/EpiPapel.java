package br.com.sgf.seguranca.model;

/** EPI envolvido em uma ocorrência e seu papel no evento (faltou / falhou / ajudou). */
public record EpiPapel(String epiId, String papel) {}

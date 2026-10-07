package br.com.sgf.seguranca.model;

/** Item da lista da tela de login (autenticação simulada por matrícula). */
public record OpcaoLogin(String matricula, String nome, boolean fiscal, String obraNome) {}

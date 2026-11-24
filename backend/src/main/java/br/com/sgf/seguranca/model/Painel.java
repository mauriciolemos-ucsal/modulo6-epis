package br.com.sgf.seguranca.model;

import java.util.List;
import java.util.Map;

/** RN09 — indicadores agregados da obra, exclusivos do Fiscal. */
public record Painel(int total, Map<String, Integer> contagem, Map<String, Integer> papeis,
                     int percentualConformes, List<NaoConforme> naoConformes) {

    public record NaoConforme(String trabalhadorId, String nome, List<String> faltando) {}
}

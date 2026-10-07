package br.com.sgf.seguranca.service;

import java.util.List;

/** Falha de regra de negócio. O controller traduz o `tipo` em status HTTP. */
public class ErroNegocio extends RuntimeException {
    public enum Tipo { INVALIDO, NAO_AUTENTICADO, PROIBIDO, NAO_ENCONTRADO }

    private final Tipo tipo;
    private final List<String> erros;

    public ErroNegocio(Tipo tipo, String... erros) {
        this(tipo, List.of(erros));
    }

    public ErroNegocio(Tipo tipo, List<String> erros) {
        super(String.join("; ", erros));
        this.tipo = tipo;
        this.erros = erros;
    }

    public Tipo tipo() {
        return tipo;
    }

    public List<String> erros() {
        return erros;
    }
}

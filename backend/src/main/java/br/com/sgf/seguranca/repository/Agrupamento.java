package br.com.sgf.seguranca.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Agrupa consultas de duas colunas (chave, valor) em `chave -> [valores]`. */
final class Agrupamento {
    private Agrupamento() {}

    static Map<String, List<String>> agrupar(JdbcClient jdbc, String sql) {
        Map<String, List<String>> mapa = new LinkedHashMap<>();
        jdbc.sql(sql).query((rs, i) -> {
            mapa.computeIfAbsent(rs.getString(1), k -> new ArrayList<>()).add(rs.getString(2));
            return null;
        }).list();
        return mapa;
    }
}

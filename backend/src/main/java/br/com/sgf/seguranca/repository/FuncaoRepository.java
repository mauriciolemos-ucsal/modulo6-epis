package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.Funcao;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class FuncaoRepository {
    private final JdbcClient jdbc;

    public FuncaoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Funcao> listar() {
        Map<String, List<String>> epis =
            Agrupamento.agrupar(jdbc, "SELECT funcao_id, epi_id FROM funcao_epi ORDER BY epi_id");
        return jdbc.sql("SELECT id, nome FROM funcao ORDER BY nome")
            .query((rs, i) -> new Funcao(rs.getString("id"), rs.getString("nome"),
                epis.getOrDefault(rs.getString("id"), List.of())))
            .list();
    }

    public boolean existe(String id) {
        return jdbc.sql("SELECT COUNT(*) FROM funcao WHERE id = ?").param(id).query(Long.class).single() > 0;
    }

    /** RN04 — liga ou desliga um EPI obrigatório da função. */
    public void definirEpi(String funcaoId, String epiId, boolean ativo) {
        String sql = ativo
            ? "INSERT IGNORE INTO funcao_epi (funcao_id, epi_id) VALUES (?, ?)"
            : "DELETE FROM funcao_epi WHERE funcao_id = ? AND epi_id = ?";
        jdbc.sql(sql).param(funcaoId).param(epiId).update();
    }
}

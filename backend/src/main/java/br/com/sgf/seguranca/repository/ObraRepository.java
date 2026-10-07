package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.Obra;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ObraRepository {
    private final JdbcClient jdbc;

    public ObraRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Obra> porId(String id) {
        List<String> epis = jdbc.sql("SELECT epi_id FROM obra_epi WHERE obra_id = ? ORDER BY epi_id")
            .param(id).query(String.class).list();
        return jdbc.sql("SELECT id, nome FROM obra WHERE id = ?").param(id)
            .query((rs, i) -> new Obra(rs.getString("id"), rs.getString("nome"), epis)).optional();
    }

    /** RN03 — liga ou desliga um EPI básico da obra. */
    public void definirEpi(String obraId, String epiId, boolean ativo) {
        String sql = ativo
            ? "INSERT IGNORE INTO obra_epi (obra_id, epi_id) VALUES (?, ?)"
            : "DELETE FROM obra_epi WHERE obra_id = ? AND epi_id = ?";
        jdbc.sql(sql).param(obraId).param(epiId).update();
    }
}

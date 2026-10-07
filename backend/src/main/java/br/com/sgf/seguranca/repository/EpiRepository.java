package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.Epi;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class EpiRepository {
    private final JdbcClient jdbc;

    public EpiRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Epi> listar() {
        return jdbc.sql("SELECT id, nome FROM epi ORDER BY nome")
            .query((rs, i) -> new Epi(rs.getString("id"), rs.getString("nome"))).list();
    }

    public boolean existe(String id) {
        return jdbc.sql("SELECT COUNT(*) FROM epi WHERE id = ?").param(id).query(Long.class).single() > 0;
    }
}

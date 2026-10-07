package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.Notificacao;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NotificacaoRepository {
    private final JdbcClient jdbc;

    public NotificacaoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Notificacao> doTrabalhador(String trabalhadorId) {
        return jdbc.sql("SELECT id, destinatario_id, ocorrencia_id, lida FROM notificacao WHERE destinatario_id = ? ORDER BY id")
            .param(trabalhadorId)
            .query((rs, i) -> new Notificacao(rs.getLong("id"), rs.getString("destinatario_id"),
                rs.getLong("ocorrencia_id"), rs.getBoolean("lida")))
            .list();
    }

    /** RN08 — uma notificação para cada trabalhador da obra da ocorrência. */
    public void gerarParaObra(long ocorrenciaId, String obraId) {
        jdbc.sql("""
                INSERT INTO notificacao (destinatario_id, ocorrencia_id, lida)
                SELECT id, ?, FALSE FROM trabalhador WHERE obra_id = ?""")
            .param(ocorrenciaId).param(obraId).update();
    }

    public void marcarLidas(String trabalhadorId) {
        jdbc.sql("UPDATE notificacao SET lida = TRUE WHERE destinatario_id = ? AND lida = FALSE")
            .param(trabalhadorId).update();
    }
}

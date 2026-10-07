package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.EpiPapel;
import br.com.sgf.seguranca.model.Ocorrencia;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OcorrenciaRepository {
    private final JdbcClient jdbc;

    public OcorrenciaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Ocorrências da obra, mais recentes primeiro. `foto` traz o nome do arquivo (ou null). */
    public List<Ocorrencia> daObra(String obraId) {
        Map<Long, List<EpiPapel>> epis = new HashMap<>();
        jdbc.sql("""
                SELECT oe.ocorrencia_id, oe.epi_id, oe.papel FROM ocorrencia_epi oe
                JOIN ocorrencia o ON o.id = oe.ocorrencia_id WHERE o.obra_id = ? ORDER BY oe.epi_id""")
            .param(obraId)
            .query((rs, i) -> {
                epis.computeIfAbsent(rs.getLong("ocorrencia_id"), k -> new ArrayList<>())
                    .add(new EpiPapel(rs.getString("epi_id"), rs.getString("papel")));
                return null;
            }).list();
        return jdbc.sql("""
                SELECT id, obra_id, tipo, autor_id, registrado_por_id, data_hora, descricao, foto
                FROM ocorrencia WHERE obra_id = ? ORDER BY data_hora DESC, id DESC""")
            .param(obraId)
            .query((rs, i) -> new Ocorrencia(rs.getLong("id"), rs.getString("obra_id"), rs.getString("tipo"),
                rs.getString("autor_id"), rs.getString("registrado_por_id"),
                rs.getTimestamp("data_hora").toLocalDateTime(), rs.getString("descricao"),
                rs.getString("foto"), epis.getOrDefault(rs.getLong("id"), List.of())))
            .list();
    }

    /** Insere a ocorrência e seus EPIs; devolve o id gerado. */
    public long inserir(String obraId, String tipo, String autorId, String registradoPorId,
                        LocalDateTime dataHora, String descricao, String foto, List<EpiPapel> epis) {
        var chave = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO ocorrencia (obra_id, tipo, autor_id, registrado_por_id, data_hora, descricao, foto)
                VALUES (?, ?, ?, ?, ?, ?, ?)""")
            .param(obraId).param(tipo).param(autorId).param(registradoPorId)
            .param(Timestamp.valueOf(dataHora)).param(descricao).param(foto)
            .update(chave);
        long id = chave.getKey().longValue();
        for (EpiPapel e : epis) {
            jdbc.sql("INSERT INTO ocorrencia_epi (ocorrencia_id, epi_id, papel) VALUES (?, ?, ?)")
                .param(id).param(e.epiId()).param(e.papel()).update();
        }
        return id;
    }
}

package br.com.sgf.seguranca.repository;

import br.com.sgf.seguranca.model.OpcaoLogin;
import br.com.sgf.seguranca.model.Trabalhador;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TrabalhadorRepository {
    private static final String COLUNAS = "SELECT id, matricula, nome, obra_id, fiscal FROM trabalhador";
    private static final String FUNCOES = "SELECT trabalhador_id, funcao_id FROM trabalhador_funcao ORDER BY funcao_id";
    private static final String EPIS_EM_USO = "SELECT trabalhador_id, epi_id FROM trabalhador_epi_uso ORDER BY epi_id";

    private final JdbcClient jdbc;

    public TrabalhadorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Trabalhador> porId(String id) {
        return buscar(" WHERE id = ?", id).stream().findFirst();
    }

    public Optional<Trabalhador> porMatricula(String matricula) {
        return buscar(" WHERE matricula = ?", matricula).stream().findFirst();
    }

    public List<Trabalhador> daObra(String obraId) {
        return buscar(" WHERE obra_id = ? ORDER BY id", obraId);
    }

    /** Lista usada pela tela de login (autenticação simulada, por matrícula). */
    public List<OpcaoLogin> opcoesLogin() {
        return jdbc.sql("""
                SELECT t.matricula, t.nome, t.fiscal, o.nome AS obra_nome
                FROM trabalhador t JOIN obra o ON o.id = t.obra_id ORDER BY t.matricula""")
            .query((rs, i) -> new OpcaoLogin(rs.getString("matricula"), rs.getString("nome"),
                rs.getBoolean("fiscal"), rs.getString("obra_nome")))
            .list();
    }

    private List<Trabalhador> buscar(String filtro, String parametro) {
        Map<String, List<String>> funcoes = Agrupamento.agrupar(jdbc, FUNCOES);
        Map<String, List<String>> epis = Agrupamento.agrupar(jdbc, EPIS_EM_USO);
        return jdbc.sql(COLUNAS + filtro).param(parametro)
            .query((rs, i) -> new Trabalhador(rs.getString("id"), rs.getString("matricula"), rs.getString("nome"),
                rs.getString("obra_id"), funcoes.getOrDefault(rs.getString("id"), List.of()),
                rs.getBoolean("fiscal"), epis.getOrDefault(rs.getString("id"), List.of())))
            .list();
    }
}

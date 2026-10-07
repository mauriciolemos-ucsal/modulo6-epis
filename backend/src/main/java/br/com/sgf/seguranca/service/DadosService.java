package br.com.sgf.seguranca.service;

import br.com.sgf.seguranca.model.Dados;
import br.com.sgf.seguranca.model.Trabalhador;
import br.com.sgf.seguranca.repository.EpiRepository;
import br.com.sgf.seguranca.repository.FuncaoRepository;
import br.com.sgf.seguranca.repository.NotificacaoRepository;
import br.com.sgf.seguranca.repository.ObraRepository;
import br.com.sgf.seguranca.repository.OcorrenciaRepository;
import br.com.sgf.seguranca.repository.TrabalhadorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DadosService {
    static final String URL_FOTOS = "/api/fotos/";

    private final UsuarioService usuarios;
    private final EpiRepository epis;
    private final FuncaoRepository funcoes;
    private final ObraRepository obras;
    private final TrabalhadorRepository trabalhadores;
    private final OcorrenciaRepository ocorrencias;
    private final NotificacaoRepository notificacoes;

    public DadosService(UsuarioService usuarios, EpiRepository epis, FuncaoRepository funcoes, ObraRepository obras,
                        TrabalhadorRepository trabalhadores, OcorrenciaRepository ocorrencias,
                        NotificacaoRepository notificacoes) {
        this.usuarios = usuarios;
        this.epis = epis;
        this.funcoes = funcoes;
        this.obras = obras;
        this.trabalhadores = trabalhadores;
        this.ocorrencias = ocorrencias;
        this.notificacoes = notificacoes;
    }

    /** RN10 — só entram dados da obra do usuário (e as notificações dele). */
    @Transactional(readOnly = true)
    public Dados dadosDoUsuario(String usuarioId) {
        Trabalhador u = usuarios.exigir(usuarioId);
        return new Dados(
            epis.listar(),
            funcoes.listar(),
            obras.porId(u.obraId()).map(List::of).orElse(List.of()),
            trabalhadores.daObra(u.obraId()),
            ocorrencias.daObra(u.obraId()).stream()
                .map(o -> o.comFoto(o.foto() == null ? null : URL_FOTOS + o.foto()))
                .toList(),
            notificacoes.doTrabalhador(u.id()));
    }
}

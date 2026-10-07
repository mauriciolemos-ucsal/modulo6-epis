package br.com.sgf.seguranca.service;

import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.INVALIDO;
import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.PROIBIDO;

import br.com.sgf.seguranca.model.EpiPapel;
import br.com.sgf.seguranca.model.NovaOcorrencia;
import br.com.sgf.seguranca.model.Regras;
import br.com.sgf.seguranca.model.Trabalhador;
import br.com.sgf.seguranca.repository.EpiRepository;
import br.com.sgf.seguranca.repository.FotoRepository;
import br.com.sgf.seguranca.repository.NotificacaoRepository;
import br.com.sgf.seguranca.repository.OcorrenciaRepository;
import br.com.sgf.seguranca.repository.TrabalhadorRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OcorrenciaService {
    private final UsuarioService usuarios;
    private final TrabalhadorRepository trabalhadores;
    private final EpiRepository epis;
    private final OcorrenciaRepository ocorrencias;
    private final NotificacaoRepository notificacoes;
    private final FotoRepository fotos;

    public OcorrenciaService(UsuarioService usuarios, TrabalhadorRepository trabalhadores, EpiRepository epis,
                             OcorrenciaRepository ocorrencias, NotificacaoRepository notificacoes,
                             FotoRepository fotos) {
        this.usuarios = usuarios;
        this.trabalhadores = trabalhadores;
        this.epis = epis;
        this.ocorrencias = ocorrencias;
        this.notificacoes = notificacoes;
        this.fotos = fotos;
    }

    /** Valida (RN06, RN07, RN09, RN10), grava foto e ocorrência e notifica a obra (RN08). Devolve o id. */
    @Transactional
    public long registrar(NovaOcorrencia pedido) {
        Trabalhador usuario = usuarios.exigir(pedido.usuarioId());
        Trabalhador autor = resolverAutor(usuario, pedido.autorId());
        // RN09/RN10 — só o Fiscal registra em nome de outro, e só de quem é da mesma obra.
        if (!Regras.podeRegistrarEmNomeDe(usuario, autor)) {
            throw new ErroNegocio(PROIBIDO, "Você não tem permissão para registrar em nome deste trabalhador.");
        }

        boolean temFoto = pedido.foto() != null && pedido.foto().length > 0;
        List<String> erros = new ArrayList<>(
            Regras.validarOcorrencia(pedido.tipo(), temFoto, pedido.descricao(), pedido.epis()));
        List<EpiPapel> episEfetivos = Regras.episEfetivos(pedido.tipo(), pedido.epis());
        for (EpiPapel e : episEfetivos) {
            if (!epis.existe(e.epiId())) erros.add("EPI desconhecido: " + e.epiId() + ".");
        }
        if (!erros.isEmpty()) throw new ErroNegocio(INVALIDO, erros);

        String nomeFoto = fotos.salvar(pedido.foto())
            .orElseThrow(() -> new ErroNegocio(INVALIDO, "A foto deve ser uma imagem JPG, PNG, GIF ou WebP."));
        try {
            String descricao = pedido.descricao() == null ? "" : pedido.descricao().trim();
            long id = ocorrencias.inserir(autor.obraId(), pedido.tipo(), autor.id(), usuario.id(),
                LocalDateTime.now().withNano(0), descricao, nomeFoto, episEfetivos);
            notificacoes.gerarParaObra(id, autor.obraId());
            return id;
        } catch (RuntimeException e) {
            fotos.apagar(nomeFoto);
            throw e;
        }
    }

    private Trabalhador resolverAutor(Trabalhador usuario, String autorId) {
        if (autorId == null || autorId.isBlank() || autorId.equals(usuario.id())) return usuario;
        return trabalhadores.porId(autorId)
            .orElseThrow(() -> new ErroNegocio(INVALIDO, "Trabalhador autor não encontrado."));
    }
}

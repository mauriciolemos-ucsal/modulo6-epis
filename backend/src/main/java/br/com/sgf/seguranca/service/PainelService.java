package br.com.sgf.seguranca.service;

import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.NAO_ENCONTRADO;
import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.PROIBIDO;

import br.com.sgf.seguranca.model.Obra;
import br.com.sgf.seguranca.model.Painel;
import br.com.sgf.seguranca.model.Regras;
import br.com.sgf.seguranca.model.Trabalhador;
import br.com.sgf.seguranca.repository.FuncaoRepository;
import br.com.sgf.seguranca.repository.ObraRepository;
import br.com.sgf.seguranca.repository.OcorrenciaRepository;
import br.com.sgf.seguranca.repository.TrabalhadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Painel de indicadores: só o Fiscal acessa (RN09) e só da própria obra (RN10). */
@Service
public class PainelService {
    private final UsuarioService usuarios;
    private final ObraRepository obras;
    private final FuncaoRepository funcoes;
    private final TrabalhadorRepository trabalhadores;
    private final OcorrenciaRepository ocorrencias;

    public PainelService(UsuarioService usuarios, ObraRepository obras, FuncaoRepository funcoes,
                         TrabalhadorRepository trabalhadores, OcorrenciaRepository ocorrencias) {
        this.usuarios = usuarios;
        this.obras = obras;
        this.funcoes = funcoes;
        this.trabalhadores = trabalhadores;
        this.ocorrencias = ocorrencias;
    }

    @Transactional(readOnly = true)
    public Painel doUsuario(String usuarioId) {
        Trabalhador usuario = usuarios.exigir(usuarioId);
        if (!Regras.podeVerPainel(usuario)) {
            throw new ErroNegocio(PROIBIDO, "Acesso restrito a usuários com permissão de Fiscal.");
        }
        Obra obra = obras.porId(usuario.obraId())
            .orElseThrow(() -> new ErroNegocio(NAO_ENCONTRADO, "Obra não encontrada."));
        return Regras.indicadores(ocorrencias.daObra(obra.id()), trabalhadores.daObra(obra.id()),
            obra, funcoes.listar());
    }
}

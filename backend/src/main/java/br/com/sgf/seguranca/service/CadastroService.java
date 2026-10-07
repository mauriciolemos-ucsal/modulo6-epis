package br.com.sgf.seguranca.service;

import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.NAO_ENCONTRADO;
import static br.com.sgf.seguranca.service.ErroNegocio.Tipo.PROIBIDO;

import br.com.sgf.seguranca.repository.EpiRepository;
import br.com.sgf.seguranca.repository.FuncaoRepository;
import br.com.sgf.seguranca.repository.ObraRepository;
import org.springframework.stereotype.Service;

/** Cadastro de EPIs básicos da obra (RN03) e obrigatórios da função (RN04). */
@Service
public class CadastroService {
    private final UsuarioService usuarios;
    private final EpiRepository epis;
    private final ObraRepository obras;
    private final FuncaoRepository funcoes;

    public CadastroService(UsuarioService usuarios, EpiRepository epis, ObraRepository obras, FuncaoRepository funcoes) {
        this.usuarios = usuarios;
        this.epis = epis;
        this.obras = obras;
        this.funcoes = funcoes;
    }

    /** Cada usuário só altera a própria obra (RN10). */
    public void definirEpiDaObra(String usuarioId, String obraId, String epiId, boolean ativo) {
        if (!usuarios.exigir(usuarioId).obraId().equals(obraId)) {
            throw new ErroNegocio(PROIBIDO, "Você só pode alterar a sua própria obra.");
        }
        exigirEpi(epiId);
        obras.definirEpi(obraId, epiId, ativo);
    }

    public void definirEpiDaFuncao(String funcaoId, String epiId, boolean ativo) {
        if (!funcoes.existe(funcaoId)) throw new ErroNegocio(NAO_ENCONTRADO, "Função não encontrada.");
        exigirEpi(epiId);
        funcoes.definirEpi(funcaoId, epiId, ativo);
    }

    private void exigirEpi(String epiId) {
        if (!epis.existe(epiId)) throw new ErroNegocio(NAO_ENCONTRADO, "EPI não encontrado.");
    }
}

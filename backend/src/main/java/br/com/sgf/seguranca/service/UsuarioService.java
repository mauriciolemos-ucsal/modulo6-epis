package br.com.sgf.seguranca.service;

import br.com.sgf.seguranca.model.Trabalhador;
import br.com.sgf.seguranca.repository.TrabalhadorRepository;
import org.springframework.stereotype.Service;

/** Resolve o trabalhador dono da sessão para os demais serviços. */
@Service
public class UsuarioService {
    private final TrabalhadorRepository trabalhadores;

    public UsuarioService(TrabalhadorRepository trabalhadores) {
        this.trabalhadores = trabalhadores;
    }

    public Trabalhador exigir(String usuarioId) {
        return trabalhadores.porId(usuarioId)
            .orElseThrow(() -> new ErroNegocio(ErroNegocio.Tipo.NAO_AUTENTICADO, "Sessão inválida."));
    }
}

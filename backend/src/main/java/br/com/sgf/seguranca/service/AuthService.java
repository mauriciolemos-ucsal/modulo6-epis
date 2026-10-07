package br.com.sgf.seguranca.service;

import br.com.sgf.seguranca.model.OpcaoLogin;
import br.com.sgf.seguranca.model.Sessao;
import br.com.sgf.seguranca.repository.TrabalhadorRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Login por matrícula e sessões em memória (token -> id do trabalhador). A autenticação central
 * está fora do escopo do módulo: não há senha, e reiniciar o backend encerra as sessões.
 */
@Service
public class AuthService {
    private final TrabalhadorRepository trabalhadores;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, String> sessoes = new ConcurrentHashMap<>();

    public AuthService(TrabalhadorRepository trabalhadores) {
        this.trabalhadores = trabalhadores;
    }

    public List<OpcaoLogin> opcoesLogin() {
        return trabalhadores.opcoesLogin();
    }

    public Sessao login(String matricula) {
        String m = matricula == null ? "" : matricula.trim();
        var trabalhador = trabalhadores.porMatricula(m)
            .orElseThrow(() -> new ErroNegocio(ErroNegocio.Tipo.NAO_AUTENTICADO, "Matrícula não encontrada."));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessoes.put(token, trabalhador.id());
        return new Sessao(token, trabalhador.id());
    }

    public void logout(String token) {
        if (token != null) sessoes.remove(token);
    }

    public Optional<String> trabalhadorDaSessao(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(sessoes.get(token));
    }
}

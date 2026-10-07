package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.model.OpcaoLogin;
import br.com.sgf.seguranca.model.Sessao;
import br.com.sgf.seguranca.service.AuthService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    public record LoginPedido(String matricula) {}

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/login/opcoes")
    public List<OpcaoLogin> opcoes() {
        return auth.opcoesLogin();
    }

    @PostMapping("/login")
    public Sessao login(@RequestBody LoginPedido pedido) {
        return auth.login(pedido.matricula());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestAttribute(AuthInterceptor.ATRIBUTO_TOKEN) String token) {
        auth.logout(token);
    }
}

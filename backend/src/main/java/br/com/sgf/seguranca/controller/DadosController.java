package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.model.Dados;
import br.com.sgf.seguranca.service.DadosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dados")
public class DadosController {
    private final DadosService dados;

    public DadosController(DadosService dados) {
        this.dados = dados;
    }

    @GetMapping
    public Dados dados(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) String usuarioId) {
        return dados.dadosDoUsuario(usuarioId);
    }
}

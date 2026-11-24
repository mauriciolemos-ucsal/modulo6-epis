package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.model.Painel;
import br.com.sgf.seguranca.service.PainelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
public class PainelController {
    private final PainelService painel;

    public PainelController(PainelService painel) {
        this.painel = painel;
    }

    @GetMapping
    public Painel painel(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) String usuarioId) {
        return painel.doUsuario(usuarioId);
    }
}

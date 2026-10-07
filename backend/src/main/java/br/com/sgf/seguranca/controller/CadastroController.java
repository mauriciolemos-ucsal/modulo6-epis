package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.service.CadastroService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CadastroController {
    public record Ativo(boolean ativo) {}

    private final CadastroService cadastro;

    public CadastroController(CadastroService cadastro) {
        this.cadastro = cadastro;
    }

    @PutMapping("/obras/{obraId}/epis/{epiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void epiDaObra(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) String usuarioId,
                          @PathVariable String obraId, @PathVariable String epiId, @RequestBody Ativo corpo) {
        cadastro.definirEpiDaObra(usuarioId, obraId, epiId, corpo.ativo());
    }

    @PutMapping("/funcoes/{funcaoId}/epis/{epiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void epiDaFuncao(@PathVariable String funcaoId, @PathVariable String epiId, @RequestBody Ativo corpo) {
        cadastro.definirEpiDaFuncao(funcaoId, epiId, corpo.ativo());
    }
}

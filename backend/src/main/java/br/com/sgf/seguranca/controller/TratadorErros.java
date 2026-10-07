package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.service.ErroNegocio;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Converte exceções em respostas `{ "erros": [...] }` com o status HTTP adequado. */
@RestControllerAdvice
public class TratadorErros {
    public record Corpo(List<String> erros) {}

    @ExceptionHandler(ErroNegocio.class)
    ResponseEntity<Corpo> negocio(ErroNegocio e) {
        HttpStatus status = switch (e.tipo()) {
            case INVALIDO -> HttpStatus.BAD_REQUEST;
            case NAO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
            case PROIBIDO -> HttpStatus.FORBIDDEN;
            case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
        };
        return ResponseEntity.status(status).body(new Corpo(e.erros()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Corpo> grande(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(new Corpo(List.of("A foto excede o tamanho máximo de 10 MB.")));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Corpo> inesperado(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Corpo(List.of("Erro interno no servidor.")));
    }
}

package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.service.FotoService;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serve as fotos sem cabeçalho de autenticação (a tag <img> não consegue enviá-lo).
 * Os nomes são UUIDs aleatórios, então só quem recebeu o link pelo feed consegue abrir.
 */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {
    private final FotoService fotos;

    public FotoController(FotoService fotos) {
        this.fotos = fotos;
    }

    @GetMapping("/{nome}")
    public ResponseEntity<Resource> foto(@PathVariable String nome) {
        return fotos.localizar(nome)
            .map(p -> ResponseEntity.ok()
                .contentType(tipo(p))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePrivate().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .<Resource>body(new FileSystemResource(p)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static MediaType tipo(Path p) {
        String n = p.getFileName().toString();
        if (n.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (n.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (n.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}

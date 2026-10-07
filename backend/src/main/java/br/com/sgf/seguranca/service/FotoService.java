package br.com.sgf.seguranca.service;

import br.com.sgf.seguranca.repository.FotoRepository;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FotoService {
    private final FotoRepository fotos;

    public FotoService(FotoRepository fotos) {
        this.fotos = fotos;
    }

    public Optional<Path> localizar(String nome) {
        return fotos.localizar(nome);
    }
}

package br.com.sgf.seguranca.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/** Grava e lê as fotos das ocorrências na pasta configurada em `sgf.fotos-dir`. */
@Repository
public class FotoRepository {
    private static final Pattern NOME_VALIDO =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|gif|webp)$");

    private final Path pasta;

    public FotoRepository(@Value("${sgf.fotos-dir}") String dir) throws IOException {
        this.pasta = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(pasta);
    }

    /** Extensão pelo conteúdo real do arquivo (não confia no nome nem no Content-Type enviados). */
    static Optional<String> extensaoPorAssinatura(byte[] c) {
        if (c.length >= 3 && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) return Optional.of("jpg");
        if (c.length >= 8 && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G') return Optional.of("png");
        if (c.length >= 6 && c[0] == 'G' && c[1] == 'I' && c[2] == 'F' && c[3] == '8') return Optional.of("gif");
        if (c.length >= 12 && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
            && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') return Optional.of("webp");
        return Optional.empty();
    }

    /** Salva a foto com nome aleatório e devolve o nome; vazio se não for uma imagem aceita. */
    public Optional<String> salvar(byte[] conteudo) {
        Optional<String> ext = extensaoPorAssinatura(conteudo);
        if (ext.isEmpty()) return Optional.empty();
        String nome = UUID.randomUUID() + "." + ext.get();
        try {
            Files.write(pasta.resolve(nome), conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar a foto.", e);
        }
        return Optional.of(nome);
    }

    public void apagar(String nome) {
        if (nome == null || !NOME_VALIDO.matcher(nome).matches()) return;
        try {
            Files.deleteIfExists(pasta.resolve(nome));
        } catch (IOException ignorado) {
            // melhor esforço: arquivo órfão não afeta a consistência do banco
        }
    }

    /** Caminho do arquivo, se o nome for válido e o arquivo existir. */
    public Optional<Path> localizar(String nome) {
        if (nome == null || !NOME_VALIDO.matcher(nome).matches()) return Optional.empty();
        Path p = pasta.resolve(nome).normalize();
        return p.startsWith(pasta) && Files.isRegularFile(p) ? Optional.of(p) : Optional.empty();
    }
}

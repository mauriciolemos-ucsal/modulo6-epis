package br.com.sgf.seguranca.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/** `foto` guarda o nome do arquivo no repositório e, nas respostas da API, a URL pública da foto. */
public record Ocorrencia(long id, String obraId, String tipo, String autorId, String registradoPorId,
                         @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataHora,
                         String descricao, String foto, List<EpiPapel> epis) {

    public Ocorrencia comFoto(String foto) {
        return new Ocorrencia(id, obraId, tipo, autorId, registradoPorId, dataHora, descricao, foto, epis);
    }
}

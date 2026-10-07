package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.model.EpiPapel;
import br.com.sgf.seguranca.model.NovaOcorrencia;
import br.com.sgf.seguranca.service.ErroNegocio;
import br.com.sgf.seguranca.service.OcorrenciaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {
    public record OcorrenciaCriada(long id) {}

    private final OcorrenciaService ocorrencias;
    private final ObjectMapper json;

    public OcorrenciaController(OcorrenciaService ocorrencias, ObjectMapper json) {
        this.ocorrencias = ocorrencias;
        this.json = json;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaCriada registrar(
        @RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) String usuarioId,
        @RequestParam(required = false) String autorId,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) String descricao,
        @RequestParam(required = false) String epis,
        @RequestParam(required = false) MultipartFile foto) throws IOException {
        byte[] bytes = foto == null || foto.isEmpty() ? null : foto.getBytes();
        long id = ocorrencias.registrar(new NovaOcorrencia(usuarioId, autorId, tipo, descricao, lerEpis(epis), bytes));
        return new OcorrenciaCriada(id);
    }

    private List<EpiPapel> lerEpis(String epis) {
        if (epis == null || epis.isBlank()) return List.of();
        try {
            return json.readValue(epis, new TypeReference<List<EpiPapel>>() {});
        } catch (IOException e) {
            throw new ErroNegocio(ErroNegocio.Tipo.INVALIDO, "Lista de EPIs inválida.");
        }
    }
}

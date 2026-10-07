package br.com.sgf.seguranca.model;

import java.util.List;

/** Pedido de registro de ocorrência. `autorId` vazio = o próprio usuário; `foto` são os bytes da imagem. */
public record NovaOcorrencia(String usuarioId, String autorId, String tipo, String descricao,
                             List<EpiPapel> epis, byte[] foto) {}

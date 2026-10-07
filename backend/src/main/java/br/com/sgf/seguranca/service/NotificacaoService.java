package br.com.sgf.seguranca.service;

import br.com.sgf.seguranca.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {
    private final NotificacaoRepository notificacoes;

    public NotificacaoService(NotificacaoRepository notificacoes) {
        this.notificacoes = notificacoes;
    }

    public void marcarLidas(String usuarioId) {
        notificacoes.marcarLidas(usuarioId);
    }
}

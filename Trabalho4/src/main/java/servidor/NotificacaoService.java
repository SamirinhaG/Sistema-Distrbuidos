package servidor;

import broker.Topicos;
import entidades.Notificacao;
import entidades.TipoNotificacao;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;

public class NotificacaoService {
    public static final String NOME = "NotificacaoService";

    private final Biblioteca biblioteca;
    private final PublicadorBroker publicador;

    public NotificacaoService(Biblioteca biblioteca, PublicadorBroker publicador) {
        this.biblioteca = biblioteca;
        this.publicador = publicador;
    }

    @MetodoRemoto
    public void publicarAviso(String token, Notificacao notificacao) {
        biblioteca.exigirBibliotecario(token);
        if (notificacao == null || notificacao.getMensagem() == null || notificacao.getMensagem().isBlank()) {
            throw new ExcecaoRemota("A mensagem não pode ser vazia");
        }
        publicador.publicar(Topicos.AVISOS, new Notificacao(
                notificacao.getTipo() == null ? TipoNotificacao.NOTIFICACAO : notificacao.getTipo(),
                notificacao.getMensagem()));
    }
}

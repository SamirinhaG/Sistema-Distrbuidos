package servidor;

import entidades.Notificacao;
import entidades.TipoNotificacao;
import entidades.Usuario;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;
import rr.RemoteObjectRef;

/** Objeto remoto "NotificacaoService": entrar/sair do grupo e enviar notas informativas (administradores). */
public class NotificacaoService {
    public static final String NOME = "NotificacaoService";

    private final Biblioteca biblioteca;
    private final GrupoNotificacao grupo;

    public NotificacaoService(Biblioteca biblioteca, GrupoNotificacao grupo) {
        this.biblioteca = biblioteca;
        this.grupo = grupo;
    }

    /**
     * joinGroup - O callback é um objeto remoto do CLIENTE passado por referência: o servidor guarda a
     * referência e a usa depois para invocar receberNotificacao nesse cliente.
     */
    @MetodoRemoto
    public int entrarNoGrupo(String token, RemoteObjectRef callback) {
        Usuario usuario = biblioteca.exigirSessao(token);
        if (callback == null || callback.getHost() == null || callback.getNome() == null) {
            throw new ExcecaoRemota("Referência de callback inválida");
        }
        return grupo.entrar(usuario.getLogin(), callback);
    }

    /** leaveGroup. */
    @MetodoRemoto
    public boolean sairDoGrupo(String token) {
        Usuario usuario = biblioteca.exigirSessao(token);
        return grupo.sair(usuario.getLogin());
    }

    /** Envio a todos os membros do grupo (somente bibliotecários). Retorna o número de destinatários. */
    @MetodoRemoto
    public int enviarNotificacao(String token, Notificacao notificacao) {
        biblioteca.exigirBibliotecario(token);
        if (notificacao == null || notificacao.getMensagem() == null || notificacao.getMensagem().isBlank()) {
            throw new ExcecaoRemota("A mensagem não pode ser vazia");
        }
        Notificacao nova = new Notificacao(
                notificacao.getTipo() == null ? TipoNotificacao.NOTIFICACAO : notificacao.getTipo(),
                notificacao.getMensagem());
        return grupo.enviar(nova);
    }
}

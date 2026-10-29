package cliente;

import entidades.Notificacao;
import rr.MetodoRemoto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Objeto remoto publicado pelo CLIENTE. O servidor recebe a referência a ele
 * em entrarNoGrupo e invoca receberNotificacao sempre que há uma mensagem para o grupo.
 *
 * As invocações chegam na thread do ServidorRR do cliente, que é a "thread de escuta", a thread principal
 * fica livre para a interação com o usuário
 */
public class CallbackCliente {
    public static final String NOME = "ClienteCallback";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String login;

    public CallbackCliente(String login) {
        this.login = login;
    }

    @MetodoRemoto
    public void receberNotificacao(Notificacao notificacao) {
        String hora = HORA.format(Instant.ofEpochMilli(notificacao.getTimestamp()).atZone(ZoneId.systemDefault()));
        synchronized (System.out) {
            System.out.println();
            System.out.println("  >>> [" + hora + "] " + notificacao.getTipo() + ": " + notificacao.getMensagem());
            System.out.print("> ");
            System.out.flush();
        }
    }
}

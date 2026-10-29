package servidor;

import entidades.Notificacao;
import rr.ClienteRR;
import rr.ProxyRemoto;
import rr.RemoteObjectRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Equivalente, em RMI, do grupo multicast da questão 4 do Trabalho 1.
 *
 * joinGroup  -> "entrar": o cliente passa por referencia o seu objeto remoto de callback.
 * leaveGroup -> "sair"
 * envio multicast -> "enviar": o servidor invoca, em paralelo, o método remoto receberNotificacao
 * de cada cliente do grupo
 */
public class GrupoNotificacao {
    public static final String METODO_CALLBACK = "receberNotificacao";

    private final Map<String, RemoteObjectRef> membros = new ConcurrentHashMap<>();
    private final ClienteRR rr;
    private final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "multicast-sender");
        t.setDaemon(true);
        return t;
    });

    public GrupoNotificacao(ClienteRR rr) {
        this.rr = rr;
    }

    public int entrar(String login, RemoteObjectRef callback) {
        membros.put(login, callback);
        System.out.println("[grupo] " + login + " entrou (" + callback + "). Membros: " + membros.size());
        return membros.size();
    }

    public boolean sair(String login) {
        boolean removido = membros.remove(login) != null;
        if (removido) {
            System.out.println("[grupo] " + login + " saiu. Membros: " + membros.size());
        }
        return removido;
    }

    public boolean contem(String login) {
        return membros.containsKey(login);
    }

    /** Dispara a notificação para todos os membros, de forma concorrente. Retorna quantos membros foram alvo */
    public int enviar(Notificacao notificacao) {
        List<Map.Entry<String, RemoteObjectRef>> alvos = new ArrayList<>(membros.entrySet());
        for (Map.Entry<String, RemoteObjectRef> alvo : alvos) {
            pool.submit(() -> {
                try {
                    new ProxyRemoto(rr, alvo.getValue()).chamarSemRetorno(METODO_CALLBACK, notificacao);
                } catch (Exception e) {
                    membros.remove(alvo.getKey());
                    System.out.println("[grupo] " + alvo.getKey() + " removido (inacessível): " + e.getMessage());
                }
            });
        }
        System.out.println("[grupo] " + notificacao.getTipo() + " enviada a " + alvos.size() + " membro(s): "
                + notificacao.getMensagem());
        return alvos.size();
    }
}

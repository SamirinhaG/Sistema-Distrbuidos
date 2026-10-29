package rr;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lado "servidor" do protocolo requisição-resposta. Tanto o servidor da biblioteca quanto cada
 * cliente (que expõe o seu callback) usam esta classe para publicar objetos remotos
 *
 * Laço principal: getRequest() -> despachar -> sendReply(). Cada requisição é atendida numa thread
 * do pool, portanto o servidor é multithread
 */
public class ServidorRR {
    public static final String NOME_TRANSPORTE = "Transporte";

    private final int porta;
    private final Despachante despachante;
    private final ExecutorService pool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "rr-worker");
        t.setDaemon(true);
        return t;
    });
    private TransporteImpl transporte;
    private Registry registro;
    private volatile boolean ativo;

    public ServidorRR(int porta, Despachante despachante) {
        this.porta = porta;
        this.despachante = despachante;
    }

    /** Cria o registro RMI na porta indicada, publica o transporte e inicia o laço de atendimento */
    public void iniciar() throws RemoteException {
        registro = LocateRegistry.createRegistry(porta);
        transporte = new TransporteImpl(porta);
        registro.rebind(NOME_TRANSPORTE, transporte);
        ativo = true;
        Thread laco = new Thread(this::lacoDeAtendimento, "rr-dispatcher-" + porta);
        laco.setDaemon(true);
        laco.start();
    }

    public void parar() {
        ativo = false;
        try {
            registro.unbind(NOME_TRANSPORTE);
            UnicastRemoteObject.unexportObject(transporte, true);
            UnicastRemoteObject.unexportObject(registro, true);
        } catch (Exception ignorado) {
            // encerrando
        }
        pool.shutdownNow();
    }

    /** Obtém uma requisição de um cliente, bloqueia até que uma chegue */
    public byte[] getRequest() throws InterruptedException {
        return transporte.getRequest();
    }

    public InetAddress getClientHost(long requestId) {
        return transporte.getClientHost(requestId);
    }

    /**
     * Envia a mensagem de resposta ao cliente. Em vez da porta do cliente (conceito de sockets), a
     * resposta é endereçada pelo requestId, que identifica a chamada RMI que está aguardando.
     */
    public void sendReply(byte[] reply, InetAddress clientHost, long requestId) {
        transporte.sendReply(reply, requestId);
    }

    private void lacoDeAtendimento() {
        while (ativo) {
            try {
                byte[] bytes = getRequest();
                pool.submit(() -> atender(bytes));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void atender(byte[] bytes) {
        Mensagem requisicao = Mensagem.desempacotar(bytes);
        InetAddress cliente = getClientHost(requisicao.getRequestId());
        byte[] resultado = despachante.despachar(requisicao);
        Mensagem resposta = new Mensagem(Mensagem.REPLY, requisicao.getRequestId(),
                requisicao.getObjectReference(), requisicao.getMethodId(), resultado);
        sendReply(resposta.empacotar(), cliente, requisicao.getRequestId());
    }
}

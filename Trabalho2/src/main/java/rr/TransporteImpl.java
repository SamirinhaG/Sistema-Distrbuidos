package rr;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Lado servidor do canal. Cada chamada RMI a "enviar" entra numa fila consumida por
 * "getRequest e fica bloqueada até que "endReply" entregue a resposta com o
 * mesmo requestId. Assim a thread do servidor trabalha exatamente como no modelo do livro:
 * getRequest -> executa -> sendReply.
 */
class TransporteImpl extends UnicastRemoteObject implements Transporte {
    private static final long TIMEOUT_SEGUNDOS = 30;

    private static class Pedido {
        final byte[] mensagem;
        final InetAddress cliente;
        final CompletableFuture<byte[]> resposta = new CompletableFuture<>();

        Pedido(byte[] mensagem, InetAddress cliente) {
            this.mensagem = mensagem;
            this.cliente = cliente;
        }
    }

    private final BlockingQueue<Pedido> fila = new LinkedBlockingQueue<>();
    private final Map<Long, Pedido> pendentes = new ConcurrentHashMap<>();

    TransporteImpl(int porta) throws RemoteException {
        super(porta);
    }

    @Override
    public byte[] enviar(byte[] mensagemRequisicao) throws RemoteException {
        long requestId = Mensagem.desempacotar(mensagemRequisicao).getRequestId();
        Pedido pedido = new Pedido(mensagemRequisicao, enderecoDoCliente());
        pendentes.put(requestId, pedido);
        fila.add(pedido);
        try {
            return pedido.resposta.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RemoteException("Sem resposta para a requisição " + requestId, e);
        } finally {
            pendentes.remove(requestId);
        }
    }

    /** Bloqueia até chegar uma requisição */
    byte[] getRequest() throws InterruptedException {
        return fila.take().mensagem;
    }

    InetAddress getClientHost(long requestId) {
        Pedido pedido = pendentes.get(requestId);
        return pedido == null ? null : pedido.cliente;
    }

    void sendReply(byte[] resposta, long requestId) {
        Pedido pedido = pendentes.get(requestId);
        if (pedido != null) {
            pedido.resposta.complete(resposta);
        }
    }

    private static InetAddress enderecoDoCliente() {
        try {
            return InetAddress.getByName(RemoteServer.getClientHost());
        } catch (ServerNotActiveException | java.net.UnknownHostException e) {
            return null;
        }
    }
}

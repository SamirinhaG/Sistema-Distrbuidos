package rr;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Lado "cliente" do protocolo requisição-resposta. */
public class ClienteRR {
    private final Map<String, Transporte> canais = new ConcurrentHashMap<>();
    private final AtomicInteger contador = new AtomicInteger();
    private final long prefixo = ((long) new SecureRandom().nextInt() & 0xffffffffL) << 32;

    /**
     * Envia uma mensagem de requisição ao objeto remoto {o} e retorna os bytes dos argumentos
     * da mensagem de resposta. Bloqueia até a resposta chegar
     */
    public byte[] doOperation(RemoteObjectRef o, String methodId, byte[] arguments) throws RemoteException {
        long requestId = prefixo | (contador.incrementAndGet() & 0xffffffffL);
        Mensagem requisicao = new Mensagem(Mensagem.REQUEST, requestId, o.getNome(), methodId, arguments);
        String chave = o.getHost() + ":" + o.getPorta();
        try {
            byte[] bytes = canal(chave, o).enviar(requisicao.empacotar());
            Mensagem resposta = Mensagem.desempacotar(bytes);
            if (resposta.getMessageType() != Mensagem.REPLY || resposta.getRequestId() != requestId) {
                throw new RemoteException("Resposta inesperada para a requisição " + requestId);
            }
            return resposta.getArguments();
        } catch (RemoteException e) {
            canais.remove(chave);
            throw e;
        }
    }

    private Transporte canal(String chave, RemoteObjectRef o) throws RemoteException {
        Transporte canal = canais.get(chave);
        if (canal == null) {
            try {
                Registry registro = LocateRegistry.getRegistry(o.getHost(), o.getPorta());
                canal = (Transporte) registro.lookup(ServidorRR.NOME_TRANSPORTE);
            } catch (NotBoundException e) {
                throw new RemoteException("Nó sem serviço de transporte: " + chave, e);
            }
            canais.put(chave, canal);
        }
        return canal;
    }
}

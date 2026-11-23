package broker;

import com.google.gson.reflect.TypeToken;
import entidades.Notificacao;
import rr.ClienteRR;
import rr.ProxyRemoto;
import rr.RemoteObjectRef;

import java.util.List;

public class BrokerCliente {
    private final ProxyRemoto broker;

    public BrokerCliente(ClienteRR rr, String host, int porta) {
        this.broker = new ProxyRemoto(rr, new RemoteObjectRef(host, porta, BrokerService.NOME));
    }

    public boolean assinar(String assinante, String topico) {
        return broker.chamar("assinar", boolean.class, assinante, topico);
    }

    public boolean cancelar(String assinante, String topico) {
        return broker.chamar("cancelar", boolean.class, assinante, topico);
    }

    public List<String> assinaturas(String assinante) {
        return broker.chamar("assinaturas", new TypeToken<List<String>>() {
        }.getType(), assinante);
    }

    public int publicar(String topico, Notificacao notificacao) {
        return broker.chamar("publicar", int.class, topico, notificacao);
    }

    public List<Entrega> consumir(String assinante) {
        return broker.chamar("consumir", new TypeToken<List<Entrega>>() {
        }.getType(), assinante);
    }

    public int confirmar(String assinante, long ateId) {
        return broker.chamar("confirmar", int.class, assinante, ateId);
    }

    public String resumo() {
        return broker.chamar("resumo", String.class);
    }
}

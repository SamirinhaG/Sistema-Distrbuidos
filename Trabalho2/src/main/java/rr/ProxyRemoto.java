package rr;

import com.google.gson.JsonArray;
import util.Json;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Representante local de um objeto remoto. Empacota os argumentos em JSON, chama doOperation e
 * desempacota o resultado (ou lança uma exceção remota se o servidor devolveu um erro)
 */
public class ProxyRemoto {
    private final ClienteRR rr;
    private final RemoteObjectRef referencia;

    public ProxyRemoto(ClienteRR rr, RemoteObjectRef referencia) {
        this.rr = rr;
        this.referencia = referencia;
    }

    public <T> T chamar(String metodo, Type tipoRetorno, Object... argumentos) {
        JsonArray array = new JsonArray();
        for (Object argumento : argumentos) {
            array.add(Json.GSON.toJsonTree(argumento));
        }
        byte[] resposta;
        try {
            resposta = rr.doOperation(referencia, metodo, array.toString().getBytes(StandardCharsets.UTF_8));
        } catch (java.rmi.RemoteException e) {
            throw new ExcecaoRemota("Falha de comunicação com " + referencia + ": " + e.getMessage(), e);
        }
        RespostaRPC r = Json.GSON.fromJson(new String(resposta, StandardCharsets.UTF_8), RespostaRPC.class);
        if (!r.isSucesso()) {
            throw new ExcecaoRemota(r.getErro());
        }
        if (tipoRetorno == void.class || tipoRetorno == Void.class || r.getDados().isJsonNull()) {
            return null;
        }
        return Json.GSON.fromJson(r.getDados(), tipoRetorno);
    }

    public void chamarSemRetorno(String metodo, Object... argumentos) {
        chamar(metodo, void.class, argumentos);
    }

    public RemoteObjectRef getReferencia() {
        return referencia;
    }
}

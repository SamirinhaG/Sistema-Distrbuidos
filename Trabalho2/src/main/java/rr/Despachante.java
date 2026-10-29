package rr;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import util.Json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Despachante: usa o objectReference para escolher o objeto remoto e o methodId para
 * escolher o método; desempacota os argumentos (JSON), invoca o método e empacota o resultado.
 *
 * Os argumentos chegam como um array JSON; cada elemento é convertido para o tipo do parâmetro
 * correspondente. Parâmetros de tipos locais (Livro, Usuario, Notificacao...) chegam por valor (cópia);
 */
public class Despachante {
    private final Map<String, Object> objetos = new ConcurrentHashMap<>();

    public void registrar(String nome, Object objeto) {
        objetos.put(nome, objeto);
    }

    /** Executa a requisição e devolve os bytes do campo "arguments" da resposta. */
    public byte[] despachar(Mensagem requisicao) {
        RespostaRPC resposta;
        try {
            Object alvo = objetos.get(requisicao.getObjectReference());
            if (alvo == null) {
                throw new ExcecaoRemota("Objeto remoto inexistente: " + requisicao.getObjectReference());
            }
            Method metodo = localizar(alvo, requisicao.getMethodId());
            JsonArray argumentos = JsonParser
                    .parseString(new String(requisicao.getArguments(), StandardCharsets.UTF_8))
                    .getAsJsonArray();
            Type[] tipos = metodo.getGenericParameterTypes();
            if (argumentos.size() != tipos.length) {
                throw new ExcecaoRemota("O método " + requisicao.getMethodId() + " espera " + tipos.length
                        + " argumento(s), mas recebeu " + argumentos.size());
            }
            Object[] valores = new Object[tipos.length];
            for (int i = 0; i < tipos.length; i++) {
                valores[i] = Json.GSON.fromJson(argumentos.get(i), tipos[i]);
            }
            Object resultado = metodo.invoke(alvo, valores);
            resposta = RespostaRPC.ok(metodo.getReturnType() == void.class
                    ? JsonNull.INSTANCE
                    : Json.GSON.toJsonTree(resultado, metodo.getGenericReturnType()));
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            resposta = RespostaRPC.falha(causa.getMessage() != null ? causa.getMessage() : causa.toString());
        } catch (Exception e) {
            resposta = RespostaRPC.falha(e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return Json.GSON.toJson(resposta).getBytes(StandardCharsets.UTF_8);
    }

    private Method localizar(Object alvo, String nome) {
        for (Method m : alvo.getClass().getMethods()) {
            if (m.isAnnotationPresent(MetodoRemoto.class) && m.getName().equals(nome)) {
                return m;
            }
        }
        throw new ExcecaoRemota("Método remoto inexistente: " + nome);
    }
}

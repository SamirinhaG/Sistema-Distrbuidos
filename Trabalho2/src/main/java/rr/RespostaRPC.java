package rr;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/** Conteúdo (JSON) do campo arguments de uma mensagem de resposta. */
public class RespostaRPC {
    private boolean sucesso;
    private JsonElement dados;
    private String erro;

    public static RespostaRPC ok(JsonElement dados) {
        RespostaRPC r = new RespostaRPC();
        r.sucesso = true;
        r.dados = dados == null ? JsonNull.INSTANCE : dados;
        return r;
    }

    public static RespostaRPC falha(String erro) {
        RespostaRPC r = new RespostaRPC();
        r.sucesso = false;
        r.erro = erro;
        return r;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public JsonElement getDados() {
        return dados;
    }

    public String getErro() {
        return erro;
    }
}

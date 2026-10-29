package util;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import entidades.Aluno;
import entidades.Bibliotecario;
import entidades.Professor;
import entidades.Usuario;

import java.lang.reflect.Type;

/**
 * Representação externa de dados para a hierarquia Usuario: acrescenta o campo "tipo" no JSON
 * e o usa para reconstruir a subclasse correta no outro lado.
 */
public class UsuarioAdapter implements JsonSerializer<Usuario>, JsonDeserializer<Usuario> {
    private static final Gson SIMPLES = new Gson();

    @Override
    public JsonElement serialize(Usuario origem, Type tipo, JsonSerializationContext ctx) {
        JsonObject json = SIMPLES.toJsonTree(origem, origem.getClass()).getAsJsonObject();
        json.addProperty("tipo", origem.getTipo());
        return json;
    }

    @Override
    public Usuario deserialize(JsonElement json, Type tipo, JsonDeserializationContext ctx)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("tipo")) {
            throw new JsonParseException("Usuário sem o campo 'tipo'");
        }
        Class<? extends Usuario> classe;
        switch (obj.get("tipo").getAsString()) {
            case "ALUNO":
                classe = Aluno.class;
                break;
            case "PROFESSOR":
                classe = Professor.class;
                break;
            case "BIBLIOTECARIO":
                classe = Bibliotecario.class;
                break;
            default:
                throw new JsonParseException("Tipo de usuário desconhecido: " + obj.get("tipo"));
        }
        return SIMPLES.fromJson(obj, classe);
    }
}

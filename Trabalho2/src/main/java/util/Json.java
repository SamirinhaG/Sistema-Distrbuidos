package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entidades.Usuario;

/** Instância única do Gson, usada em todo o sistema como representação externa de dados (JSON). */
public final class Json {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Usuario.class, new UsuarioAdapter())
            .create();

    private Json() {
    }
}

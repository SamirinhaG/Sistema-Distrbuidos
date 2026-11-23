package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entidades.Usuario;

public final class Json {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Usuario.class, new UsuarioAdapter())
            .create();

    private Json() {
    }
}

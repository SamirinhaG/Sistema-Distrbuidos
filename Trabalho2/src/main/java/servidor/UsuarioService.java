package servidor;

import entidades.Bibliotecario;
import entidades.Sessao;
import entidades.Usuario;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;
import util.Json;

/** Objeto remoto "UsuarioService": cadastro e autenticação (substitui a autenticação TCP) */
public class UsuarioService {
    public static final String NOME = "UsuarioService";

    private final Biblioteca biblioteca;

    public UsuarioService(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    /** O usuario chega por valor (cópia desempacotada do JSON). Devolve o cadastro, já com id e sem senha */
    @MetodoRemoto
    public Usuario cadastrarUsuario(Usuario usuario) {
        if (usuario == null || vazio(usuario.getNome()) || vazio(usuario.getLogin()) || vazio(usuario.getSenha())) {
            throw new ExcecaoRemota("Nome, login e senha são obrigatórios");
        }
        if (usuario instanceof Bibliotecario) {
            throw new ExcecaoRemota("Bibliotecários só podem ser criados pelo administrador do sistema");
        }
        return copia(biblioteca.registrarUsuario(usuario));
    }

    @MetodoRemoto
    public Sessao autenticar(String login, String senha) {
        String token = biblioteca.autenticar(login, senha);
        return new Sessao(token, copia(biblioteca.exigirSessao(token)));
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    static Usuario copia(Usuario usuario) {
        return Json.GSON.fromJson(Json.GSON.toJson(usuario), Usuario.class);
    }
}

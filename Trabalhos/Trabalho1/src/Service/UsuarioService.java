package Service;

import Entities.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private UsuarioService usuario;
    private List<Usuario> usuarios = new ArrayList<>();

    public UsuarioService(UsuarioService usuario) {
        this.usuario = usuario;
    }

    public void cadastrar(Usuario usuario) {
        usuarios.add(usuario);
    }

    public List<Usuario> listar() {
        return usuarios;
    }

    public UsuarioService getUsuarioService() {
        return usuario;
    }

}

package entidades;

public class Sessao {
    private String token;
    private Usuario usuario;

    public Sessao() {
    }

    public Sessao(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}

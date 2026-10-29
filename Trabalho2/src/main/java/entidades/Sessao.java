package entidades;

/** Resultado do login: token de sessão (enviado nas chamadas seguintes) e o usuário autenticado. */
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

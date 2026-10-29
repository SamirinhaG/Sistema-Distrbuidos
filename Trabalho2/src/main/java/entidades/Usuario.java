package entidades;

/**
 * O campo {senha} só trafega no cadastro (cliente -> servidor); o servidor guarda apenas
 * o hash e nunca devolve a senha.
 */
public abstract class Usuario {
    private Integer idUsuario;
    private String nome;
    private String login;
    private String senha;
    private String email;

    protected Usuario() {
    }

    protected Usuario(Integer idUsuario, String nome, String login, String senha, String email) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.email = email;
    }


    public abstract String getTipo();

    public abstract int getLimiteEmprestimos();

    public abstract int getPrazoDias();

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getTipo() + " #" + idUsuario + " " + nome + " (" + login + ")";
    }
}

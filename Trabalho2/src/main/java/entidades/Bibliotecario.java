package entidades;

/** Administrador do sistema: cadastra livros e envia notificações ao grupo */
public class Bibliotecario extends Usuario {
    private String matriculaFuncional;

    public Bibliotecario() {
    }

    public Bibliotecario(Integer idUsuario, String nome, String login, String senha, String email,
                         String matriculaFuncional) {
        super(idUsuario, nome, login, senha, email);
        this.matriculaFuncional = matriculaFuncional;
    }

    @Override
    public String getTipo() {
        return "BIBLIOTECARIO";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 0;
    }

    @Override
    public int getPrazoDias() {
        return 0;
    }

    public String getMatriculaFuncional() {
        return matriculaFuncional;
    }

    public void setMatriculaFuncional(String matriculaFuncional) {
        this.matriculaFuncional = matriculaFuncional;
    }
}

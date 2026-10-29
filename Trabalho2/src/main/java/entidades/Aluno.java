package entidades;

public class Aluno extends Usuario {
    private String matricula;
    private String curso;

    public Aluno() {
    }

    public Aluno(Integer idUsuario, String nome, String login, String senha, String email,
                 String matricula, String curso) {
        super(idUsuario, nome, login, senha, email);
        this.matricula = matricula;
        this.curso = curso;
    }

    @Override
    public String getTipo() {
        return "ALUNO";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 3;
    }

    @Override
    public int getPrazoDias() {
        return 14;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }
}

package entidades;

public class Professor extends Usuario {
    private String departamento;

    public Professor() {
    }

    public Professor(Integer idUsuario, String nome, String login, String senha, String email,
                     String departamento) {
        super(idUsuario, nome, login, senha, email);
        this.departamento = departamento;
    }

    @Override
    public String getTipo() {
        return "PROFESSOR";
    }

    @Override
    public int getLimiteEmprestimos() {
        return 5;
    }

    @Override
    public int getPrazoDias() {
        return 30;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}

package entidades;

import java.time.LocalDate;

public class Emprestimo {
    private Integer idEmprestimo;
    private Usuario usuario;
    private Livro livro;
    private String dataEmprestimo;
    private String dataPrevistaDevolucao;
    private String dataDevolucao;
    private StatusEmprestimo status;

    public Emprestimo() {
    }

    public Emprestimo(Integer idEmprestimo, Usuario usuario, Livro livro) {
        this.idEmprestimo = idEmprestimo;
        this.usuario = usuario;
        this.livro = livro;
        LocalDate hoje = LocalDate.now();
        this.dataEmprestimo = hoje.toString();
        this.dataPrevistaDevolucao = hoje.plusDays(usuario.getPrazoDias()).toString();
        this.status = StatusEmprestimo.ATIVO;
    }

    public void registrarDevolucao() {
        this.dataDevolucao = LocalDate.now().toString();
        this.status = StatusEmprestimo.DEVOLVIDO;
    }

    public Integer getIdEmprestimo() {
        return idEmprestimo;
    }

    public void setIdEmprestimo(Integer idEmprestimo) {
        this.idEmprestimo = idEmprestimo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public String getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(String dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public String getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(String dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public String getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(String dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Empréstimo #" + idEmprestimo + " - " + livro.getTitulo() + " (" + usuario.getNome()
                + ") " + status + " | retirada " + dataEmprestimo + " | prevista " + dataPrevistaDevolucao
                + (dataDevolucao != null ? " | devolvido " + dataDevolucao : "");
    }
}

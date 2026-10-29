package Entities;

public class Livro {
    private Integer idLivro;
    private String titulo;
    private String descricao;
    private Integer anoPublicacao;
    private String autor;

    public Livro(Integer idLivro, String titulo, String descricao, Integer anoPublicacao, String autor) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.anoPublicacao = anoPublicacao;
        this.autor = autor;
    }

    public Integer getIdLivro() {
        return idLivro;
    }

    public void setIdLivro(Integer idLivro) {
        this.idLivro = idLivro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}

package entidades;

public class Livro {
    private Integer idLivro;
    private String titulo;
    private String descricao;
    private Integer anoPublicacao;
    private Autor autor;
    private int exemplaresTotal;
    private int exemplaresDisponiveis;

    public Livro() {
    }

    public Livro(String titulo, String descricao, Integer anoPublicacao, Autor autor, int exemplares) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.anoPublicacao = anoPublicacao;
        this.autor = autor;
        this.exemplaresTotal = exemplares;
        this.exemplaresDisponiveis = exemplares;
    }

    /** Tenta reservar um exemplar. Retorna false se não houver nenhum disponível. */
    public synchronized boolean emprestar() {
        if (exemplaresDisponiveis <= 0) {
            return false;
        }
        exemplaresDisponiveis--;
        return true;
    }

    public synchronized void devolver() {
        if (exemplaresDisponiveis < exemplaresTotal) {
            exemplaresDisponiveis++;
        }
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

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public int getExemplaresTotal() {
        return exemplaresTotal;
    }

    public void setExemplaresTotal(int exemplaresTotal) {
        this.exemplaresTotal = exemplaresTotal;
    }

    public int getExemplaresDisponiveis() {
        return exemplaresDisponiveis;
    }

    public void setExemplaresDisponiveis(int exemplaresDisponiveis) {
        this.exemplaresDisponiveis = exemplaresDisponiveis;
    }

    @Override
    public String toString() {
        return "#" + idLivro + " " + titulo + " - " + autor + " (" + anoPublicacao + ") ["
                + exemplaresDisponiveis + "/" + exemplaresTotal + " disponíveis]";
    }
}

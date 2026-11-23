package servidor;

import broker.Topicos;
import entidades.Livro;
import entidades.Notificacao;
import entidades.TipoNotificacao;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;

import java.util.List;

public class LivroService {
    public static final String NOME = "LivroService";

    private final Biblioteca biblioteca;
    private final PublicadorBroker publicador;

    public LivroService(Biblioteca biblioteca, PublicadorBroker publicador) {
        this.biblioteca = biblioteca;
        this.publicador = publicador;
    }

    @MetodoRemoto
    public Livro cadastrarLivro(String token, Livro livro) {
        biblioteca.exigirBibliotecario(token);
        if (livro == null || livro.getTitulo() == null || livro.getTitulo().isBlank()
                || livro.getAutor() == null || livro.getAutor().getNome() == null
                || livro.getAutor().getNome().isBlank()) {
            throw new ExcecaoRemota("Título e autor são obrigatórios");
        }
        if (livro.getExemplaresTotal() < 1) {
            livro.setExemplaresTotal(1);
        }
        Livro cadastrado = biblioteca.registrarLivro(livro);
        publicador.publicar(Topicos.ACERVO, new Notificacao(TipoNotificacao.NOTIFICACAO,
                "Novo livro no acervo: " + cadastrado.getTitulo() + " (" + cadastrado.getAutor() + ")"));
        return cadastrado;
    }

    @MetodoRemoto
    public List<Livro> listarLivros() {
        return biblioteca.listarLivros();
    }

    @MetodoRemoto
    public Livro buscarLivro(int idLivro) {
        return biblioteca.buscarLivro(idLivro);
    }
}

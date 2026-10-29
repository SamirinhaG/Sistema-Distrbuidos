package servidor;

import entidades.Livro;
import entidades.Notificacao;
import entidades.TipoNotificacao;
import rr.ExcecaoRemota;
import rr.MetodoRemoto;

import java.util.List;

/** Objeto remoto "LivroService": consulta e cadastro de livros. */
public class LivroService {
    public static final String NOME = "LivroService";

    private final Biblioteca biblioteca;
    private final GrupoNotificacao grupo;

    public LivroService(Biblioteca biblioteca, GrupoNotificacao grupo) {
        this.biblioteca = biblioteca;
        this.grupo = grupo;
    }

    /** Só bibliotecários. Ao cadastrar, todos os clientes do grupo são notificados. */
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
        grupo.enviar(new Notificacao(TipoNotificacao.NOTIFICACAO,
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

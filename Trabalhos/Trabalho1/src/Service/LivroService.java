package Service;

import Entities.Livro;

import java.util.ArrayList;
import java.util.List;

public class LivroService {

    private final LivroService livro;
    private List<Livro> livros =  new ArrayList<>();

    public LivroService(LivroService livroService) {
        this.livro = livroService;
    }

    public void cadastrarLivro(Livro livro) {
        livros.add(livro);
    }

    public List<Livro> listarLivros(){
        return livros;
    }

    public LivroService getLivroService() {
        return livro;
    }
}

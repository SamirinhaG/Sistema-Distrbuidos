package servidor;

import entidades.Autor;
import entidades.Bibliotecario;
import entidades.Emprestimo;
import entidades.Livro;
import entidades.StatusEmprestimo;
import entidades.Usuario;
import rr.ExcecaoRemota;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Os objetos guardados aqui são locais ao servidor: os clientes só
 * recebem cópias (passagem por valor).
 */
public class Biblioteca {
    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<String, String> senhasComHash = new ConcurrentHashMap<>();
    private final Map<String, Usuario> sessoes = new ConcurrentHashMap<>();
    private final Map<Integer, Livro> livros = new ConcurrentHashMap<>();
    private final Map<String, Autor> autores = new ConcurrentHashMap<>();
    private final List<Emprestimo> emprestimos = new ArrayList<>();
    private int proximoIdUsuario = 1;
    private int proximoIdLivro = 1;
    private int proximoIdAutor = 1;
    private int proximoIdEmprestimo = 1;


    public synchronized Usuario registrarUsuario(Usuario usuario) {
        if (buscarPorLogin(usuario.getLogin()) != null) {
            throw new ExcecaoRemota("Já existe um usuário com o login '" + usuario.getLogin() + "'");
        }
        usuario.setIdUsuario(proximoIdUsuario++);
        senhasComHash.put(usuario.getLogin(), hash(usuario.getSenha()));
        usuario.setSenha(null); // a senha em claro nunca fica guardada
        usuarios.put(usuario.getIdUsuario(), usuario);
        return usuario;
    }

    public Usuario buscarPorLogin(String login) {
        for (Usuario u : usuarios.values()) {
            if (u.getLogin().equals(login)) {
                return u;
            }
        }
        return null;
    }

    public String autenticar(String login, String senha) {
        Usuario usuario = login == null ? null : buscarPorLogin(login);
        if (usuario == null || senha == null || !hash(senha).equals(senhasComHash.get(login))) {
            throw new ExcecaoRemota("Login ou senha inválidos");
        }
        String token = UUID.randomUUID().toString();
        sessoes.put(token, usuario);
        return token;
    }

    public Usuario exigirSessao(String token) {
        Usuario usuario = token == null ? null : sessoes.get(token);
        if (usuario == null) {
            throw new ExcecaoRemota("Sessão inválida: faça login novamente");
        }
        return usuario;
    }

    public Usuario exigirBibliotecario(String token) {
        Usuario usuario = exigirSessao(token);
        if (!(usuario instanceof Bibliotecario)) {
            throw new ExcecaoRemota("Operação permitida apenas para bibliotecários");
        }
        return usuario;
    }


    public synchronized Livro registrarLivro(Livro livro) {
        Autor autor = livro.getAutor();
        Autor existente = autores.get(autor.getNome().toLowerCase());
        if (existente == null) {
            autor.setIdAutor(proximoIdAutor++);
            autores.put(autor.getNome().toLowerCase(), autor);
        } else {
            livro.setAutor(existente);
        }
        livro.setIdLivro(proximoIdLivro++);
        livro.setExemplaresDisponiveis(livro.getExemplaresTotal());
        livros.put(livro.getIdLivro(), livro);
        return livro;
    }

    public Livro buscarLivro(int id) {
        Livro livro = livros.get(id);
        if (livro == null) {
            throw new ExcecaoRemota("Livro #" + id + " não encontrado");
        }
        return livro;
    }

    public List<Livro> listarLivros() {
        List<Livro> lista = new ArrayList<>(livros.values());
        lista.sort((a, b) -> a.getIdLivro().compareTo(b.getIdLivro()));
        return lista;
    }


    public synchronized Emprestimo registrarEmprestimo(Usuario usuario, Livro livro) {
        Emprestimo emprestimo = new Emprestimo(proximoIdEmprestimo++, usuario, livro);
        emprestimos.add(emprestimo);
        return emprestimo;
    }

    public synchronized long contarEmprestimosAtivos(int idUsuario) {
        return emprestimos.stream()
                .filter(e -> e.getUsuario().getIdUsuario() == idUsuario && e.getStatus() == StatusEmprestimo.ATIVO)
                .count();
    }

    public synchronized Emprestimo buscarEmprestimo(int id) {
        return emprestimos.stream()
                .filter(e -> e.getIdEmprestimo() == id)
                .findFirst()
                .orElseThrow(() -> new ExcecaoRemota("Empréstimo #" + id + " não encontrado"));
    }

    public synchronized List<Emprestimo> emprestimosDoUsuario(int idUsuario) {
        List<Emprestimo> lista = new ArrayList<>();
        for (Emprestimo e : emprestimos) {
            if (e.getUsuario().getIdUsuario() == idUsuario) {
                lista.add(e);
            }
        }
        return lista;
    }

    private static String hash(String texto) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

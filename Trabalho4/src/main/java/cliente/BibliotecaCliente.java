package cliente;

import com.google.gson.reflect.TypeToken;
import entidades.Emprestimo;
import entidades.Livro;
import entidades.Notificacao;
import entidades.Sessao;
import entidades.Usuario;
import rr.ClienteRR;
import rr.ProxyRemoto;
import rr.RemoteObjectRef;
import servidor.EmprestimoService;
import servidor.LivroService;
import servidor.NotificacaoService;
import servidor.UsuarioService;

import java.util.List;

public class BibliotecaCliente {
    private final ProxyRemoto usuarios;
    private final ProxyRemoto livros;
    private final ProxyRemoto emprestimos;
    private final ProxyRemoto notificacoes;

    public BibliotecaCliente(ClienteRR rr, String hostServidor, int portaServidor) {
        usuarios = new ProxyRemoto(rr, new RemoteObjectRef(hostServidor, portaServidor, UsuarioService.NOME));
        livros = new ProxyRemoto(rr, new RemoteObjectRef(hostServidor, portaServidor, LivroService.NOME));
        emprestimos = new ProxyRemoto(rr, new RemoteObjectRef(hostServidor, portaServidor, EmprestimoService.NOME));
        notificacoes = new ProxyRemoto(rr, new RemoteObjectRef(hostServidor, portaServidor, NotificacaoService.NOME));
    }

    public Usuario cadastrarUsuario(Usuario usuario) {
        return usuarios.chamar("cadastrarUsuario", Usuario.class, usuario);
    }

    public Sessao autenticar(String login, String senha) {
        return usuarios.chamar("autenticar", Sessao.class, login, senha);
    }

    public Livro cadastrarLivro(String token, Livro livro) {
        return livros.chamar("cadastrarLivro", Livro.class, token, livro);
    }

    public List<Livro> listarLivros() {
        return livros.chamar("listarLivros", new TypeToken<List<Livro>>() {
        }.getType());
    }

    public Livro buscarLivro(int idLivro) {
        return livros.chamar("buscarLivro", Livro.class, idLivro);
    }

    public Emprestimo emprestarLivro(String token, int idLivro) {
        return emprestimos.chamar("emprestarLivro", Emprestimo.class, token, idLivro);
    }

    public Emprestimo devolverLivro(String token, int idEmprestimo) {
        return emprestimos.chamar("devolverLivro", Emprestimo.class, token, idEmprestimo);
    }

    public List<Emprestimo> listarMeusEmprestimos(String token) {
        return emprestimos.chamar("listarMeusEmprestimos", new TypeToken<List<Emprestimo>>() {
        }.getType(), token);
    }

    public void publicarAviso(String token, Notificacao notificacao) {
        notificacoes.chamarSemRetorno("publicarAviso", token, notificacao);
    }
}
